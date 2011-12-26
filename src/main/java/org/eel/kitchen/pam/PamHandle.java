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

public final class PamHandle
    implements Closeable
{
    private static final Logger logger
        = LoggerFactory.getLogger(PamHandle.class);

    private static native void initLog(final Logger logger);

    static {
        System.loadLibrary("pam4j");
        initLog(logger);
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
    PamHandle(final String service, final String user)
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

    private native int auth(final long handle, final String passwd);

    /**
     * Native method to destroy our PAM handle.
     *
     * @return the status of the operation
     */
    private native int destroyHandle(final long handle, final int status);


    public synchronized PamReturnValue authenticate(final String passwd)
    {
        _lastStatus = auth(_handleRef, passwd);
        return PamReturnValue.fromId(_lastStatus);
    }

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
        final PamReturnValue retval
            = PamReturnValue.fromId(destroyHandle(_handleRef, _lastStatus));
        closeOK = true;
        if (retval != PamReturnValue.PAM_SUCCESS)
            throw new IOException("failed to release handle: " + retval);
    }
}
