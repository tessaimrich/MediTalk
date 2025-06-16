package at.fhj.tessaimrich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;


public class SettingsActivity extends BaseDrawerActivity {
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "tts_prefs";
    private static final String KEY_SPEECH_RATE = "speech_rate";
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

}