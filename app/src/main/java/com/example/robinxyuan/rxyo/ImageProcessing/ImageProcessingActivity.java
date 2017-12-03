package com.example.robinxyuan.rxyo.ImageProcessing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.robinxyuan.rxyo.Image.ImageLoader;
import com.example.robinxyuan.rxyo.Image.ListViewAdapter;
import com.example.robinxyuan.rxyo.Image.SelectPhotoActivity;
import com.example.robinxyuan.rxyo.Image.SelectPhotoAdapter;
import com.example.robinxyuan.rxyo.R;

import java.util.ArrayList;
import java.util.List;

public class ImageProcessingActivity extends AppCompatActivity{

    ImageLoader imageLoader;
    int screenWidth = 0;
    ListViewAdapter<SelectPhotoAdapter.SelectPhotoEntity> photoListViewAdapter;

    HorizontalListView hListView;
    HorizontalListViewAdapter hListViewAdapter;
    ImageView previewImg;
    View olderSelectView = null;

    private Bitmap bitmap = null;

    Intent data = null;

//    @Override;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_processing);

        imageLoader = new ImageLoader(this);
        screenWidth = SelectPhotoAdapter.getScreenWidth(this);
        photoListViewAdapter = new ListViewAdapter<SelectPhotoAdapter.SelectPhotoEntity>(this, R.layout.listview_item, null) {
            @Override
            public void convert(ViewHolder holder, int position, SelectPhotoAdapter.SelectPhotoEntity entity) {
                ImageView imageView = holder.getView(R.id.iv_selected_photo);
                imageLoader.setAsyncBitmapFromSD(entity.url, imageView, screenWidth, true, true, false);//这里因为图片太大，所以不要保存缩略图
            }
        };

        initUI();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void initUI(){

        data = getIntent();

        if(data == null)
            return;

        boolean isFromCamera = data.getBooleanExtra("isFromCamera",false);
        ArrayList<SelectPhotoAdapter.SelectPhotoEntity> selectedPhotos = data.getParcelableArrayListExtra("selectPhotos");
//        Log.i("Alex","选择的图片是"+selectedPhotos);
//        ListView listView = findViewById(R.id.listView);

        ImageView imageView = findViewById(R.id.show_image);

        if (selectedPhotos.size() > 0) {
            SelectPhotoAdapter.SelectPhotoEntity enUrl = selectedPhotos.get(0);
            String url = enUrl.getUrl();
            bitmap = BitmapFactory.decodeFile(url);
        }

        hListView = (HorizontalListView)findViewById(R.id.horizon_listview);
        previewImg = (ImageView)findViewById(R.id.show_image);
        String[] titles = {"Original"};


        final Bitmap[] bitmaps = {bitmap};



        hListViewAdapter = new HorizontalListViewAdapter(getApplicationContext(), titles, bitmaps);
        hListView.setAdapter(hListViewAdapter);

        imageView.setImageBitmap(bitmap);

        hListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                    // TODO Auto-generated method stub
//              if(olderSelectView == null){
//                  olderSelectView = view;
//              }else{
//                  olderSelectView.setSelected(false);
//                  olderSelectView = null;
//              }
//              olderSelectView = view;
//              view.setSelected(true);
                previewImg.setImageBitmap(bitmaps[position]);
                hListViewAdapter.setSelectIndex(position);
                hListViewAdapter.notifyDataSetChanged();

            }
        });


//        photoListViewAdapter.setmDatas(selectedPhotos);
//        photoListViewAdapter
//        listView.setAdapter(photoListViewAdapter);

    }

}
