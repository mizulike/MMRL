#include <jni.h>
#include <sys/stat.h>
#include <jni.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>
#include <dirent.h>
#include <cstring>
#include <stack>
#include <cstdio>
#include <cerrno>
#include <string>
#include <fstream>
#include <logging.hpp>

#define MMRL_UNUSED(x) x __attribute__((__unused__))

extern "C"
JNIEXPORT jboolean JNICALL
Java_dev_dergoogler_mmrl_compat_impl_FileManagerImpl_nativeSetOwner(JNIEnv *env,
                                                                    jobject MMRL_UNUSED(thiz),
                                                                    jstring path, jint owner,
                                                                    jint group) {
    const char *cpath = env->GetStringUTFChars(path, nullptr);
    bool success = (chown(cpath, owner, group) == 0);
    env->ReleaseStringUTFChars(path, cpath);
    return success ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_dev_dergoogler_mmrl_compat_impl_FileManagerImpl_nativeSetPermissions(JNIEnv *env,
                                                                          jobject MMRL_UNUSED(thiz),
                                                                          jstring path,
                                                                          jint mode) {
    const char *cpath = env->GetStringUTFChars(path, nullptr);
    bool success = (chmod(cpath, static_cast<mode_t>(mode)) == 0);
    env->ReleaseStringUTFChars(path, cpath);
    return success ? JNI_TRUE : JNI_FALSE;
}