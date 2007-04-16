/************************************************************
** Library functions to interact with the Linux-PAM        **
** modules in order to update a user's password on         **
** the system.                                             **
**                                                         **
** Make sure you add the following lines to the            **
** pam.conf file (ore equivalent):                         **
** cs_password auth     required                           **
**                          /lib/security/pam_unix_auth.so **
** cs_password account  required                           **
**                          /lib/security/pam_unix_acct.so **
** cs_password password required                           **
**                        /lib/security/pam_unix_passwd.so **
** cs_password session  required                           **
**                          /lib/security/pam_unix_acct.so **
**                                                         **
** Author:      Daryle Niedermayer (dpn)                   **
**              daryle@gpfn.ca                             **
**              Greg Luck                                  **
**              David Lutterkort                           **
** Date:        2002-06-17                                 **
**                                                         **
** $Id: Pam.c,v 1.11 2005/06/15 03:02:36 gregluck Exp $
** $Log: Pam.c,v $
**
************************************************************/


#include "net_sf_jpam_Pam.h"
#include <dlfcn.h>
#include <jni.h>
#include <ctype.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

//Mac OS X has its PAM libraries in a different place
#ifdef __APPLE__
#include <pam/pam_appl.h>
#include <pam/pam_misc.h>
#else
#include <security/pam_appl.h>
#endif

#include <unistd.h>
#include <sys/types.h>

#define MAX_USERNAMESIZE 32
#define MAX_PASSWORDSIZE 18
#define CS_BAD_DATA      -2
#define CS_BAD_USAGE 	 -1
#define CS_SUCCESS    	 0
#define COPY_STRING(s) (s) ? strdup(s) : NULL

/* Expected prompts on pam_authenticate / pam_conv */
#define PASS_PROMPT_SECUREID "Enter PASSCODE: "
#define PASS_PROMPT_DEFAULT	 "Password: "
#define PASS_PROMPT_MACOSX	 "Password:"

/* DEFINE STATIC EXTERNAL STRUCTURES AND VARIABLES SO THAT
   THEY ONLY HAVE SCOPE WITHIN THE METHODS AND FUNCTIONS OF
   THIS SOURCE FILE */
static const char*  service_name;
static const char*  username;
static const char*  password;
static jboolean     debug;
static int PAM_conv (int, struct pam_message**,
                     struct pam_response**, void*);
static struct pam_conv PAM_converse = {
 	PAM_conv,
	NULL
};

// We use these to hold handles to the libs we
// dlopen in JNI_OnLoad
static void* libpam;
static void* libpam_misc;

/*************************************************
** PAM Conversation function                    **
*************************************************/

static int PAM_conv (int num_msg,  struct pam_message **msg,
                     struct pam_response **resp, void *appdata_ptr) {
   int replies = 0;
   struct pam_response *reply = NULL;

   reply = malloc(sizeof(struct pam_response) * num_msg);
   if (!reply) return PAM_CONV_ERR;

   for (replies = 0; replies < num_msg; replies++) 
   {
      if (debug) {
          printf("***Message from PAM is: |%s|\n", msg[replies]->msg);
          printf("***Msg_style to PAM is: |%d|\n", msg[replies]->msg_style);
      }
      
      switch (msg[replies]->msg_style) {
      	case PAM_ERROR_MSG:
		if (debug)
			printf("***Received PAM_ERROR_MESSAGE, modifying strings\n");
		reply[replies].resp = strdup("\0");
		msg[replies]->msg = strdup("\0");
		break;
		
	case PAM_TEXT_INFO:
		if (debug)
			printf("***Received PAM_TEXT_INFO, modifying strings\n");
		reply[replies].resp = strdup("\0");
		msg[replies]->msg = strdup("\0");
		break;
		
	/* jpam does not yet support password changing, we just copy password */
	case PAM_PROMPT_ECHO_OFF:
	case PAM_PROMPT_ECHO_ON:
		/* Get a prompt, fill the password */
		if ( 	(! strcmp(msg[replies]->msg, PASS_PROMPT_SECUREID)) || 
			(! strcmp(msg[replies]->msg, PASS_PROMPT_DEFAULT)) ||
			(! strcmp(msg[replies]->msg, PASS_PROMPT_MACOSX)) ) 
		{
			if (debug)
				printf("***Sending password\n");
			reply[replies].resp = COPY_STRING(password);
		}
		break;

	/* XXX Not implemented yet. Just ignore.*/
	case PAM_BINARY_PROMPT:
		if (debug)
			printf("***Received PAM_BINARY_PROMPT, not implemented yet, modifying strings\n");
		reply[replies].resp = strdup("\0");
		msg[replies]->msg = strdup("\0");
		break;

	default:
		if (debug)
			fprintf(stderr, "***erroneous conversation (%d)\n", msg[replies]->msg_style);
		/* XXX Need to cleanup the message stack */
		reply[replies].resp = strdup("\0");
		msg[replies]->msg = strdup("\0");
      }
      if (debug)
        printf("***Response to PAM is: |%s|\n", reply[replies].resp);
   }
   *resp = reply;
   return PAM_SUCCESS;
}

