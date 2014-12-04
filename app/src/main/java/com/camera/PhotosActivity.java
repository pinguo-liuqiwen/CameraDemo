package com.camera;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.camera.camera360.camerademo.R;

import com.camera.photos.Photos;


/**
 * Created by camera360 on 14-12-3.
 */
public class PhotosActivity extends Activity{


    private GridView mGvPhotos;
    private Cursor mCursor;
    private Context mContext;
    private Photos mPhotos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mPhotos = new Photos(this);
        mCursor = mPhotos.getPhotos();
        startManagingCursor(mCursor);

        setContentView(R.layout.activity_photo);

        mCursor.moveToFirst();
        mGvPhotos = (GridView) findViewById(R.id.gv_photos);
        mGvPhotos.setAdapter(new MyAdapter(mContext, mCursor));
    }


    private class MyAdapter extends CursorAdapter{

        public MyAdapter(Context context, Cursor c) {
            super(context, c, true);
        }

        @Override
        public int getCount() {

            return super.getCount();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = View.inflate(context, R.layout.item_photo, null);
            ImageView image = (ImageView) view.findViewById(R.id.iv_photo);
            ViewHolder holder = new ViewHolder();
            holder.photoImage = image;
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            Bitmap bitmap = mPhotos.compressionBitmap(path, 70, 70);
            holder.photoImage.setImageBitmap(bitmap);
        }
    }

    private class ViewHolder{
        public ImageView photoImage;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
