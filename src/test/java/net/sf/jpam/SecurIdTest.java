package net.sf.jpam;

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
        assertTrue(pam.authenticateSuccessful(user1Name, "655635"));
    }

    @Test
    public void testUserWithBadCredentialsNotAuthenticated()
    {
        final Pam pam = new Pam(SECURID_SERVICE);
        assertFalse(pam.authenticateSuccessful(user1Name, user1BadCredentials));
    }

    @Test
    public void testUserWithUnkownUserName()
    {
        final Pam pam = new Pam(SECURID_SERVICE);
        assertFalse(pam.authenticateSuccessful("zzzunknown", user1Credentials));
    }
}
