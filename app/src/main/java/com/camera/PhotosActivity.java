package com.camera;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.camera.camera360.camerademo.R;

import com.camera.config.Util;
import com.camera.photos.Photos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Created by camera360 on 14-12-3.
 */
public class PhotosActivity extends Activity implements View.OnClickListener, View.OnLongClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {


    private GridView mGvPhotos;
    private Cursor mCursor;
    private Context mContext;
    private Photos mPhotos;

    private RelativeLayout mRlTopBar;
    private LinearLayout mLlBottomBar;

    private Button mBtnSelectAll;
    private Button mBtnDelete;
    private Button mBtnCancel;

    private boolean isEdit = false;

    private MyAdapter mAdapter;
    private Map<String,Boolean> mSelectedIds = new HashMap<String, Boolean>();
    private Map<String,Boolean> mNoSelectedIds = new HashMap<String, Boolean>();
    private boolean isAllSelected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mPhotos = new Photos(this);
        mCursor = mPhotos.getPhotos();
        startManagingCursor(mCursor);

        setContentView(R.layout.activity_photo);

        mRlTopBar = (RelativeLayout) findViewById(R.id.rl_top_bar);
        mGvPhotos = (GridView) findViewById(R.id.gv_photos);
        mLlBottomBar = (LinearLayout) findViewById(R.id.ll_bottom_bar);

        mBtnSelectAll = (Button) findViewById(R.id.btn_select_all);
        mBtnDelete = (Button) findViewById(R.id.btn_delete);
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);

        mBtnSelectAll.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        mGvPhotos.setOnLongClickListener(this);
        mGvPhotos.setOnItemLongClickListener(this);
        mGvPhotos.setOnItemClickListener(this);

        mCursor.moveToFirst();
        mGvPhotos = (GridView) findViewById(R.id.gv_photos);
        mAdapter = new MyAdapter(mContext, mCursor);
        mGvPhotos.setAdapter(mAdapter);

        //Util.LogCursorInfo(mCursor);
//        mCursor.moveToFirst();
//        while(mCursor.moveToNext()){
//            String id = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
//            Cursor cursor = getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, null,
//
//                    MediaStore.Images.Thumbnails.IMAGE_ID + "=?", new String[]{id}, null);
//            cursor.moveToNext();
//            for(int i=0, l=cursor.getColumnCount(); i<l; i++){
//                System.out.print("     " + cursor.getColumnName(i) + " : " + cursor.getString(i));
//            }
//            //Util.LogCursorInfo(cursor);
//            cursor.close();
//        }

//        Cursor cursor = getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, null,null, null, null);
//        Util.LogCursorInfo(cursor);
//        cursor.close();
        //MediaStore.Images.Thumbnails.get
        //MediaStore.Images.Media.getBitmap()

    }

    @Override
    public void onClick(View v) {
        Object[] ids;
        switch(v.getId()){
            case R.id.btn_select_all :  // 全选
                if(mCursor.getCount() == 0){
                    return;
                }
                if(!isAllSelected){
                    isAllSelected = true;
                    mBtnSelectAll.setTextColor(Color.BLUE);
                }else{
                    isAllSelected = false;
                    mBtnSelectAll.setTextColor(Color.BLACK);
                }
                mNoSelectedIds.clear();
                mSelectedIds.clear();
                break;
            case R.id.btn_delete : // 删除
                if(mSelectedIds.size() > 0){//删除选中
                    mPhotos.deleteByIds(mSelectedIds.keySet().toArray());
                    mSelectedIds.clear();
                }else if(mNoSelectedIds.size() > 0){//删除反选
                    mPhotos.deleteByNoIds(mNoSelectedIds.keySet().toArray());
                    mNoSelectedIds.clear();
                }else if(isAllSelected){//删除所有
                    mPhotos.deleteAll();
                }else{
                    Toast.makeText(getApplicationContext(), "请选择要删除的照片", Toast.LENGTH_SHORT).show();
                }
                mBtnSelectAll.setTextColor(Color.BLACK);
                break;
            case R.id.btn_cancel : // 取消
                //mRlTopBar.setVisibility(View.GONE);
                //mLlBottomBar.setVisibility(View.GONE);
                isEdit = false;
                mSelectedIds.clear();
                mNoSelectedIds.clear();
                isAllSelected = false;
                mBtnSelectAll.setTextColor(Color.BLACK);
                break;
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String photoId = holder.id;

        //如果是全选  进行反选
        if(isAllSelected){
            isAllSelected = false;
            mBtnSelectAll.setTextColor(Color.BLACK);
            mNoSelectedIds.put(photoId, true); //添加反选
            view.setAlpha(1.0f);
            mSelectedIds.clear();
        }else if(mNoSelectedIds.size() > 0){//反选操作
            if(mNoSelectedIds.containsKey(photoId)){
                mNoSelectedIds.remove(photoId);//删除反选
                view.setAlpha(0.3f);
            }else{//添加反选
                mNoSelectedIds.put(photoId, true);
                view.setAlpha(1.0f);
                //如果全部反选
                if(mNoSelectedIds.size() == mCursor.getCount()){
                    mNoSelectedIds.clear();
                }
            }
        }else{//未选中
            if(mSelectedIds.containsKey(photoId)){
                mSelectedIds.remove(photoId);
                view.setAlpha(1.0f);
            }else{//选中
                mSelectedIds.put(photoId,true);
                view.setAlpha(0.3f);
            }
        }
        // 全选
        if(mSelectedIds.size() == mCursor.getCount() && mCursor.getCount() > 0){
            isAllSelected = true;
            mBtnSelectAll.setTextColor(Color.BLUE);
            mNoSelectedIds.clear();
        }
        System.out.println(" -=-=-=-=-=-=-=-= mSelectedIds " + mSelectedIds.size());
        System.out.println(" -=-=-=-=-=-=-=-= mNoSelectedIds " + mNoSelectedIds.size());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if(!isEdit){
            isEdit = true;
            ViewHolder holder = (ViewHolder) view.getTag();
            String photoId = holder.id;
            mRlTopBar.setVisibility(View.VISIBLE);
            mLlBottomBar.setVisibility(View.VISIBLE);
            mSelectedIds.put(photoId,true);
        }
        return false;
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
            String id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            holder.id = id;

            Bitmap bitmap = mPhotos.getThumbnails(id);
            if(bitmap != null){
                holder.photoImage.setImageBitmap(bitmap);
            }

            view.setAlpha( isSelected(id) ? 0.3f : 1.0f);
        }
    }

    /**
     * 判断当前照片是否选中
     * @param id
     * @return
     */
    private boolean isSelected(String id){
        if(isAllSelected || mSelectedIds.containsKey(id)){
            return true;
        }
        return mNoSelectedIds.size() > 0 && !mNoSelectedIds.containsKey(id);
    }

    private class ViewHolder{
        public ImageView photoImage;
        public String id;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
