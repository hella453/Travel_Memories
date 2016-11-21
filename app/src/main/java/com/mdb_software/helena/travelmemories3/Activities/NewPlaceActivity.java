package com.mdb_software.helena.travelmemories3.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mdb_software.helena.travelmemories3.DataBase.UserFunctions;
import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.adapters.TwoWayListAdapter;
import com.mdb_software.helena.travelmemories3.adapters.ViewPagerAdapter2;
import com.mdb_software.helena.travelmemories3.model.ListViewPlaces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lucasr.twowayview.TwoWayView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


public class NewPlaceActivity extends ActionBarActivity implements LocationListener {
    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static final String TAG = NewPlaceActivity.class.getSimpleName();
    private static final String IMAGE_DIRECTORY_NAME = "Travel Memories";

    private ArrayList<Bitmap> items = new ArrayList<Bitmap>();
    private List<String> listaPutovanja = new ArrayList<String>();
    private List<String> listaUrla = new ArrayList<String>();

    private TwoWayListAdapter adapter;
    private ViewPagerAdapter2 adapter1;

    private SharedPreferences prefs;
    private Bitmap bitmap, slikaZaUpload;
    private File mediaStorageDir;

    private String id, title, imeSlike, tripName, datum, vrijeme;
    private int putovanjeID, currentItem;
    private double longi, lati;
    boolean flag = false;
    private UserFunctions userFunctions = new UserFunctions();

    private EditText biljeska, lokacija, newTrip;;
    private TextView textPutovanje, locationTv;
    private ImageView transparentImageView;

