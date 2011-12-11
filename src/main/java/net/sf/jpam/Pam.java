/**
 *  Copyright 2003-2007 Greg Luck
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.sf.jpam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eel.kitchen.pam.PamReturnValue;

public class Pam {
    private static final Logger LOG = LoggerFactory.getLogger(Pam.class);
    private static final String JPAM_SHARED_LIBRARY_NAME = "jpam";
    private String serviceName;


    /**
     * The default service name of "net-sf-pam".
     * <p/>
     * This service is expected to be configured in /etc/pam.d
     */
    public static final String DEFAULT_SERVICE_NAME = "net-sf-" + JPAM_SHARED_LIBRARY_NAME;

    static {
            System.loadLibrary(JPAM_SHARED_LIBRARY_NAME);
    }


    /**
     * Creates a new Pam object configured to use the {@link #DEFAULT_SERVICE_NAME}
     */
    public Pam() {
        this(DEFAULT_SERVICE_NAME);
    }

    /**
     * Creates a new PAM object configured with the specified service name.
     * <p/>
     * A file with the same name must exist in /etc/pam.d
     *
     * @param serviceName
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public Pam(String serviceName) throws NullPointerException, IllegalArgumentException {
        if (serviceName == null) {
            throw new NullPointerException("Service name is null");
        } else if (serviceName.length() == 0) {
            throw new IllegalArgumentException("Service name is empty");
        }
        this.serviceName = serviceName;
    }

    /**
     * A simple way to check that JNI is installed and properly works
     *
     * @return true if working
     */
    native boolean isSharedLibraryWorking();

    /**
     * The {@link #isSharedLibraryWorking()} native method callsback to this method to make sure all is well.
     */
    private void callback() {
        //noop
    }

    public PamReturnValue authenticate(String username, String credentials) throws NullPointerException {
        if (serviceName == null) {
            throw new NullPointerException("Service name is null");
        } else if (username == null) {
            throw new NullPointerException("User name is null");
        } else if (credentials == null) {
            throw new NullPointerException("Credentials are null");
        }
        synchronized (Pam.class) {
            final PamReturnValue pamReturnValue
                = PamReturnValue.fromId(authenticate(serviceName, username,
                    credentials, LOG.isDebugEnabled()));
            LOG.debug("retval: " + pamReturnValue);
            return pamReturnValue;
        }
    }


    /**
     * A main method
     */
    public static void main(String[] args) {
        Pam pam = new Pam();
        PamReturnValue pamReturnValue = pam.authenticate(args[0], args[1]);
        System.out.println("Response: " + pamReturnValue);
    }

    /**
     * Authenticates a user.
     *
     * Warning: Any calls to this method should be synchronized on the class. The underlying PAM mechanism is not
     * threadsafe.
     *
     * @param serviceName the pam.d config file to use
     * @param username    the username to be authenticated
     * @param credentials the credentials to be authenticated
     * @param debug       if true, debugging information will be emitted
     * @return an integer, which can be converted to a {@link PamReturnValue} using {@link PamReturnValue#fromId(int)}
     */
    private native int authenticate(String serviceName, String username, String credentials, boolean debug);

    /**
     * @return the system dependent name of the shared library the Pam class is expecting.
     */
    public static String getLibraryName() {
        return System.mapLibraryName(JPAM_SHARED_LIBRARY_NAME);
    }


    /**
     * @return the servicename this PAM object is using
     */
    public String getServiceName() {
        return serviceName;
    }
}
