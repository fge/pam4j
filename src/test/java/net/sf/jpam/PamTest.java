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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;


public class PamTest
    extends AbstractPamTest
{
    private static final Logger LOG = LoggerFactory.getLogger(PamTest.class);

    private PamService service;

    @BeforeClass
    public void setUp2()
        throws PamException
    {
        service = Pam.getService();
    }

    @Test
    public void testSharedLibraryInstalledInLibraryPath()
    {
        final String libraryPath = System.getProperty("java.library.path");
        final String pathSeparator = System.getProperty("path.separator");
        final String libraryName = Pam.getLibraryName();
        final String[] pathElements = libraryPath.split(pathSeparator);
        boolean found = false;
        for (final String pathElement : pathElements) {
            final File sharedLibraryFile = new File(
                pathElement + File.separator + libraryName);
            if (sharedLibraryFile.exists()) {
                found = true;
                LOG.info("Library " + libraryName + " found in " + pathElement);
            }
        }
        assertTrue(found);
    }

    @Test
    public void testUserAuthenticated()
        throws PamException
    {
        final PamHandle handle = service.getHandle(user, passwd);
        assertEquals(handle.authenticate(), PamReturnValue.PAM_SUCCESS);
    }

    @Test
    public void testUserWithBadCredentialsNotAuthenticated()
        throws PamException
    {
        final PamHandle handle = service.getHandle(user, badPasswd);
        assertNotEquals(handle.authenticate(), PamReturnValue.PAM_SUCCESS);
    }

    @Test
    public void testUserWithNullCredentials()
        throws PamException
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
        throws PamException
    {
        try {
            service.getHandle(null, "whatever");
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "user name is null");
        }
    }

    @Test
    public void testUserWithEmptyUsername()
        throws PamException
    {
        final PamHandle handle = service.getHandle("", "");
        final PamReturnValue retval = handle.authenticate();
        assertEquals(retval, PamReturnValue.PAM_USER_UNKNOWN);
    }

    @Test
    public void testNullService()
    {
        try {
            Pam.getService(null);
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "service name is null");
        }
    }

    @Test
    public void testEmptyServiceName()
    {
        try {
            Pam.getService("");
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "service name is empty");
        }
    }
}
