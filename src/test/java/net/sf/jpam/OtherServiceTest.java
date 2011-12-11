package net.sf.jpam;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class OtherServiceTest
    extends AbstractPamTest
{
    private Pam pam;

    @Override
    @BeforeClass
    public void setUp()
    {
        super.setUp();
        pam = new Pam("other");
    }

    @Test
    public void testUserAuthenticated()
    {
        final PamReturnValue returnValue = pam.authenticate(user1Name,
            user1Credentials);
        assertTrue(PamReturnValue.PAM_AUTH_ERR.equals(returnValue));
    }

    @Test(
        expectedExceptions = NullPointerException.class)
    public void testUserWithNullCredentials()
    {
        pam.authenticate(user1Credentials, null);
    }

    @Test
    public void testUserWithEmptyCredentials()
    {
        final PamReturnValue pamReturnValue = pam.authenticate(user1Credentials,
            "");
        assertTrue(pamReturnValue.equals(PamReturnValue.PAM_AUTH_ERR));
    }

    @Test(
        expectedExceptions = NullPointerException.class)
    public void testUserWithNullUsername()
    {
        pam.authenticate(user1Name, null);
    }

    @Test
    public void testUserWithEmptyUsername()
    {
        final PamReturnValue pamReturnValue = pam.authenticate(user1Name, "");
        assertTrue(pamReturnValue.equals(PamReturnValue.PAM_AUTH_ERR));
    }
}
