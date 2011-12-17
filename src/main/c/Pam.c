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
#include "net_sf_jpam_Pam.h"
#include <jni.h>

JNIEXPORT void JNICALL Java_net_sf_jpam_Pam_nativeMethod(JNIEnv *env,
    jclass cls);

JNIEXPORT void JNICALL Java_net_sf_jpam_Pam_nativeMethod(JNIEnv *env,
    jclass cls)
{
    jmethodID mid = (*env)->GetStaticMethodID(env, cls, "callback", "()V");

    if (mid == NULL)
        return; /* method not found */

    (*env)->CallVoidMethod(env, cls, mid);
}

/*
 * Class:     net_sf_jpam_Pam
 * Method:    isSharedLibraryWorking
 * Signature: ()Z
 * Calls Pam.callback() to check that method callbacks into Java are working
 */
JNIEXPORT jboolean JNICALL Java_net_sf_jpam_Pam_isSharedLibraryWorking(
    JNIEnv *env, jclass cls)
{
    Java_net_sf_jpam_Pam_nativeMethod(env, cls);
    return JNI_TRUE;
}

