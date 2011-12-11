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

import junit.framework.TestCase;

/**
 * Performs tests on the Pam class.
 * <p/>
 * Create the following users before running tests:
 * <ol>
 * <li>user test with password test01
 * <li>user test2 with password test02
 * </ol>
 * Linux systems may need /etc/shadow to be made readable by the user
 *
 * @author <a href="mailto:gregluck@users.sourceforge.net">Greg Luck</a>
 * @version $Id$
 */
public class AbstractPamTest extends TestCase {
    /**
     * user 1 name
     */
    protected String user1Name = "test";
    /**
     * user 1 credentials
     */
    protected String user1Credentials = "test01";
    /**
     * user 1 bad credentials
     */
    protected String user1BadCredentials = "test01bad";


    /**
     * An empty test to keep IntelliJ Test Runner happy
     */
    public void testPlaceHolder() {
        //
    }
}
