/*
 * Copyright (c) 2011, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eel.kitchen.pam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

public final class PamHandle2
    implements Closeable
{
    private static final Logger logger
        = LoggerFactory.getLogger(PamHandle2.class);

    static {
        System.loadLibrary("pam4j");
    }
    /**
     * This is the pam_handle_t * as a long. It is accessed natively ONLY,
     * so DO NOT EVER affect it!
     */
    private long _handleRef;

    /**
     * Status of last operation. Necessary since pam_end() requires it to
     * clean up the handler correctly. It is accessed natively in {@link
     * #destroyHandle()}.
     */
    private int _lastStatus;

    /**
     * Has the handler been closed correctly? Detected in, yes, {@link
     * #finalize()}...
     */
    private boolean closeOK = false;

    private final String user;

    /**
     * Create a new PAM handle
     *
     * @param service the service name
     * @param user the user name
     * @throws PamException service name is null or empty, user name is empty,
     * or PAM initialization (via {@link #createHandle(String, String)}) fails
     */
    PamHandle2(final String service, final String user)
        throws PamException
    {
        if (service == null || service.isEmpty())
            throw new PamException("service is null or empty");

        if (user == null)
            throw new PamException("user name is null");

        this.user = user;

        _lastStatus = createHandle(service, user);

        final PamReturnValue retval = PamReturnValue.fromId(_lastStatus);

        if (retval != PamReturnValue.PAM_SUCCESS)
            throw new PamException("failed to initialize handle: " + retval);
    }

    /**
     * Native method to create a {@code pam_handle_t}. Note that it WILL NOT
     * fail if the service does not exist.
     *
     * @param service the name of a service
     * @param user the username to user
     * @return a constant to be parsed by {@link PamReturnValue#fromId(int)}
     */
    private native int createHandle(final String service, final String user);

    /**
     * Native method to destroy our PAM handle.
     *
     * @return the status of the operation
     */
    private native int destroyHandle();

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close()
        throws IOException
    {
        if (closeOK)
            return;
        final PamReturnValue retval = PamReturnValue.fromId(destroyHandle());
        closeOK = true;
        if (retval != PamReturnValue.PAM_SUCCESS)
            throw new IOException("failed to release handle: " + retval);
    }

    /**
     * Called by the garbage collector on an object when garbage collection
     * determines that there are no more references to the object.
     * A subclass overrides the <code>finalize</code> method to dispose of
     * system resources or to perform other cleanup.
     * <p/>
     * The general contract of <tt>finalize</tt> is that it is invoked
     * if and when the Java<font size="-2"><sup>TM</sup></font> virtual
     * machine has determined that there is no longer any
     * means by which this object can be accessed by any thread that has
     * not yet died, except as a result of an action taken by the
     * finalization of some other object or class which is ready to be
     * finalized. The <tt>finalize</tt> method may take any action, including
     * making this object available again to other threads; the usual purpose
     * of <tt>finalize</tt>, however, is to perform cleanup actions before
     * the object is irrevocably discarded. For example, the finalize method
     * for an object that represents an input/output connection might perform
     * explicit I/O transactions to break the connection before the object is
     * permanently discarded.
     * <p/>
     * The <tt>finalize</tt> method of class <tt>Object</tt> performs no
     * special action; it simply returns normally. Subclasses of
     * <tt>Object</tt> may override this definition.
     * <p/>
     * The Java programming language does not guarantee which thread will
     * invoke the <tt>finalize</tt> method for any given object. It is
     * guaranteed, however, that the thread that invokes finalize will not
     * be holding any user-visible synchronization locks when finalize is
     * invoked. If an uncaught exception is thrown by the finalize method,
     * the exception is ignored and finalization of that object terminates.
     * <p/>
     * After the <tt>finalize</tt> method has been invoked for an object, no
     * further action is taken until the Java virtual machine has again
     * determined that there is no longer any means by which this object can
     * be accessed by any thread that has not yet died, including possible
     * actions by other objects or classes which are ready to be finalized,
     * at which point the object may be discarded.
     * <p/>
     * The <tt>finalize</tt> method is never invoked more than once by a Java
     * virtual machine for any given object.
     * <p/>
     * Any exception thrown by the <code>finalize</code> method causes
     * the finalization of this object to be halted, but is otherwise
     * ignored.
     *
     * @throws Throwable the <code>Exception</code> raised by this method
     */
    @Override
    protected void finalize()
        throws Throwable
    {
        super.finalize();
        if (closeOK)
            return;
        logger.error("luser did not call close()!");
        close();
    }
}
