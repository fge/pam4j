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

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.UUID;

import static org.testng.Assert.*;

public final class PamHandle2Test
    extends AbstractPamTest
{
    @Test
    public void testNullOrEmptyService()
    {
        try {
            new PamHandle2(null, null);
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "service is null or empty");
        }

        try {
            new PamHandle2("", null);
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "service is null or empty");
        }
    }

    @Test
    public void testNullUser()
    {
        try {
            new PamHandle2("login", null);
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "user name is null");
        }
    }

    @Test
    public void testSetupOnly()
        throws IOException
    {
        final PamHandle2 handle = new PamHandle2("login", "fge");
        handle.close();
        assertTrue(true);
    }

    @Test
    public void testSuccessfulAuth()
        throws IOException
    {
        final PamHandle2 handle = new PamHandle2("login", user);
        final PamReturnValue expected = handle.authenticate(passwd);
        handle.close();
        assertEquals(expected, PamReturnValue.PAM_SUCCESS);
    }

    @Test
    public void testFailedAuth()
        throws IOException
    {
        final PamHandle2 handle = new PamHandle2("login", user);
        final PamReturnValue expected = handle.authenticate(badPasswd);
        handle.close();
        assertEquals(expected, PamReturnValue.PAM_AUTH_ERR);
    }

    @Test
    public void testUnknownUser()
        throws IOException
    {
        final String dummyUser = UUID.randomUUID().toString();
        final PamHandle2 handle = new PamHandle2("pam4j", dummyUser);
        final PamReturnValue expected = handle.authenticate("whatever");
        handle.close();
        assertEquals(expected, PamReturnValue.PAM_USER_UNKNOWN);
    }
}