JNIEXPORT void JNICALL Java_net_sf_jpam_Pam_nativeMethod(JNIEnv *env, jobject obj) {
    jclass cls = (*env)->GetObjectClass(env, obj);
    jmethodID mid = (*env)->GetMethodID(env, cls, "callback", "()V");
    if (mid == NULL) {
        return; /* method not found */
    }
    (*env)->CallVoidMethod(env, obj, mid);
}

JNIEXPORT jint JNICALL JNI_OnLoad (JavaVM * vm, void * reserved) {
  libpam = dlopen("libpam.so", RTLD_GLOBAL | RTLD_LAZY); 
  libpam_misc = dlopen("libpam_misc.so", RTLD_GLOBAL | RTLD_LAZY);
  return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
  dlclose(libpam);
  dlclose(libpam_misc);
}

/*
 * Class:     net_sf_jpam_Pam
 * Method:    isSharedLibraryWorking
 * Signature: ()Z
 * Calls Pam.callback() to check that method callbacks into Java are working
 */
JNIEXPORT jboolean JNICALL Java_net_sf_jpam_Pam_isSharedLibraryWorking
  (JNIEnv *env, jobject obj) {
    Java_net_sf_jpam_Pam_nativeMethod(env, obj);
    return JNI_TRUE;
}

/* Prints the meaning of the retval message */
void printreturnmeaning(int retval, pam_handle_t *pamh) {
    const char * pamerror = pam_strerror(pamh, retval);
    printf("PAM Response: %s\n", pamerror);
}


/*
 * Class:     net_sf_jpam_Pam
 * Method:    authenticate
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)I
 */
JNIEXPORT jint JNICALL Java_net_sf_jpam_Pam_authenticate
  (JNIEnv *pEnv, jobject pObj, jstring pServiceName, jstring pUsername, jstring pPassword, jboolean debug) {

     /* DEFINITIONS */
       pam_handle_t*    pamh = NULL;
       int              retval;

    service_name = (*pEnv)->GetStringUTFChars(pEnv, pServiceName,0);
    username = (*pEnv)->GetStringUTFChars(pEnv, pUsername,0);
    password = (*pEnv)->GetStringUTFChars(pEnv, pPassword,0);

    if (debug) {
        printf("service_name is %s\n", service_name);
        printf("password is %s\n", password);
        printf("username is %s\n", username);
    }


        /* GET A HANDLE TO A PAM INSTANCE */
	   if (debug) {
		   printf("Trying to get a handle to the PAM service...\n");
	    }
       retval = pam_start(service_name, username, &PAM_converse, &pamh);

        /* IS THE USER REALLY A USER? */
       if (retval == PAM_SUCCESS) {
		  if (debug) {
	          printf("...Service handle was created.\n");
	          printf("Trying to see if the user is a valid system user...\n");
		  }
	  pam_set_item(pamh, PAM_AUTHTOK, password);
          retval = pam_authenticate(pamh, 0);
       }
       else {
       		if (debug) {
          		printf("...Call to create service handle failed with error: %d\n",retval);
       		}
       }

        /* IS USER PERMITTED ACCESS? */
       if (retval == PAM_SUCCESS) {
       		if (debug) {
		          printf("...User %s is a real user.\n",username);
		          printf("Trying to pass info to the pam_acct_mgmt function...\n");
       		}
          retval = pam_acct_mgmt(pamh, 0);
       }
       else {
       	if (debug){
          if (retval == PAM_USER_UNKNOWN)
             printf("...Failed to find user %s with error: %d\n",username,retval);
          else
          	printf("...Failed to authenticate for an unknown error: %d\n",retval);
        }
       }

		if (debug) {
	       if (retval == PAM_SUCCESS) {
	          printf("...User %s is permitted access.\n",username);
	       }
	       else {
	          printf("...cs_password error: User %s is not authenticated\n",username);
	          printf("...Call returned with error: %d\n",retval);
	       }
		}

        /* CLEAN UP OUR HANDLES AND VARIABLES */
       if (pam_end(pamh, retval) != PAM_SUCCESS) {
          pamh = NULL;
          if (debug) {
          	fprintf(stderr, "cs_password error: Failed to release authenticator\n");
          }
       }

       return retval;


}
