package com.mdb_software.helena.travelmemories3.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.mdb_software.helena.travelmemories3.DataBase.UserFunctions;
import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.adapters.GridViewGalleryAdapter;
import com.mdb_software.helena.travelmemories3.adapters.TwoWayListAdapter;
import com.mdb_software.helena.travelmemories3.adapters.ViewPagerAdapter;
import com.mdb_software.helena.travelmemories3.model.GridViewPhotos;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class GalleryActivity extends ActionBarActivity {

    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private List<GridViewPhotos> fotke = new ArrayList<GridViewPhotos>();
    private int markerId, photoId, putovanjeId;
    private String photoUrl;
    private GridViewGalleryAdapter adapter;
    private Bitmap bitmap, slikaZaUpload;
    private File mediaStorageDir;
    private List<String> listaUrla = new ArrayList<String>();
    private ArrayList<Bitmap> items = new ArrayList<Bitmap>();
    private static final String IMAGE_DIRECTORY_NAME = "Travel Memories";
    private static final String TAG = NewPlaceActivity.class.getSimpleName();
    private String id, imeSlike;
    private SharedPreferences prefs;
    private TwoWayListAdapter adapterTwoWay;
    private UserFunctions userFunctions = new UserFunctions();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        //Get Extra
        markerId = getIntent().getIntExtra("markerId", -1);
        putovanjeId = getIntent().getIntExtra("putovanjeId", -1);

        //Get User Id
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean Islogin = prefs.getBoolean("Islogin", false); // get value of last login status
        if (Islogin) {   // condition true means user is already login
            id = prefs.getString("uid", null); // get value of last login status
        }
        //Swipe refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });

        //Set adapter
        setAdapter();

        dohvatiIzBaze();
    }

    /**
     * Postavlja grid view adapter
     */
    private void setAdapter() {
        GridView photoGridView = (GridView) findViewById(R.id.photoGridView);
        photoGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                makeViewPageDialog(position);
            }
        });
        photoGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                postaviDijalog(position);
                return true;
            }
        });
        adapter = new GridViewGalleryAdapter(this, fotke);

        photoGridView.setAdapter(adapter);
    }

    /**
     * Postavlja dijalog s mogucnoscu brisanja fotografije
     *
     */
    private void postaviDijalog(final int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.delete));
        dialogBuilder.setMessage(getString(R.string.deleteTrip));
        dialogBuilder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GridViewPhotos item = fotke.get(position);
                try {
                    izbrisiIzBaze(item);
                }catch(Exception e){

                }
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
     * Brise sliku iz baze
     * @param item
     */
    private void izbrisiIzBaze(GridViewPhotos item) {
        UserFunctions uf = new UserFunctions();
        uf.deletePhoto(this, item.getPhotoID());
        refreshList();
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
        }, 2000);
    }

    /**
     * Otvara dijalog za pregled slika preko View pager adaptera
     * @param position
     */
    private void makeViewPageDialog(int position) {
        Dialog d = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        Window window = d.getWindow();
        d.setContentView(R.layout.view_pager_dialog);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, fotke);
        final ViewPager myPager = (ViewPager)d. findViewById(R.id.myfivepanelpager);
        myPager.setAdapter(adapter);
        myPager.setCurrentItem(position);
        d.show();
    }


    /**
     * Dohvaca slike iz baze
     */
    private void dohvatiIzBaze() {
        UserFunctions userFunctions = new UserFunctions();
        try {
            JSONObject json = null;
            json = userFunctions.getPhotos(this, String.valueOf(markerId));
            if (json.getString(KEY_SUCCESS) != null) {
                String res = json.getString(KEY_SUCCESS);
                if (Integer.parseInt(res) == 1) {
                    // Podaci su uspjesno dohvaceni
                    parsirajPodatke(json);
                    adapter.notifyDataSetChanged();
                }//end if
            } else if (json.getString(KEY_ERROR) != null) {
                TextView errorTxt = (TextView) findViewById(R.id.errorPlacesTxt);
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
     * @param json
     */
    private void parsirajPodatke(JSONObject json) {
        JSONArray recs = null;
        fotke.clear();
        try {
            recs = json.getJSONArray("arrayRezultati");
            ListViewPlaces item = new ListViewPlaces();
            for (int i=0; i<recs.length();i++) {
                JSONObject rec = recs.getJSONObject(i);
                photoId = (rec.getInt("photoId"));
                photoUrl = (rec.getString("photoUrl"));
                // adding item to list
                fotke.add(new GridViewPhotos(photoId, photoUrl));
            }//end for
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
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
     * Izraduje horizontalnu listu za slike pomocu vanjskog libraryja
     */
    private void izradiListuZaSlike() {
        int resourceId = this.getResources().getIdentifier("two_way_list_item", "layout", getPackageName());
        adapterTwoWay = new TwoWayListAdapter(this, items, resourceId);
        TwoWayView listView = (TwoWayView) findViewById(R.id.twoWayList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                makeViewPageDialog(position);

            }
        });
        listView.setAdapter(adapterTwoWay);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                postaviDialog(position);

                return true;
            }
        });
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
                adapterTwoWay.notifyDataSetChanged();

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
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                    byte[] byte_arr = stream.toByteArray();
                    userFunctions.uploadajSliku(this, byte_arr, String.valueOf(markerId), String.valueOf(putovanjeId), id, 0, 0);
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
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                byte[] byte_arr = stream.toByteArray();
                userFunctions.uploadajSliku(this, byte_arr, String.valueOf(markerId), String.valueOf(putovanjeId), id, 0, 0);
                refreshList();
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

}
