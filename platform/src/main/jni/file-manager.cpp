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
#include <dlfcn.h>

#define MMRL_UNUSED(x) x __attribute__((__unused__))

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_dergoogler_mmrl_platform_file_FileManager_nativeSetOwner(JNIEnv *env,
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
Java_com_dergoogler_mmrl_platform_file_FileManager_nativeSetPermissions(JNIEnv *env,
                                                                        jobject MMRL_UNUSED(thiz),
                                                                        jstring path,
                                                                        jint mode) {
    const char *cpath = env->GetStringUTFChars(path, nullptr);
    bool success = (chmod(cpath, static_cast<mode_t>(mode)) == 0);
    env->ReleaseStringUTFChars(path, cpath);
    return success ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_dergoogler_mmrl_platform_file_FileManager_nativeLoadSharedObject(JNIEnv *env,
                                                                          jobject /* this */,
                                                                          jstring jSoPath) {
    const char *soPath = env->GetStringUTFChars(jSoPath, nullptr);
    if (!soPath) return JNI_FALSE;

    void *handle = dlopen(soPath, RTLD_NOW);
    env->ReleaseStringUTFChars(jSoPath, soPath);

    if (!handle) {
        return JNI_FALSE;  // failed to load
    }

    return JNI_TRUE;  // success
}