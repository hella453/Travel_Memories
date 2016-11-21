package com.mdb_software.helena.travelmemories3.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.mdb_software.helena.travelmemories3.DataBase.Volley.AppController;
import com.mdb_software.helena.travelmemories3.DataBase.Volley.CircularNetworkImageView;
import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.model.ListViewTrips;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Helena on 5/10/2015.
 * Adapter za postavljanje ListView-a sa putovanjima
 */
public class ListViewTripsAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<ListViewTrips> listaPutovanja;
    private ListViewTrips item;
    private static final String IMAGE_URL= "http://hella.mdb-software.com/android_travel_memories/images/";
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    //Constructor
    public ListViewTripsAdapter(Activity activity, List<ListViewTrips> listaPutovanja) {
        this.activity = activity;
        this.listaPutovanja = listaPutovanja;
    }

    @Override
    public int getCount() {
        return listaPutovanja.size();
    }

    @Override
    public Object getItem(int location) {
        return listaPutovanja.get(location);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_view_trips, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        //Initializing widgets
        CircularNetworkImageView thumbNail = (CircularNetworkImageView) convertView
                .findViewById(R.id.slikaPutovanja);
        ImageView circle = (ImageView)convertView.findViewById(R.id.circleImageView);
        circle.bringToFront();
        TextView title = (TextView) convertView.findViewById(R.id.titleTripTxt);
        TextView dateTxt = (TextView) convertView.findViewById(R.id.datumTxt);
        TextView brojLokacija = (TextView)convertView.findViewById(R.id.locationNmb);
        dateTxt.bringToFront();

        // Getting Trips data for the row
        item = listaPutovanja.get(position);
        // Thumbnail image
        thumbNail.setImageUrl(IMAGE_URL+item.getImageUrl(), imageLoader);

        // Title
        title.setText(item.getTitle().toString());
        // Date
        String startDate = new SimpleDateFormat("dd.MMM.yyyy").format(item.getStartDate());
        String endDate = new SimpleDateFormat("dd.MMM.yyyy").format(item.getEndDate());
        dateTxt.setText(startDate + " - " + endDate);
        //brojLokacija
        int broj = item.getBrojLokacija();
        String lokacija;
        if (broj==1){
        lokacija = "location";
        }else lokacija = "locations";
        brojLokacija.setText(String.valueOf(broj)+ " " + lokacija);

        return convertView;
    }
}
