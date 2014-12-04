package com.camera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;

import java.util.List;

/**
 * Created by camera360 on 14-12-1.
 */
public class CameraManager {

    private static CameraManager  mInstance = new CameraManager();
    //摄像头数量
    private int mCameraNumber;
    //前置摄像头
    private int mFrontCameraId = -1;
    //后置摄像头
    private int mBackCametaId = -1;
    //当前摄像头
    private int mCurCameraId = -1;
    private Camera mCamera;
    //是否支持闪光灯
    private int isCameraFlash = -1;


    private CameraManager(){

        mCameraNumber = Camera.getNumberOfCameras();
        for(int i=0; i<mCameraNumber; i++){
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if(mBackCametaId == -1 && info.facing == Camera.CameraInfo.CAMERA_FACING_BACK ){
                mBackCametaId = i;
            }else if(mFrontCameraId == -1 && info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                mFrontCameraId = i;
            }
        }

    }

    /**
     * 打开相机
     * @param cameraId
     * @return
     */
    public Camera open(int cameraId){
        if(cameraId >= mCameraNumber){
            return null;
        }
        try{
            mCamera = Camera.open(cameraId);
            mCurCameraId = cameraId;
            return mCamera;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭相机
     */
    public void release(){
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview(){
        if(mCamera != null){
            mCamera.stopPreview();
        }
    }

    public static CameraManager getInstance(){
        return mInstance;
    }

    public int getmFrontCameraId() {
        return mFrontCameraId;
    }

    public int getmBackCametaId() {
        return mBackCametaId;
    }

    public int getmCurCameraId() {
        if(mCurCameraId == -1){
            return getmBackCametaId();
        }
        return mCurCameraId;
    }

    /**
     * 得到预览旋转角度
     * @param activity
     * @return
     */
    public int getRotationDegrees(Activity activity){
        if(mCurCameraId == -1){
            throw new RuntimeException(" 相机未打开 ");
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCurCameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /**
     * 设置相机相机旋转
     */
    public void setCameraDisplayOrientation(Activity activity) {
        mCamera.setDisplayOrientation(getRotationDegrees(activity));
    }

    public Camera getmCamera() {
        return mCamera;
    }

    /**
     * 得到相机最大 聚焦值
     * @return
     */
    public int getCameraMaxZoom(){
        if(mCamera != null){
            return mCamera.getParameters().getMaxZoom();
        }
        return 1;
    }

    /**
     * 得到当前焦距值
     * @return
     */
    public int getCurrentZoom(){
        if(mCamera != null){
            return mCamera.getParameters().getZoom();
        }
        return 0;
    }


    /**
     * 设置相机的焦距值
     * @param proportion ［0..1)
     */
    public void setCameraProportionZoom(float proportion){
        if(mCamera == null){
            return;
        }
        setCameraZoom((int) (getCameraMaxZoom() * proportion));
    }

    /**
     * 设置相机焦距值
     * @param value > 0
     */
    public void setCameraZoom(int value){
        if(mCamera == null){
            return;
        }
        int max = getCameraMaxZoom();
        if(max < value){
            value = max;
        }else if(value < 0){
            value = 0;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setZoom(value);
        mCamera.setParameters(parameters);
    }

    /**
     * 增量修改相机焦距
     * @param add
     */
    public void addCameraZoom(float add){
        if(mCamera == null){
            return;
        }
        setCameraZoom((int) (getCurrentZoom() + add));
    }

    /**
     * 是否支持闪光灯
     * @param context
     * @return
     */
    public boolean isCameraFlash(Context context){

        if(isCameraFlash == -1){
            isCameraFlash = 0;
            FeatureInfo[] features = context.getPackageManager().getSystemAvailableFeatures();
            for(FeatureInfo info : features){
                if(PackageManager.FEATURE_CAMERA_FLASH.equals(info.name)){
                    isCameraFlash = 1;
                }
            }
        }
        return isCameraFlash > 0;
    }

    /**
     * 设置闪光灯
     * @param context
     */
    public void setCameraFlashMode(Context context, String mode){
        if(mCamera == null || getmCurCameraId() == getmFrontCameraId() || !isCameraFlash(context)){
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(mode);
        mCamera.setParameters( parameters );
    }

    /**
     * 得到当前闪光灯状态
     * @param context
     * @return
     */
    public String getCameraFlashMode(Context context){
        if(mCamera == null || !isCameraFlash(context)){
            return null;
        }
        return mCamera.getParameters().getFlashMode();
    }


    /**
     * 得到适合的分辨率
     * @param currentActivity
     * @param sizes
     * @param targetRatio
     * @return
     */
    public static Camera.Size getOptimalPreviewSize(Activity currentActivity,
                                                                     List<Camera.Size> sizes, double targetRatio) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.001;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Because of bugs of overlay and layout, we sometimes will try to
        // layout the viewfinder in the portrait orientation and thus get the
        // wrong size of mSurfaceView. When we change the preview size, the
        // new overlay will be created before the old one closed, which causes
        // an exception. For now, just get the screen size

        Display display = currentActivity.getWindowManager().getDefaultDisplay();
        int targetHeight = Math.min(display.getHeight(), display.getWidth());

        if (targetHeight <= 0) {
            // We don't know the size of SurfaceView, use screen height
            targetHeight = display.getHeight();
        }

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio. This should not happen.
        // Ignore the requirement.
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }


}
