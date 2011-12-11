package net.sf.jpam;

import org.eel.kitchen.pam.PamReturnValue;
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
        assertEquals(pam.authenticate(user1Name, user1Credentials),
            PamReturnValue.PAM_SUCCESS);
    }

    @Test
    public void testUserWithBadCredentialsNotAuthenticated()
    {
        assertNotEquals(pam.authenticate(user1Name, user1BadCredentials),
            PamReturnValue.PAM_SUCCESS);
    }
}
