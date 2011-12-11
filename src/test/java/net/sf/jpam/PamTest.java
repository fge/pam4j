package net.sf.jpam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;

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
        assertTrue(pam.authenticateSuccessful(user1Name, user1Credentials));
    }

    @Test
    public void testUserWithBadCredentialsNotAuthenticated()
    {
        final Pam pam = new Pam();
        assertFalse(pam.authenticateSuccessful(user1Name, user1BadCredentials));
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
        final Pam pam = new Pam();
        final PamReturnValue pamReturnValue
            = pam.authenticate(user1Credentials, "");
        assertTrue(pamReturnValue.equals(PamReturnValue.PAM_USER_UNKNOWN)
            || pamReturnValue.equals(PamReturnValue.PAM_AUTH_ERR));
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
        final Pam pam = new Pam();
        final PamReturnValue pamReturnValue = pam.authenticate(user1Name, "");
        assertTrue(pamReturnValue.equals(PamReturnValue.PAM_PERM_DENIED)
            || pamReturnValue.equals(PamReturnValue.PAM_AUTH_ERR));
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
