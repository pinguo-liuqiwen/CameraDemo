package com.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import com.camera.camera360.camerademo.R;
import com.camera.config.Util;
import com.camera.photos.Photos;

import java.io.IOException;
import java.util.List;

import com.camera.ui.SeekBar;

public class CameraActivity extends Activity implements View.OnTouchListener, View.OnClickListener, SurfaceHolder.Callback, View.OnLongClickListener {



    private CameraManager mCameraManager = CameraManager.getInstance();

    private SurfaceView mSfvCamera;
    private Button mBtnTrigger;
    private ImageButton mIBtnTakePicture;
    private Button mBtnRotating;
    private ImageView mIvPhoto;
    private SurfaceHolder mHolder;
    private Photos mPhotos;

    //当前打开摄像头id
    private int mCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;

    //屏幕宽高
    private int mWidth;
    private int mHeight;

    //聚焦条
    private SeekBar mCameraZoomSeekBar;

    private static final int CAMERA_SEEK_BAR_HIDE = 0; //隐藏聚焦条
    //手指触摸状态
    private int mTouchStatus;
    private final int TOUCH_STATUS_DOUBLE = 2;
    private final int TOUCH_STATUS_SINGLE = 1;

    //是否正在拍照
    private boolean isTakePhoto = false;
    private static final int TAKE_PHOTO_STATE = 3;
    private static final int TAKE_PHOTO_SUCCESS = 4;

    private boolean isPreview = true;

    private Handler mHandler = new MyHandler();

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case CAMERA_SEEK_BAR_HIDE :
                    hideSeekBar();
                    break;
                case TAKE_PHOTO_STATE:
                    isTakePhoto = false;
                    break;
                case TAKE_PHOTO_SUCCESS :
                    String uri = mPhotos.save((byte[]) msg.obj);
                    Bitmap bm = mPhotos.compressionBitmap(uri, 90, 90);
                    mIvPhoto.setImageBitmap(bm);
                    Toast.makeText(getApplicationContext(), uri, Toast.LENGTH_LONG).show();
                    isTakePhoto = false;
                    break;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        Util.initialize(this);

        mCameraZoomSeekBar = (SeekBar) findViewById(R.id.pgsb_focal_length);
        mCameraZoomSeekBar.setOrientation(false);

        Display display = getWindowManager().getDefaultDisplay();

        mWidth = display.getWidth();
        mHeight = display.getHeight();

        mBtnTrigger = (Button) findViewById(R.id.btn_trigger);
        mIBtnTakePicture = (ImageButton) findViewById(R.id.ibtn_take_picture);
        mBtnRotating = (Button) findViewById(R.id.btn_rotating);
        mIvPhoto = (ImageView) findViewById(R.id.iv_photo);

        mBtnTrigger.setOnClickListener(this);
        mIBtnTakePicture.setOnClickListener(this);
        mIBtnTakePicture.setOnLongClickListener(this);
        mBtnRotating.setOnClickListener(this);
        mIvPhoto.setOnClickListener(this);

        mSfvCamera = (SurfaceView) findViewById(R.id.sfv_camera);
        mSfvCamera.setOnTouchListener(this);
        mHolder = mSfvCamera.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mPhotos = new Photos(this);
        initZoomSeekBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraManager.open(mCameraManager.getmCurCameraId());
        mCameraManager.setCameraDisplayOrientation(this);
        if(!isPreview){
            startPreview();
            isPreview = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraManager.stopPreview();
        mCameraManager.release();
        isPreview = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch(event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                mTouchStatus = TOUCH_STATUS_SINGLE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mTouchStatus = TOUCH_STATUS_DOUBLE;
                break;
        }

        switch (mTouchStatus){
            case TOUCH_STATUS_DOUBLE :
                gesturesToZoom(v, event);
                break;
        }
        return true;
    }

