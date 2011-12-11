package net.sf.jpam;

import org.eel.kitchen.pam.PamReturnValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.util.EnumSet;

import static org.testng.Assert.*;


public class PamTest
    extends AbstractPamTest
{
    private static final Logger LOG = LoggerFactory.getLogger(PamTest.class);

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
    {
        final Pam pam = new Pam();
        assertTrue(pam.isSharedLibraryWorking());
    }

    @Test
    public void testUserAuthenticated()
    {
        final Pam pam = new Pam();
        assertEquals(pam.authenticate(user1Name, user1Credentials),
            PamReturnValue.PAM_SUCCESS);
    }

    @Test
    public void testUserWithBadCredentialsNotAuthenticated()
    {
        final Pam pam = new Pam();
        assertNotEquals(pam.authenticate(user1Name, user1BadCredentials),
            PamReturnValue.PAM_SUCCESS);
    }

    @Test(
        expectedExceptions = NullPointerException.class
    )
    public void testUserWithNullCredentials()
    {
        new Pam().authenticate(user1Credentials, null);
    }

    @Test
    public void testUserWithEmptyCredentials()
    {
        final EnumSet<PamReturnValue> set
            = EnumSet.of(PamReturnValue.PAM_USER_UNKNOWN,
                PamReturnValue.PAM_AUTH_ERR);

        final Pam pam = new Pam();
        final PamReturnValue pamReturnValue
            = pam.authenticate(user1Credentials, "");
        assertTrue(set.contains(pamReturnValue));
    }

    @Test(
        expectedExceptions = NullPointerException.class
    )
    public void testUserWithNullUsername()
    {
        new Pam().authenticate(user1Name, null);
    }

    @Test
    public void testUserWithEmptyUsername()
    {
        final EnumSet<PamReturnValue> set
            = EnumSet.of(PamReturnValue.PAM_PERM_DENIED,
            PamReturnValue.PAM_AUTH_ERR);

        final Pam pam = new Pam();
        final PamReturnValue pamReturnValue = pam.authenticate(user1Name, "");
        assertTrue(set.contains(pamReturnValue));
    }

    @Test(
        expectedExceptions = NullPointerException.class
    )
    public void testNullService()
    {
        new Pam(null);
    }

    @Test(
        expectedExceptions = IllegalArgumentException.class
    )
    public void testEmptyServiceName()
    {
        new Pam("");
    }
}
