package net.sf.jpam;

import org.eel.kitchen.pam.PamReturnValue;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DigiPassTest
    extends AbstractPamTest
{

    private static final String RADIUS_SERVICE = "net-sf-jpam-digipass";

    @Test
    public void testUserAuthenticated()
    {
        final Pam pam = new Pam(RADIUS_SERVICE);
        assertEquals(pam.authenticate(user1Name, "1234745549"),
            PamReturnValue.PAM_SUCCESS);
    }

    @Test
    public void testUserWithBadCredentialsNotAuthenticated()
    {
        final Pam pam = new Pam(RADIUS_SERVICE);
        assertNotEquals(pam.authenticate(user1Name, user1BadCredentials),
            PamReturnValue.PAM_SUCCESS);
    }

    @Test
    public void testUserWithUnkownUserName()
    {
        final Pam pam = new Pam(RADIUS_SERVICE);
        assertNotEquals(pam.authenticate("zzzunknown", user1Credentials),
            PamReturnValue.PAM_SUCCESS);
    }
}
