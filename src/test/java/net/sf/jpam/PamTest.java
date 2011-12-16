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
    public void testJNIWorking()
        throws PamException
    {
        assertTrue(Pam.isSharedLibraryWorking());
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
