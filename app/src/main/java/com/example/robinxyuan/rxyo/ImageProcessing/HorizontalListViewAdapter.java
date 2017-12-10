package com.example.robinxyuan.rxyo.ImageProcessing;

/**
 * Created by robinxyuan on 2017/12/3.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robinxyuan.rxyo.R;

import java.util.List;

public class HorizontalListViewAdapter extends BaseAdapter{
    private Bitmap[] bitmaps;
    private String[] mTitles;
    private Context mContext;
    private LayoutInflater mInflater;
    Bitmap iconBitmap;
    private int selectIndex = -1;

    public HorizontalListViewAdapter(Context context, String[] titles, Bitmap[] bitmap){
        this.mContext = context;
        this.bitmaps = bitmap;
        this.mTitles = titles;
        mInflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);
    }
    @Override
    public int getCount() {
        return bitmaps.length;
//        return mIconIDs.size();
    }
    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView==null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.horizontal_list_item, null);
            holder.mImage = (ImageView)convertView.findViewById(R.id.img_list_item);
            holder.mTitle = (TextView)convertView.findViewById(R.id.text_list_item);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        if(position == selectIndex){
            convertView.setSelected(true);
        }else{
            convertView.setSelected(false);
        }

        holder.mTitle.setText(mTitles[position]);
//        iconBitmap = getPropThumnail(mIconIDs[position]);
        iconBitmap = getPropThumnail(bitmaps[position]);

        holder.mImage.setImageBitmap(iconBitmap);

        return convertView;
    }

    private static class ViewHolder {
        private TextView mTitle ;
        private ImageView mImage;
    }
    private Bitmap getPropThumnail(Bitmap id){
//        Drawable d = mContext.getResources().getDrawable(id);
//        Bitmap b = BitmapUtil.drawableToBitmap(d);
        Bitmap b = id;
//      Bitmap bb = BitmapUtil.getRoundedCornerBitmap(b, 100);
        int w = mContext.getResources().getDimensionPixelOffset(R.dimen.thumnail_default_width);
        int h = mContext.getResources().getDimensionPixelSize(R.dimen.thumnail_default_height);

        Bitmap thumBitmap = ThumbnailUtils.extractThumbnail(b, w, h);

        return thumBitmap;
    }
    public void setSelectIndex(int i){
        selectIndex = i;
    }
}
