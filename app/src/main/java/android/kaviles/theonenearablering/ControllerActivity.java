package android.kaviles.theonenearablering;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ControllerActivity extends AppCompatActivity
        implements View.OnClickListener,TextToSpeech.OnInitListener {
    private final static String TAG = ControllerActivity.class.getSimpleName();

    private long timer;

    private BeaconManager beaconManager;
    private String scanId;

    private TextView tv_identifier;
    private TextView tv_x;
    private TextView tv_y;
    private TextView tv_z;
    private TextView tv_orientation;

    private Button btn_flag_data;
    private boolean flag_data;
//    private Button btn_up;
//    private Button btn_left;
//    private Button btn_right;
//    private Button btn_down;

    MusicManager mm;
    TextManager tm;
    WeatherManager wm;
    private int activityState;
    private TextToSpeech tts;
    private boolean ttsInitialized;

    private String targetID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        timer = System.currentTimeMillis();

        mm = new MusicManager(this);
        tm = new TextManager(this);
        wm = new WeatherManager(this);
        activityState = 0;
        tts = new TextToSpeech(this, this);

        flag_data = true;
        btn_flag_data = (Button) findViewById(R.id.btn_flag_data);
        btn_flag_data.setOnClickListener(this);

        tv_identifier = (TextView) findViewById(R.id.tv_identifier);
        tv_x = (TextView) findViewById(R.id.tv_x);
        tv_y = (TextView) findViewById(R.id.tv_y);
        tv_z = (TextView) findViewById(R.id.tv_z);
        tv_orientation = (TextView) findViewById(R.id.tv_orientation);

        Intent intent = getIntent();
        targetID = null;
        targetID = intent.getStringExtra("IDENTIFIER");
        tv_identifier.setText(targetID);

        beaconManager = new BeaconManager(this);
        beaconManager.setNearableListener(new BeaconManager.NearableListener() {
            @Override
            public void onNearablesDiscovered(List<Nearable> nearables) {
                Log.d(TAG, "Discovered nearables: " + nearables);

                for (Nearable nearable : nearables) {

                    if (targetID != null && nearable.identifier.compareTo(targetID) == 0) {

                        processNearable(nearable);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                scanId = beaconManager.startNearableDiscovery();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        beaconManager.stopNearableDiscovery(scanId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        beaconManager.disconnect();
    }

    @Override
    public void onInit(int status) {
        if (status  == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This language isn't supported");
            } else {
                ttsInitialized = true;
            }
        } else {
            Log.e("TTS", "Initialization Failed");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void speakSomething(String speak) {
        if (!ttsInitialized) {
            Log.e("TTS", "TextToSpeech Was Not Initialized");
            return;
        }
        else {
            if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(speak, TextToSpeech.QUEUE_FLUSH, null, "speak");
            } else {
                tts.speak(speak, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
//
        switch (i) {
            case R.id.btn_flag_data:
                if (flag_data) {
                    flag_data = false;
                    btn_flag_data.setText("Use Accelerometer");
                }
                else {
                    flag_data = true;
                    btn_flag_data.setText("Use Orientation");
                }
                break;
//            //When up arrow is pressed, cycle through activities.
//            // 0 = music
//            // 1 = text
//            // 2 = weather
//            case R.id.btn_up:
//                if(activityState == 0) {
//                    activityState = 2;
//                    speakSomething("Weather");
//                }
//                else if (activityState == 1) {
//                    activityState--;
//                    speakSomething("Music");
//                }
//                else if (activityState == 2){
//                    activityState--;
//                    speakSomething("Text Messages");
//                }
//
//                break;
//
//            case R.id.btn_left:
//                if(activityState == 0)
//                    mm.skipBackMusic();
//                break;
//            case R.id.btn_down:
//                if(activityState == 0)
//                    mm.playOrPauseMusic();
//                if(activityState == 2) {
//                    Utils.toast(this, "Reading Today's Weather");
//                    speakSomething(wm.getTodaysWeather());
//                }
//                break;
//            case R.id.btn_right:
//                if(activityState == 0)
//                    mm.skipForwardMusic();
//                if(activityState == 1)
//                    speakSomething(tm.readTexts());
//                if(activityState == 2) {
//                    Utils.toast(this, "Reading Tomorrows Weather");
//                    speakSomething(wm.getTomorrowsWeather());
//                }
//                break;
            default:
                break;
        }
    }

    public void handleMenuOption(String val) {

        switch (val) {
            //When up arrow is pressed, cycle through activities.
            // 0 = music
            // 1 = text
            // 2 = weather
            case "U":
                if (activityState == 0) {
                    activityState = 2;
                    mm.pauseMusic();
                    speakSomething("Weather");
                } else if (activityState == 1) {
                    activityState--;
                    speakSomething("Music");
                } else if (activityState == 2) {
                    activityState--;
                    speakSomething("Text Messages");
                }
                break;
            case "L":
                if (activityState == 0)
                    mm.skipBackMusic();
                break;
            case "D":
                if (activityState == 0)
                    mm.playOrPauseMusic();
                if (activityState == 2) {
                    Utils.toast(this, "Reading Today's Weather");
                    speakSomething(wm.getTodaysWeather());
                }
                break;
            case "R":
                if (activityState == 0)
                    mm.skipForwardMusic();
                if (activityState == 1)
                    speakSomething(tm.readTexts());
                if (activityState == 2) {
                    Utils.toast(this, "Reading Tomorrows Weather");
                    speakSomething(wm.getTomorrowsWeather());
                }
                break;
            default:
                break;
        }
    }

    public void setTextViews(String orientation, double x, double y, double z) {
        tv_x.setText("X " + Double.toString(x));
        tv_y.setText("Y " + Double.toString(y));
        tv_z.setText("Z " + Double.toString(z));
        tv_orientation.setText(orientation);
    }

    public void processNearable(Nearable nearable) {

        double x = nearable.xAcceleration;
        double y = nearable.yAcceleration;
        double z = nearable.zAcceleration;
        Nearable.Orientation orientation = nearable.orientation;

        setTextViews(orientation.name(), x, y, z);

        if (System.currentTimeMillis() > timer) {

            if (flag_data) {
                if (y <= 500 && y >= -500) {

                    if (x <= 500 && x >= -500) {
                        if (z <= -500) {
                            handleMenuOption("U");
                            timer = System.currentTimeMillis() + 2000;
                        }
                        else if (z >= 500) {
                            handleMenuOption("D");
                            timer = System.currentTimeMillis() + 2000;
                        }
                    }
                    else if (z <= 500 && z >= -500) {
                        if (x <= -500) {
                            handleMenuOption("R");
                            timer = System.currentTimeMillis() + 2000;
                        }
                        else if (x >= 500) {
                            handleMenuOption("L");
                            timer = System.currentTimeMillis() + 2000;
                        }
                    }
                }
            }
            else {
                switch (orientation) {
                    case HORIZONTAL: {
                        handleMenuOption("U");
                        timer = System.currentTimeMillis() + 2000;
                        break;
                    }
                    case HORIZONTAL_UPSIDE_DOWN: {
                        handleMenuOption("D");
                        timer = System.currentTimeMillis() + 2000;
                        break;
                    }
                    case RIGHT_SIDE: {
                        handleMenuOption("L");
                        timer = System.currentTimeMillis() + 2000;
                        break;
                    }
                    case LEFT_SIDE: {
                        handleMenuOption("R");
                        timer = System.currentTimeMillis() + 2000;
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }
}
