package com.sloth.feelings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends ActionBarActivity implements View.OnTouchListener {

    private ViewFlipper flipper = null;
    private float fromPosition;
    private float MOVE_LENGTH = 150;
    private int slideCount = 1;
    private int slidesToRight = 2;
    private int slidesToLeft = 0;
    private SharedPreferences mSettings;
    private static final String APP_PREFERENCES_REGISTERED = "registered";
    private static final String APP_PREFERENCES = "ClientInfo";
    private static final String APP_PREFERENCES_CURRENT_USER_ID = "currentUserId";
    private static final String APP_PREFERENCES_SECOND_USER_ID = "secondUserId";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        checkPlayServices();


        if (mSettings.getBoolean(APP_PREFERENCES_REGISTERED,false))
        {
            Intent intent = new Intent(this, MainScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("currentUserId",mSettings.getString(APP_PREFERENCES_CURRENT_USER_ID,"not registered"));
            intent.putExtra("secondUserId",mSettings.getString(APP_PREFERENCES_SECOND_USER_ID,"not registered"));
            //intent.putExtra("regId",mSettings.getString(APP_PREFERENCES_GCM_REG_ID,"not registered"));

            startActivity(intent);
        }

        else {
            setContentView(R.layout.layout_main);

            flipper = (ViewFlipper) findViewById(R.id.flipper);

            LinearLayout mainLayout = (LinearLayout) findViewById(R.id.main_layout);
            mainLayout.setOnTouchListener(this);

            final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int layouts[] = new int[]{R.layout.layout_first_slide, R.layout.layout_second_slide, R.layout.layout_third_slide};
            for (int layout : layouts)
            flipper.addView(inflater.inflate(layout, null));

            Button connectButton = (Button) findViewById(R.id.connectButton);
            connectButton.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v) {

                    if(isNetworkAvailable() )
                    {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.toast_slides_connect_text),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, SyncronizeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.toast_slide_no_internet_text),
                                Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
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

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),getString(R.string.toast_slide_not_compatible_verion),Toast.LENGTH_LONG).show();
                Log.i("Welcome Slides", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                fromPosition = event.getX();
            }
            case MotionEvent.ACTION_MOVE: {
                float toPosition = event.getX();
                // MOVE_LENGTH - расстояние по оси X, после которого можно переходить на след. экран
                // В моем тестовом примере MOVE_LENGTH = 150
                if ((fromPosition - MOVE_LENGTH) > toPosition && slideCount!=0&&slidesToRight!=0) {
                    fromPosition = toPosition;
                    flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.go_next_in));
                    flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.go_next_out));
                    flipper.showNext();
                    slideCount = 0;
                    slidesToRight--;
                    slidesToLeft++;
                    break;
                } else
                {
                    if ((fromPosition + MOVE_LENGTH) < toPosition && slideCount!=0 && slidesToLeft!=0) {
                        fromPosition = toPosition;
                        flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.go_prev_in));
                        flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.go_prev_out));
                        flipper.showPrevious();
                        slideCount = 0;
                        slidesToRight++;
                        slidesToLeft--;
                        break;

                    }
                    break;
                }
            }

            case MotionEvent.ACTION_UP:
            {
                slideCount = 1;
                break;
            }
            default:
                break;
        }
        return true;

    }
}
