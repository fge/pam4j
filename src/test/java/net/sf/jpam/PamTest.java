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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * Performs tests on the Pam class.
 * <p/>
 * Before running this test please:
 * <p/>
 * 1. Add the net-sf-jpam config file to /etc/pam.d
 * 2. Create a user called test with password test01 in the PAM module configured
 *
 * @author <a href="mailto:gluck@thoughtworks.com">Greg Luck</a>
 * @version $Id$
 */
public class PamTest extends AbstractPamTest {

    private static final Log LOG = LogFactory.getLog(PamTest.class.getName());

    /**
     * Checks that the shared object libjpam.so is installed in
     * the first path contained in java.library.path
     */
    public void testSharedLibraryInstalledInLibraryPath() {
        String libraryPath = System.getProperty("java.library.path");
        String pathSeparator = System.getProperty("path.separator");
        String libraryName = Pam.getLibraryName();
        String[] pathElements = libraryPath.split(pathSeparator);
        boolean found = false;
        for (int i = 0; i < pathElements.length; i++) {
            String pathElement = pathElements[i];
            File sharedLibraryFile = new File(pathElement + File.separator + libraryName);
            if (sharedLibraryFile.exists()) {
                found = true;
                LOG.info("Library " + libraryName + " found in " + pathElement);
            }
        }
        assertTrue("Shared Library installed: ", found);
    }

    /**
     * Tests that we can call a simple method in the shared library
     */
    public void testJNIWorking() {
        Pam pam = new Pam();
        assertTrue("Pam working", pam.isSharedLibraryWorking());
    }

    /**
     * A positive test that a known correct username and credentials are authenticated
     */
    public void testUserAuthenticated() {
        Pam pam = new Pam();
        assertTrue("Test user authenticated: ", pam.authenticateSuccessful(user1Name, user1Credentials));
    }

    /**
     * A negative test that a known correct username and and known incorrect
     * credentials are  not authenticated
     */
    public void testUserWithBadCredentialsNotAuthenticated() {
        Pam pam = new Pam();
        assertFalse("Test user authenticated: ", pam.authenticateSuccessful(user1Name, user1BadCredentials));
    }

    /**
     * A test which confirms that null credentials cause a NullPointerException
     * <p/>
     * This is important. If null gets through to the native code it causes a JVM crash
     */
    public void testUserWithNullCredentials() {
        Pam pam = new Pam();
        try {
            PamReturnValue returnValue = pam.authenticate(user1Credentials, null);
            fail();
        } catch (NullPointerException e) {
            //do nothing;
        }
    }

    /**
     * A test that empty credentials cause an error.
     *
     * The actual error depends on the PAM modules involved. The test checks for the errors thrown
     * on Mac OS X and Linux.
     */
    public void testUserWithEmptyCredentials() {
        Pam pam = new Pam();
        PamReturnValue pamReturnValue = pam.authenticate(user1Credentials, "");
        assertTrue(pamReturnValue.equals(PamReturnValue.PAM_USER_UNKNOWN)
                || pamReturnValue.equals(PamReturnValue.PAM_AUTH_ERR));
    }

    /**
     * A test which confirms that null usernames cause a NullPointerException
     * <p/>
     * This is important. If null gets through to the native code it causes a JVM crash
     */
    public void testUserWithNullUsername() {
        Pam pam = new Pam();
        try {
            PamReturnValue returnValue = pam.authenticate(user1Name, null);
            fail();
        } catch (NullPointerException e) {
            //do nothing;
        }
    }

    /**
     * The actual error depends on the PAM modules involved. The test checks for the errors thrown
     * on Mac OS X and Linux.
     */
    public void testUserWithEmptyUsername() {
        Pam pam = new Pam();
        PamReturnValue pamReturnValue = pam.authenticate(user1Name, "");
        assertTrue(pamReturnValue.equals(PamReturnValue.PAM_PERM_DENIED)
            || pamReturnValue.equals(PamReturnValue.PAM_AUTH_ERR));
    }

    /**
     * A test which confirms that null usernames cause a NullPointerException
     * <p/>
     * This is important. If null gets through to the native code it causes a JVM crash
     */
    public void testNullService() {
        try {
            Pam pam = new Pam(null);
            fail();
        } catch (NullPointerException e) {
            //do nothing;
        }
    }

    /**
     * Tests that a null service name causes a {@link NullPointerException}
     * rather than a JVM crash
     */
    public void testNullServiceName() {
        try {
            Pam pam = new Pam(null);
            fail();
        } catch (NullPointerException e) {
            //do nothing;
        }
    }

    /**
     * Tests that not specifying a service name causes an IllegalArgumentException
     * not any other type of error.
     */
    public void testEmptyServiceName() {
        try {
            Pam pam = new Pam("");
            fail();
        } catch (IllegalArgumentException e) {
            //do nothing;
        }
    }

    /**
     * Stress tests jpam with net-sf-jpam
     * @throws InterruptedException
     */
    public void testJPamConcurrent() throws InterruptedException {
        concurrentPamStressTest(new Pam(), new PamReturnValue[] {PamReturnValue.PAM_SUCCESS});
    }
}
