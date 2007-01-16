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
 * Performs tests on the Pam class using the "login" service.
 * <p/>
 * Before running this test please see {@link AbstractPamTest}
 *
 * @author <a href="mailto:gluck@thoughtworks.com">Greg Luck</a>
 * @version $Id$
 */
public class LoginTest extends AbstractPamTest {

    private Pam pam;

    private static final Log LOG = LogFactory.getLog(LoginTest.class.getName());


    /**
     * Creates a new PAM for service login
     * @throws Exception
     */
    protected void setUp() throws Exception {
        pam = new Pam("login");
    }

    /**
     * A positive test that a known correct username and credentials are authenticated
     */
    public void testUserAuthenticated() {
        assertTrue("Test user authenticated: ", pam.authenticateSuccessful(user1Name, user1Credentials));
    }


    /**
     * A negative test that a known correct username and and known incorrect
     * credentials are  not authenticated
     */
    public void testUserWithBadCredentialsNotAuthenticated() {
        assertFalse("Test user authenticated: ", pam.authenticateSuccessful(user1Name, user1BadCredentials));
    }
}