    private Spinner ongoingTrip;
    private Toolbar toolbar;
    private Switch switchTrip;
    private GoogleMap googleMap;
    private ScrollView mainScrollView;
    private Marker marker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_place);

        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("   New place");

        //Resizes ScrollView's height when keyboard is on
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // home bttn
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        checkLogin();

        //Widgets
        switchTrip = (Switch) findViewById(R.id.switchTrip);
        newTrip = (EditText) findViewById(R.id.putovanjeTxt);
        ongoingTrip = (Spinner) findViewById(R.id.spinnerPutovanja);
        textPutovanje = (TextView) findViewById(R.id.textPutovanje);
        lokacija = (EditText) findViewById(R.id.addLocationTxt);
        mainScrollView = (ScrollView)findViewById(R.id.scrollNewPlace);
        transparentImageView = (ImageView) findViewById(R.id.transparent_image);
        locationTv = (TextView) findViewById(R.id.textLokacijaa);
        biljeska = (EditText) findViewById(R.id.biljeskaTxt);

        //Horizontalna lista za slike
        izradiListuZaSlike();

        //Poboljsaj scrool
        improveScroll();

        //Odaberi izmedju novog i postojeceg putovanja
        switchChanged();

        //Napuni spinner sa putovanjima iz baze
        dohvatiIzBaze();
        napuniSpinner();

        //Postavi Google Maps
        setGoogleMaps();

        //Podesi datum i vrijeme
        datum = (new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime()));
        vrijeme = (new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()));

    }//end of onCreate()

    /**
     * Provjerava je li korisni prijavljen u aplikaciju pomocu preferemnce managera
     */
    private void checkLogin() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean Islogin = prefs.getBoolean("Islogin", false); // get value of last login status
        if (Islogin) {   // condition true means user is already login
            id = prefs.getString("uid", null); // get value of last login status
        }
    }

    /**
     * Puni spinner sa putovanjima iz baze
     */
    private void napuniSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, listaPutovanja);
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        if (listaPutovanja.size() > 0) {
            ongoingTrip.setAdapter(adapter);
            ongoingTrip.setSelection(0);
        } else {
            switchTrip.setChecked(true);
            flag = true;
            textPutovanje.setText(getString(R.string.newTrip));
            newTrip.setVisibility(View.VISIBLE);
            ongoingTrip.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Odabir izmedju novog i postojeceg putovanja
     */
    private void switchChanged() {
        switchTrip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (listaPutovanja.size() > 0) {
                    if (isChecked) {
                        flag = true;
                        textPutovanje.setText(getString(R.string.newTrip));
                        newTrip.setVisibility(View.VISIBLE);
                        ongoingTrip.setVisibility(View.INVISIBLE);
                    } else {
                        textPutovanje.setText(getString(R.string.existingTrip));
                        newTrip.setVisibility(View.INVISIBLE);
                        flag = false;
                        ongoingTrip.setVisibility(View.VISIBLE);

                    }
                } else {
                    switchTrip.setChecked(true);
                }
            }
        });
    }
    /**
     * Omogucava pomicanje google mapsa gore-dolje koji je unutar scroll view-a
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
     * Izraduje horizontalnu listu za slike pomocu vanjskog libraryja
     */
    private void izradiListuZaSlike() {
        int resourceId = this.getResources().getIdentifier("two_way_list_item", "layout", getPackageName());
        adapter = new TwoWayListAdapter(this, items, resourceId);
        TwoWayView listView = (TwoWayView) findViewById(R.id.twoWayList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                makeViewPageDialog(position);

            }
        });
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                postaviDialog(position);

                return true;
            }
        });
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * Dohvaca google map fragment i postavlja marker
     */
    private void setGoogleMaps() {
        final LatLng TutorialsPoint = new LatLng(21 , 53);

        try {
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
           // Marker marker = googleMap.addMarker(new MarkerOptions().
               //    position(starting).title("bok"));
          // marker.showInfoWindow();
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {
                    longi=marker.getPosition().longitude;
                    lati = marker.getPosition().latitude;
                    locationTv.setText("Latitude:" + longi + ", Longitude:" + lati);

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    longi=marker.getPosition().longitude;
                    lati = marker.getPosition().latitude;
                    locationTv.setText("Latitude:" + longi + ", Longitude:" + lati);



                }
            });
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                onLocationChanged(location);
            }

        }


        catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Na promjenu lokacije dodaje pomice marker
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        lati = location.getLatitude();
        longi = location.getLongitude();
        LatLng latLng = new LatLng(lati, longi);

        marker = googleMap.addMarker(new MarkerOptions().
                position(latLng).draggable(true));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        locationTv.setText("Latitude:" + longi + ", Longitude:" + lati);
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

                }//end if
            } else if (json.getString(KEY_ERROR) != null) {
                TextView errorTxt = (TextView) findViewById(R.id.errorTxt);
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
     * Parsira podatke iz baze i sprema ih u listuPutovanja.
     *
     * @param json
     */
    private void parsirajPodatke(JSONObject json) {
        JSONArray recs = null;
        try {
            recs = json.getJSONArray("arrayTrips");
            ListViewPlaces item = new ListViewPlaces();
            for (int i = 0; i < recs.length(); i++) {
                JSONObject rec = recs.getJSONObject(i);
                title = (rec.getString("putovanje"));

                putovanjeID = (rec.getInt("putovanjeID"));

                // adding item to list
                listaPutovanja.add(title);
            }//end for
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Izraduje ViewPage dialog za listanje fotografija dodanih u aplikaciju
     *
     * @param position
     */
    private void makeViewPageDialog(int position) {
        Dialog d = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        Window window = d.getWindow();
        d.setContentView(R.layout.view_pager_dialog);
        adapter1 = new ViewPagerAdapter2(this, items);
        final ViewPager myPager = (ViewPager) d.findViewById(R.id.myfivepanelpager);
        myPager.setAdapter(adapter1);
        myPager.setCurrentItem(position);
        currentItem = myPager.getCurrentItem();
        d.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_place, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.spremiMjesto:
                try {
                    upload();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Otvara AlertDialog sa tri opcije: Slikaj s kamerom, odaberi iz galerije, odustani
     *
     * @param view
     */
    public void onClickPhoto(View view) {
        final CharSequence[] options = {"Slikaj s kamerom", "Odaberi iz galerije", "Odustani"};
        AlertDialog.Builder builder = new AlertDialog.Builder(NewPlaceActivity.this);
        builder.setTitle("Dodajte fotografiju!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Slikaj s kamerom")) {
                    slikajSKamerom();
                } else if (options[item].equals("Odaberi iz galerije"))

                {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("Odustani")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    /**
     * Stvara direktorij za slike ukoliko ne postoji, stvara jedinstveno ime slike i poziva ACTION_IMAGE_CAPTURE intent
     */
    private void slikajSKamerom() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // External sdcard location
        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
            }
        }
        // Create a file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        imeSlike = "IMG_" + timeStamp + "_" + id + ".jpg";

        //Create file
        File f = new File(mediaStorageDir, imeSlike);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        startActivityForResult(intent, 1);

    }

    /**
     * Izraduje bitmap od slike odabrane iz galerije ili snimljene kamerom
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //Ako je slikana
            if (requestCode == 1) {
                File f = new File(mediaStorageDir.toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals(imeSlike)) {
                        f = temp;
                        break;
                    }
                }
                try {
                    listaUrla.add(f.getPath().toString());
                    bitmap = scalePhoto(f, 570, 480);
                    items.add(0, bitmap);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Ako je iz galerije
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                listaUrla.add(picturePath);
                c.close();
                File fil = new File(picturePath);
                bitmap = scalePhoto(fil, 352, 240);
                items.add(0, bitmap);
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Skalira bitmap
     *
     * @param f
     * @param WIDTH
     * @param HIGHT
     * @return
     */
    public static Bitmap scalePhoto(File f, int WIDTH, int HIGHT) {
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            //The new size we want to scale to
            final int REQUIRED_WIDTH = WIDTH;
            final int REQUIRED_HIGHT = HIGHT;
            //Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_WIDTH && o.outHeight / scale / 2 >= REQUIRED_HIGHT)
                scale *= 2;
            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    /**
     * Postavlja dijalog za mogucnost brisanja slika iz horizontalne liste
     * @param row
     */
    private void postaviDialog(final int row) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.delete));
        dialogBuilder.setMessage(getString(R.string.deletePhoto));
        dialogBuilder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                items.remove(row);
                listaUrla.remove(listaUrla.size()-1-row);
                adapter.notifyDataSetChanged();

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
     * Prosljeduje podatke prema bazi, formatira slike u byte array
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws JSONException
     */
    public void upload() throws ExecutionException, InterruptedException, JSONException {
        BitmapFactory.Options options = null;
        options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        if (flag == true) {
            tripName = newTrip.getText().toString();
        } else {
            tripName = ongoingTrip.getSelectedItem().toString();
        }
        String note = biljeska.getText().toString();
        String location = lokacija.getText().toString();
        if (checkInputs(tripName, note, location)) {

            JSONObject json = userFunctions.InsertData(this, tripName, flag, id, note,
                    longi, lati, datum, vrijeme, location);
            String markerId = json.getString("markerId");
            String putovanjeId = json.getString("putovanjeId");
            int tripCover = 1;
            int markerCover = 1;
            if (flag == false) {
                tripCover = 0;
            }
            for (int i = 0; i < listaUrla.size(); i++) {
                slikaZaUpload = scalePhoto(new File(listaUrla.get(i)), 352, 240);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                slikaZaUpload.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                byte[] byte_arr = stream.toByteArray();
                userFunctions.uploadajSliku(this, byte_arr, markerId, putovanjeId, id, tripCover, markerCover);
                tripCover = 0;
                markerCover = 0;
            }
            Intent i = new Intent(this, TripsActivity.class);
            startActivity(i);
            finish();
        }
    }

    /**
     * Provjerava jesu li popunjena sva polja u obrascu
     * @param tripName
     * @param note
     * @param location
     * @return
     */
    private boolean checkInputs(String tripName, String note, String location) {
        String errMsg;
        if (tripName != null && !tripName.isEmpty()  && note != null && !note.isEmpty() && location != null && !location.isEmpty()) {
            if (listaUrla.size() > 0) {
                return true;
            } else {
                errMsg = "Please add a photo!";
            }
        }else{
                errMsg="Please fill all fields!";
        }
        Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show();
        return false;
    }


}
