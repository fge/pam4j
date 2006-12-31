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
 * Performs tests on the Pam class using the DigiPass service. Digipass uses the PAM Radius module.
 * <p/>
 * Before running you need to do some configuration:
 * <p/>
 * 1. Copy the net-sf-jpam-digipass config file to /etc/pam.d. You need to be root to do this.
 * 2. Create a user called test in the DigiPass server
 * 3. Have a DigiPass token set up on the DigiPass server.
 * 5. Verify that the all is working using the Radius test tool radtest.
 * 6. Verify that the pam_securid.so PAM module is working. Change a service like login to use it and test it.
 * 7. Make sure the user has permission to all of the files in /opt/pam and /lib/security/
 *
 * @author <a href="mailto:gluck@thoughtworks.com">Greg Luck</a>
 * @version $Id$
 */
public class DigiPassTest extends AbstractPamTest {

    private static final String RADIUS_SERVICE = "net-sf-jpam-digipass";

    private static final Log LOG = LogFactory.getLog(DigiPassTest.class.getName());



    /**
     * A positive test that a known correct username and credentials are authenticated
     *
     * You need to replace the key with the number from the token and run the test before it changes
     * which is a maximum of 1 minute.
     */
    public void xTestUserAuthenticated() {
        Pam pam = new Pam(RADIUS_SERVICE);
        assertTrue("Test user authenticated: ", pam.authenticateSuccessful(user1Name, "1234745549"));
    }


    /**
     * A negative test that a known correct username and and known incorrect
     * credentials are  not authenticated
     */
    public void testUserWithBadCredentialsNotAuthenticated() {
        Pam pam = new Pam(RADIUS_SERVICE);
        assertFalse("Test user authenticated: ", pam.authenticateSuccessful(user1Name, user1BadCredentials));
    }


    /**
     * A negative test that a known correct username and and known incorrect
     * credentials are  not authenticated
     */
    public void testUserWithUnkownUserName() {
        Pam pam = new Pam(RADIUS_SERVICE);
        assertFalse("Test user authenticated: ", pam.authenticateSuccessful("zzzunknown", user1Credentials));
    }

    /**
     * Stress tests jpam with net-sf-jpam-securid
     * @throws InterruptedException
     */
    public void testJPamConcurrent() throws InterruptedException {
        concurrentPamStressTest(new Pam("net-sf-jpam-digipass"), 
                new PamReturnValue[] {PamReturnValue.PAM_AUTH_ERR, PamReturnValue.PAM_AUTH_ERR});
    }
}
