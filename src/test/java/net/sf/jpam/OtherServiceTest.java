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
        throws PamException
    {
        super.setUp();
        pam = new Pam("other");
    }

    @Test
    public void testUserAuthenticated()
        throws PamException
    {
        final PamReturnValue retval = pam.authenticate(user, passwd);
        assertEquals(retval, PamReturnValue.PAM_AUTH_ERR);
    }

    @Test
    public void testUserWithNullCredentials()
    {
        try {
            pam.authenticate(user, null);
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "credentials are null");
        }
    }

    @Test
    public void testUserWithEmptyCredentials()
        throws PamException
    {
        final PamReturnValue retval = pam.authenticate(user, "");
        assertEquals(retval, PamReturnValue.PAM_AUTH_ERR);
    }

    @Test
    public void testUserWithNullUsername()
    {
        try {
            pam.authenticate(null, null);
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "user name is null");
        }
    }

    @Test
    public void testUserWithEmptyUsername()
        throws PamException
    {
        final PamReturnValue retval = pam.authenticate("", passwd);
        assertEquals(retval, PamReturnValue.PAM_AUTH_ERR);
    }
}
