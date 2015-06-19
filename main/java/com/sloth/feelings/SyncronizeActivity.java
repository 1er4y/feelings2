package com.sloth.feelings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SyncronizeActivity extends ActionBarActivity {


    private int doubledValue;

    GoogleCloudMessaging gcm;
    Context context;
    Context context2;
    String regId;
    ShareToServer shareToServer;
    AlertDialog.Builder ad;

   // public static final String REG_ID = "regId";
   // private static final String APP_VERSION = "appVersion";
    private String currentUserId;
    private String secondUserId;
   // private String result;
    private static final String APP_PREFERENCES = "ClientInfo";
    private static final String APP_PREFERENCES_CURRENT_USER_ID = "currentUserId";
    private static final String APP_PREFERENCES_SECOND_USER_ID = "secondUserId";
    private static final String APP_PREFERENCES_REGISTERED = "registered";
    private SharedPreferences mSettings;
    Intent intent;
    //ProgressBar progressBar;
    //private int progressStatus = 0;


    // String URL = "http://52.10.76.194";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_syncronize);

        final TextView codeTextView = (TextView) findViewById(R.id.codeTextView);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        regId = registerGCM();
        final Button doneButton = (Button) findViewById(R.id.doneButton);
        final EditText secondUserIdEditText = (EditText) findViewById(R.id.secondUserIdEditText);
        secondUserIdEditText.setEnabled(false);
        context2 = SyncronizeActivity.this;
        ad = new AlertDialog.Builder(context2);

            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //progressBar.setProgress(progressStatus);
                    try {

                        URL url = new URL("http://androidserver-jxbj8qesua.elasticbeanstalk.com/");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoOutput(true);
                        connection.setConnectTimeout(10000);
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                        connection.setRequestProperty("Accept", "*//*");
                        connection.setDoInput(true);
                        connection.setRequestMethod("POST");
                       // progressStatus++;
                        String inputString = "2048";

                        Log.d("inputString", inputString);

                        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                        out.write(inputString);
                        out.close();
                       // progressStatus++;
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                        String returnString;
                        //returnString = "";
                        doubledValue = 0;
                        //progressStatus++;
                        while ((returnString = in.readLine()) != null) {
                            doubledValue = Integer.parseInt(returnString);
                        }
                        in.close();
                        connection.disconnect();
                        //progressStatus++;

                        //handler.post();
                        runOnUiThread(new Runnable() {
                            public void run() {

                               // progressBar.setVisibility(View.GONE);
                                if (doubledValue!=0) {
                                    codeTextView.setText(String.valueOf(doubledValue));
                                    currentUserId = String.valueOf(doubledValue);
                                    doneButton.setEnabled(true);
                                    secondUserIdEditText.setEnabled(true);

                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.d("Exception", e.toString());
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               codeTextView.setHint(":-(");
                               Toast.makeText(getApplicationContext(),getString(R.string.toast_sync_code_requesting_error),Toast.LENGTH_LONG).show();
                               doneButton.setEnabled(false);
                           }
                       });
                    }

                }
            });

            thread.start();
        //}



        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                secondUserId = secondUserIdEditText.getText().toString();
                intent = new Intent(SyncronizeActivity.this, MainScreenActivity.class);

                ad.setTitle(R.string.check_secondUserId_dialog_title_text);
                ad.setMessage(secondUserId + " - " + getString(R.string.check_secondUserId_dialog_text));
                ad.setCancelable(false);

                if(secondUserId.isEmpty() || secondUserId.contains(currentUserId))
                {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.toast_sync_wrong_code_text),
                            Toast.LENGTH_SHORT).show();
                }

                else {

                    ad.setPositiveButton(R.string.check_secondUserId_dialog_yesButton_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            doneButton.setEnabled(false);
                            secondUserIdEditText.setEnabled(false);

                            Toast.makeText(getApplicationContext(),
                                   getString(R.string.toast_sync_please_wait),
                                    Toast.LENGTH_SHORT).show();

                            shareToServer = new ShareToServer();


                            while(regId.isEmpty())
                            {
                                Log.d("SyncronizeActivity:","Идет получение regId");
                            }

                            if(!regId.isEmpty()) {

                                Thread thread2 = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                           // boolean registered2 = false;
                                            if(shareToServer.shareRegIdWithAppServer(context, regId, currentUserId, secondUserId))
                                            {
                                                //registered2 = true;
                                                storeClientIDs(currentUserId, secondUserId, true);
                                            }
                                           //

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                thread2.start();
                            }

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("currentUserId", currentUserId);
                            intent.putExtra("secondUserId", secondUserId);

                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.toast_sync_finishing),
                                    Toast.LENGTH_SHORT).show();


                            Log.d("Syncronize Activity!", "Done sharing with server :)");
                            startActivity(intent);
                        }
                    });

                    ad.setNegativeButton(R.string.check_secondUserId_dialog_noButton_text,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            secondUserIdEditText.setEnabled(true);
                            secondUserIdEditText.setHint(R.string.example_code_text);
                            secondUserIdEditText.setText("");
                            Toast.makeText(getApplicationContext(),getString(R.string.toast_sync_reenter_code),Toast.LENGTH_SHORT).show();
                        }
                    });

                    ad.show();

               }

            }

        });

    }

    private void storeClientIDs(String currentUserId, String secondUserId, Boolean registered) {

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_CURRENT_USER_ID, currentUserId);
        editor.putString(APP_PREFERENCES_SECOND_USER_ID,secondUserId);
        editor.putBoolean(APP_PREFERENCES_REGISTERED,registered);
        //editor.putString(APP_PREFERENCES_GCM_REG_ID,regID);
        editor.apply();
    }

    public String registerGCM() {

        gcm = GoogleCloudMessaging.getInstance(this);
       // regId = getRegistrationId(context);

      // if (TextUtils.isEmpty(regId)) {

            registerInBackground();

            Log.d("RegisterActivity",
                    "registerGCM - successfully registered with GCM server - regId: "
                            + regId);
       // } else {
            //Toast.makeText(getApplicationContext(),
                  //  "RegId already available. RegId: " + regId,
                    //Toast.LENGTH_LONG).show();
       // }
        return regId;
    }

    /*
    private String getRegistrationId(Context context) {
        //final SharedPreferences prefs = getSharedPreferences(
           //     MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String registrationId; //= mSettings.getString(APP_PREFERENCES_GCM_REG_ID,"");
        if (mSettings.getString(APP_PREFERENCES_GCM_REG_ID,"").isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        else
        {
            registrationId = mSettings.getString(APP_PREFERENCES_GCM_REG_ID,"");
        }*/
        //int registeredVersion = prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
        //int currentVersion = getAppVersion(context);
        /*
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }*/
/*
        return registrationId;
    }
*/
    /*
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("RegisterActivity",
                    "I never expected this! Going down, going down!" + e);
            throw new RuntimeException(e);
        }
    }*/

    private void registerInBackground() {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(Config.GOOGLE_PROJECT_ID);
                    Log.d("RegisterActivity", "registerInBackground - regId: "
                            + regId);
                    msg = "Device registered, registration ID=" + regId;

                    // AsyncTask shareRegidTask;

                    //storeRegistrationId(context, regId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.d("RegisterActivity", "Error: " + msg);
                }
                Log.d("RegisterActivity", "AsyncTask completed: " + msg);
                return msg;
            }
        }.execute(null, null, null);
    }


    //String regId;

    /*
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.putInt(APP_VERSION, appVersion);
        editor.commit();
    }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_syncronize, menu);
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


}
