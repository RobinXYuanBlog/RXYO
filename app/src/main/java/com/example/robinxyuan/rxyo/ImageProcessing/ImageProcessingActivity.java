package com.example.robinxyuan.rxyo.ImageProcessing;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.robinxyuan.rxyo.Image.ImageLoader;
import com.example.robinxyuan.rxyo.Image.ListViewAdapter;
import com.example.robinxyuan.rxyo.Image.SelectPhotoActivity;
import com.example.robinxyuan.rxyo.Image.SelectPhotoAdapter;
import com.example.robinxyuan.rxyo.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageColorDodgeBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHalftoneFilter;
import jp.co.cyberagent.android.gpuimage.GPUImagePixelationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSketchFilter;

public class ImageProcessingActivity extends AppCompatActivity{

    ImageLoader imageLoader;
    int screenWidth = 0;
    ListViewAdapter<SelectPhotoAdapter.SelectPhotoEntity> photoListViewAdapter;

    HorizontalListView horizontalListView;
    HorizontalListViewAdapter horizontalListViewAdapter;
//    ImageView previewImg;
    View olderSelectView = null;

    private GPUImage gpuImage = new GPUImage(this);

    private Bitmap bitmap = null;
    private Bitmap iconBitmap = null;

    Intent data = getIntent();

    @BindView(R.id.filter_button)
    Button filterButton;

    @BindView(R.id.tools_button)
    Button toolsButton;

    @BindView(R.id.share_button)
    Button shareButton;

    @BindView(R.id.show_image)
    ImageView previewImg;

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

        initImageView();

        FragmentManager manager = getFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();

        final BottomDialog bottomDialog = new BottomDialog();

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                transaction.replace(R.id.bottom_sheet, bottomDialog);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void initImageView() {

        if(data == null) {
            return;
        }

        boolean isFromCamera = data.getBooleanExtra("isFromCamera",false);
        ArrayList<SelectPhotoAdapter.SelectPhotoEntity> selectedPhotos = data.getParcelableArrayListExtra("selectPhotos");

        if (selectedPhotos.size() > 0) {
            SelectPhotoAdapter.SelectPhotoEntity enUrl = selectedPhotos.get(0);
            String url = enUrl.getUrl();
            bitmap = BitmapFactory.decodeFile(url);
        }

        previewImg.setImageBitmap(bitmap);


    }

    private void showMenuSheet(final HorizontalListView horizontalListView) {

    }

    public void initUI(){

//        data = getIntent();
//        gpuImage = new GPUImage(this);
//
//
//        if(data == null)
//            return;

//        boolean isFromCamera = data.getBooleanExtra("isFromCamera",false);
//        ArrayList<SelectPhotoAdapter.SelectPhotoEntity> selectedPhotos = data.getParcelableArrayListExtra("selectPhotos");
//        Log.i("Alex","选择的图片是"+selectedPhotos);
//        ListView listView = findViewById(R.id.listView);

//        ImageView imageView = findViewById(R.id.show_image);



        iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.original);

        horizontalListView = (HorizontalListView)findViewById(R.id.horizon_listview);
//        previewImg = (ImageView) findViewById(R.id.show_image);

        String[] titles = {"Original", "Gray", "Emboss", "Sketch", "Pixellate", "Halftone"};

        // Use GPUImage to process images

        // Get all images after filtering
        Bitmap iconGrayBitmap = grayScaleImage(iconBitmap);
        Bitmap iconEmbossBitmap = embossImage(iconBitmap);
        Bitmap iconSkecthBitmap = sketchImage(iconBitmap);
        Bitmap iconPixellateBitmap = pixellateImage(iconBitmap);
        Bitmap iconHalftoneBitmap = halftoneImage(iconBitmap);

//        final Bitmap[] bitmaps = {bitmap, grayBitmap, embossBitmap};
        final Bitmap[] iconBitmaps = {iconBitmap, iconGrayBitmap, iconEmbossBitmap, iconSkecthBitmap,
                iconPixellateBitmap, iconHalftoneBitmap};


        horizontalListViewAdapter = new HorizontalListViewAdapter(getApplicationContext(), titles, iconBitmaps);
        horizontalListView.setAdapter(horizontalListViewAdapter);

//        imageView.setImageBitmap(bitmap);

        horizontalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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

                switch (position) {
                    case 0:
                        horizontalListViewAdapter.setSelectIndex(position);
                        previewImg.setImageBitmap(bitmap);
                        horizontalListViewAdapter.notifyDataSetChanged();
                        break;
                    case 1:
                        horizontalListViewAdapter.setSelectIndex(position);
                        Bitmap grayBitmap = grayScaleImage(bitmap);
                        previewImg.setImageBitmap(grayBitmap);
                        horizontalListViewAdapter.notifyDataSetChanged();
                        break;
                    case 2:
                        horizontalListViewAdapter.setSelectIndex(position);
                        Bitmap embossBitmap = embossImage(bitmap);
                        previewImg.setImageBitmap(embossBitmap);
                        horizontalListViewAdapter.notifyDataSetChanged();
                        break;
                    case 3:
                        horizontalListViewAdapter.setSelectIndex(position);
                        Bitmap sketchBitmap = sketchImage(bitmap);
                        previewImg.setImageBitmap(sketchBitmap);
                        horizontalListViewAdapter.notifyDataSetChanged();
                        break;
                    case 4:
                        horizontalListViewAdapter.setSelectIndex(position);
                        Bitmap pixellateBitmap = pixellateImage(bitmap);
                        previewImg.setImageBitmap(pixellateBitmap);
                        horizontalListViewAdapter.notifyDataSetChanged();
                        break;
                    case 5:
                        horizontalListViewAdapter.setSelectIndex(position);
                        Bitmap halftoneBitmap = halftoneImage(bitmap);
                        previewImg.setImageBitmap(halftoneBitmap);
                        horizontalListViewAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }

//                previewImg.setImageBitmap(bitmaps[position]);

            }
        });


//        photoListViewAdapter.setmDatas(selectedPhotos);
//        photoListViewAdapter
//        listView.setAdapter(photoListViewAdapter);

    }

    private Bitmap grayScaleImage(Bitmap bitmap) {

        gpuImage.setImage(bitmap);
        gpuImage.setFilter(new GPUImageGrayscaleFilter());

        Bitmap grayBitmap = gpuImage.getBitmapWithFilterApplied();

        return grayBitmap;
    }

    private Bitmap embossImage(Bitmap bitmap) {

        gpuImage.setImage(bitmap);
        gpuImage.setFilter(new GPUImageEmbossFilter());

        Bitmap embossBitmap = gpuImage.getBitmapWithFilterApplied();

        return embossBitmap;
    }

    private Bitmap sketchImage(Bitmap bitmap) {

        gpuImage.setImage(bitmap);
        gpuImage.setFilter(new GPUImageSketchFilter());

        Bitmap sketchBitmap = gpuImage.getBitmapWithFilterApplied();

        return sketchBitmap;
    }

    private Bitmap pixellateImage(Bitmap bitmap) {

        gpuImage.setImage(bitmap);
        gpuImage.setFilter(new GPUImagePixelationFilter());

        Bitmap pixellateBitmap = gpuImage.getBitmapWithFilterApplied();

        return pixellateBitmap;
    }

    private Bitmap halftoneImage(Bitmap bitmap) {

        gpuImage.setImage(bitmap);
        gpuImage.setFilter(new GPUImageHalftoneFilter());

        Bitmap halftoneBitmap = gpuImage.getBitmapWithFilterApplied();

        return halftoneBitmap;
    }

}
