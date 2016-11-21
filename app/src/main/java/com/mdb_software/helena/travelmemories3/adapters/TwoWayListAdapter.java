package com.mdb_software.helena.travelmemories3.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.DataBase.Volley.AppController;

import java.util.ArrayList;

/**
 * Created by Helena on 6/7/2015.
 * Adapter za postavljanje horizontalne liste sa slikama
 */
public class TwoWayListAdapter extends BaseAdapter {

    Context mcontext;
    LayoutInflater inflater;
    int id;
    private ArrayList<Bitmap> photos;


    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public TwoWayListAdapter(Context context, ArrayList<Bitmap> photos, int id) {
        mcontext = context;
        this.id=id;
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
            convertView = inflater.inflate(id, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        //Initializing widgets
       ImageView thumbNail = (ImageView) convertView
                .findViewById(R.id.photoTwoWay);
        thumbNail.setImageBitmap(photos.get(position));

        return convertView;
    }
}