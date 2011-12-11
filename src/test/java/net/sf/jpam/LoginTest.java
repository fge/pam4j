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
        throws PamException
    {
        super.setUp();
        pam = new Pam("login");
    }

    @Test
    public void testUserAuthenticated()
        throws PamException
    {
        assertEquals(pam.authenticate(user, passwd),
            PamReturnValue.PAM_SUCCESS);
    }

    @Test
    public void testUserWithBadCredentialsNotAuthenticated()
        throws PamException
    {
        assertNotEquals(pam.authenticate(user, badPasswd),
            PamReturnValue.PAM_SUCCESS);
    }
}
