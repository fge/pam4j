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

import org.testng.annotations.BeforeClass;

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
public class AbstractPamTest
{
    protected String user1Name;
    protected String user1Credentials;
    protected String user1BadCredentials;

    @BeforeClass
    public void setUp()
    {
        user1Name = System.getProperty("test.login");
        user1Credentials = System.getProperty("test.passwd");
        if (user1Name == null || user1Credentials == null)
            throw new IllegalStateException("Please define test.login and"
                + " test.passwd before running tests");
        user1BadCredentials = user1Credentials + "x";
    }
}
