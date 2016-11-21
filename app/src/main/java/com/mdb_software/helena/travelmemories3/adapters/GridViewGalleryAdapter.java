package com.mdb_software.helena.travelmemories3.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.DataBase.Volley.AppController;
import com.mdb_software.helena.travelmemories3.model.GridViewPhotos;

import java.util.List;

/**
 * Created by Helena on 6/5/2015.
 * Adapter za postavljanje GridView galerije
 *
 */
public class GridViewGalleryAdapter extends BaseAdapter {
    private List<GridViewPhotos> photos;
    private GridViewPhotos item;
    private LayoutInflater inflater;
    Context mcontext;
    private static final String IMAGE_URL= "http://hella.mdb-software.com/android_travel_memories/images/";
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public GridViewGalleryAdapter(Context mcontext, List<GridViewPhotos> photos){
        this.mcontext=mcontext;
        this.photos = photos;
    }
    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) mcontext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.grid_view_photos, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        NetworkImageView thumbNail = (NetworkImageView) convertView
                .findViewById(R.id.photoImageView);
        item = photos.get(position);
        thumbNail.setImageUrl(IMAGE_URL+item.getPhotoUrl(), imageLoader);
        return convertView;
    }


}
