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

import org.eel.kitchen.pam.PamHandle;
import org.eel.kitchen.pam.PamReturnValue;
import org.eel.kitchen.pam.PamService;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class OtherServiceTest
    extends AbstractPamTest
{
    private PamService service;

    @BeforeClass
    public void setUp2()
        throws PamException
    {
        service = Pam.getService("other");
    }

    @Test
    public void testUserAuthenticated()
        throws PamException
    {
        final PamHandle handle = service.getHandle(user, passwd);
        assertEquals(handle.authenticate(), PamReturnValue.PAM_AUTH_ERR);
    }

    @Test
    public void testUserWithNullCredentials()
    {
        try {
            service.getHandle(user, null);
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "credentials are null");
        }
    }

    @Test
    public void testUserWithNullUsername()
    {
        try {
            service.getHandle(null, null);
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "user name is null");
        }
    }

    @Test
    public void testUserWithEmptyUsername()
        throws PamException
    {
        final PamHandle handle = service.getHandle("", passwd);
        assertEquals(handle.authenticate(), PamReturnValue.PAM_AUTH_ERR);
    }
}
