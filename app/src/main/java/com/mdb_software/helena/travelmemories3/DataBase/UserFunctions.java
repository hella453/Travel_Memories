/**
 * Author: Helena
 * */
package com.mdb_software.helena.travelmemories3.DataBase;

import android.content.Context;
import android.util.Base64;

import com.mdb_software.helena.travelmemories3.Activities.EmailLoginActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UserFunctions {

    private JSONParser jsonParser;
    public static UserFunctions userFunctions = new UserFunctions();
    private static String login_tag = "login";
    private static String register_tag = "register";
    private static String logout_tag = "logout";
    private static String trips_tag = "trips";
    private static String places_tag = "places";



    // constructor
    public UserFunctions(){
        jsonParser = new JSONParser();
    }

    /**
     * Metoda radi Login Request
     * @param phone
     * @param password
     * */
    public JSONObject loginUser(Context ctx, String phone, String password) throws ExecutionException, InterruptedException {
        // Building Parameters
        Context context = null;
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", login_tag));
        params.add(new BasicNameValuePair("phone", phone));
        params.add(new BasicNameValuePair("password", password));
        EmailLoginActivity em = new EmailLoginActivity();
        JSONObject json = new MyAsyncTask(ctx).execute(params).get();
        return json;
    }

    /**
     * Salje upit async task-u za dohvacanje putovanja
     * @param ctx
     * @param userId
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public JSONObject getTrips(Context ctx, String userId) throws ExecutionException, InterruptedException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", trips_tag));
        params.add(new BasicNameValuePair("userId", userId));
        JSONObject json = new MyAsyncTask(ctx).execute(params).get();
        return json;

    }

    /**
     * Pokusaj pozivanja AsyncTaska i dohvacanje odgovora pomocu interfaca - ne radi
     * @param ctx
     * @param putovanjeId
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    /*   private JSONObject getResult(Context ctx, List<NameValuePair> params) {

  /*     AsyncTsk callbackservice=new AsyncTsk(ctx) {
              @Override
              public void receiveData(JSONObject object) {
                 json = object;

              }
          };
          callbackservice.execute(params);
          return json;
      }
  */

    /**
     * Salje upit async task-u za dohvacanje svih mjesta odredjenog putovanja
     * @param ctx
     * @param putovanjeId
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public JSONObject getPlaces(Context ctx, String putovanjeId) throws ExecutionException, InterruptedException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", places_tag));
        params.add(new BasicNameValuePair("putovanjeId", putovanjeId));
        JSONObject json = new MyAsyncTask(ctx).execute(params).get();
        return json;

    }

    /**
     * Salje upit async task-u za dohvacanje detalja i slika za neko mjesto
     * @param ctx
     * @param markerId
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public JSONObject getPlaceDetails(Context ctx, String markerId) throws ExecutionException, InterruptedException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", "placeDetails"));
        params.add(new BasicNameValuePair("markerId", markerId));
        JSONObject json = new MyAsyncTask(ctx).execute(params).get();
        return json;
    }

    /**
     * salj zahtjev za uploada fotke na server
     * @param ctx
     * @param byte_arr
     * @param markerId
     * @param putovanjeId
     * @param id
     * @param tripCover
     * @param markerCover
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void uploadPhotos(Context ctx, byte[] byte_arr,String markerId, String putovanjeId, String id, int tripCover, int markerCover) throws ExecutionException, InterruptedException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("tag", "uploadPhotos"));
        nameValuePairs.add(new BasicNameValuePair("base64", Base64.encodeToString(byte_arr, 0)));
        String fileName = System.currentTimeMillis() + "_" + id + ".jpg";
        nameValuePairs.add(new BasicNameValuePair("ImageName", fileName));
        nameValuePairs.add(new BasicNameValuePair("markerId", markerId));
        nameValuePairs.add(new BasicNameValuePair("putovanjeId", putovanjeId));
        nameValuePairs.add(new BasicNameValuePair("markerCover", String.valueOf(markerCover)));
        nameValuePairs.add(new BasicNameValuePair("tripCover", String.valueOf(tripCover)));
        new MyAsyncTask(ctx).execute(nameValuePairs);

    }

    /**
     * Salje zahtjev sa podacia za spremanje u bazu
     * @param ctx
     * @param tripName
     * @param flag
     * @param userId
     * @param biljeska
     * @param longitude
     * @param latitude
     * @param datum
     * @param vrijeme
     * @param location
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public JSONObject InsertData(Context ctx, String tripName,boolean flag, String userId, String biljeska,
                                 double longitude, double latitude, String datum, String vrijeme, String location )
            throws ExecutionException, InterruptedException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("tag", "insertData"));
        //nameValuePairs.add(new BasicNameValuePair("imeSlike", imeSlike));
        nameValuePairs.add(new BasicNameValuePair("userId", userId));
        nameValuePairs.add(new BasicNameValuePair("biljeska", biljeska));
        nameValuePairs.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));
        nameValuePairs.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
        nameValuePairs.add(new BasicNameValuePair("tripName", tripName));
        nameValuePairs.add(new BasicNameValuePair("flag", String.valueOf(flag)));
        nameValuePairs.add(new BasicNameValuePair("datum",datum));
        nameValuePairs.add(new BasicNameValuePair("vrijeme",vrijeme));
        nameValuePairs.add(new BasicNameValuePair("location",location));
        JSONObject json = new MyAsyncTask(ctx).execute(nameValuePairs).get();

        return json;

    }

    public boolean uploadajSliku(Context ctx, byte[] byte_arr, String markerId, String putovanjeId, String id, int tripCover, int markerCover) {

        try {

            uploadPhotos(ctx, byte_arr, markerId,putovanjeId, id, tripCover, markerCover);
            return true;
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    /**
     * Zahtjev za dohvacanjem slika sa servera
     * @param ctx
     * @param markerId
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public JSONObject getPhotos(Context ctx, String markerId) throws ExecutionException, InterruptedException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", "photos"));
        params.add(new BasicNameValuePair("markerId", markerId));
        JSONObject json = new MyAsyncTask(ctx).execute(params).get();
        return json;
    }


    /**
     * Zahtjev za registacijom
     * @param name
     *
     * @param password
     * */
    public JSONObject registerUser(Context ctx, String name, String email, String password) throws ExecutionException, InterruptedException {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", register_tag));
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));
        JSONObject json = new MyAsyncTask(ctx).execute(params).get();
        return json;
    }

    /**
     * Zahtjev za brisanjem putovanja ili mjesta iz baze
     * @param ctx
     * @param tag
     * @param id
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public JSONObject delete(Context ctx, String tag, int id) throws ExecutionException, InterruptedException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", tag));
        params.add(new BasicNameValuePair("tripID", String.valueOf(id)));

        JSONObject json = new MyAsyncTask(ctx).execute(params).get();

        return json;
    }


    public void deletePhoto(Context ctx, int photoID) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", "deletePhoto"));
        params.add(new BasicNameValuePair("photoID", String.valueOf(photoID)));

        new MyAsyncTask(ctx).execute(params);
    }
}
