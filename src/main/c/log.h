#ifndef __LOG_H__
#define __LOG_H__

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_org_eel_kitchen_pam_PamHandle_initLog(JNIEnv *env,
    jclass cls, jobject jlogger);

void debug(JNIEnv *env, const char *fmt, ...);
void info(JNIEnv *env, const char *fmt, ...);
void warn(JNIEnv *env, const char *fmt, ...);
void error(JNIEnv *env, const char *fmt, ...);

#ifdef __cplusplus
}
#endif

#endif /* __LOG_H__ */