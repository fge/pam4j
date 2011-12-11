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
        assertEquals(pam.authenticate(user, "655635"),
            PamReturnValue.PAM_SUCCESS);
    }

    @Test
    public void testUserWithBadCredentialsNotAuthenticated()
    {
        final Pam pam = new Pam(SECURID_SERVICE);
        assertNotEquals(pam.authenticate(user, badPasswd),
            PamReturnValue.PAM_SUCCESS);
    }

    @Test
    public void testUserWithUnkownUserName()
    {
        final Pam pam = new Pam(SECURID_SERVICE);
        assertNotEquals(pam.authenticate("zzzunknown", passwd),
            PamReturnValue.PAM_SUCCESS);
    }
}
