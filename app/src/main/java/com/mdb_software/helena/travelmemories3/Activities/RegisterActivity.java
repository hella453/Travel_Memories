package com.mdb_software.helena.travelmemories3.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.Spinners;
import com.mdb_software.helena.travelmemories3.DataBase.CheckRegisterInputs;
import com.mdb_software.helena.travelmemories3.DataBase.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


public class RegisterActivity extends ActionBarActivity {

    private TextView inputPassword, inputKontakt, inputName, inputEmail, errorMessage;
    private String name, pass, email, result;

    // JSON Response node varijable
    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static String KEY_NAME = "name";
    private static String KEY_USER = "user";
    private static String KEY_USERID = "uid";
    private ImageView emailIcon, personIcon, keyIcon, callIcon;
    private Toolbar toolbar;
    private Spinner brojevi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("   Registration");

        //Resizes ScrollView's height when keyboard is on
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // home bttn
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //widgets
        inputName = (TextView)findViewById(R.id.imeTxt);
        inputKontakt = (TextView)findViewById(R.id.telTxt);
        inputEmail = (TextView)findViewById(R.id.registerEmail);
        inputPassword = (TextView)findViewById(R.id.registerPassTxt);
        personIcon=(ImageView)findViewById(R.id.iconPerson);
        emailIcon=(ImageView)findViewById(R.id.iconEmail);
        callIcon=(ImageView)findViewById(R.id.iconCall);
        keyIcon=(ImageView)findViewById(R.id.iconKey);
        errorMessage = (TextView)findViewById(R.id.errorTxt);

        //Fokus ikona
        checkFocus();

        //spinner
        brojevi =(Spinner) findViewById(R.id.registerSpinner);
        Spinners.setSpinner(getApplication(), brojevi, "pozivniBroj_array");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                finish();
                return true;
            case R.id.registerIcon:
                try {
                    registerUser();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Registrira korisnika u bazi
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws JSONException
     */
    public void registerUser() throws ExecutionException, InterruptedException, JSONException {
        name = inputName.getText().toString();
        email = inputEmail.getText().toString();
        pass = inputPassword.getText().toString();
        //Check inputs
        CheckRegisterInputs check = new CheckRegisterInputs();
        result = check.checkInputs(name, email, pass);
        if (result.equals("true")) {
            // Provjeri login response
            try {
                UserFunctions userFunction = new UserFunctions();
                JSONObject json = userFunction.registerUser(this, name, email, pass);
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);
                    if (Integer.parseInt(res) == 1) {
                        // Korisnik uspjesno spremljen

                        JSONObject json_user = json.getJSONObject(KEY_USER);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                        prefs.edit().putBoolean("Islogin", true).commit(); // islogin is a boolean value of your login status
                        prefs.edit().putString("uid", json_user.getString(KEY_USERID)).commit();
                        // Otvori Login Screen
                        Intent i = new Intent(this,TripsActivity.class);
                        i.putExtra(KEY_USER, json_user.getString(KEY_NAME));
                        // Zatvori sve ostalo
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        // Zatvori Registration Screen
                        finish();
                    } else if (json.getString(KEY_ERROR) != null) {
                        errorMessage.setText(json.getString(KEY_ERROR_MSG));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            errorMessage.setText(result);
        }

    }

    /**
     * Provjerava fokus textview-ova i mjenja boju ikone
     */
    private void checkFocus() {
        //checkFocus
        inputName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    personIcon.setImageResource(R.drawable.ic_person_blue);
                } else {
                    personIcon.setImageResource(R.drawable.ic_person_grey);
                }
            }
        });

        inputEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    emailIcon.setImageResource(R.drawable.ic_mail_blue);
                } else {
                    emailIcon.setImageResource(R.drawable.ic_mail_grey);
                }
            }
        });

        inputKontakt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    callIcon.setImageResource(R.drawable.ic_call_blue);
                } else {
                    callIcon.setImageResource(R.drawable.ic_call_grey);
                }
            }
        });


        inputPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    keyIcon.setImageResource(R.drawable.ic_vpn_key_blue);
                } else {
                    keyIcon.setImageResource(R.drawable.ic_vpn_key_grey);
                }
            }
        });
    }

}
