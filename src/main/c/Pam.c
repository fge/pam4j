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

#define COPY_STRING(s) (s) ? strdup(s) : NULL

/* DEFINE STATIC EXTERNAL STRUCTURES AND VARIABLES SO THAT
   THEY ONLY HAVE SCOPE WITHIN THE METHODS AND FUNCTIONS OF
   THIS SOURCE FILE */
static const char *service_name;
static const char *username;
static const char *password;
static jboolean debug;

static int PAM_conv(int, const struct pam_message **, struct pam_response**,
    void*);

static struct pam_conv PAM_converse = {
    .conv = PAM_conv,
    .appdata_ptr = NULL
};

// We use these to hold handles to the libs we
// dlopen in JNI_OnLoad
static void* libpam;
static void* libpam_misc;

/*************************************************
** PAM Conversation function                    **
*************************************************/

static int PAM_conv(int num_messages, const struct pam_message **messages,
    struct pam_response **resp, void *appdata_ptr)
{
    int i = 0;
    const struct pam_message *msg;
    struct pam_response *reply;
    const char *prompt;

    struct pam_response *replies = calloc(num_messages,
        sizeof(struct pam_response));
    if (!replies)
        return PAM_CONV_ERR;

    for (i = 0; i < num_messages; i++) {
        msg = messages[i];
        prompt = msg->msg;
        reply = &replies[i];

        if (debug) {
            printf("***Message from PAM is: |%s|\n", prompt);
            printf("***Msg_style to PAM is: |%d|\n", msg->msg_style);
        }

        //SecurId requires this syntax.
        if (!strcmp(prompt, "Enter PASSCODE: ")) {
            if (debug)
                printf("***Sending password\n");
            reply->resp = COPY_STRING(password);
        }

        if (!strcmp(prompt, "Password: ")) {
            if (debug)
                printf("***Sending password\n");
            reply->resp = COPY_STRING(password);
        }

        //Mac OS X
        if (! strcmp(prompt, "Password:")) {
            if (debug)
                printf("***Sending password\n");
            reply->resp = COPY_STRING(password);
        }

        // HP-UX
        if (!strcmp(prompt, "System Password:")) {
            if (debug)
                printf("***Sending password\n");
            reply->resp = COPY_STRING(password);
        }

        // If none of the above matches, make sure the printf() does not
        // crash because replies[i].resp is NULL
        if (debug && reply->resp != NULL)
            printf("***Response to PAM is: |%s|\n", reply->resp);
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

#define pr_debug(args...) do { \
    if (debug) \
        printf(args); \
} while (0)

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

    pr_debug("service_name is %s\n", service_name);
    pr_debug("password is %s\n", password);
    pr_debug("username is %s\n", username);

    /* Get a handle to a PAM instance */
    pr_debug("Trying to get a handle to the PAM service...\n");

    retval = pam_start(service_name, username, &PAM_converse, &pamh);

    if (retval != PAM_SUCCESS) {
        pr_debug("...call to create service handle failed with error: %d\n",
            retval);
        goto out_nohandle;
    }

    /* Is the user really a user? */
    pr_debug("...service handle was created.\n");
    pr_debug("Trying to see if the user is a valid system user...\n");

    pam_set_item(pamh, PAM_AUTHTOK, password);
    retval = pam_authenticate(pamh, 0);

    /* Is user permitted access? */
    if (retval != PAM_SUCCESS) {
        if (retval == PAM_USER_UNKNOWN)
            pr_debug("...failed to find user %s with error: %d\n", username,
                retval);
        else
            pr_debug("...failed to authenticate with error: %d\n", retval);
        goto out_free;
    }

    pr_debug("...user %s is a real user.\n",username);
    pr_debug("Trying to pass info to the pam_acct_mgmt function...\n");

    retval = pam_acct_mgmt(pamh, 0);

    if (retval == PAM_SUCCESS)
        pr_debug("...user %s is permitted access.\n",username);
    else {
        pr_debug("...call returned with error: %d\n",retval);
    }

out_free:
    /* Clean up our handles and variables */
    if (pam_end(pamh, retval) != PAM_SUCCESS) {
        pamh = NULL;
        if (debug)
            fprintf(stderr, "cs_password error: failed to release handle\n");
    }

out_nohandle:
    (*pEnv)->ReleaseStringUTFChars(pEnv, pServiceName, service_name);
    (*pEnv)->ReleaseStringUTFChars(pEnv, pUsername, username);
    (*pEnv)->ReleaseStringUTFChars(pEnv, pPassword, password);
    return retval;
}
