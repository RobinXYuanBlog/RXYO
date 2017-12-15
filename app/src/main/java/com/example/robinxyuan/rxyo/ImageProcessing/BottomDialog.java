package com.example.robinxyuan.rxyo.ImageProcessing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.example.robinxyuan.rxyo.CustomView.HorizontalListView.HorizontalListView;
import com.example.robinxyuan.rxyo.Adapter.HorizontalListViewAdapter;
import com.example.robinxyuan.rxyo.R;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHalftoneFilter;
import jp.co.cyberagent.android.gpuimage.GPUImagePixelationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSketchFilter;

public class BottomDialog extends DialogFragment {

    HorizontalListView horizontalListView;
    HorizontalListViewAdapter horizontalListViewAdapter;

    private Bitmap iconBitmap = null;

    private GPUImage gpuImage;

    protected Dialog dialog;

    private static final String TAG = "BottomDialogFragment";

    private Submit submit;

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        submit = (Submit) activity;
    }

    public interface Submit {
        public void submit(int position);
    }

    public static BottomDialog newInstance() {
        Bundle args = new Bundle();
        BottomDialog fragment = new BottomDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public View initFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.BottomDialog);
        View view = View.inflate(getActivity(), R.layout.fragment_bottom_dialog, null);

        initView(view);

        builder.setView(view);

        dialog = builder.create();

        dialog.setCanceledOnTouchOutside(true);

        // 设置宽度为屏宽、靠近屏幕底部。
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        //设置没有效果
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);

        return dialog;
    }

    @Override public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (null != dialog) {
            dialog.getWindow().setLayout(-1, -2);
        }
    }

    private void initView(View view) {

        gpuImage = new GPUImage(getActivity());

        iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.original);

        horizontalListView = (HorizontalListView) view.findViewById(R.id.horizon_listview);

        String[] titles = {"Original", "Gray", "Emboss", "Sketch", "Pixellate", "Halftone"};

        Bitmap iconGrayBitmap = grayScaleImage(iconBitmap);
        Bitmap iconEmbossBitmap = embossImage(iconBitmap);
        Bitmap iconSkecthBitmap = sketchImage(iconBitmap);
        Bitmap iconPixellateBitmap = pixellateImage(iconBitmap);
        Bitmap iconHalftoneBitmap = halftoneImage(iconBitmap);

        final Bitmap[] iconBitmaps = {iconBitmap, iconGrayBitmap, iconEmbossBitmap, iconSkecthBitmap,
                iconPixellateBitmap, iconHalftoneBitmap};


        horizontalListViewAdapter = new HorizontalListViewAdapter(getActivity().getApplicationContext(), titles, iconBitmaps);
        horizontalListView.setAdapter(horizontalListViewAdapter);

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

                submit.submit(position);

//                switch (position) {
//                    case 0:
//
//                        break;
//                    case 1:
//
//                        break;
//                    case 2:
//
//                        break;
//                    case 3:
//
//                        break;
//                    case 4:
//
//                        break;
//                    case 5:
//
//
//                        break;
//                    default:
//                        break;
//                }

//                previewImg.setImageBitmap(bitmaps[position]);

            }
        });


    }

    //防止重复弹出
    public static BottomDialog showDialog(AppCompatActivity appCompatActivity) {
        android.support.v4.app.FragmentManager fragmentManager = appCompatActivity.getSupportFragmentManager();
        BottomDialog bottomDialogFragment =
                (BottomDialog) fragmentManager.findFragmentByTag(TAG);
        if (null == bottomDialogFragment) {
            bottomDialogFragment = newInstance();
        }

        if (!appCompatActivity.isFinishing()
                && null != bottomDialogFragment
                && !bottomDialogFragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .add(bottomDialogFragment, TAG)
                    .commitAllowingStateLoss();
        }

        return bottomDialogFragment;
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