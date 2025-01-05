package com.hzw.silkcodec;

import android.annotation.SuppressLint;
import android.content.Context;

import java.lang.reflect.Method;

/**
 * @author HZWei
 * @date 2024/8/3
 * @desc
 */
public class ContextUtils {
    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static Context getContext() {
        if (sContext == null) {
            sContext = getContextByReflect();
        }
        return sContext;
    }

    @SuppressLint("PrivateApi")
    private static Context getContextByReflect() {
        try {
            // 首先尝试ActivityThread
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = activityThread.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object thread = currentActivityThread.invoke(null);

            Method getApplication = activityThread.getDeclaredMethod("getApplication");
            getApplication.setAccessible(true);
            Context context = (Context) getApplication.invoke(thread);
            if (context != null) {
                return context;
            }

            // 如果失败，尝试AppGlobals
             Class<?> appGlobals = Class.forName("android.app.AppGlobals");
            Method getInitialApplication = appGlobals.getDeclaredMethod("getInitialApplication");
            getInitialApplication.setAccessible(true);
            return (Context) getInitialApplication.invoke(null);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}