package com.example.robinxyuan.rxyo.ImageProcessing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.robinxyuan.rxyo.Image.ImageLoader;
import com.example.robinxyuan.rxyo.Image.ListViewAdapter;
import com.example.robinxyuan.rxyo.Image.SelectPhotoActivity;
import com.example.robinxyuan.rxyo.Image.SelectPhotoAdapter;
import com.example.robinxyuan.rxyo.R;

import java.util.ArrayList;

public class ImageProcessingActivity extends AppCompatActivity{

    ImageLoader imageLoader;
    int screenWidth = 0;
    ListViewAdapter<SelectPhotoAdapter.SelectPhotoEntity> photoListViewAdapter;

    Intent data = null;

//    @Override;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_processing);

        imageLoader = new ImageLoader(this);
        screenWidth = SelectPhotoAdapter.getScreenWidth(this);
        photoListViewAdapter = new ListViewAdapter<SelectPhotoAdapter.SelectPhotoEntity>(this,R.layout.listview_item,null) {
            @Override
            public void convert(ViewHolder holder, int position, SelectPhotoAdapter.SelectPhotoEntity entity) {
                ImageView imageView = (ImageView) holder.getView(R.id.iv_selected_photo);
                imageLoader.setAsyncBitmapFromSD(entity.url,imageView,screenWidth,true,true,false);//这里因为图片太大，所以不要保存缩略图
            }
        };

        data = getIntent();

        if(data == null)
            return;
        boolean isFromCamera = data.getBooleanExtra("isFromCamera",false);
        ArrayList<SelectPhotoAdapter.SelectPhotoEntity> selectedPhotos = data.getParcelableArrayListExtra("selectPhotos");
//        Log.i("Alex","选择的图片是"+selectedPhotos);
//        ListView listView = findViewById(R.id.listView);
        try {
            ImageView imageView = findViewById(R.id.show_image);
            SelectPhotoAdapter.SelectPhotoEntity enUrl = selectedPhotos.get(0);
            String url = enUrl.getUrl();
            Bitmap bitmap = BitmapFactory.decodeFile(url);

            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        photoListViewAdapter.setmDatas(selectedPhotos);
//        photoListViewAdapter
//        listView.setAdapter(photoListViewAdapter);

    }

}
