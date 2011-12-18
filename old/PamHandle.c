/*
 * Copyright (c) 2011, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
#include "org_eel_kitchen_pam_PamHandle.h"
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <security/pam_appl.h>

#if 0
#include <security/pam_misc.h>
#endif

#define pr_debug(args...) do { \
    if (debug) { \
        printf(args); \
        fflush(stdout); \
    } \
} while (0)

static const char *service_name;
static const char *username;
static const char *password;

/*
 * Needed: pam's misc_conv() function will wait for a passwd in stdin... We
 * want non interactive stuff here.
 *
 * TODO: look in native code how this is done. It probably detects the presence
 * of a tty device or something, and if this is the case, we'd better not
 * reinvent the wheel and use what PAM has to offer. Who knows, PAM may even
 * provide a "conversation switch" function or something.
 */

static int PAM_conv(int, const struct pam_message **, struct pam_response **,
    void *);

static struct pam_conv PAM_converse = {
//    .conv = misc_conv,
    .conv = PAM_conv,
    .appdata_ptr = NULL
};

/*************************************************
** PAM Conversation function                    **
*************************************************/

static int PAM_conv(int num_messages, const struct pam_message **messages,
    struct pam_response **resp, void *appdata_ptr)
{
    /*
     * Note that this function is NOT suitable for anything other than
     * authentication purposes... We only enter the password once.
     */
    int i = 0;
    int password_entered = 0;
    const struct pam_message *msg;
    struct pam_response *reply;
    int msg_style;

    struct pam_response *replies;

    printf("pam_conv start\n");
    fflush(stdout);

    replies = calloc(num_messages, sizeof(struct pam_response));
    if (!replies)
        return PAM_CONV_ERR;

    memset(replies, 0, sizeof(*replies));

    for (i = 0; i < num_messages; i++) {
        msg = messages[i];
        msg_style = msg->msg_style;

        switch (msg_style) {
            case PAM_PROMPT_ECHO_OFF: case PAM_PROMPT_ECHO_ON:
                if (password_entered) {
                    // FIXME: this does not free individual entries!!!
                    free(replies);
                    return PAM_CONV_ERR;
                }
                break;
            default:
                continue;
        }

        printf("pam_conv: passwd entered\n");
        fflush(stdout);
        reply = &replies[i];
        reply->resp = password ? strdup(password) : NULL;
        password_entered = 1;
    }

    *resp = replies;
    printf("pam_conv end\n");
    fflush(stdout);
    return PAM_SUCCESS;
}

/*
 * Class:     net_sf_jpam_Pam
 * Method:    authenticate
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)I
 */
JNIEXPORT jint JNICALL Java_org_eel_kitchen_pam_PamHandle_authenticate(
    JNIEnv *pEnv, jobject pObj, jstring pServiceName, jstring pUsername,
    jstring pPassword, jboolean debug)
{
    pam_handle_t *pamh = NULL;
    int retval;

    /*
     * TODO: unclear, see what's what
     *
     * With my first tests, it appears that GetStringUTFChars() makes the JVM
     * crash if memory cannot be allocated... But an array copy was made. See
     * what happens if the JVM decides NOT to make a copy. Right now it is
     * assumed that allocations succeed. And the JNI spec says
     * GetStringUTFChars() does NOT throw an OOM on failure.
     */
    service_name = (*pEnv)->GetStringUTFChars(pEnv, pServiceName, NULL);
    username = (*pEnv)->GetStringUTFChars(pEnv, pUsername, NULL);
    password = (*pEnv)->GetStringUTFChars(pEnv, pPassword, NULL);

    /* Get a handle to a PAM instance */
    pr_debug("pam_start\n");
    retval = pam_start(service_name, username, &PAM_converse, &pamh);

    if (retval != PAM_SUCCESS) {
        pr_debug("pam_start failed for service %s: %s\n", service_name,
            pam_strerror(NULL, retval));
        goto out_nohandle;
    }

    pr_debug("pam_set_item\n");
    pam_set_item(pamh, PAM_AUTHTOK, password);
    pr_debug("pam_authenticate\n");
    retval = pam_authenticate(pamh, 0);

    /* Is user permitted access? */
    if (retval != PAM_SUCCESS) {
        pr_debug("failed to authenticate user %s: %s\n", username,
            pam_strerror(NULL, retval));
        goto out_free;
    }

    pr_debug("pam_acct_mgmt\n");
    retval = pam_acct_mgmt(pamh, 0);

    if (retval != PAM_SUCCESS)
        pr_debug("failed to setup account for user %s: %s\n", username,
            pam_strerror(NULL, retval));

out_free:
    /* Clean up our handles and variables */
    if (pam_end(pamh, retval) != PAM_SUCCESS) {
        pamh = NULL;
        pr_debug("Fuchs! Failed to release PAM handle\n");
    }

out_nohandle:
    (*pEnv)->ReleaseStringUTFChars(pEnv, pServiceName, service_name);
    (*pEnv)->ReleaseStringUTFChars(pEnv, pUsername, username);
    (*pEnv)->ReleaseStringUTFChars(pEnv, pPassword, password);

    pr_debug("pam: end\n");
    return retval;
}
