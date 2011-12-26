#include "log.h"

static struct {
    jobject loggerRef;
    jmethodID debug;
} jlogdata = {
    .loggerRef = NULL,
    .debug = NULL,
};

#define LOG_METHOD_SIGNATURE "(Ljava/lang/String;)V"

JNIEXPORT void JNICALL Java_org_eel_kitchen_pam_PamHandle_initLog(JNIEnv *env,
    jclass cls, jobject jlogger)
{
    jobject logger;
    jclass loggerClass;
    jmethodID tmp;

    logger = (*env)->NewGlobalRef(env, jlogger);
    loggerClass = (*env)->GetObjectClass(env, logger);
    tmp = (*env)->GetMethodID(env, loggerClass, "debug", LOG_METHOD_SIGNATURE);

    jlogdata.loggerRef = logger;
    jlogdata.debug = tmp;
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
