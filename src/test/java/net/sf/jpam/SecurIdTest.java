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
 * Performs tests on the Pam class using the SecurId service.
 * <p/>
 * Before running this test please:
 * <p/>
 * 1. Add the net-sf-jpam-securid config file to /etc/pam.d
 * 2. Create a user called test in the ACE Server and enable for this host.
 * 3. Have a SecurId token setup on the ACE Server
 * 4. Have a VAR_ACE pointing to the location of your sdconf.rec. Consider adding to /etc/profile so that it is set for IDE support
 * 5. Verify that the agent and ACE Server are communicating using acestatus and acetest
 * 6. Verify that the pam_securid.so PAM module is working. Change a service like login to use it and test it.
 * 7. make sure the user has permission to all of the files in /opt/pam and /lib/security/ace
 *
 * @author <a href="mailto:gluck@thoughtworks.com">Greg Luck</a>
 * @version $Id$
 */
public class SecurIdTest extends AbstractPamTest {

    private static final String SECURID_SERVICE = "net-sf-jpam-securid";

    private static final Log LOG = LogFactory.getLog(SecurIdTest.class.getName());



    /**
     * A positive test that a known correct username and credentials are authenticated
     *
     * You need to replace the key with the number from the token and run the test before it changes
     * which is a maximum of 1 minute.
     * <p/>
     * The Pam.c needs to have the following line
     * <code>if (! strcmp(msg[replies]->msg,"Password: ")) {</code>
     * changed to:
     * <code>//if (! strcmp(msg[replies]->msg,"Enter PASSCODE: ")) {</code>
     */
    public void xTestUserAuthenticated() {
        Pam pam = new Pam(SECURID_SERVICE);
        assertTrue("Test user authenticated: ", pam.authenticateSuccessful(user1Name, "655635"));
    }


    /**
     * A negative test that a known correct username and and known incorrect
     * credentials are  not authenticated
     */
    public void testUserWithBadCredentialsNotAuthenticated() {
        Pam pam = new Pam(SECURID_SERVICE);
        assertFalse("Test user authenticated: ", pam.authenticateSuccessful(user1Name, user1BadCredentials));
    }


    /**
     * A negative test that a known correct username and and known incorrect
     * credentials are  not authenticated
     */
    public void testUserWithUnkownUserName() {
        Pam pam = new Pam(SECURID_SERVICE);
        assertFalse("Test user authenticated: ", pam.authenticateSuccessful("zzzunknown", user1Credentials));
    }
}
