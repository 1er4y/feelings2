package com.sloth.feelings;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;



public class MainScreenActivity extends ActionBarActivity {

    private ShareToServer appUtil = new ShareToServer();
    private String currentUserId;
    private String secondUserId;
    private String messageToSend;

    private SharedPreferences mSettings;
    private ListView drawListView;


    private static final String APP_PREFERENCES = "ClientInfo";
    private static final String APP_PREFERENCES_END_REMINDERS_TIME = "endRemindersTime";
    private static final String APP_PREFERENCES_COUNT_REMINDERS= "countReminders";
    private static final String APP_PREFERENCES_REGISTERED = "registered";
    private static final String APP_PREFERENCES_CURRENT_USER_ID = "currentUserId";
    private static final String APP_PREFERENCES_SECOND_USER_ID = "secondUserId";

    private Animation animationEnlarge, animationShrink;
    private ImageView heartImageView;
    private TextView countDownTextView;
    private int countReminders;
    private long endRemindersTime;
    private final long oneDayTime = 43200000;
    private final long oneHourTime = 3600000;
    private DrawerLayout mDrawerLayout;
    private ImageView settingsImageView;
    private TypedArray navMenuIcons;
    private ArrayList<SettingsDrawerItem> navDrawerItems;
    private SettingsDrawerAdapter adapter;
    private AlertDialog.Builder helpAlertDialog;
    private AlertDialog.Builder cancelSynchronizationAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_screen);
        messageToSend = getString(R.string.gcm_notification_message);
        mSettings = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        currentUserId = mSettings.getString(APP_PREFERENCES_CURRENT_USER_ID,"0");
        secondUserId = mSettings.getString(APP_PREFERENCES_SECOND_USER_ID,"0");

        countReminders = mSettings.getInt(APP_PREFERENCES_COUNT_REMINDERS,3);

        createDialogs();

        settingsImageView = (ImageView) findViewById(R.id.settingsImageView);

        drawListView = (ListView) findViewById(R.id.left_drawer);
        String[] items = new String[] {getString(R.string.settings_icon_text),getString(R.string.settings_cancel_sync_text),getString(R.string.settings_help_text),getString(R.string.settings_rate_app_text)};
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.settings_icons);

        navDrawerItems = new ArrayList<>();
        navDrawerItems.add(new SettingsDrawerItem(items[1],navMenuIcons.getResourceId(0,-1)));
        navDrawerItems.add(new SettingsDrawerItem(items[2],navMenuIcons.getResourceId(1,-1)));
        navDrawerItems.add(new SettingsDrawerItem(items[3],navMenuIcons.getResourceId(2,-1)));
        navMenuIcons.recycle();

        TextView settingsHeader = new TextView(this);
        settingsHeader.setText(getString(R.string.settings_icon_text));
        settingsHeader.setTextColor(Color.WHITE);
        settingsHeader.setPadding(16, 16, 16, 16);
        settingsHeader.setGravity(Gravity.CENTER);
        drawListView.addHeaderView(settingsHeader,null,false);

        drawListView.setHeaderDividersEnabled(true);

        adapter = new SettingsDrawerAdapter(getApplicationContext(),
                navDrawerItems);
        drawListView.setAdapter(adapter);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position)
                {
                    case 1:
                        cancelSynchronizationAlertDialog.show();
                        break;
                    case 2:
                        helpAlertDialog.show();
                        break;
                    case 3:
                        rate();
                        break;
                    default:
                        break;
                }

                mDrawerLayout.closeDrawer(drawListView);
            }
        });

        settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.START);
            }
        });

        countDownTextView = (TextView) findViewById(R.id.countDownTextView);
        heartImageView = (ImageView) findViewById(R.id.heartImageView);



        if(countReminders==0)
        {
            endRemindersTime = mSettings.getLong(APP_PREFERENCES_END_REMINDERS_TIME,0);
            long currentTime = System.currentTimeMillis();
            if(oneDayTime<(currentTime-endRemindersTime))
            {
                countReminders = 3;
                heartImageView.setColorFilter(Color.WHITE);
                countDownTextView.setText(getString(R.string.reminders_left_text)+" "+countReminders);
            }
            else
            {
                long leftTime = (oneDayTime - (currentTime-endRemindersTime)) / oneHourTime;
                if(leftTime>=2)
                {
                    countDownTextView.setText(getString(R.string.reminders_not_left_text)+" "
                            +String.valueOf(leftTime)+" "+getString(R.string.reminder_not_left_hours));
                }
                else
                {
                    countDownTextView.setText(getString(R.string.reminders_not_left_text)+" 1 "+getString(R.string.reminder_not_left_4_hours));
                }

                heartImageView.setColorFilter(0xFFFFB4CE);
            }
        }
        else
        {
            countDownTextView.setText(getString(R.string.reminders_left_text)+" "+countReminders);
            heartImageView.setColorFilter(Color.WHITE);
        }

        animationEnlarge = AnimationUtils.loadAnimation(this, R.anim.heart_anim);
        animationShrink = AnimationUtils.loadAnimation(this,R.anim.heart_direct);

        currentUserId = getIntent().getStringExtra("currentUserId");
        secondUserId = getIntent().getStringExtra("secondUserId");

        Log.d("MainScreenActivity currentUserId:", currentUserId);
        Log.d("MainScreenActivity secondUserId:", secondUserId);

        animationEnlarge = AnimationUtils.loadAnimation(this, R.anim.heart_anim);
        animationShrink = AnimationUtils.loadAnimation(this,R.anim.heart_direct);

        heartImageView.startAnimation(animationShrink);

        animationEnlarge.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                heartImageView.startAnimation(animationShrink);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        final Bitmap bitmap = ((BitmapDrawable)heartImageView.getDrawable()).getBitmap();
        heartImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int pixel = bitmap.getPixel((int)event.getX(), (int)event.getY());
                int alphaValue= Color.alpha(pixel);

                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:

                        if(alphaValue != 0 && countReminders>0) {

                            if(countReminders>0)
                            {
                                heartImageView.startAnimation(animationEnlarge);
                                //heartImageView.setColorFilter(0xFFFFB4CE);
                                sendMessageToGCMAppServer(currentUserId, secondUserId, messageToSend);
                                countReminders--;
                                countDownTextView.setText(getString(R.string.reminders_left_text)+" "+countReminders);
                                SharedPreferences.Editor editor;
                                editor = mSettings.edit();
                                editor.putInt(APP_PREFERENCES_COUNT_REMINDERS,countReminders);
                                editor.commit();
                                //heartImageView.setEnabled(false);
                            }
                            if(countReminders==0)
                            {
                                SharedPreferences.Editor editor;
                                editor = mSettings.edit();
                                long endTime = System.currentTimeMillis();
                                editor.putLong(APP_PREFERENCES_END_REMINDERS_TIME,endTime);

                                Log.d("Pausing activity: ",String.valueOf(SystemClock.elapsedRealtime()));
                                countDownTextView.setText(getString(R.string.reminders_not_left_text)
                                        + " 12 " + getString(R.string.reminder_not_left_hours));
                                heartImageView.setColorFilter(0xFFFFB4CE);
                                editor.commit();
                            }
                        }
                }
                return false;
            }
        });
    }

    private void cancelSynchronization() {

        try {
            Thread thread3 = new Thread(new Runnable() {

                @Override
                public void run() {
                    appUtil.cancelSynchronization(currentUserId);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),getString(R.string.toast_main_screen_sync_canceled), Toast.LENGTH_SHORT).show();
                            }
                        });

                }
            });
            thread3.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),getString(R.string.toast_main_screen_sync_canceled_error),Toast.LENGTH_SHORT).show();
        }


        SharedPreferences.Editor editor;
        editor = mSettings.edit();
        editor.putBoolean(APP_PREFERENCES_REGISTERED,false);
        editor.putInt(APP_PREFERENCES_COUNT_REMINDERS,3);
        editor.commit();
        finish();
    }

    @Override
    protected void onResume()
    {
        if(countReminders==0)
        {
            endRemindersTime = mSettings.getLong(APP_PREFERENCES_END_REMINDERS_TIME,0);
            long currentTime = System.currentTimeMillis();
            if(oneDayTime<(currentTime-endRemindersTime))
            {
                countReminders = 3;
                countDownTextView.setText(getString(R.string.reminders_left_text)+" "+countReminders);
                heartImageView.setColorFilter(Color.WHITE);
            }
            else
            {
                long leftTime = (oneDayTime - (currentTime-endRemindersTime)) / oneHourTime;
                if(leftTime>=2)
                {
                    countDownTextView.setText(getString(R.string.reminders_not_left_text)+" "
                            +String.valueOf(leftTime)+" "+getString(R.string.reminder_not_left_hours));
                }
                else
                {
                    countDownTextView.setText(getString(R.string.reminders_not_left_text)+" 1 "+getString(R.string.reminder_not_left_4_hours));
                }

                heartImageView.setColorFilter(0xFFFFB4CE);
            }
        }
        else
        {
            countDownTextView.setText(getString(R.string.reminders_left_text)+" "+countReminders);
            heartImageView.setColorFilter(Color.WHITE);
        }
        heartImageView.startAnimation(animationShrink);

        super.onResume();
    }

    private void createDialogs()
    {
        helpAlertDialog = new AlertDialog.Builder(MainScreenActivity.this);
        helpAlertDialog.setCancelable(true);
        helpAlertDialog.setTitle(getString(R.string.dialog_main_screen_help_title));
        helpAlertDialog.setMessage(currentUserId+" "+getString(R.string.dialog_main_screen_help_text));
        helpAlertDialog.setPositiveButton(getString(R.string.dialog_main_screen_help_positive_btn),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        cancelSynchronizationAlertDialog = new AlertDialog.Builder(MainScreenActivity.this);
        cancelSynchronizationAlertDialog.setCancelable(true);
        cancelSynchronizationAlertDialog.setTitle(getString(R.string.dialog_main_screen_cancel_sync_title));
        cancelSynchronizationAlertDialog.setMessage(getString(R.string.dialog_main_screen_cancel_sync_text));
        cancelSynchronizationAlertDialog.setPositiveButton(getString(R.string.dialog_main_screen_cancel_sync_positive_btn),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    cancelSynchronization();
            }
        });
        cancelSynchronizationAlertDialog.setNegativeButton(getString(R.string.dialog_main_screen_cancel_sync_negative_btn),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

    }

    private void rate() {
        try
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.sloth.feelings"));
            startActivity(intent);
        }
        catch (Exception e)
        {
          e.printStackTrace();
            Toast.makeText(MainScreenActivity.this,getString(R.string.toast_main_screen_google_play_not_found),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {

        heartImageView.clearAnimation();
        super.onPause();

    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
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

    @Override
    public void onBackPressed () {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }

    private void sendMessageToGCMAppServer(final String currentUserId, final String secondUserId,
                                           final String messageToSend) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                boolean result = appUtil.sendMessage(currentUserId, secondUserId,
                        messageToSend);
                if (result)
                {
                    return "done";
                }

                Log.d("MainActivity", "Result: " + result);
                return "not done";
            }

            @Override
            protected void onPostExecute(String msg) {

                    Log.d("MainActivity", "Result");
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_main_screen_message_sended),
                            Toast.LENGTH_SHORT)
                            .show();
            }
        }.execute(null, null, null);
    }

}
