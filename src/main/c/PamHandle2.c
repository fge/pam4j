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
#include "org_eel_kitchen_pam_PamHandle2.h"
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <security/pam_appl.h>

#if 0
#include <security/pam_misc.h>
#endif

#define pr_debug(args...) do { \
    printf(args); \
    fflush(stdout); \
} while (0)

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

#if 0
static struct pam_conv PAM_converse = {
    .conv = PAM_conv,
    .appdata_ptr = NULL
};
#endif

/*************************************************
** PAM Conversation function                    **
*************************************************/

static int PAM_conv(int num_messages, const struct pam_message **messages,
    struct pam_response **resp, void *appdata_ptr)
{
#if 0
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

        reply = &replies[i];
        reply->resp = password ? strdup(password) : NULL;
        password_entered = 1;
    }

    *resp = replies;
#endif
    return PAM_SUCCESS;
}

/*
 * Class:     org_eel_kitchen_pam_PamHandle2
 * Method:    createHandle
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_eel_kitchen_pam_PamHandle2_createHandle(
    JNIEnv *env, jobject instance, jstring jservice, jstring juser)
{
    jclass class;
    jfieldID handleRef;
    const char *service;
    const char *user;
    pam_handle_t *handle;
    struct pam_conv *conv;
    jint retval;

    conv = malloc(sizeof(struct pam_conv));

    if (!conv)
        return PAM_SYSTEM_ERR; // PAM_BUF_ERR?

    conv->conv = PAM_conv;
    conv->appdata_ptr = NULL;  // Set it later to pamh

    /*
     * Get the class of the instance, and grab a handle to the field
     */
    class = (*env)->GetObjectClass(env, instance);
    handleRef = (*env)->GetFieldID(env, class, "_handleRef", "J");

    if (!handleRef) {
        pr_debug("Fuchs! No \"_handleRef\" in object instance!");
        return PAM_SYSTEM_ERR;
    }

    service = (*env)->GetStringUTFChars(env, jservice, NULL);
    user = (*env)->GetStringUTFChars(env, juser, NULL);

    retval = pam_start(service, user, conv, &handle);

    if (!retval) /* Success */
        (*env)->SetLongField(env, instance, handleRef, (unsigned long) handle);

    (*env)->ReleaseStringUTFChars(env, jservice, service);
    (*env)->ReleaseStringUTFChars(env, juser, user);

    return (jint) retval;
}

/*
 * Class:     org_eel_kitchen_pam_PamHandle2
 * Method:    destroyHandle
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_eel_kitchen_pam_PamHandle2_destroyHandle(
    JNIEnv *env, jobject instance)
{
    jclass class;
    jfieldID handleRef;
    jfieldID statusRef;
    pam_handle_t *handle;
    int status;

    /*
     * Get the class of the instance, and grab a handle to the field
     */
    class = (*env)->GetObjectClass(env, instance);
    handleRef = (*env)->GetFieldID(env, class, "_handleRef", "J");
    statusRef = (*env)->GetFieldID(env, class, "_lastStatus", "I");

    if (!(handleRef && statusRef)) {
        pr_debug("Fuchs! Missing fields in object instance!");
        return PAM_SYSTEM_ERR;
    }

    handle = (pam_handle_t *) (*env)->GetLongField(env, instance, handleRef);
    status = (*env)->GetIntField(env, instance, statusRef);

    return (jint) pam_end(handle, status);
}
