#include "log.h"
#include <stdio.h>
#include <stdarg.h>

static struct {
    jobject loggerRef;
    jmethodID debug;
    jmethodID info;
    jmethodID warn;
    jmethodID error;
} jlogdata;

#define LOG_METHOD_SIGNATURE "(Ljava/lang/String;)V"
#define MAX_MSG_SIZE 2048

static void doLog(JNIEnv *env, jmethodID method, const char *fmt, va_list args);

static void doLog(JNIEnv *env, jmethodID method, const char *fmt, va_list args)
{
    char msg[MAX_MSG_SIZE];
    jstring jmsg;
    jobject obj = jlogdata.loggerRef;

    vsnprintf(msg, MAX_MSG_SIZE, fmt, args);

    jmsg = (*env)->NewStringUTF(env, msg);

    if (jmsg)
        (*env)->CallVoidMethod(env, obj, method, jmsg);
}

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

void debug(JNIEnv *env, const char *fmt, ...)
{
    va_list args;

    va_start(args, fmt);
    doLog(env, jlogdata.debug, fmt, args);
    va_end(args);
}

void info(JNIEnv *env, const char *fmt, ...)
{
    va_list args;

    va_start(args, fmt);
    doLog(env, jlogdata.info, fmt, args);
    va_end(args);
}

void warn(JNIEnv *env, const char *fmt, ...)
{
    va_list args;

    va_start(args, fmt);
    doLog(env, jlogdata.warn, fmt, args);
    va_end(args);
}

void error(JNIEnv *env, const char *fmt, ...)
{
    va_list args;

    va_start(args, fmt);
    doLog(env, jlogdata.error, fmt, args);
    va_end(args);
}

