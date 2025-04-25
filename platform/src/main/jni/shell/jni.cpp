#include <jni.h>
#include "ShellWrapper.h"
#include <map>

extern "C" JNIEXPORT jlong JNICALL
Java_com_dergoogler_mmrl_platform_util_Shell_nativeCreateShell(JNIEnv *, jobject) {
    return reinterpret_cast<jlong>(new ShellWrapper());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_dergoogler_mmrl_platform_util_Shell_nativeIsAlive(JNIEnv *, jobject, jlong ptr) {
    auto *shell = reinterpret_cast<ShellWrapper *>(ptr);
    return shell->isAlive();
}

extern "C" JNIEXPORT void JNICALL
Java_com_dergoogler_mmrl_platform_util_Shell_nativeClose(JNIEnv *, jobject, jlong ptr) {
    auto *shell = reinterpret_cast<ShellWrapper *>(ptr);
    shell->close();
    delete shell;
}

extern "C" JNIEXPORT void JNICALL
Java_com_dergoogler_mmrl_platform_util_Shell_nativeExec(JNIEnv *env, jobject, jlong ptr,
                                                        jobjectArray jCommands, jobject jModule,
                                                        jobject jCallback, jobject jEnvMap) {
    auto *shell = reinterpret_cast<ShellWrapper *>(ptr);

    // Global refs to safely use in callback lambdas (may run on different thread)
    jobject globalCallback = env->NewGlobalRef(jCallback);
    jobject globalModule = env->NewGlobalRef(jModule);
    JavaVM *jvm;
    env->GetJavaVM(&jvm);

    std::vector<std::string> commands;
    jsize len = env->GetArrayLength(jCommands);
    for (jsize i = 0; i < len; ++i) {
        auto jCmd = (jstring) env->GetObjectArrayElement(jCommands, i);
        const char *raw = env->GetStringUTFChars(jCmd, nullptr);
        commands.emplace_back(raw);
        env->ReleaseStringUTFChars(jCmd, raw);
        env->DeleteLocalRef(jCmd);
    }

    std::map<std::string, std::string> envMap;
    if (jEnvMap) {
        jclass mapCls = env->GetObjectClass(jEnvMap);
        jmethodID entrySetMid = env->GetMethodID(mapCls, "entrySet", "()Ljava/util/Set;");
        jobject entrySet = env->CallObjectMethod(jEnvMap, entrySetMid);

        jclass setCls = env->GetObjectClass(entrySet);
        jmethodID iteratorMid = env->GetMethodID(setCls, "iterator", "()Ljava/util/Iterator;");
        jobject iterator = env->CallObjectMethod(entrySet, iteratorMid);

        jclass iteratorCls = env->GetObjectClass(iterator);
        jmethodID hasNextMid = env->GetMethodID(iteratorCls, "hasNext", "()Z");
        jmethodID nextMid = env->GetMethodID(iteratorCls, "next", "()Ljava/lang/Object;");

        jclass entryCls = nullptr;
        jmethodID getKeyMid = nullptr;
        jmethodID getValueMid = nullptr;

        while (env->CallBooleanMethod(iterator, hasNextMid)) {
            jobject entry = env->CallObjectMethod(iterator, nextMid);
            if (!entryCls) {
                entryCls = env->GetObjectClass(entry);
                getKeyMid = env->GetMethodID(entryCls, "getKey", "()Ljava/lang/Object;");
                getValueMid = env->GetMethodID(entryCls, "getValue", "()Ljava/lang/Object;");
            }

            auto key = (jstring) env->CallObjectMethod(entry, getKeyMid);
            auto val = (jstring) env->CallObjectMethod(entry, getValueMid);

            const char* k = env->GetStringUTFChars(key, nullptr);
            const char* v = env->GetStringUTFChars(val, nullptr);
            envMap[k] = v;

            env->ReleaseStringUTFChars(key, k);
            env->ReleaseStringUTFChars(val, v);
            env->DeleteLocalRef(key);
            env->DeleteLocalRef(val);
            env->DeleteLocalRef(entry);
        }

        env->DeleteLocalRef(entrySet);
        env->DeleteLocalRef(iterator);
    }

    // Callbacks to Java from native
    int result = shell->exec(
            commands,
            [jvm, globalCallback](const std::string &line) {
                JNIEnv *env;
                jvm->AttachCurrentThread(&env, nullptr);

                jclass cbCls = env->GetObjectClass(globalCallback);
                jmethodID mid = env->GetMethodID(cbCls, "onStdout", "(Ljava/lang/String;)V");
                jstring jLine = env->NewStringUTF(line.c_str());
                env->CallVoidMethod(globalCallback, mid, jLine);
                env->DeleteLocalRef(jLine);
            },
            [jvm, globalCallback](const std::string &line) {
                JNIEnv *env;
                jvm->AttachCurrentThread(&env, nullptr);

                jclass cbCls = env->GetObjectClass(globalCallback);
                jmethodID mid = env->GetMethodID(cbCls, "onStderr", "(Ljava/lang/String;)V");
                jstring jLine = env->NewStringUTF(line.c_str());
                env->CallVoidMethod(globalCallback, mid, jLine);
                env->DeleteLocalRef(jLine);
            },
            envMap
    );

    // Final success/failure callback
    JNIEnv *finalEnv;
    jvm->AttachCurrentThread(&finalEnv, nullptr);

    jclass cbCls = finalEnv->GetObjectClass(globalCallback);
    jmethodID midFinal = finalEnv->GetMethodID(
            cbCls,
            (result == 0 ? "onSuccess" : "onFailure"),
            "(Lcom/dergoogler/mmrl/platform/content/LocalModule;)V"
    );
    finalEnv->CallVoidMethod(globalCallback, midFinal, globalModule);

    // Clean up
    finalEnv->DeleteGlobalRef(globalCallback);
    finalEnv->DeleteGlobalRef(globalModule);
}
