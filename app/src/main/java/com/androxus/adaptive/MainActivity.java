package com.androxus.adaptive;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    static final private double EMA_FILTER = 0.6;
    private static double mEMA = 0.0;
    final Handler mHandler = new Handler();
    TextView mStatusView, mStatusAvgView, mStatus;
    MediaRecorder mRecorder;
    SeekBar seekBar;
    int checkState=1;
    boolean played=false;
    final Runnable updater = new Runnable() {

        public void run() {
            updateTv();
        }

    };
    Thread runner;
    int countnew;
    int volumes;
    private List<Double> valuesAvg = new ArrayList<>();
    private long timestamp = System.currentTimeMillis() / 1000L;
    private long lastTimestamp = System.currentTimeMillis() / 1000L;
    private String schedule = "NA";

    private AudioManager myAudioManager;

    private int MIC_PERMISSION_CODE = 1;



    Date c = Calendar.getInstance().getTime();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
    SimpleDateFormat time = new SimpleDateFormat("HH:mm:");

    String formattedDate = df.format(c);
    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    */
 //   SettingsContentObserver settingsContentObserver;
    MaterialButtonToggleGroup btnToggle;
    MaterialButtonToggleGroup btnToggleState;

    int dblstrength=65;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final MaterialButtonToggleGroup tGroup =findViewById(R.id.toggleGroup);
        final MaterialButtonToggleGroup tGroupState =findViewById(R.id.toggleGroupState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        final SwitchMaterial switchMaterial=findViewById(R.id.switchMaterial);

        final int volume_level = myAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = myAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);


        if (isServiceRunningInForeground(getApplicationContext(), MyService.class)) {

        }



        /////////////////////////////toggle button

        btnToggle = findViewById(R.id.toggleGroup);
        btnToggleState=findViewById(R.id.toggleGroupState);

        btnToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.btndefault) {
                        //..
                        dblstrength=65;
                    }
                    if (checkedId == R.id.btnlow) {
                        //..
                        dblstrength=65;
                    }
                    if (checkedId == R.id.btnmedium) {
                        //..
                        dblstrength=75;
                    }
                    if (checkedId == R.id.btnhigh) {
                        //..
                        dblstrength=80;
                    }

                }


            }
        });

        btnToggleState.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.btnIncrease) {
                        //..
                        checkState=0;

                        if (switchMaterial.isChecked()) {
                            switchMaterial.setChecked(false);
                            runner=null;
                        }

                    }
                    if (checkedId == R.id.btnDicrease) {
                        //..
                        checkState=1;
                        if (switchMaterial.isChecked()) {
                            switchMaterial.setChecked(false);
                            runner=null;
                        }


                    }


                }


            }
        });



        switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    Intent intent = new Intent(MainActivity.this, MyService.class);
                    intent.setAction(MyService.ACTION_START_FOREGROUND_SERVICE);
                    startService(intent);

                    Toast.makeText(MainActivity.this, "Switch On", Toast.LENGTH_SHORT).show();
                    if (runner == null) {
                        runner = new Thread() {
                            public void run() {
                                while (runner != null) {
                                    try {
                                        Thread.sleep(1000);
                                        Log.i("Noise", "Tock");
                                    } catch (InterruptedException e) {
                                    }

                                    mHandler.post(updater);
                                }
                            }
                        };
                        runner.start();
                        Log.d("Noise", "start runner()");
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, MyService.class);
                    intent.setAction(MyService.ACTION_STOP_FOREGROUND_SERVICE);
                    startService(intent);
                    runner=null;
                    Toast.makeText(MainActivity.this, "Switch Off", Toast.LENGTH_SHORT).show();
                }
            }
        });





        ///////////////////////////////////////////////////////////        seekbar

        seekBar=(SeekBar)findViewById(R.id.seekBar);
        seekBar.setMax(maxVolume);
        seekBar.setProgress(volume_level);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                //Toast.makeText(getApplicationContext(),"seekbar progress: "+progress, Toast.LENGTH_SHORT).show();
                myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
               // myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume_level, 0);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume_level, 0);

            }
        });



//        settingsContentObserver = new SettingsContentObserver(this, new Handler());
//
//        getApplicationContext().getContentResolver().registerContentObserver(Settings.System.
//                CONTENT_URI, true, settingsContentObserver);



    }


    ///////////////////////constent observer

