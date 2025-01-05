package com.hzw.silkcodec;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author HZWei
 * @date 2025/1/2
 * @desc
 */
public class FileUtils {

    public static Boolean exists(String path){
        if (path == null) return  false;
        var file = new File(path);
        return file.exists();
    }


    public static File copyToCacheFromAssets(String fileName) {
        var file = new File(ContextUtils.getContext().getCacheDir(), fileName);
        try {
            InputStream in = ContextUtils.getContext().getAssets().open(fileName);
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return  file;
    }


    public static File createDir(File parent, String dirName) {
        var file = new File(parent, dirName);
        if (!exists(file.getAbsolutePath())) {
            file.mkdirs();
            return file;
        }
        return file;
    }


    public static boolean deleteFile(String path) {
        if (exists(path)){
            var file = new File(path);
            return file.delete();
        }
        return false;
    }




}
