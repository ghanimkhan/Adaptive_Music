package com.androxus.adaptive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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

    boolean played=false;
    int lastvolume;
    int previousVolume;
    final Runnable updater = new Runnable() {

        public void run() {
            updateTv();
        }

        ;
    };
    Thread runner;
    int countnew;
    int volumes;
    private List<Double> valuesAvg = new ArrayList<>();
    private long timestamp = System.currentTimeMillis() / 1000L;
    private long lastTimestamp = System.currentTimeMillis() / 1000L;
    private String schedule = "NA";
    private AudioManager myAudioManager;


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
    int dblstrength=65;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final MaterialButtonToggleGroup tGroup =findViewById(R.id.toggleGroup);


        mStatusView = (TextView) findViewById(R.id.dbText);
        mStatusAvgView = (TextView) findViewById(R.id.dbAvgText);

        myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        final int volume_level = myAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = myAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);




        /////////////////////////////toggle button

        btnToggle = findViewById(R.id.toggleGroup);

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

        SwitchMaterial switchMaterial=findViewById(R.id.switchMaterial);

        switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

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

    public void onResume() {
        super.onResume();
        startRecorder();
    }

    public void onPause() {
        super.onPause();
        //stopRecorder();
    }

    public void startRecorder() {
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
            mStatusView.setText(Double.toString(dbl)+ "dB");
            int num= (int) dbl;

            if(dbl>=dblstrength){
                myAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);

            }
            if(dbl<=dblstrength){
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

                mStatusAvgView.setText(String.format("%.2f", average)+ "dB");

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


}

