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
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <security/pam_appl.h>

#include "PamHandle.h"
#include "log.h"

#define pr_debug(args...) do { \
    printf(args); \
    fflush(stdout); \
} while (0)

static jfieldID handleRef = NULL;

/*
 * Needed: pam's misc_conv() function will wait for a passwd in stdin... We
 * want non interactive stuff here.
 *
 * TODO: look in native code how this is done. It probably detects the presence
 * of a tty device or something, and if this is the case, we'd better not
 * reinvent the wheel and use what PAM has to offer. Who knows, PAM may even
 * provide a "conversation switch" function or something.
 */

static int custom_conv(int, const struct pam_message **, struct pam_response **,
    void *);

static int custom_conv(int num_messages, const struct pam_message **messages,
    struct pam_response **resp, void *appdata_ptr)
{
    int i;
    int password_entered = 0;
    char *passwd;
    int msg_style;
    const struct pam_message *msg;
    struct pam_response *replies, *reply;

    replies = calloc(num_messages, sizeof(struct pam_response));
    if (!replies)
        return PAM_CONV_ERR;

    passwd = strdup((const char *)appdata_ptr);
    if (!passwd) {
        free(replies);
        return PAM_CONV_ERR;
    }

    memset(replies, 0, sizeof(*replies));
    reply = replies;

    pr_debug("Entering pam_conv\n");
    for (i = 0; i < num_messages; reply++, i++) {
        msg = messages[i];
        msg_style = msg->msg_style;

        switch (msg_style) {
            case PAM_PROMPT_ECHO_OFF: case PAM_PROMPT_ECHO_ON:
                if (password_entered)
                    pr_debug("Eh? Entering password more than once?\n");
                replies->resp = passwd;
                password_entered = 1;
        }
    }

    pr_debug("Password entered: %d\n", password_entered);
    pr_debug("Exiting pam_conv\n");

    *resp = replies;
    return PAM_SUCCESS;
}

/*
 * Create our PAM handle. This function is also in charge of initializing
 * handleRef.
 */
JNIEXPORT jint JNICALL Java_org_eel_kitchen_pam_PamHandle_createHandle(
    JNIEnv *env, jobject instance, jstring jservice, jstring juser)
{
    const char *service;
    const char *user;
    pam_handle_t *handle;
    int retval;
    struct pam_conv conv = {
        .conv = custom_conv,
        .appdata_ptr = NULL
    };

    if (!handleRef) {
        jclass class = (*env)->GetObjectClass(env, instance);
        handleRef = (*env)->GetFieldID(env, class, "_handleRef", "J");
    }

    service = (*env)->GetStringUTFChars(env, jservice, NULL);
    user = (*env)->GetStringUTFChars(env, juser, NULL);

    retval = pam_start(service, user, &conv, &handle);

    if (retval == PAM_SUCCESS)
        (*env)->SetLongField(env, instance, handleRef, (long) handle);

    (*env)->ReleaseStringUTFChars(env, jservice, service);
    (*env)->ReleaseStringUTFChars(env, juser, user);

    debug(env, "pam_start() result: %d", retval);
    return (jint) retval;
}
/*
 * Authenticate ourselves.
 */
JNIEXPORT jint JNICALL Java_org_eel_kitchen_pam_PamHandle_auth(JNIEnv *env,
    jobject instance, jlong jhandle, jstring jpasswd)
{
    pam_handle_t *handle;
    const char *passwd;
    int retval;
    struct pam_conv conv;

    handle = (pam_handle_t *) jhandle;

    passwd = (*env)->GetStringUTFChars(env, jpasswd, NULL);

    conv.conv = custom_conv;
    conv.appdata_ptr = (void *) passwd;

    retval = pam_set_item(handle, PAM_CONV, &conv);
    if (retval != PAM_SUCCESS) {
        error(env, "Ick! Cannot set PAM_CONV!");
        goto out;
    }

    retval = pam_authenticate(handle, 0);
    debug(env, "pam_authenticate() result: %d", retval);
out:
    (*env)->ReleaseStringUTFChars(env, jpasswd, passwd);
    return (jint) retval;
}

/*
 * Destroy our handle.
 */
JNIEXPORT jint JNICALL Java_org_eel_kitchen_pam_PamHandle_destroyHandle(
    JNIEnv *env, jobject instance, jlong jhandle, jint jstatus)
{
    pam_handle_t *handle = (pam_handle_t *) jhandle;

    debug(env, "destroy handle @%p", handle);

    return (jint) pam_end(handle, (int) jstatus);
}
