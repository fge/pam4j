package net.sf.jpam;

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
        assertTrue(pam.authenticateSuccessful(user1Name, "1234745549"));
    }

    @Test
    public void testUserWithBadCredentialsNotAuthenticated()
    {
        final Pam pam = new Pam(RADIUS_SERVICE);
        assertFalse(pam.authenticateSuccessful(user1Name, user1BadCredentials));
    }

    @Test
    public void testUserWithUnkownUserName()
    {
        final Pam pam = new Pam(RADIUS_SERVICE);
        assertFalse(pam.authenticateSuccessful("zzzunknown", user1Credentials));
    }
}
