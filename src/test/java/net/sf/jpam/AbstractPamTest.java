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

package net.sf.jpam;

import org.testng.annotations.BeforeClass;

public class AbstractPamTest
{
    protected String user;
    protected String passwd;
    protected String badPasswd;

    @BeforeClass
    public void setUp()
    {
        user = System.getProperty("test.login");
        passwd = System.getProperty("test.passwd");
        if (user == null || passwd == null)
            throw new IllegalStateException("Please define test.login and"
                + " test.passwd before running tests");
        badPasswd = passwd + "x";
    }
}
