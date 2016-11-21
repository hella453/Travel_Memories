package com.mdb_software.helena.travelmemories3.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mdb_software.helena.travelmemories3.Fragments.MapDrawerFragment;
import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.adapters.ListViewPlacesAdapter;
import com.mdb_software.helena.travelmemories3.DataBase.UserFunctions;
import com.mdb_software.helena.travelmemories3.model.ListViewPlaces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class PlacesActivity extends ActionBarActivity {


    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private ListViewPlacesAdapter adapter;
    private List<ListViewPlaces> listaMjesta = new ArrayList<ListViewPlaces>();
    private String lokacija, imageUrl, imePutovanja;
    private Date datum;
    private String datumString;
    private ProgressDialog pDialog;
    private int putovanjeID, markerID;
    private Toolbar toolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private double longitude, latitude;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView listView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_places);

        //Get Intent extra
        Intent intent = this.getIntent();
        putovanjeID =intent.getIntExtra("PutovanjeID", -1);
        imePutovanja=intent.getStringExtra("imePutovanja");

        listView = (ListView)findViewById(android.R.id.list);

        //Set toolbar
        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("   "+imePutovanja);

        //Ucitavanje podataka
        postaviAdapter();
        dohvatiIzBaze();

        //Set Map Drawer
        MapDrawerFragment drawerFragment = (MapDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_map_drawer);
        drawerFragment.setUp(R.id.fragment_map_drawer, (DrawerLayout)findViewById(R.id.drawer_layout), toolbar, listaMjesta);


        //Enable home bttn
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Postavi swipe refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        //swipeRefresh Listener
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();


            }
        });

    }

    /**
     * Refrasha listu putovanja
     */
    private void refreshList() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dohvatiIzBaze();
                adapter.notifyDataSetChanged();


                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 5000);
    }

    /**
     * Prikazuje dijalog dok se podaci ucitavaju u listu
     */
    private void showPDialog() {
        pDialog = new ProgressDialog(this);
        // Showing progress dialog before making http request
        pDialog.setMessage("Loading...");
        pDialog.show();
    }

    /**
     * Postavlja custom adapter i onClicListener
     */
    private void postaviAdapter() {
        adapter = new ListViewPlacesAdapter(this, listaMjesta);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pozoviIntent(position);

            }
        });
        //On long click delete
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,final int row, long arg3) {
                // your code

                postaviDialog(row);
                return true;
            }
        });
    }

    /**
     * Postavlja dijalog s mogucnoscu brisanja mjesta iz baze
     * @param row
     */
    private void postaviDialog(final int row) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Delete");
        dialogBuilder.setMessage("Are you sure you want to delete this place?");
        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                izbrisiIzBaze(row);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Delete
            }
        });
        dialogBuilder.create().show();
    }

    /**
     * Prosljeduje zahtjev za brisanje mjesta iz baze i prima odgovor kojeg cita iz JSON formata
     * @param row
     */
    private void izbrisiIzBaze(int row){
        ListViewPlaces item= listaMjesta.get(row);
        int tripID=item.getID();
        UserFunctions userFunctions = new UserFunctions();
        try {
            JSONObject json = null;
            json = userFunctions.delete(this, "deletePlace", tripID);
            if (json.getString(KEY_SUCCESS) != null) {
                String res = json.getString(KEY_SUCCESS);
                if (Integer.parseInt(res) == 1) {
                    // Podaci su uspjesno dohvaceni
                    listaMjesta.remove(row);
                    listItemsSize();
                    adapter.notifyDataSetChanged();
                }//end if
            }else if (json.getString(KEY_ERROR) != null){
                TextView errorTxt = (TextView)findViewById(R.id.errorTxt);
                errorTxt.setText(json.getString(KEY_ERROR_MSG));
                errorTxt.setBackgroundColor(getResources().getColor(R.color.popuniSvaPolja));
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
     * Provjerava je li lista prazna. Ako je prikazuje poruku.
     */
    private void listItemsSize() {
        if (listaMjesta.size()==0){
            TextView errorTxt = (TextView)findViewById(R.id.errorPlacesTxt);
            LinearLayout crta = (LinearLayout)findViewById(R.id.crtaLayoutPlaces);
            crta.setVisibility(View.INVISIBLE);
            errorTxt.setText("No places added. To add a place press the plus button below.");
            errorTxt.setBackgroundColor(getResources().getColor(R.color.popuniSvaPolja));
        }
    }

    /**
     * Dohvaca podatke iz baze preko JSON-a.
     */
    private void dohvatiIzBaze() {
        UserFunctions userFunctions = new UserFunctions();
        try {
            JSONObject json = null;
            json = userFunctions.getPlaces(this, String.valueOf(putovanjeID));
            if (json.getString(KEY_SUCCESS) != null) {
                String res = json.getString(KEY_SUCCESS);
                if (Integer.parseInt(res) == 1) {
                    // Podaci su uspjesno dohvaceni
                    parsirajPodatke(json);
                    listItemsSize();
                    adapter.notifyDataSetChanged();
                }//end if
            }else if (json.getString(KEY_ERROR) != null){
                TextView errorTxt = (TextView)findViewById(R.id.errorPlacesTxt);
                errorTxt.setText(json.getString(KEY_ERROR_MSG));
                errorTxt.setBackgroundColor(getResources().getColor(R.color.popuniSvaPolja));
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
     * Parsira podatke iz baze i sprema ih u listuPutovanja.
     * @param json
     */
    private void parsirajPodatke(JSONObject json) {
        JSONArray recs = null;
        listaMjesta.clear();
        try {
            recs = json.getJSONArray("arrayRezultati");
            ListViewPlaces item = new ListViewPlaces();
            for (int i=0; i<recs.length();i++) {
                JSONObject rec = recs.getJSONObject(i);
                lokacija = (rec.getString("Lokacija"));
                imageUrl = (rec.getString("url"));
                markerID = (rec.getInt("markerId"));
                longitude = (rec.getDouble("Longitude"));
                latitude = (rec.getDouble("Latitude"));
                datumString = rec.getString("Datum");
                datum = parsirajDatum(datumString);
                // adding item to list
                listaMjesta.add(new ListViewPlaces(imageUrl, lokacija, datum, markerID, longitude, latitude));
            }//end for
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Parsira datum.
     * @param datum
     * @return
     */
    private Date parsirajDatum(String datum) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(datum);
            return date;

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Poziva TripPlacesActivity na click iz listView i šalje ID putovanja.
     * @param position
     */
    private void pozoviIntent(int position) {
        ListViewPlaces item;
        item=listaMjesta.get(position);
        Intent i = new Intent (this, PlaceDetailActivity.class);
        i.putExtra("MarkerID", item.getID());
        i.putExtra("datum", item.getDatum());
        i.putExtra("lokacija", item.getTitle());
        i.putExtra("url", item.getImageUrl());
        i.putExtra("putovanjeId", putovanjeID);
        startActivity(i);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    /**
     * Kad su ucitani svi podaci iz liste sakrije dijalog.
     */
    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
     * Otvara aktivnost za unos novog mjesta
     * @param view
     */
    public void onClicknewPlace(View view){
        Intent i = new Intent(this, NewPlaceActivity.class);
        startActivity(i);
        finish();
    }


}
