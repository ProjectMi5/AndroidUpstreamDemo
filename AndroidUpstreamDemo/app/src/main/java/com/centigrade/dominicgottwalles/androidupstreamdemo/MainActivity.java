package com.centigrade.dominicgottwalles.androidupstreamdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class MainActivity extends Activity {

    GoogleCloudMessaging gcmObj;
    Context applicationContext;
    String regId = "";

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String REG_ID = "regId";
    public static final String MESSAGE_ID = "messageId";
    public static final String SHARED_PREF_NAME = "UserDetails";

    private Button button;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applicationContext = getApplicationContext();

        button = (Button)findViewById(R.id.button);
        button.setActivated(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if(text.equals("")){
                    Toast.makeText(applicationContext, "Please insert text!", Toast.LENGTH_LONG).show();
                }
                else{
                    editText.setText("");
                    sendUpstreamMessageInBackground(text);
                }
            }
        });
        editText = (EditText) findViewById(R.id.editText);

        SharedPreferences prefs = getSharedPreferences(SHARED_PREF_NAME,
                Context.MODE_PRIVATE);
        String registrationId = prefs.getString(REG_ID, "");
        if(checkPlayServices()) {
            Log.d("PlayServices", "available");
            if (registrationId.equals("")) {
                registerInBackground();
            }
            else{
                Log.d("RegId", registrationId);
                Toast.makeText(this, "RegId found", Toast.LENGTH_LONG).show();
                button.setActivated(true);
            }
        }
        else{
            Log.d("PlayServices", "not available");
            Toast.makeText(this, "Google Play Services are not available on this device", Toast.LENGTH_LONG).show();
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcmObj == null) {
                        gcmObj = GoogleCloudMessaging
                                .getInstance(applicationContext);
                    }
                    regId = gcmObj
                            .register(getResources().getString(R.string.project_id));
                    msg = "Registration ID :" + regId;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!regId.equals("")) {
                    storeRegIdInSharedPref(regId);
                    Toast.makeText(
                            applicationContext,
                            "Registered with GCM Server successfully.\n\n"
                                    + msg, Toast.LENGTH_SHORT).show();
                    button.setActivated(true);
                } else {
                    Toast.makeText(
                            applicationContext,
                            "Reg ID Creation Failed.\n\nEither you haven't enabled Internet or GCM server is busy right now. Make sure you enabled Internet and try registering again after some time."
                                    + msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }

    private void storeRegIdInSharedPref(String regId) {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREF_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.commit();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(
                        applicationContext,
                        "This device doesn't support Play services, App will not work",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        } else {
            Toast.makeText(
                    applicationContext,
                    "This device supports Play services, App will work normally",
                    Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private void sendUpstreamMessageInBackground(final String message) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    Bundle data = new Bundle();
                    data.putString("my_message", message);
                    int id = getMessageIdAndIncrement();
                    if (gcmObj == null) {
                        gcmObj = GoogleCloudMessaging
                                .getInstance(applicationContext);
                    }
                    gcmObj.send(getResources().getString(R.string.project_id) + "@gcm.googleapis.com", String.valueOf(id), data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }

    private int getMessageIdAndIncrement(){
        SharedPreferences prefs = getSharedPreferences(SHARED_PREF_NAME,
                Context.MODE_PRIVATE);
        int id = prefs.getInt(MESSAGE_ID, 1);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(MESSAGE_ID, id + 1);
        editor.commit();
        return id;
    }

}
