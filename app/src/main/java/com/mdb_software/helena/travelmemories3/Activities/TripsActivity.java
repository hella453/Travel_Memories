package com.mdb_software.helena.travelmemories3.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mdb_software.helena.travelmemories3.Fragments.MapDrawerFragment;
import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.adapters.ListViewTripsAdapter;
import com.mdb_software.helena.travelmemories3.DataBase.UserFunctions;
import com.mdb_software.helena.travelmemories3.model.ListViewPlaces;
import com.mdb_software.helena.travelmemories3.model.ListViewTrips;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class TripsActivity extends ActionBarActivity {

    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private ListViewTripsAdapter adapter;
    private List<ListViewTrips> listaPutovanja = new ArrayList<ListViewTrips>();
    private List<ListViewPlaces> listaMjesta = new ArrayList<ListViewPlaces>();
    private String title, imageUrl;
    private Date startDate, endDate;
    private ProgressDialog pDialog;
    private int putovanjeID, brojLokacija;
    private String id;
    private SharedPreferences prefs;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView listView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        //Widgets
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        listView = (ListView)findViewById(android.R.id.list);

        //Set toolbar
        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("   Trips");


        provjeriLogin();

        postaviAdapter();

        showPDialog();
        if (isOnline()) {
            dohvatiIzBaze();
        }else{
            Toast.makeText(this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
        }
        hidePDialog();

        //Set Map Drawer
        MapDrawerFragment drawerFragment = (MapDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_map_drawer);
        drawerFragment.setUp(R.id.fragment_map_drawer, (DrawerLayout)findViewById(R.id.drawer_layout), toolbar, listaMjesta);

        //swipeRefresh Listener
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               refreshList();
            }
        });
    }

    /**
     * Provjerava je li lista prazna. Ako je prikazuje poruku.
     */
    private void listItemsSize() {
        if (listaPutovanja.size()==0){
            TextView errorTxt = (TextView)findViewById(R.id.errorTxt);
            LinearLayout crta = (LinearLayout)findViewById(R.id.crtaLayout);
            crta.setVisibility(View.INVISIBLE);
            errorTxt.setText("No trips added. To add trip press the plus button below.");
            errorTxt.setBackgroundColor(getResources().getColor(R.color.popuniSvaPolja));
            errorTxt.setBackgroundColor(getResources().getColor(R.color.popuniSvaPolja));
        }
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
     * Pomocu preferenca provjerava je li korisnik prijavljen
     * @return
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pozoviIntent(position);
            }
        });
        adapter = new ListViewTripsAdapter(this, listaPutovanja);
        listView.setAdapter(adapter);

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
     * Postavlja adapter s mogucnoscu brisanja putovanja
     * @param row
     */
    private void postaviDialog(final int row) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.delete));
        dialogBuilder.setMessage(getString(R.string.deleteTrip));
        dialogBuilder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 izbrisiIzBaze(row);
            }
        });
        dialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Delete
            }
        });
        dialogBuilder.create().show();
    }

    /**
     * Prosljeduje zahtjev za brisanjem putovanja u bazi i parsira odgovor
     * @param row
     */
    private void izbrisiIzBaze(int row){
        ListViewTrips item= listaPutovanja.get(row);
        int tripID=item.getID();
        UserFunctions userFunctions = new UserFunctions();
        try {
            JSONObject json = null;
            json = userFunctions.delete(this, "deleteTrip", tripID);
            if (json.getString(KEY_SUCCESS) != null) {
                String res = json.getString(KEY_SUCCESS);
                if (Integer.parseInt(res) == 1) {
                    // Podaci su uspjesno dohvaceni
                    listaPutovanja.remove(row);
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
     * Ako nije ulogiran baci na login screen, inace spremi user id.
     */
    private void provjeriLogin() {
        //Preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean Islogin = prefs.getBoolean("Islogin", false); // get value of last login status
        if(Islogin)
        {   // condition true means user is already login
            id = prefs.getString("uid", null); // get value of last login status
        }
        else
        {
            Intent i = new Intent(this, EmailLoginActivity.class);
            startActivityForResult(i, 1);
        }
    }


    /**
     * Dohvaca podatke iz baze preko JSON-a.
     */
    private void dohvatiIzBaze() {
        UserFunctions userFunctions = new UserFunctions();
        try {
            JSONObject json = null;
            json = userFunctions.getTrips(this, id);
            if (json.getString(KEY_SUCCESS) != null) {
                String res = json.getString(KEY_SUCCESS);
                if (Integer.parseInt(res) == 1) {
                    // Podaci su uspjesno dohvaceni
                    parsirajPodatke(json);
                    listItemsSize();

                    adapter.notifyDataSetChanged();
                }else if (Integer.parseInt(json.getString(KEY_ERROR)) ==1){
                    listItemsSize();

                }//end if
            }
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }catch (NullPointerException e1){
            e1.printStackTrace();
        }
    }

    /**
     * Parsira podatke iz baze i sprema ih u listuPutovanja.
     * @param json
     */
    private void parsirajPodatke(JSONObject json) {
        JSONArray recs = null;
        JSONArray places = null;
        listaPutovanja.clear();
        listaMjesta.clear();
        try {
            recs = json.getJSONArray("arrayTrips");
            places = json.getJSONArray(("arrayPlaces"));

            for (int i=0; i<recs.length();i++) {
                JSONObject rec = recs.getJSONObject(i);
                title = (rec.getString("putovanje"));
                imageUrl = (rec.getString("url"));
                putovanjeID = (rec.getInt("putovanjeID"));
                startDate = parsirajDatum(rec.getString("startDate"));
                endDate = parsirajDatum(rec.getString("endDate"));
                brojLokacija = rec.getInt("brojLokacija");
                // adding item to list
                listaPutovanja.add(new ListViewTrips(imageUrl, title, startDate, endDate, putovanjeID, brojLokacija));
            }//end for
            for (int i=0; i<places.length();i++){
                //Dohvaca markere svih putovanja za prikaz na karti
                JSONObject place = places.getJSONObject(i);
                listaMjesta.add(new ListViewPlaces("", place.getString("Lokacija"),
                        parsirajDatum(place.getString("Datum")), place.getInt("markerID"), place.getDouble("Longitude"),
                        place.getDouble("Latitude")));
            }
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
        ListViewTrips item;
        item=listaPutovanja.get(position);
        Intent i = new Intent (this, PlacesActivity.class);
        i.putExtra("PutovanjeID", item.getID());
        i.putExtra("imePutovanja",item.getTitle());
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

    }
}
