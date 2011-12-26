#include "log.h"

static struct {
    jobject loggerRef;
    jmethodID debug;
    jmethodID info;
    jmethodID warn;
    jmethodID error;
} jlogdata;

#define LOG_METHOD_SIGNATURE "(Ljava/lang/String;)V"

JNIEXPORT void JNICALL Java_org_eel_kitchen_pam_PamHandle_initLog(JNIEnv *env,
    jclass cls, jobject jlogger)
{
    jobject logger;
    jclass loggerClass;

    /*
     * Grab a global reference to the logger object
     */
    logger = (*env)->NewGlobalRef(env, jlogger);
    jlogdata.loggerRef = logger;

    /*
     * Now grab method IDs
     */
    loggerClass = (*env)->GetObjectClass(env, logger);

    jlogdata.debug = (*env)->GetMethodID(env, loggerClass, "debug",
        LOG_METHOD_SIGNATURE);
    jlogdata.info = (*env)->GetMethodID(env, loggerClass, "info",
        LOG_METHOD_SIGNATURE);
    jlogdata.warn = (*env)->GetMethodID(env, loggerClass, "warn",
        LOG_METHOD_SIGNATURE);
    jlogdata.error = (*env)->GetMethodID(env, loggerClass, "error",
        LOG_METHOD_SIGNATURE);
}

void debug(JNIEnv *env, const char *msg)
{
    jstring jmsg;
    jobject obj = jlogdata.loggerRef;
    jmethodID method = jlogdata.debug;

    jmsg = (*env)->NewStringUTF(env, msg);

    if (jmsg)
        (*env)->CallVoidMethod(env, obj, method, jmsg);
}
