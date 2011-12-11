package net.sf.jpam;

import org.eel.kitchen.pam.PamReturnValue;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SecurIdTest
    extends AbstractPamTest
{

    private static final String SECURID_SERVICE = "net-sf-jpam-securid";

    @Test
    public void testUserAuthenticated()
    {
        final Pam pam = new Pam(SECURID_SERVICE);
        assertEquals(pam.authenticate(user1Name, "655635"),
            PamReturnValue.PAM_SUCCESS);
    }

    @Test
    public void testUserWithBadCredentialsNotAuthenticated()
    {
        final Pam pam = new Pam(SECURID_SERVICE);
        assertNotEquals(pam.authenticate(user1Name, user1BadCredentials),
            PamReturnValue.PAM_SUCCESS);
    }

    @Test
    public void testUserWithUnkownUserName()
    {
        final Pam pam = new Pam(SECURID_SERVICE);
        assertNotEquals(pam.authenticate("zzzunknown", user1Credentials),
            PamReturnValue.PAM_SUCCESS);
    }
}
