package com.mdb_software.helena.travelmemories3.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.toolbox.ImageLoader;
import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.DataBase.Volley.AppController;

import java.util.ArrayList;

/**
 * Created by Helena on 6/6/2015.
 * Adapter za postavljanje ViewPagera na aktivnost NewPlaceActivity
 */
public class ViewPagerAdapter2 extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    private ArrayList<Bitmap> photos;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public ViewPagerAdapter2(Context context, ArrayList<Bitmap> photos) {
        mContext = context;
        this.photos=photos;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item2, container, false);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        //Initializing widgets
        ImageView thumbNail = (ImageView)itemView
                .findViewById(R.id.photoViewPager2);
        thumbNail.setImageBitmap(photos.get(position));
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
