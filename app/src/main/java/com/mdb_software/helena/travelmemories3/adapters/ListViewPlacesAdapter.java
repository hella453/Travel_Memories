package com.mdb_software.helena.travelmemories3.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.DataBase.Volley.AppController;
import com.mdb_software.helena.travelmemories3.model.ListViewPlaces;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Helena on 5/10/2015.
 * Adapter za postavljanje ListView-a sa mjestima
 */
public class ListViewPlacesAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<ListViewPlaces> listaMjesta;
    private ListViewPlaces item;
    private static final String IMAGE_URL= "http://hella.mdb-software.com/android_travel_memories/images/";
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    int pos;

    //Constructor
    public ListViewPlacesAdapter(Activity activity, List<ListViewPlaces> listaObjava) {
        this.activity = activity;
        this.listaMjesta = listaObjava;
    }

    @Override
    public int getCount() {
        return listaMjesta.size();
    }

    @Override
    public Object getItem(int location) {
        return listaMjesta.get(location);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        int pos =position;
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_view_places, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        //Widgets
        NetworkImageView thumbNail = (NetworkImageView) convertView
                .findViewById(R.id.slikaMjesta);
        TextView title = (TextView) convertView.findViewById(R.id.titlePlacesTxt);
        TextView dateTxt = (TextView) convertView.findViewById(R.id.datumPlacesTxt);
        dateTxt.bringToFront();

        // Getting Trips data for the row
        item = listaMjesta.get(position);
        // Thumbnail image
        thumbNail.setImageUrl(IMAGE_URL+item.getImageUrl(), imageLoader);

        // Title
        title.setText(item.getTitle().toString());
        // Date
        String startDate = new SimpleDateFormat("dd.MMM.yyyy").format(item.getDatum());
        dateTxt.setText(startDate);
        return convertView;
    }

}
