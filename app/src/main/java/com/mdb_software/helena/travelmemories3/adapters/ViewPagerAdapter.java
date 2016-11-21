package com.mdb_software.helena.travelmemories3.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.DataBase.Volley.AppController;
import com.mdb_software.helena.travelmemories3.model.GridViewPhotos;

import java.util.List;

/**
 * Created by Helena on 6/6/2015.
 * Adapter za postavljanje ViewPager dijaloga sa slikama na Gallery aktivnosti
 */
public class ViewPagerAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    private List<GridViewPhotos> photos;
    private GridViewPhotos item;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private static final String IMAGE_URL= "http://hella.mdb-software.com/android_travel_memories/images/";

    public ViewPagerAdapter(Context context, List<GridViewPhotos> photos) {
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
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        //Initializing widgets
        NetworkImageView thumbNail = (NetworkImageView)itemView
                .findViewById(R.id.photoViewPager);

        item = photos.get(position);
        thumbNail.setImageUrl(IMAGE_URL+item.getPhotoUrl(), imageLoader);


        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
