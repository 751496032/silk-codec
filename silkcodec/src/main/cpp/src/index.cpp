#include "index.h"
#include <android/log.h>
#include <vector>
#include <string>
#include <cstring>

extern "C" {
#include <common.h>
#include <codec.h>
}

#define TAG "SilkCodec"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

void codec_callback(void *userdata, unsigned char *p, int len)
{
    auto ctx = static_cast<codec_ctx_t *>(userdata);
    ctx->output.insert(ctx->output.end(), &p[0], &p[len]);
}

// 修改后的编码函数
int silk_encode(const std::string &pcm_data, int sample_rate, codec_callback_t callback, void* userdata)
{
    std::vector<unsigned char> output;
    codec_ctx_t ctx = {output};

    auto input = reinterpret_cast<const unsigned char *>(pcm_data.data());
    int ret = silkEncode(input, pcm_data.size(), sample_rate, callback, userdata);

    if (ret == 0) {
        LOGE("Silk encode failed with error: %d", ret);
    } else {
        LOGI("Silk encode success bytes: %d", ret);
    }

    return ret;
}

// 修改后的解码函数
int silk_decode(const std::string &silk_data, int sample_rate, codec_callback_t callback, void* userdata)
{
    std::vector<unsigned char> output;
    codec_ctx_t ctx = {output};

    auto input = reinterpret_cast<const unsigned char *>(silk_data.data());
    int ret = silkDecode(input, silk_data.size(), sample_rate, callback, userdata);

    if (ret == 0) {
        LOGE("Silk decode failed with error: %d", ret);
    } else {
        LOGI("Silk decode success bytes: %d", ret);
    }

    return ret;
}