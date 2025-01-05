package com.hzw.silkdemo;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.hzw.silkcodec.Codec;
import com.hzw.silkcodec.FileUtils;
import com.hzw.silkcodec.NativeLib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    NativeLib nativeLib = new NativeLib();
    private String silkPath;
    private Boolean isConvertMp3 = false;
    private TextView tvResult;
    private TextView tvPlay;
    private TextView tvConvertMp3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        tvResult = findViewById(R.id.tv_result);
        tvPlay = findViewById(R.id.tv_play);
        tvConvertMp3 = findViewById(R.id.tv_convert_mp3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String result = nativeLib.stringFromJNI();
        Log.d("MainActivity", "从 JNI 获取的字符串: " + result);

        addClicks(
                R.id.tv_play,
                R.id.tv_pcmToMp3,
                R.id.tv_pcmToSilk,
                R.id.tv_silkToMp3,
                R.id.tv_silkToPcm,
                R.id.tv_amrToPcm,
                R.id.tv_amrToSilk,
                R.id.tv_convert_mp3);

    }

    @SuppressLint("SetTextI18n")
    private void addClicks(@IdRes int... ids) {
        for (int id : ids) {
            findViewById(id).setOnClickListener(v -> {
                var viewId = v.getId();
                if (viewId == R.id.tv_convert_mp3){
                    isConvertMp3 = !isConvertMp3;
                    tvConvertMp3.setText("是否转成mp3播放: " + (isConvertMp3 ? "是" : "否"));
                }else if (viewId != R.id.tv_play){
                    new Thread(() -> convert(viewId)).start();
                }

            });
        }
    }


    @SuppressLint("SetTextI18n")
    private synchronized void convert(@IdRes int resId) {
        runOnUiThread(() -> {
           tvResult.setText("转换中..." );
        });
        String result = "";
        if (resId == R.id.tv_pcmToSilk) {
            var pcmFile = FileUtils.copyToCacheFromAssets("mshx_p.pcm");
            silkPath = Codec.convertPcmToSilk(pcmFile.getAbsolutePath());
            result = silkPath;
        }
        if (resId == R.id.tv_pcmToMp3) {
            var pcmFile = FileUtils.copyToCacheFromAssets("mshx_p.pcm");
            result = Codec.convertPcmToMp3(pcmFile.getAbsolutePath());
        }
        if (resId == R.id.tv_silkToMp3) {
            if (invalid()) return;
            result = Codec.convertSilkToMp3(silkPath);
        }

        if (resId == R.id.tv_silkToPcm) {
            if (invalid()) return;
            result = Codec.convertSilkToPcm(silkPath);
            if (isConvertMp3){
               playMp3(Codec.convertPcmToMp3(result));
            }else {
                stopPlay();
            }
        }

        if (resId == R.id.tv_amrToPcm) {
            var armFile = FileUtils.copyToCacheFromAssets("mshx_a.amr");
            result = Codec.convertAmrToPcm(armFile.getAbsolutePath());
            if (isConvertMp3){
                playMp3(Codec.convertPcmToMp3(result));
            }else {
                stopPlay();
            }
        }

        if (resId == R.id.tv_amrToSilk){
            var armFile = FileUtils.copyToCacheFromAssets("mshx_a.amr");
            result = Codec.convertAmrToSilk(armFile.getAbsolutePath());
            if (isConvertMp3){
                playMp3(Codec.convertSilkToMp3(result));
            }else {
                stopPlay();
            }
        }
        String tR = result;
        runOnUiThread(() -> {
            tvResult.setText("转换结果：" + tR);
            if (tR != null && tR.endsWith("mp3")) {
                playMp3(tR);
            }
        });
    }


    private boolean invalid() {
        if (!FileUtils.exists(silkPath)) {
            runOnUiThread(() -> {
                Toast.makeText(this, "先执行pcmToSilk", Toast.LENGTH_SHORT).show();
            });
            return true;
        }
        return false;
    }

    private void stopPlay(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }









    @SuppressLint("SetTextI18n")
    private void playMp3(String mp3Path) {
        try {

            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(mp3Path);
                mediaPlayer.prepareAsync();
                return;
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mp3Path);
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                tvPlay.setText("播放中...");
                Log.i(TAG, "开始播放MP3");
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                tvPlay.setText("播放完成");
                Log.i(TAG, "播放完成");
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "播放错误: " + what + ", " + extra);
                tvPlay.setText("播放错误: " + what + ", " + extra);
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "播放失败", e);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }
}


