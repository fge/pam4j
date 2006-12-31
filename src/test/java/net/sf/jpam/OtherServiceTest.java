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

/**
 * Performs tests on the Pam class using the other service.
 * <p/>
 * The "other" service should be a file in /etc/pam.d and contain the following chains:
 * <pre>
 * auth     required       /lib/security/$ISA/pam_deny.so
 * account  required       /lib/security/$ISA/pam_deny.so
 * password required       /lib/security/$ISA/pam_deny.so
 * session  required       /lib/security/$ISA/pam_deny.so
 * </pre>
 * pam_deny.so denies access. So all authentication tests should fail
 *
 * Novell Linux Desktop has an other configured slightly differently. Tests modified to work with it too.
 *
 * @author <a href="mailto:gregluck@users.sourceforge.net">Greg Luck</a>
 * @version $Id$
 * @see AbstractPamTest for other requirements
 */
public class OtherServiceTest extends AbstractPamTest {

    private static final Log LOG = LogFactory.getLog(OtherServiceTest.class.getName());

    private Pam pam;

    /**
     * Sets up each test, specifying "other" as the service.
     * @throws Exception
     */
    protected void setUp() throws Exception {
        pam = new Pam("other");
    }

    /**
     * A test that a known correct username and credentials are authenticated.
     */
    public void testUserAuthenticated() {
        PamReturnValue returnValue = pam.authenticate(user1Name, user1Credentials);
        assertTrue(
                //Redhat
                PamReturnValue.PAM_AUTH_ERR.equals(returnValue)
                  //Suse
                || PamReturnValue.PAM_SUCCESS.equals(returnValue));
    }


    /**
     * A test which confirms that null credentials cause a NullPointerException
     * <p/>
     * This is important. If null gets through to the native code it causes a JVM crash
     */
    public void testUserWithNullCredentials() {
        try {
            PamReturnValue returnValue = pam.authenticate(user1Credentials, null);
            fail();
        } catch (NullPointerException e) {
            //do nothing;
        }
    }

    /**
     * A test that empty credentials cause an authentication error but no exception or
     * other type of error.
     */
    public void testUserWithEmptyCredentials() {
        PamReturnValue pamReturnValue = pam.authenticate(user1Credentials, "");
        //RedHat and Mac OS X
        assertTrue(pamReturnValue.equals(PamReturnValue.PAM_AUTH_ERR)
            // Novell Linux Desktop 9
            || pamReturnValue.equals(PamReturnValue.PAM_USER_UNKNOWN));
    }

    /**
     * A test which confirms that null usernames cause a NullPointerException
     * <p/>
     * This is important. If null gets through to the native code it causes a JVM crash
     */
    public void testUserWithNullUsername() {
        try {
            PamReturnValue returnValue = pam.authenticate(user1Name, null);
            fail();
        } catch (NullPointerException e) {
            //do nothing;
        }
    }

    /**
     * A test that empty usernames cause an authentication error but no exception or
     * other type of error.
     */
    public void testUserWithEmptyUsername() {
        PamReturnValue pamReturnValue = pam.authenticate(user1Name, "");
        assertTrue(pamReturnValue.equals(PamReturnValue.PAM_AUTH_ERR));
    }

    /**
     * Stress tests jpam with net-sf-jpam 
     * @throws InterruptedException
     */
    public void testJPamConcurrent() throws InterruptedException {
        concurrentPamStressTest(pam, new PamReturnValue[] {PamReturnValue.PAM_AUTH_ERR});
    }
}
