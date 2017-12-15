package com.example.robinxyuan.rxyo.ImageProcessing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robinxyuan.rxyo.CustomView.ArcMenu.ArcMenu;
import com.example.robinxyuan.rxyo.CustomView.HorizontalListView.HorizontalListView;
import com.example.robinxyuan.rxyo.Adapter.HorizontalListViewAdapter;
import com.example.robinxyuan.rxyo.Image.ImageLoader;
import com.example.robinxyuan.rxyo.Adapter.ListViewAdapter;
import com.example.robinxyuan.rxyo.Adapter.SelectPhotoAdapter;
import com.example.robinxyuan.rxyo.R;
import com.example.robinxyuan.rxyo.SlideBottomPanel.SlideBottomPanel;
import com.example.robinxyuan.rxyo.Utils.BitmapUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHalftoneFilter;
import jp.co.cyberagent.android.gpuimage.GPUImagePixelationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSketchFilter;
//import me.next.slidebottompanel.SlideBottomPanel;

public class ImageProcessingActivity extends AppCompatActivity{

    ImageLoader imageLoader;
    int screenWidth = 0;
    ListViewAdapter<SelectPhotoAdapter.SelectPhotoEntity> photoListViewAdapter;

    HorizontalListView horizontalListView;
    HorizontalListViewAdapter horizontalListViewAdapter;
//    ImageView previewImg;
    View olderSelectView = null;

    private GPUImage gpuImage;

    protected Bitmap bitmap;
    protected Bitmap iconBitmap;

    private ArcMenu arcMenu;

//    @BindView(R.id.filterBottomPanel)
    SlideBottomPanel slideBottomPanel;

//    @BindView(R.id.filter_button) Button filterButton;
    Button filterButton;

//    Intent data;
//
//    @BindView(R.id.filter_button)
//    Button filterButton;
//
//    @BindView(R.id.tools_button)
//    Button toolsButton;
//
//    @BindView(R.id.share_button)
//    Button shareButton;

    ImageView imageView;

//    @Override;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_processing);

        ButterKnife.bind(this);

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

//        toolsButton.setOnClickListener(this);
//        shareButton.setOnClickListener(this);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    @OnClick(R.id.btn_share)
    public void onShareClick(View view) {
        Toast.makeText(view.getContext(), "OK", Toast.LENGTH_SHORT).show();
    }

    public void initUI(){

        Intent data = getIntent();
        gpuImage = new GPUImage(this);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");

        TextView arcMenuButton = findViewById(R.id.btn_arcmenu);
        TextView shareButton = findViewById(R.id.btn_share);
        TextView saveButton = findViewById(R.id.btn_save);
        TextView cancelButton = findViewById(R.id.btn_cancel);
        TextView showPanelButton = findViewById(R.id.btn_show_panel);

        arcMenuButton.setTypeface(font);
        arcMenuButton.setText(getString(R.string.icon_plus));

        shareButton.setTypeface(font);
        shareButton.setText(getString(R.string.icon_plane));

        saveButton.setTypeface(font);
        saveButton.setText(getString(R.string.icon_save));

        cancelButton.setTypeface(font);
        cancelButton.setText(getString(R.string.icon_cancel));

        showPanelButton.setTypeface(font);
        showPanelButton.setText(R.string.icon_archive);

        arcMenu = findViewById(R.id.arcMenu);

        arcMenu.setOnMenuItemClickListener(new ArcMenu.OnMenuItemClickListener()
                {
                    @Override
                    public void onClick(View view, int pos)
                    {
                        switch (pos) {
                            case 0:
                                Toast.makeText(ImageProcessingActivity.this,
                                        "Share", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case 1:
                                Toast.makeText(ImageProcessingActivity.this,
                                        "Save", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case 2:
                                finish();
                                break;
                            case 3:
                                slideBottomPanel.displayPanel();
                                break;
                        }

                    }
                });

        slideBottomPanel = findViewById(R.id.filterBottomPanel);

//        slideBottomPanel.setOnDragListener(new View.OnDragListener() {
//            @Override
//            public boolean onDrag(View view, DragEvent dragEvent) {
//                slideBottomPanel.displayPanel();
//                return false;
//            }
//        });

//        Button drag = findViewById(R.id.drop);

//        drag.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                slideBottomPanel.displayPanel();
//            }
//        });


        if(data == null)
            return;

        boolean isFromCamera = data.getBooleanExtra("isFromCamera",false);
        ArrayList<SelectPhotoAdapter.SelectPhotoEntity> selectedPhotos = data.getParcelableArrayListExtra("selectPhotos");
//        Log.i("Alex","选择的图片是"+selectedPhotos);
//        ListView listView = findViewById(R.id.listView);

        imageView = findViewById(R.id.show_image);

        if (selectedPhotos.size() > 0) {
            SelectPhotoAdapter.SelectPhotoEntity enUrl = selectedPhotos.get(0);
            String url = enUrl.getUrl();
            bitmap = BitmapFactory.decodeFile(url);
            imageView.setImageBitmap(bitmap);
        }

        iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.original);

        horizontalListView = (HorizontalListView)findViewById(R.id.horizon_listview);
//        previewImg = (ImageView) findViewById(R.id.show_image);

        horizontalListView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                slideBottomPanel.displayPanel();
                return false;
            }
        });

        String[] titles = {"Original", "Gray", "Emboss", "Sketch", "Pixellate", "Halftone"};

