package org.eel.kitchen.pam;

public enum PamReturnValue
{
    PAM_SUCCESS("Success"),
    PAM_OPEN_ERR("Failed to load module"),
    PAM_SYMBOL_ERR("Symbol not found"),
    PAM_SERVICE_ERR("Error in service module"),
    PAM_SYSTEM_ERR("System error"),
    PAM_BUF_ERR("Memory buffer error"),
    PAM_PERM_DENIED("Permission denied"),
    PAM_AUTH_ERR("Authentication failure"),
    PAM_CRED_INSUFFICIENT("Insufficient credentials to access authentication "
        + "data"),
    PAM_AUTHINFO_UNAVAIL("Authentication service cannot retrieve "
        + "authentication info"),
    PAM_USER_UNKNOWN("User not known to the underlying authentication module"),
    PAM_MAXTRIES("Have exhausted maximum number of retries for service"),
    PAM_NEW_AUTHTOK_REQD("Authentication token is no longer valid; new one "
        + "required"),
    PAM_ACCT_EXPIRED("User account has expired"),
    PAM_SESSION_ERR("Can not make/remove an entry for the specified session"),
    PAM_CRED_UNAVAIL("Authentication service cannot retrieve user credentials"),
    PAM_CRED_EXPIRED("User credentials expired"),
    PAM_CRED_ERR("Failure setting user credentials"),
    PAM_NO_MODULE_DATA("No module specific data is present"),
    PAM_CONV_ERR("Conversation error"),
    PAM_AUTHTOK_ERR("Authentication token manipulation error"),
    PAM_AUTHTOK_RECOVER_ERR("Authentication information cannot be recovered"),
    PAM_AUTHTOK_LOCK_BUSY("Authentication token lock busy"),
    PAM_AUTHTOK_DISABLE_AGING("Authentication token aging disabled"),
    PAM_TRY_AGAIN("Failed preliminary check by password service"),
    PAM_IGNORE("The return value should be ignored by PAM dispatch"),
    PAM_ABORT("Critical error - immediate abort"),
    PAM_AUTHTOK_EXPIRED("Authentication token expired"),
    PAM_MODULE_UNKNOWN("Module is unknown"),
    PAM_BAD_ITEM("Bad item passed to pam_*_item()"),
    PAM_CONV_AGAIN("Conversation is waiting for event"),
    PAM_INCOMPLETE("Application needs to call libpam again");

    private final String description;

    PamReturnValue(final String description)
    {
        this.description = description;
    }

    /**
     * Gets the PamReturnValue that matches the given id
     * @param id a valid Integer with a value between 0 and 31
     * @return the PamReturnValue matching the id
     * @throws IllegalArgumentException if the id is outside the range of possible return values
     */
    public static PamReturnValue fromId(final int id)
        throws IllegalArgumentException
    {
        try {
            return values()[id];
        } catch (IndexOutOfBoundsException ignored) {
            throw new IllegalArgumentException("unknown PAM return code " + id);
        }
    }

    @Override
    public String toString()
    {
        return description;
    }
}

