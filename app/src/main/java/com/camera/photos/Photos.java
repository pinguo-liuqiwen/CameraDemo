package com.camera.photos;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;


import com.camera.config.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

/**
 * Created by camera360 on 14-11-19.
 */
public class Photos {

    private String mUri = "photo"; //File.separator + "images" + File.separator;
    private Context mContext;
    private final String PHOTO_FLAG = "camera_360";

    public Photos(Context context){
        this.mContext = context;
        File file = new File(mUri);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    /**
     *  保存照片
     */
    public String save(byte[] data){
        if(!Util.isSDCard()){
            return null;
        }
        String fileName = DateFormat.format("yyyyMMddHHmmss", new Date()).toString();
        //String path = Util.createFile(mUri) + "testcamera" + fileName + ".jpg";

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        //前置摄像头镜像照片
        Matrix m = new Matrix();
        m.postScale(-1, 1);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        //将照片插入系统数据库
        ContentResolver cr = mContext.getContentResolver();
        String url  = MediaStore.Images.Media.insertImage(cr, bitmap, fileName, PHOTO_FLAG);
        //Environment.getExternalStorageDirectory()
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + url)));
//        try{
//            FileOutputStream out = new FileOutputStream(path);
//            out.write(data, 0, data.length);
//            out.flush();
//            out.close();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        //Matrix m = new Matrix();
        //m.setRotate(90,(float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        //bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
        //bm.compress(Bitmap.CompressFormat.JPEG, 100, mContext.openFileOutput(uri, Context.MODE_PRIVATE));

        return getSystemPhotoPath(url);
    }

    /**
     * 根据路径得到图片
     * @param uri
     * @return
     */
    public Bitmap getBitmapByUri(String uri){
        try {
            Bitmap bm = BitmapFactory.decodeStream(mContext.openFileInput(uri));
            return bm;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 得到压缩图片
     * @param url
     * @param width
     * @param height
     * @return
     */
    public Bitmap compressionBitmap(String url, int width, int height){


        width = Util.dpToPixel(width);
        height = Util.dpToPixel(height);
        //读取图片头信息
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(url, options);

        options.inJustDecodeBounds = false;
        //计算图片压缩率
        int w = options.outWidth;
        int h = options.outHeight;
        int size = 1;
        if(w > h && w > width){
            size = w/width;
        }else if(h > w && h > height){
            size = h/height;
        }

        options.inSampleSize = size;//设置采样率
        options.inInputShareable = true;
        bitmap = BitmapFactory.decodeFile(url, options);

        return bitmap;
    }

    /**
     * 获得系统相册路径
     * @param url
     */
    public String getSystemPhotoPath(String url){

            //获得图片的uri
            //Uri originalUri = Uri.parse("");
            //显得到bitmap图片
            //bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);

            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = mContext.getContentResolver().query(Uri.parse(url), proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
        return path;
    }

    /**
     * 得到照片数据
     * @return
     */
    public Cursor getPhotos(){
        return mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media.DESCRIPTION + "=?",
                new String[]{PHOTO_FLAG}, MediaStore.Images.Media.DATE_ADDED + " desc");

//        return mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
//                 null, null);
    }

}
