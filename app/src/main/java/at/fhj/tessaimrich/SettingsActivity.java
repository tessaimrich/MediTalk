package at.fhj.tessaimrich;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Switch;


public class SettingsActivity extends BaseDrawerActivity {
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "tts_prefs";
    private static final String KEY_SPEECH_RATE = "speech_rate";
    private static final String KEY_AUTO_BRIGHTNESS = "auto_brightness";
    private static final int REQUEST_CODE_WRITE_SETTINGS = 1001;
    private Switch brightnessSwitch;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightListener;
    private TTSService ttsService;
    private boolean isServiceBound = false;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(
                R.layout.activity_settings,
                findViewById(R.id.content_frame),
                true
        );
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Intent svcIntent = new Intent(this, TTSService.class);
        startService(svcIntent);
        bindService(svcIntent, settingsServiceConnection, BIND_AUTO_CREATE);

        setupSpeechRateControls();
        setupHomeButton();

        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        brightnessSwitch = findViewById(R.id.brightnessSwitch);
        boolean isAuto = prefs.getBoolean(KEY_AUTO_BRIGHTNESS, false);
        brightnessSwitch.setChecked(isAuto);
        setupLightListener();
        applyAutoBrightness(isAuto);

        brightnessSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_AUTO_BRIGHTNESS, isChecked).apply();
            applyAutoBrightness(isChecked);
        });
    }
    private void setupSpeechRateControls() {
        // statt R.id.rgSpeechRate:
        RadioGroup rg = findViewById(R.id.playbackSpeedGroup);

        float currentRate = prefs.getFloat(KEY_SPEECH_RATE, 1.0f);
        // Default-Button: speed1x
        int toCheck = R.id.speed1x;
        if (currentRate == 0.5f)    toCheck = R.id.speed0_5x;
        else if (currentRate == 1.25f) toCheck = R.id.speed1_25x;
        else if (currentRate == 1.5f)  toCheck = R.id.speed1_5x;
        else if (currentRate == 2.0f)  toCheck = R.id.speed2x;
        rg.check(toCheck);

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            float newRate = 1.0f;
            if (checkedId == R.id.speed0_5x) {
                newRate = 0.5f;
            } else if (checkedId == R.id.speed1x) {
                newRate = 1.0f;
            } else if (checkedId == R.id.speed1_25x) {
                newRate = 1.25f;
            } else if (checkedId == R.id.speed1_5x) {
                newRate = 1.5f;
            } else if (checkedId == R.id.speed2x) {
                newRate = 2.0f;
            }
            prefs.edit().putFloat(KEY_SPEECH_RATE, newRate).apply();

            if (isServiceBound && ttsService != null) {
                ttsService.setSpeechRate(newRate);
            }
        });

    }
    private void setupHomeButton() {
        ImageButton btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
    private final ServiceConnection settingsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            ttsService = ((TTSService.LocalBinder) binder).getService();
            isServiceBound = true;
            // initial die gespeicherte Rate einstellen
            float savedRate = prefs.getFloat(KEY_SPEECH_RATE, 1.0f);
            ttsService.setSpeechRate(savedRate);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(settingsServiceConnection);
            isServiceBound = false;
        }
    }
    private void setupLightListener() {
        lightListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float lux = event.values[0];
                int brightness = (int) (Math.min(lux, 1000) / 1000 * 255);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, brightness);
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        };
    }
    private void applyAutoBrightness(boolean enable) {
        if (enable) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            sensorManager.registerListener(lightListener,
                    lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            sensorManager.unregisterListener(lightListener);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (brightnessSwitch.isChecked()) {
            sensorManager.registerListener(lightListener,
                    lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(lightListener);
    }

}