//         Use GPUImage to process images

//         Get all images after filtering
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

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleRatio;

        if(((width > 960) && (width < 2560)) || ((height > 960) && (height < 2560))) {
            if(width >= height) {
                scaleRatio = 960 / (float) width;
            } else {
                scaleRatio = 960 / (float) height;
            }
        } else if((width >= 2560) || (height >= 2560)) {
            if(width >= height) {
                scaleRatio = 2560f / (float) width;
            } else {
                scaleRatio = 2560f / (float) height;
            }
        } else {
            scaleRatio = 1.0f;
        }

        bitmap = BitmapUtils.scaleBitmap(bitmap, scaleRatio);
        imageView.setImageBitmap(bitmap);

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
                        slideBottomPanel.hide();
                        horizontalListViewAdapter.setSelectIndex(position);
                        imageView.setImageBitmap(bitmap);
                        horizontalListViewAdapter.notifyDataSetChanged();
                        break;
                    case 1:
                        slideBottomPanel.hide();
                        horizontalListViewAdapter.setSelectIndex(position);
                        Bitmap grayBitmap = grayScaleImage(bitmap);
                        imageView.setImageBitmap(grayBitmap);
                        horizontalListViewAdapter.notifyDataSetChanged();
                        break;
                    case 2:
                        slideBottomPanel.hide();
                        horizontalListViewAdapter.setSelectIndex(position);
                        Bitmap embossBitmap = embossImage(bitmap);
                        imageView.setImageBitmap(embossBitmap);
                        horizontalListViewAdapter.notifyDataSetChanged();
                        break;
                    case 3:
                        slideBottomPanel.hide();
                        horizontalListViewAdapter.setSelectIndex(position);
                        Bitmap sketchBitmap = sketchImage(bitmap);
                        imageView.setImageBitmap(sketchBitmap);
                        horizontalListViewAdapter.notifyDataSetChanged();
                        break;
                    case 4:
                        slideBottomPanel.hide();
                        horizontalListViewAdapter.setSelectIndex(position);
                        Bitmap pixellateBitmap = pixellateImage(bitmap);
                        imageView.setImageBitmap(pixellateBitmap);
                        horizontalListViewAdapter.notifyDataSetChanged();
                        break;
                    case 5:
                        slideBottomPanel.hide();
                        horizontalListViewAdapter.setSelectIndex(position);
                        Bitmap halftoneBitmap = halftoneImage(bitmap);
                        imageView.setImageBitmap(halftoneBitmap);
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
