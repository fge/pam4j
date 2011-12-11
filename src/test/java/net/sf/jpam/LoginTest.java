package net.sf.jpam;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class LoginTest
    extends AbstractPamTest
{

    private Pam pam;

    @Override
    @BeforeClass
    public void setUp()
    {
        super.setUp();
        pam = new Pam("login");
    }

    @Test
    public void testUserAuthenticated()
    {
        assertTrue(pam.authenticateSuccessful(user1Name, user1Credentials));
    }

    @Test
    public void testUserWithBadCredentialsNotAuthenticated()
    {
        assertFalse(pam.authenticateSuccessful(user1Name, user1BadCredentials));
    }
}
