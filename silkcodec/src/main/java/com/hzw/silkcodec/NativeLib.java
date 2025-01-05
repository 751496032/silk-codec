package com.hzw.silkcodec;

public class NativeLib {
    static {
        System.loadLibrary("silk-codec");
    }

    // 声明 native 方法
    public native String stringFromJNI();
    public native void processArray(byte[] input, byte[] output);

    public void example() {
        // 调用 JNI 方法
        String result = stringFromJNI();
        
        // 处理数组
        byte[] input = new byte[1024];
        byte[] output = new byte[1024];
        processArray(input, output);
    }

    // 回调接口
    public interface CodecCallback {
        void onData(byte[] data);
    }

    // Native 方法声明
    public native int silkEncode(byte[] input, int sampleRate, CodecCallback callback);
    public native int silkDecode(byte[] input, int sampleRate, CodecCallback callback);
}