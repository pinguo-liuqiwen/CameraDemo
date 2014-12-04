package com.camera.config;

import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.WindowManager;

import java.io.File;


public class Util {

    public static int SCREEN_HEIGHT = 480;
    public static int SCREEN_WIDTH = 800;
    private static float sPixelDensity = 1;
    private static String mUri = Environment.getExternalStorageDirectory() + File.separator + "testCamera" + File.separator;

    private Util() {}

    public static void initialize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        sPixelDensity = metrics.density;
        SCREEN_HEIGHT = metrics.heightPixels;
        SCREEN_WIDTH = metrics.widthPixels;
    }

    public static float dpToPixel(float dp) {
        return sPixelDensity * dp;
    }

    public static int dpToPixel(int dp) {
        return Math.round(dpToPixel((float) dp));
    }

    public static int pixelTodp(int pixel) {
        return Math.round(pixel / sPixelDensity);
    }

    /**
     * 计算两点之间的距离
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @return
     */
    public static float getSqrt(float x1, float x2, float y1, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * 是否有sd卡
     * @return
     */
    public static boolean isSDCard(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 在sd卡上创建文件
     * @param path
     */
    public static String createFile(String path){
        path = mUri + path;
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        return path;
    }



}
