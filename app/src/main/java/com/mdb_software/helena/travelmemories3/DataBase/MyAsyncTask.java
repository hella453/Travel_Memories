package com.mdb_software.helena.travelmemories3.DataBase;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.mdb_software.helena.travelmemories3.Interface.CallbackReciever;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Helena on 4/6/2015.
 */
public class MyAsyncTask extends AsyncTask<List<NameValuePair>,  String, JSONObject> implements CallbackReciever {
    private static String URL = "http://hella.mdb-software.com/android_travel_memories/index_android.php";
    private ProgressDialog pDialog;
    private Context context;

    public MyAsyncTask(Context cxt) {
        context = cxt;
        pDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        pDialog.setMessage("Loading ...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }
    @Override
    protected JSONObject doInBackground( List<NameValuePair>... args) {
        JSONParser jParser = new JSONParser();
        // Getting JSON from URL

        List <NameValuePair> passed = args[0];
        JSONObject json = jParser.getJSONFromUrl(URL, passed);
        return json;
    }

    @Override
    protected void onPostExecute(JSONObject json) {
        pDialog.dismiss();

    }

    @Override
    public void receiveData(Object result) {

    }
}