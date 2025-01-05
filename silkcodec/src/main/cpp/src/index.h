#ifndef INDEX_H
#define INDEX_H

#include <vector>
#include <string>


// #include <emscripten/val.h>

// EMSCRIPTEN_DECLARE_VAL_TYPE(callback_type);

// 定义回调函数类型
typedef void (*codec_callback_t)(void *userdata, unsigned char *data, int len);


typedef struct codec_ctx
{
    std::vector<unsigned char> output;
} codec_ctx_t;

void codec_callback(void *userdata, unsigned char *data, int len);

// 修改函数签名，使用标准的回调函数
int silk_encode(const std::string &pcm_data, int sample_rate, codec_callback_t callback, void* userdata);

int silk_decode(const std::string &silk_data, int sample_rate, codec_callback_t callback, void* userdata);

#endif
