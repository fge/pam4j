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
** Revision 1.11  2005/06/15 03:02:36  gregluck
** Patches for native library loading and solaris
**
** Revision 1.10  2005/04/16 21:55:55  gregluck
** (From David Lutterkort) When the JVM opens a JNI library, it does a dlopen _without_ the
** RTLD_GLOBAL flag, so that in turn libjpam has access to libpam, but when
** libpam loads modules (like pam_unix), those modules can not be resolved
** against libpam.
** Added JNI_OnLoad/JNI_OnUnload functions that reopen libpam and
** libpam_misc with RTLD_GLOBAL, which makes the libs available for PAM
** modules.
**
** Revision 1.9  2004/11/11 10:24:34  gregluck
** Added c to Java callback and fixed the library installation test.
**
** Revision 1.8  2004/11/11 09:23:30  gregluck
** Fix error. should use PAM_conv
**
** Revision 1.7  2004/09/05 09:43:19  gregluck
** Further Mac OS X porting
**
** Revision 1.6  2004/09/04 11:52:04  gregluck
** compile fixes. Added some support for Mac OS X.
**
** Revision 1.5  2004/08/31 00:04:30  gregluck
** Holiday commit. Added JAAS support
**
** Revision 1.4  2004/08/20 03:07:14  gregluck
** All tests working.
**
** Revision 1.3  2004/08/18 12:22:20  gregluck
** Added some tests. Concurrency not working
**
** Revision 1.2  2004/08/17 02:38:52  gregluck
** Turn of printf statements unless debug mode set
**
** Revision 1.1.1.1  2004/08/17 01:46:26  gregluck
** Imported sources
**
** Revision 1.2  2002/06/20 19:51:24  root
** Fully documented and debugged test of how to change a password.
**
** Revision 1.1  2002/06/19 16:26:19  root
** Initial revision
**:
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
//#include <security/pam_misc.h>
#endif

#include <unistd.h>
#include <sys/types.h>

#define MAX_USERNAMESIZE 32
#define MAX_PASSWORDSIZE 18
#define CS_BAD_DATA  		-2
#define CS_BAD_USAGE 		-1
#define CS_SUCCESS    		0
#define COPY_STRING(s) (s) ? strdup(s) : NULL

/* DEFINE STATIC EXTERNAL STRUCTURES AND VARIABLES SO THAT
   THEY ONLY HAVE SCOPE WITHIN THE METHODS AND FUNCTIONS OF
   THIS SOURCE FILE */
static const char*  service_name;
static const char*  username;
static const char*  password;
static jboolean     debug;
static int PAM_conv (int, const struct pam_message**,
                     struct pam_response**, void*);
static struct pam_conv PAM_converse = {
//	misc_conv,
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

static int PAM_conv (int num_msg, const struct pam_message **msg,
                     struct pam_response **resp, void *appdata_ptr) {
   int replies = 0;
   struct pam_response *reply = NULL;

   reply = malloc(sizeof(struct pam_response) * num_msg);
   if (!reply) return PAM_CONV_ERR;

   for (replies = 0; replies < num_msg; replies++) {
    if (debug) {
          printf("***Message from PAM is: |%s|\n", msg[replies]->msg);
          printf("***Msg_style to PAM is: |%d|\n", msg[replies]->msg_style);
      }
      //SecurId requires this syntax.
      if (! strcmp(msg[replies]->msg,"Enter PASSCODE: ")) {
        if (debug)
            printf("***Sending password\n");
         reply[replies].resp = COPY_STRING(password);
      }
      if (! strcmp(msg[replies]->msg,"Password: ")) {
        if (debug)
            printf("***Sending password\n");
         reply[replies].resp = COPY_STRING(password);
      }
      //Mac OS X
      if (! strcmp(msg[replies]->msg,"Password:")) {
        if (debug)
            printf("***Sending password\n");
         reply[replies].resp = COPY_STRING(password);
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
