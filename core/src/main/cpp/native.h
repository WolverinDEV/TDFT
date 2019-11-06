#include <jni.h>
#pragma once

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     dev_wolveringer_tdft_Native
 * Method:    cd
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_dev_wolveringer_tdft_Native_cd
  (JNIEnv *, jclass, jstring);

#ifdef __cplusplus
}
#endif
