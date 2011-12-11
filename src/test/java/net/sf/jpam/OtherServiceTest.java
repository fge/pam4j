package net.sf.jpam;

import org.eel.kitchen.pam.PamReturnValue;
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
        final PamReturnValue retval = pam.authenticate(user, passwd);
        assertEquals(retval, PamReturnValue.PAM_AUTH_ERR);
    }

    @Test(
        expectedExceptions = NullPointerException.class
    )
    public void testUserWithNullCredentials()
    {
        pam.authenticate(passwd, null);
    }

    @Test
    public void testUserWithEmptyCredentials()
    {
        final PamReturnValue retval = pam.authenticate(passwd,
            "");
        assertEquals(retval, PamReturnValue.PAM_AUTH_ERR);
    }

    @Test(
        expectedExceptions = NullPointerException.class)
    public void testUserWithNullUsername()
    {
        pam.authenticate(user, null);
    }

    @Test
    public void testUserWithEmptyUsername()
    {
        final PamReturnValue retval = pam.authenticate(user, "");
        assertEquals(retval, PamReturnValue.PAM_AUTH_ERR);
    }
}