//    public class SettingsContentObserver extends ContentObserver {
//
//        Context context;
//        @SuppressLint("NewApi")
//        SettingsContentObserver(Context c, Handler handler) {
//            super(handler);
//            context = c;
//            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//            previousVolume = Objects.requireNonNull(audio).getStreamVolume(AudioManager.STREAM_MUSIC);
//        }
//        @Override
//        public boolean deliverSelfNotifications() {
//            return super.deliverSelfNotifications();
//        }
//        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//        @Override
//        public void onChange(boolean selfChange) {
//            super.onChange(selfChange);
//            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//            int currentVolume = Objects.requireNonNull(audio).getStreamVolume(AudioManager.STREAM_MUSIC);
//            int delta = previousVolume - currentVolume;
//            if (delta > 0) {
//                //Toast.makeText(MainActivity.this, "Volume Decreased", Toast.LENGTH_SHORT).show();
//                previousVolume = currentVolume;
//            }
//            else if (delta < 0) {
//               // Toast.makeText(MainActivity.this, "Volume Increased", Toast.LENGTH_SHORT).show();
//                previousVolume = currentVolume;
//
//            }
//        }
//    }
//    @Override
//    protected void onDestroy() {
//        getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
//        super.onDestroy();
//    }
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return true;
}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                return true;
            case R.id.action_about:
                Intent myIntent = new Intent(MainActivity.this, AboutActivity.class);
                MainActivity.this.startActivity(myIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onResume() {
        super.onResume();
        startRecorder();
    }

    public void onPause() {
        super.onPause();
        //stopRecorder();
    }

    public void startRecorder() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile("/dev/null");
                try {
                    mRecorder.prepare();
                } catch (java.io.IOException ioe) {
                    android.util.Log.e("[Monkey]", "IOException: " +
                            android.util.Log.getStackTraceString(ioe));

                } catch (java.lang.SecurityException e) {
                    android.util.Log.e("[Monkey]", "SecurityException: " +
                            android.util.Log.getStackTraceString(e));
                }
                try {
                    mRecorder.start();
                } catch (java.lang.SecurityException e) {
                    android.util.Log.e("[Monkey]", "SecurityException: " +
                            android.util.Log.getStackTraceString(e));
                }

                //mEMA = 0.0;
            }
        } else {
            requestStoragePermission();
        }


    }

    public void stopRecorder() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void updateTv() {


        double amplitude = mRecorder.getMaxAmplitude();
        if(amplitude > 0 && amplitude < 1000000) {
            double dbl = convertdDb(amplitude);
           // mStatusView.setText(Double.toString(dbl)+ "dB");
            int num= (int) dbl;

            if(dbl>=dblstrength){
                if(checkState==0) {
                    myAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                }
                else if(checkState==1){
                    myAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                }

            }
            else if(dbl<=dblstrength){
                myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) seekBar.getProgress(), 0);
            }


            valuesAvg.add(dbl);
            lastTimestamp = System.currentTimeMillis() / 1000L;

            if(lastTimestamp - timestamp > 60 ){
                double sum = 0;
                int count = 0;

                for(Double value : valuesAvg) {
                    count++;
                    sum+= value;
                }
                valuesAvg = new ArrayList<>();
                timestamp = lastTimestamp;
                float average = (float) sum/count;

               // mStatusAvgView.setText(String.format("%.2f", average)+ "dB");

                Date currentTime = Calendar.getInstance().getTime();

            if(schedule != "NA"){
                Map<String, Object> avg = new HashMap<>();
                double ans = Double.parseDouble(new DecimalFormat("##.##").format(average));

                avg.put("value", ans);
                avg.put("date", formattedDate);
                avg.put("time", time.format(Calendar.getInstance().getTime()));
                avg.put("schedule", schedule);


            }

            }
        }
    }
    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }

            }
        }
        return false;
    }

    public double soundDb(double ampl) {
        return 20 * (float) Math.log10(getAmplitudeEMA() / ampl);
    }
    public double convertdDb(double amplitude) {

        double EMA_FILTER = 0.6;
        SharedPreferences sp = this.getSharedPreferences("device-base", MODE_PRIVATE);
        double amp = (double) sp.getFloat("amplitude", 0);
        double mEMAValue = EMA_FILTER * amplitude + (1.0 - EMA_FILTER) * mEMA;
        Log.d("db", Double.toString(amp));

        return 20 * (float) Math.log10((mEMAValue/51805.5336)/ 0.000028251);
    }


    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude());
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed measure the noise around you")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] {Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MIC_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }



}

