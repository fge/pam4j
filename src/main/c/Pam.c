#include "net_sf_jpam_Pam.h"
#include <dlfcn.h>
#include <jni.h>
#include <ctype.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include <security/pam_appl.h>

#include <unistd.h>
#include <sys/types.h>

#define pr_debug(args...) do { \
    if (debug) \
        printf(args); \
} while (0)

/* DEFINE STATIC EXTERNAL STRUCTURES AND VARIABLES SO THAT
   THEY ONLY HAVE SCOPE WITHIN THE METHODS AND FUNCTIONS OF
   THIS SOURCE FILE */
static const char *service_name;
static const char *username;
static const char *password;
static jboolean debug;

static int PAM_conv(int, const struct pam_message **, struct pam_response **,
    void *);

static struct pam_conv PAM_converse = {
    .conv = PAM_conv,
    .appdata_ptr = NULL
};

// We use these to hold handles to the libs we
// dlopen in JNI_OnLoad
static void *libpam;
static void *libpam_misc;

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
    const char *prompt;
    int msg_style;

    struct pam_response *replies;

    replies = calloc(num_messages, sizeof(struct pam_response));
    if (!replies)
        return PAM_CONV_ERR;

    memset(replies, 0, sizeof(*replies));

    for (i = 0; i < num_messages; i++) {
        msg = messages[i];
        msg_style = msg->msg_style;
        prompt = msg->msg;
        reply = &replies[i];

        switch (msg_style) {
            case PAM_PROMPT_ECHO_OFF: case PAM_PROMPT_ECHO_ON:
                if (password_entered) {
                    free(replies);
                    return PAM_CONV_ERR;
                }
                break;
            default:
                continue;
        }

        reply->resp = password ? strdup(password) : NULL;
        password_entered = 1;
    }

    *resp = replies;
    return PAM_SUCCESS;
}

JNIEXPORT void JNICALL Java_net_sf_jpam_Pam_nativeMethod(JNIEnv *env,
    jobject obj)
{
    jclass cls = (*env)->GetObjectClass(env, obj);
    jmethodID mid = (*env)->GetMethodID(env, cls, "callback", "()V");

    if (mid == NULL)
        return; /* method not found */

    (*env)->CallVoidMethod(env, obj, mid);
}

JNIEXPORT jint JNICALL JNI_OnLoad (JavaVM *vm, void *reserved)
{
    libpam = dlopen("libpam.so", RTLD_GLOBAL | RTLD_LAZY);
    libpam_misc = dlopen("libpam_misc.so", RTLD_GLOBAL | RTLD_LAZY);
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved)
{
    dlclose(libpam);
    dlclose(libpam_misc);
}

/*
 * Class:     net_sf_jpam_Pam
 * Method:    isSharedLibraryWorking
 * Signature: ()Z
 * Calls Pam.callback() to check that method callbacks into Java are working
 */
JNIEXPORT jboolean JNICALL Java_net_sf_jpam_Pam_isSharedLibraryWorking(
    JNIEnv *env, jobject obj)
{
    Java_net_sf_jpam_Pam_nativeMethod(env, obj);
    return JNI_TRUE;
}

/*
 * Class:     net_sf_jpam_Pam
 * Method:    authenticate
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)I
 */
JNIEXPORT jint JNICALL Java_net_sf_jpam_Pam_authenticate(JNIEnv *pEnv,
    jobject pObj, jstring pServiceName, jstring pUsername, jstring pPassword,
    jboolean debug)
{
    pam_handle_t *pamh = NULL;
    int retval;

    service_name = (*pEnv)->GetStringUTFChars(pEnv, pServiceName, NULL);
    username = (*pEnv)->GetStringUTFChars(pEnv, pUsername, NULL);
    password = (*pEnv)->GetStringUTFChars(pEnv, pPassword, NULL);

    /* Get a handle to a PAM instance */
    retval = pam_start(service_name, username, &PAM_converse, &pamh);

    if (retval != PAM_SUCCESS) {
        pr_debug("pam_start failed for service %s: %s\n", service_name,
            pam_strerror(NULL, retval));
        goto out_nohandle;
    }

    pam_set_item(pamh, PAM_AUTHTOK, password);
    retval = pam_authenticate(pamh, 0);

    /* Is user permitted access? */
    if (retval != PAM_SUCCESS) {
        pr_debug("failed to authenticate user %s: %s\n", username,
            pam_strerror(NULL, retval));
        goto out_free;
    }

    retval = pam_acct_mgmt(pamh, 0);

    if (retval != PAM_SUCCESS)
        pr_debug("failed to setup account for user %s: %s\n", username,
            pam_strerror(NULL, retval));

out_free:
    /* Clean up our handles and variables */
    if (pam_end(pamh, retval) != PAM_SUCCESS) {
        pamh = NULL;
        pr_debug("cs_password error: failed to release handle\n");
    }

out_nohandle:
    (*pEnv)->ReleaseStringUTFChars(pEnv, pServiceName, service_name);
    (*pEnv)->ReleaseStringUTFChars(pEnv, pUsername, username);
    (*pEnv)->ReleaseStringUTFChars(pEnv, pPassword, password);

    return retval;
}
