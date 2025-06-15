package at.fhj.tessaimrich;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;


public class CreamDetailActivity extends BaseDrawerActivity {

    private TTSService ttsService;
    private ImageButton btnAudio;
    private String creamName;
    private boolean isSpeaking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_cream_detail,
                findViewById(R.id.content_frame),
                true
        );

        creamName = getIntent().getStringExtra("cream_name");
        ((TextView)findViewById(R.id.tvCreamName))
                .setText(creamName != null ? creamName : "");

        // b) Audio-Button referenzieren
        btnAudio = findViewById(R.id.btnAudioCream);

        // c) Play/Stop-Logik
        btnAudio.setOnClickListener(v -> {
            if (ttsService == null || !ttsService.isTTSReady()) {
                Toast.makeText(this, "Sprachausgabe nicht bereit", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isSpeaking) {
                ttsService.stop();
                isSpeaking = false;
                Toast.makeText(this, "Wiedergabe gestoppt", Toast.LENGTH_SHORT).show();
            } else {
                String text = loadTtsTextForCream(creamName);
                if (text != null && !text.isEmpty()) {
                    ttsService.speak(text);
                    isSpeaking = true;
                } else {
                    Toast.makeText(this, "Text nicht gefunden", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        Intent intent = new Intent(this, TTSService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private String loadTtsTextForCream(String name) {
        try {
            Medication med = AppDatabase
                    .getInstance(getApplicationContext())
                    .medicationDao()
                    .findByName(name);
            if (med == null) return null;

            String key = med.getTtsText();
            SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
            String lang = prefs.getString("selected_language", "en");
            String filename = "tts/pills/cream/" + key + "_" + lang + ".txt";

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(filename))
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(" ");
            }
            reader.close();
            return sb.toString().trim();
        } catch (IOException e) {
            Log.e("CreamDetail", "Fehler beim Laden der TTS-Datei", e);
            return null;
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            ttsService = ((TTSService.LocalBinder)binder).getService();
            SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
            ttsService.setLanguage(prefs.getString("selected_language", "en"));
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            ttsService = null;
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsService != null) {
            unbindService(serviceConnection);
        }
    }

}
