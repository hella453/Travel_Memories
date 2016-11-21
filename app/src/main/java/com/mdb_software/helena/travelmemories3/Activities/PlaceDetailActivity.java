package com.mdb_software.helena.travelmemories3.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mdb_software.helena.travelmemories3.DataBase.UserFunctions;
import com.mdb_software.helena.travelmemories3.DataBase.Volley.AppController;
import com.mdb_software.helena.travelmemories3.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class PlaceDetailActivity extends ActionBarActivity {

    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static final String IMAGE_URL= "http://hella.mdb-software.com/android_travel_memories/images/";
    private  NetworkImageView coverPhoto;
    private TextView biljeskeTxt, brojSlikaTxt, datumTxt;
    private int markerId, brojSlika, putovanjeId;
    private String lokacija, imageName, biljeska, datum, vrijeme;

    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private Toolbar toolbar;
    private GoogleMap googleMap;
    private ScrollView mainScrollView;
    private double longitude, latitude;
    private ImageView transparentImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        //Widgets
        coverPhoto = (NetworkImageView)findViewById(R.id.detailCoverPhoto);
        biljeskeTxt = (TextView)findViewById(R.id.biljeskeTxt);
        brojSlikaTxt = (TextView)findViewById(R.id.brojSlikaTxt);
        mainScrollView = (ScrollView)findViewById(R.id.scrollDetail);
        transparentImageView = (ImageView) findViewById(R.id.transparent_image);
        datumTxt = (TextView)findViewById(R.id.dateTxtdetail);

        //Set scroll
        improveScroll();
        //Get extras
        markerId = getIntent().getIntExtra("MarkerID", -1);
        lokacija = getIntent().getStringExtra("lokacija");
        putovanjeId = getIntent().getIntExtra("putovanjeId",-1);


        //Set toolbar
        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("   "+lokacija);

        //Enable home bttn
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dohvatiIzBaze();

        //Setting widgets
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        coverPhoto.setImageUrl(IMAGE_URL+imageName, imageLoader);
        biljeskeTxt.setText(biljeska);
        brojSlikaTxt.setText(String.valueOf(brojSlika));
        datumTxt.setText(datum+" "+vrijeme);

        //Set Google Maps
        setGoogleMaps();
    }

    /**
     * Omogucava pomicanje unutar google mapsa gore-dolje koji je unutar scroll view-a
     */
    private void improveScroll() {
        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });

    }

    /**
     * Dohvaca google map fragment i postavlja marker
     */
    private void setGoogleMaps() {
        final LatLng TutorialsPoint = new LatLng(latitude , longitude);

        try {
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            Marker marker = googleMap.addMarker(new MarkerOptions().
                    position(TutorialsPoint).title(lokacija));
            marker.showInfoWindow();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        googleMap.moveCamera( CameraUpdateFactory.newLatLng(new LatLng(latitude,
                longitude)));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    /**
     * Prosljeduje podatke prema bazi i provjerava odgovor sa servera
     */
    private void dohvatiIzBaze() {
        UserFunctions userFunctions = new UserFunctions();
        try {
            JSONObject json = null;
            json = userFunctions.getPlaceDetails(this, String.valueOf(markerId));
            if (json.getString(KEY_SUCCESS) != null) {
                String res = json.getString(KEY_SUCCESS);
                if (Integer.parseInt(res) == 1) {
                    // Podaci su uspjesno dohvaceni
                    parsirajPodatke(json);

                }//end if
            }else if (json.getString(KEY_ERROR) != null){
                TextView errorTxt = (TextView)findViewById(R.id.errorPlacesTxt);
                errorTxt.setText(json.getString(KEY_ERROR_MSG));
            }//end if
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Parsira primljene podatke sa servera iz JSON formata
     * @param json
     */
    private void parsirajPodatke(JSONObject json) {
        try {
                biljeska = (json.getString("biljeska"));
                brojSlika = (json.getInt("brojSlika"));
                longitude = (json.getDouble("longitude"));
                latitude = (json.getDouble("latitude"));
                imageName = json.getString("URL");
                parsirajVrijeme(json.getString("Vrijeme"));
            }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }
    /**
     * Parsira datum.
     * @param datum
     * @return
     */
    private void parsirajVrijeme(String datum) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(datum);
            vrijeme = new SimpleDateFormat("HH:mm").format(date);
            this.datum = new SimpleDateFormat("dd.MMM.yyyy").format(date);

        } catch (ParseException e) {
            e.printStackTrace();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                finish();
                return true;

            case R.id.logout:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putBoolean("Islogin", false).commit(); // islogin is a boolean value of your login status
                prefs.edit().putString("uid", null).commit();
                Intent i = new Intent(this, EmailLoginActivity.class);
                startActivity(i);
                finish();

                return true;
            }
            return super.onOptionsItemSelected(item);

    }

    /**
     * Otvara galeriju sa slikama
     * @param view
     */
    public void onPhotoClick(View view) {

        Intent i = new Intent(this, GalleryActivity.class);
        i.putExtra("markerId", markerId);
        i.putExtra("putovanjeId", putovanjeId);
        startActivity(i);
    }

    /**
     * Otvara activity za dodavanje novog mjesta
     * @param view
     */
    public void onClicknewPlace(View view){
        Intent i = new Intent(this, NewPlaceActivity.class);
        startActivity(i);
    }
}
