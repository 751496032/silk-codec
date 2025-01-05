package com.hzw.silkcodec;


import static com.hzw.silkcodec.FileUtils.exists;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author HZWei
 * @date 2025/1/2
 * @desc silk、pcm、mp3、amr间相互转换
 */
public class Codec {
    private static final String TAG = "Codec";
    private static final  NativeLib nativeLib = new NativeLib();
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final int BIT_DEPTH = 16;
    private static final String defaultDir = "silks";


    /**
     * 解码：silk文件转成pcm文件
     * @param silkPath silk文件路径
     * @param pcmPath  pcm文件路径
     * @return true 转换成功
     */
    @WorkerThread
    public static boolean convertSilkToPcm(String silkPath, String pcmPath) {
        Log.i(TAG, "start convertSilkToPcm... ");
        try {
            if (!exists(silkPath)){
                Log.e(TAG, "convertSilkToPcm fail: " + silkPath + " not exist");
                return false;
            }
            // 读取SILK文件
            var fis = new FileInputStream(silkPath);
            var silkBytes = new byte[fis.available()];
            var r = fis.read(silkBytes);
            fis.close();

            Log.i(TAG, "silk bytes: " + silkBytes.length);

            FileOutputStream fos = new FileOutputStream(pcmPath);
            // 执行解码
            int ret = nativeLib.silkDecode(silkBytes, SAMPLE_RATE, data -> {
                try {
                    fos.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fos.close();
            if (ret < 0) {
                Log.e(TAG, "convertSilkToPcm fail: " + ret);
                return false;
            }
            Log.i(TAG, "convertSilkToPcm success bytes: " + ret);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "convertSilkToPcm err: " + e.getMessage());
            return false;
        }

    }


    /**
     * 编码：pcm文件转成silk文件
     * @param pcmPath pcm文件路径
     * @param silkPath silk文件路径
     * @return  true 转换成功
     */
    public static boolean convertPcmToSilk(String pcmPath, String silkPath) {
        Log.i(TAG, "start convertPcmToSilk...");
        try {
            if (!exists(pcmPath)){
                Log.e(TAG, "convertPcmToSilk fail: " + pcmPath + " not exist");
                return false;
            }
            // 读取PCM文件
            var fis = new FileInputStream(pcmPath);
            byte[] pcmBytes = new byte[fis.available()];
            var r = fis.read(pcmBytes);
            fis.close();

            Log.i(TAG, "pcm bytes: " + pcmBytes.length);

            FileOutputStream fos = new FileOutputStream(silkPath);
            int ret = nativeLib.silkEncode(pcmBytes, SAMPLE_RATE, data -> {
                try {
                    fos.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            fos.close();
            if (ret < 0) {
                Log.e(TAG, "convertPcmToSilk fail: " + ret);
                return false;
            }
            Log.i(TAG, "convertPcmToSilk success bytes: " + ret);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "convertPcmToSilk err: " + e.getMessage());
            return false;
        }
    }

    /**
     * silk转成pcm文件
     * @param silkPath silk文件路径
     * @return String 转换的pcm文件路径
     */
    @WorkerThread
    @Nullable
    public static String convertSilkToPcm(String silkPath) {
        var name = System.currentTimeMillis() + ".pcm";
        var dest = getDestFile(name);
        if (convertSilkToPcm(silkPath, dest.getAbsolutePath())) {
            return dest.getAbsolutePath();
        }
        return null;
    }


    /**
     * pcm转成silk文件
     * @param pcmPath pcm文件路径
     * @return String 转换后的silk文件路径
     */
    @WorkerThread
    @Nullable
    public static String convertPcmToSilk(String pcmPath) {
        var name = System.currentTimeMillis() + ".silk";
        var dest = getDestFile(name);
        if (convertPcmToSilk(pcmPath, dest.getAbsolutePath())) {
            return dest.getAbsolutePath();
        }
        return null;
    }

    /**
     * silk转成mp3文件
     * silk -> pcm -> mp3
     * @param silkPath silk文件路径
     * @param mp3Path mp3文件路径
     * @return true 转换成功
     */
    @WorkerThread
    public static boolean convertSilkToMp3(String silkPath, String mp3Path) {
        // silk -> pcm -> mp3
        var pcmPath = convertSilkToPcm(silkPath);
        var ret = convertPcmToMp3(pcmPath, mp3Path);
        FileUtils.deleteFile(pcmPath);
        return ret;
    }


    /**
     * silk转成mp3文件
     * @param silkPath silk文件路径
     * @return true 转换成功
     */
    @WorkerThread
    @Nullable
    public static String convertSilkToMp3(String silkPath){
        var name = System.currentTimeMillis() + ".mp3";
        var dest = getDestFile(name);
        if (convertSilkToMp3(silkPath, dest.getAbsolutePath())) {
            return dest.getAbsolutePath();
        }
        return null;
    }


    /**
     * pcm转成mp3文件
     * @param pcmPath pcm文件路径
     * @param mp3Path mp3文件路径
     * @return true 转换成功
     */
    public static boolean convertPcmToMp3(String pcmPath, String mp3Path) {
        Log.i(TAG, "start convertPcmToMp3 ...."  );
        if (!exists(pcmPath)){
            Log.e(TAG, "convertPcmToMp3 fail: " + pcmPath + " not exist");
            return false;
        }
        try {
            // 使用 FFmpeg 命令进行转换
            String[] cmd = {
                    "-f", "s16le",              // PCM 16位小端格式
                    "-ar", String.valueOf(SAMPLE_RATE),  // 采样率
                    "-ac", String.valueOf(CHANNELS),    // 声道数
                    "-i", pcmPath,              // 输入文件
                    "-b:a", "128k",      // 比特率 (如 128k)
                    "-y",                       // 覆盖已存在的文件
                    mp3Path                     // 输出文件
            };
            // 执行FFmpeg命令
            var ret = FFmpeg.execute(cmd);
            Log.i(TAG, "convertPcmToMp3 ret: " + ret);
            return ret == 0;
        } catch (Exception e) {
            Log.e(TAG, "convertPcmToMp3 err: " + e.getMessage());
            return false;
        }
    }


    @Nullable
    @WorkerThread
    public static String convertPcmToMp3(String pcmPath) {
        var mp3Path = getDestFile(System.currentTimeMillis() + ".mp3").getAbsolutePath();
        if (convertPcmToMp3(pcmPath, mp3Path)) {
            return mp3Path;
        }
        return null;
    }

    /**
     * amr转成silk文件
     * @param amrPath amr文件路径
     * @param silkPath silk文件路径
     * @return true 转换成功
     */
    @WorkerThread
    public static boolean convertAmrToSilk(String amrPath, String silkPath) {
        // arm -> pcm -> silk
        var pcmPath = convertAmrToPcm(amrPath);
        var r = convertPcmToSilk(pcmPath, silkPath);
        FileUtils.deleteFile(pcmPath);
        return r ;

    }


    /**
     * amr转成silk文件
     *
     * @param amrPath amr文件路径
     * @return 转换成功后silk文件路径
     */
    @WorkerThread
    @Nullable
    public static String convertAmrToSilk(String amrPath) {
        // arm -> pcm -> silk
        var destFile = getDestFile(System.currentTimeMillis() + ".silk");
        var pcmPath = convertAmrToPcm(amrPath);
        var r = convertPcmToSilk(pcmPath, destFile.getAbsolutePath());
        FileUtils.deleteFile(pcmPath);
        if (r) {
            return destFile.getAbsolutePath();
        }
        return null;
    }


    /**
     * amr转成pcm文件
     * @param amrPath arm文件路径
     * @param pcmPath  pcm文件路径
     * @return true 转换成功
     */
    @WorkerThread
    public static boolean convertAmrToPcm(String amrPath, String pcmPath) {
        Log.i(TAG, "start convertAmrToPcm....");
        if (!exists(amrPath)) {
            Log.e(TAG, "convertAmrToPcm fail: " + amrPath + " not exist");
            return false;
        }
        try {
            // FFmpeg命令
            String[] cmd = {
                    "-i", amrPath,              // 输入文件
                    "-f", "s16le",              // PCM 16位小端格式
                    "-acodec", "pcm_s16le",     // PCM 16位编码
                    "-ar", String.valueOf(SAMPLE_RATE),  // 采样率
                    "-ac", String.valueOf(CHANNELS),    // 声道数
                    "-y",                       // 覆盖已存在的文件
                    pcmPath                     // 输出文件
            };
            int ret = FFmpeg.execute(cmd);
            Log.i(TAG, "convertAmrToPcm ret: " + ret);
            return ret == 0;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "convertAmrToPcm err: " + e.getMessage());
            return false;
        }
    }


    /**
     * amr转成pcm文件
     * @param amrPath amr文件路径
     * @return String 转换后的pcm文件路径
     */
    @Nullable
    @WorkerThread
    public static String convertAmrToPcm(String amrPath) {
        var name = System.currentTimeMillis() + ".pcm";
        var dest = getDestFile(name);
        if (convertAmrToPcm(amrPath, dest.getAbsolutePath())) {
            return dest.getAbsolutePath();
        }
        return null;
    }



    private static File getDestFile(String fileName) {
        return new File(FileUtils.createDir(ContextUtils.getContext().getFilesDir(), defaultDir), fileName);
    }









}
