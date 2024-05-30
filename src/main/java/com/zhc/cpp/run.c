#include <jni.h>
#include <string.h>
#include "com_zhc_aeojcodesandbox_sandbox_NativeSandBox.h"

JNIEXPORT jobject JNICALL Java_com_zhc_aeojcodesandbox_sandbox_NativeSandBox_run
  (JNIEnv *env, jobject obj, jobject config) {
    jclass resultClass = (*env)->FindClass(env, "com/zhc/aeojcodesandbox/sandbox/Result");
      if (resultClass == NULL) {
          return NULL;
      }

    jmethodID constructor = (*env)->GetMethodID(env, resultClass, "<in it>", "(IIIIIII)V");
    if (constructor == NULL) {
        return NULL;
    }

    jobject jresult = (*env)->NewObject(env, resultClass, constructor,
                                        0, -1, -2,
                                        -3, -4, -5, -7);
    return jresult;
}