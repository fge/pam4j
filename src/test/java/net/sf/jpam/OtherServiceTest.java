package net.sf.jpam;

import org.eel.kitchen.pam.PamHandle;
import org.eel.kitchen.pam.PamReturnValue;
import org.eel.kitchen.pam.PamService;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class OtherServiceTest
    extends AbstractPamTest
{
    private PamService service;

    @BeforeClass
    public void setUp2()
        throws PamException
    {
        service = Pam.getService("other");
    }

    @Test
    public void testUserAuthenticated()
        throws PamException
    {
        final PamHandle handle = service.getHandle(user, passwd);
        assertEquals(handle.authenticate(), PamReturnValue.PAM_AUTH_ERR);
    }

    @Test
    public void testUserWithNullCredentials()
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
    {
        try {
            service.getHandle(null, null);
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "user name is null");
        }
    }

    @Test
    public void testUserWithEmptyUsername()
        throws PamException
    {
        final PamHandle handle = service.getHandle("", passwd);
        assertEquals(handle.authenticate(), PamReturnValue.PAM_AUTH_ERR);
    }
}
