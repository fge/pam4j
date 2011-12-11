package org.eel.kitchen.pam;

public enum PamReturnValue
{
    PAM_SUCCESS("Successful function return"),
    PAM_OPEN_ERR("dlopen() failure when dynamically loading a service module"),
    PAM_SYMBOL_ERR("Symbol not found"),
    PAM_SERVICE_ERR("Error in service module"),
    PAM_SYSTEM_ERR("System error"),
    PAM_BUF_ERR("Memory buffer error"),
    PAM_PERM_DENIED("Permission denied"),
    PAM_AUTH_ERR("Authentication failure"),
    PAM_CRED_INSUFFICIENT("Can not access authentication data due to "
        + "insufficient credentials"),
    PAM_AUTHINFO_UNAVAIL("Underlying authentication service can not retrieve "
        + "authentication information"),
    PAM_USER_UNKNOWN("User not known to the underlying authentication module"),
    PAM_MAXTRIES("An authentication service has maintained a retry count which "
        + "has been reached. No further retries should be attempted"),
    PAM_NEW_AUTHTOK_REQD("New authentication token required. This is normally "
        + "returned if the machine security policies require that the password "
        + "should be changed because the password is NULL or it has aged"),
    PAM_ACCT_EXPIRED("User account has expired"),
    PAM_SESSION_ERR("Can not make/remove an entry for the specified session"),
    PAM_CRED_UNAVAIL("Underlying authentication service can not retrieve user "
        + "credentials"),
    PAM_CRED_EXPIRED("User credentials expired"),
    PAM_CRED_ERR("Failure setting user credentials"),
    PAM_NO_MODULE_DATA("No module specific data is present"),
    PAM_CONV_ERR("Conversation error"),
    PAM_AUTHTOK_ERR("Authentication token manipulation error"),
    PAM_AUTHTOK_RECOVER_ERR("Authentication information cannot be recovered"),
    PAM_AUTHTOK_LOCK_BUSY("Authentication token lock busy"),
    PAM_AUTHTOK_DISABLE_AGING("Authentication token aging disabled"),
    PAM_TRY_AGAIN("Preliminary check by password service"),
    PAM_IGNORE("Ignore underlying account module regardless of whether the "
        + "control flag is required, optional, or sufficient"),
    PAM_ABORT("Critical error (?module fail now request)"),
    PAM_AUTHTOK_EXPIRED("User's authentication token has expired"),
    PAM_MODULE_UNKNOWN("Module is not known"),
    PAM_BAD_ITEM("Bad item passed to pam_*_item()"),
    PAM_CONV_AGAIN("Conversation function is event driven and data is not "
        + "available yet"),
    PAM_INCOMPLETE("Please call this function again to complete authentication "
        + "stack Before calling again, verify that conversation is completed");


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

    /**
     * @return the String description of the return value
     */
    @Override
    public String toString()
    {
        return description;
    }
}

