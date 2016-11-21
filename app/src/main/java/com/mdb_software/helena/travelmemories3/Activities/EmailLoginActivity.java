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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.DataBase.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class EmailLoginActivity extends ActionBarActivity {

    // JSON Response node varijable
    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static String KEY_USER = "user";
    private static String KEY_USERID = "uid";
    private Toolbar toolbar;

    private EditText inputEmail, inputPass;
    private TextView loginErrorMessage;
    private String email, password;
    private ImageView emailIcon, keyIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inputEmail = (EditText)findViewById(R.id.loginEmail);
        inputPass = (EditText)findViewById(R.id.loginPassTxt);
        loginErrorMessage = (TextView)findViewById(R.id.loginErrorMessage);
        emailIcon=(ImageView)findViewById(R.id.loginIconEmail);
        keyIcon=(ImageView)findViewById(R.id.loginIconKey);

        //Set toolbar
        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("   Login");

        //Keyboard problems
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        checkFocus();

    }//End onCreate()

    /**
     * Provjerava fokus textvieova te sukladno s time mjenja boju ikona
     */
    private void checkFocus() {
        inputPass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    keyIcon.setImageResource(R.drawable.ic_vpn_key_blue);
                } else {
                    keyIcon.setImageResource(R.drawable.ic_vpn_key_grey);
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
    }

    /**
     * Prijavljuje korisnika u aplikaciju
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws JSONException
     */
    public void loginClick() throws ExecutionException, InterruptedException, JSONException {
        email = inputEmail.getText().toString();
        password = inputPass.getText().toString();
        UserFunctions userFunction = new UserFunctions();
        JSONObject json = userFunction.loginUser(this, email, password);
        //  Provjeri login response
        try {
            if (json.getString(KEY_SUCCESS) != null) {
                String res = json.getString(KEY_SUCCESS);
                if(Integer.parseInt(res) == 1){
                    // Korisnik je uspjesno ulogiran

                    JSONObject json_user = json.getJSONObject(KEY_USER);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    prefs.edit().putBoolean("Islogin", true).commit();
                    prefs.edit().putString("uid", json_user.getString(KEY_USERID)).commit();

                    // Pokreni Dashboard Screen
                    Intent i = new Intent(getApplicationContext(), TripsActivity.class);

                    // Zatvori sve ostalo
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);

                    // Zatvori Login Screen
                    finish();
                }else if (json.getString(KEY_ERROR) != null){
                    loginErrorMessage.setText(json.getString(KEY_ERROR_MSG));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), "Server unavailable!", Toast.LENGTH_LONG).show();
            finish();
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_email_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.loginIcon:
                try {
                    loginClick();
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
     * Otvara aktivnost za registraciju
     * @param view
     */
    public void registerClick(View view)
    {
        Intent i = new Intent(EmailLoginActivity.this, RegisterActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);


    }
    //Facebook login
    public void facebookLogin(View view) {

        Toast.makeText(getApplicationContext(),getString(R.string.FacebookPrijava),Toast.LENGTH_LONG ).show();
    }
    //Google + login
    public void googleLogin(View view) {
        Toast.makeText(getApplicationContext(),getString(R.string.GooglePrijava),Toast.LENGTH_LONG ).show();
    }
    //Twitter login
    public void twitterLogin(View view) {
        Toast.makeText(getApplicationContext(),getString(R.string.twitterPrijava),Toast.LENGTH_LONG ).show();
    }

}