    @Override
    public boolean onLongClick(View v) {
        switch(v.getId()){
            case R.id.ibtn_take_picture :
                //聚焦
                mCameraManager.getmCamera().autoFocus(null);
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ibtn_take_picture ://拍照
                if(!isTakePhoto){
                    isTakePhoto = true;
                    takePicture();
                }
            break;

            case R.id.btn_trigger : //前置摄像头切换
                mCameraManager.stopPreview();
                mCameraManager.release();
                mCameraId = mCameraManager.getmFrontCameraId() == mCameraId ?
                        mCameraManager.getmBackCametaId() : mCameraManager.getmFrontCameraId();
                mCameraManager.open(mCameraId);
                mCameraManager.setCameraDisplayOrientation(this);
                startPreview();
            break;

            case R.id.iv_photo :
                startActivity(new Intent(getApplicationContext(), PhotosActivity.class));
            break;

            case R.id.btn_rotating ://旋转
                //cameraRotating();
            break;
        }
    }




    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}



    /**
     * 开始预览1
     */
    private void startPreview(){

        android.hardware.Camera mCamera = mCameraManager.getmCamera();
        android.hardware.Camera.Parameters parameters =  mCamera.getParameters();
        android.hardware.Camera.Size size = parameters.getPictureSize();
        List<android.hardware.Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        android.hardware.Camera.Size targetSize = mCameraManager.getOptimalPreviewSize(this, sizes, (double) size.width / size.height);
        parameters.setPreviewSize(targetSize.width, targetSize.height);
        parameters.setRotation(mCameraManager.getmCurCameraId() == mCameraManager.getmBackCametaId() ? 90 : 270);
        try {
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            mCameraManager.release();
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     */
    private void takePicture() {

        final android.hardware.Camera mCamera = mCameraManager.getmCamera();
        mCamera.autoFocus(new android.hardware.Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, android.hardware.Camera camera) {
                //自动聚焦失败
                if(!success){
                    //isTakePhoto = false;
                    //return;
                }
                mCamera.takePicture(null, null, new android.hardware.Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(final byte[] data, android.hardware.Camera camera) {
                        //保存照片
                        Message message = Message.obtain();
                        message.what = TAKE_PHOTO_SUCCESS;
                        message.obj = data;
                        mHandler.sendMessage(message);
                        //设置图片显示
                        mCamera.startPreview();
                    }
                });
            }
        });

    }



    private float mDistance = 0;
    private int mMaxSliDistance = 800;
    private int mCameraZoomStep = -1;

    /**
     * 手势缩放
     * @param v
     * @param event
     */
    private void gesturesToZoom(View v, MotionEvent event) {
        try {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    showSeekBar();
                    mDistance = Util.getSqrt(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
                    break;
                case MotionEvent.ACTION_MOVE:
                        float distance = Util.getSqrt(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
                        if (mCameraZoomStep == -1) {
                            mMaxSliDistance = (int) (mHeight * 0.6);
                            mCameraZoomStep = mMaxSliDistance / CameraManager.getInstance().getCameraMaxZoom();
                        }
                        if ((distance > mDistance + mCameraZoomStep) || (distance < mDistance - mCameraZoomStep)) {
                            CameraManager manager = CameraManager.getInstance();
                            manager.addCameraZoom((distance - mDistance) / mCameraZoomStep);
                            float rate = ((float) manager.getCurrentZoom()) / manager.getCameraMaxZoom();
                            mCameraZoomSeekBar.setCurrentSeekValue(rate);
                            mDistance = distance;
                        }
                    break;
                case MotionEvent.ACTION_UP:
                    timHideSeekBar();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 初始化焦距UI
     */
    private void initZoomSeekBar() {
        mCameraZoomSeekBar.setThumbRadius(12);
        mCameraZoomSeekBar.setOrientation(false);
        hideSeekBar();
        mCameraZoomSeekBar.setOnSeekChangedListener(new SeekBar.OnSeekChangedListener() {
            @Override
            public void onSeekValueChanged(float rate) {
                CameraManager.getInstance().setCameraProportionZoom(rate);
            }
        });
        mCameraZoomSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isShowSeekBar) {
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        showSeekBar();
                        break;
                    case MotionEvent.ACTION_UP:
                        timHideSeekBar();
                        break;
                }
                return false;
            }
        });
    }

    private boolean isShowSeekBar = true;
    private boolean isShowSeekBarTimer = false;

    /**
     * 隐藏聚焦条
     */
    private void hideSeekBar() {
        if (isShowSeekBar) {
            cameraSeekBarAnimation(1, 0, false);
            isShowSeekBar = false;
        }
    }

    /**
     * 显示聚焦条
     */
    private void showSeekBar() {
        if (!isShowSeekBar) {
            cameraSeekBarAnimation(0, 1, true);
            isShowSeekBar = true;
        }
        if (isShowSeekBarTimer) {
            mHandler.removeMessages(CAMERA_SEEK_BAR_HIDE);
            isShowSeekBarTimer = false;
        }

    }

    private void timHideSeekBar() {
        mHandler.removeMessages(CAMERA_SEEK_BAR_HIDE);
        mHandler.sendEmptyMessageDelayed(CAMERA_SEEK_BAR_HIDE, 3000);
        isShowSeekBarTimer = true;
    }

    /**
     * 聚焦条动画
     */
    private void cameraSeekBarAnimation(int start, int end, final boolean display) {
        if (display) {
            mCameraZoomSeekBar.setVisibility(View.VISIBLE);
        }
        mCameraZoomSeekBar.clearAnimation();
        Animation animation = new AlphaAnimation(start, end);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        mCameraZoomSeekBar.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!display) {
                    mCameraZoomSeekBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * 音量键拍照
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:

                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
