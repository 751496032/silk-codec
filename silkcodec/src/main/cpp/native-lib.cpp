#include "index.h"

extern "C" {
#include <common.h>
#include <codec.h>
}

#include <jni.h>
#include <string>
#include <android/log.h>

#define TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" {

// 字符串处理
JNIEXPORT jstring JNICALL
Java_com_hzw_silkcodec_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

// 数组处理
JNIEXPORT void JNICALL
Java_com_hzw_silkcodec_NativeLib_processArray(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray input,
        jbyteArray output) {

    jbyte* inputData = env->GetByteArrayElements(input, nullptr);
    jbyte* outputData = env->GetByteArrayElements(output, nullptr);

    jsize length = env->GetArrayLength(input);

    // 处理数据
    for(int i = 0; i < length; i++) {
        outputData[i] = inputData[i];  // 简单复制
    }

    // 释放数组
    env->ReleaseByteArrayElements(input, inputData, JNI_ABORT);
    env->ReleaseByteArrayElements(output, outputData, 0);
}

JNIEXPORT jint JNICALL
Java_com_hzw_silkcodec_NativeLib_silkEncode(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray input,
        jint sampleRate,
        jobject callback) {

    if (!input || !callback) {
        LOGE("Invalid parameters for silk encode");
        return -1;
    }

    // 获取输入数据
    jsize inputLength = env->GetArrayLength(input);
    jbyte* inputData = env->GetByteArrayElements(input, nullptr);
    std::string pcmData(reinterpret_cast<char*>(inputData), inputLength);

    // 创建回调上下文
    struct AndroidCallback {
        JNIEnv* env;
        jobject callback;
        jmethodID methodId;
    } androidCb;

    androidCb.env = env;
    androidCb.callback = env->NewGlobalRef(callback);
    jclass callbackClass = env->GetObjectClass(callback);
    androidCb.methodId = env->GetMethodID(callbackClass, "onData", "([B)V");

    // 编码回调函数
    auto jniCallback = [](void* userdata, unsigned char* data, int len) {
        auto* cb = static_cast<AndroidCallback*>(userdata);
        JNIEnv* env = cb->env;

        jbyteArray result = env->NewByteArray(len);
        env->SetByteArrayRegion(result, 0, len, reinterpret_cast<jbyte*>(data));
        env->CallVoidMethod(cb->callback, cb->methodId, result);
        env->DeleteLocalRef(result);
    };

    // 执行编码
    int result = silk_encode(pcmData, sampleRate, jniCallback, &androidCb);

    // 清理
    env->ReleaseByteArrayElements(input, inputData, JNI_ABORT);
    env->DeleteGlobalRef(androidCb.callback);

    return result;
}


JNIEXPORT jint JNICALL
Java_com_hzw_silkcodec_NativeLib_silkDecode(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray input,
        jint sampleRate,
        jobject callback) {

    if (!input || !callback) {
    LOGE("Invalid parameters for silk encode");
    return -1;
    }

    // 获取输入数据
    jsize inputLength = env->GetArrayLength(input);
    jbyte* inputData = env->GetByteArrayElements(input, nullptr);
    std::string pcmData(reinterpret_cast<char*>(inputData), inputLength);

    // 创建回调上下文
    struct AndroidCallback {
        JNIEnv* env;
        jobject callback;
        jmethodID methodId;
    } androidCb;

    androidCb.env = env;
    androidCb.callback = env->NewGlobalRef(callback);
    jclass callbackClass = env->GetObjectClass(callback);
    androidCb.methodId = env->GetMethodID(callbackClass, "onData", "([B)V");

    // 编码回调函数
    auto jniCallback = [](void* userdata, unsigned char* data, int len) {
        auto* cb = static_cast<AndroidCallback*>(userdata);
        JNIEnv* env = cb->env;

        jbyteArray result = env->NewByteArray(len);
        env->SetByteArrayRegion(result, 0, len, reinterpret_cast<jbyte*>(data));
        env->CallVoidMethod(cb->callback, cb->methodId, result);
        env->DeleteLocalRef(result);
    };

    // 执行编码
    int result = silk_decode(pcmData, sampleRate, jniCallback, &androidCb);

    // 清理
    env->ReleaseByteArrayElements(input, inputData, JNI_ABORT);
    env->DeleteGlobalRef(androidCb.callback);

    return result;
}

} // extern "C"