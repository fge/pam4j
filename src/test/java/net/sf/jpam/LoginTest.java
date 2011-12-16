package net.sf.jpam;

import org.eel.kitchen.pam.PamHandle;
import org.eel.kitchen.pam.PamReturnValue;
import org.eel.kitchen.pam.PamService;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class LoginTest
    extends AbstractPamTest
{

    private PamService service;

    @BeforeClass
    public void setUp2()
        throws PamException
    {
        service = Pam.getService("login");
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
}
