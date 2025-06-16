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

import java.io.FileNotFoundException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

import java.io.IOException;
import java.util.Arrays;

public class PillDetailActivity extends BaseDrawerActivity {

    private TTSService ttsService;
    private ImageButton btnAudio;
    private String pillName;
    private boolean isSpeaking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_pill_detail,
                findViewById(R.id.content_frame),
                true
        );

        // 1) Pillenname holen und anzeigen
        pillName = getIntent().getStringExtra("pill_name");
        ((TextView)findViewById(R.id.tvPillName)).setText(pillName != null ? pillName : "");

        // 2) Audio-Button referenzieren
        btnAudio = findViewById(R.id.btnAudio1);

        // 3) Button-Logik: einmal Play, nächstes Mal Stop
        btnAudio.setOnClickListener(v -> {
            if (ttsService == null || !ttsService.isTTSReady()) {
                Toast.makeText(this, "Sprachausgabe nicht bereit", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isSpeaking) {
                // Stoppen
                ttsService.stop();
                isSpeaking = false;
                Toast.makeText(this, "Wiedergabe gestoppt", Toast.LENGTH_SHORT).show();
            } else {
                // Starten
                String text = loadTtsTextForPill(pillName);
                if (text != null && !text.isEmpty()) {
                    ttsService.speak(text);
                    isSpeaking = true;
                } else {
                    Toast.makeText(this, "Text nicht gefunden", Toast.LENGTH_SHORT).show();
                }
            }
        });


        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(PillDetailActivity.this, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });


        // 4) TTS-Service starten und binden
        Intent intent = new Intent(this, TTSService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);



    }

    /** Lädt den TTS-Text aus DB + Assets */
    private String loadTtsTextForPill(String name) {
        try {
            Medication med = AppDatabase
                    .getInstance(getApplicationContext())
                    .medicationDao()
                    .findByName(name);
            if (med == null) return null;

            String key = med.getTtsText();
            SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
            String lang = prefs.getString("selected_language", "en");
            String filename = "tts/pills/" + key + "_" + lang + ".txt";

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
        } catch (Exception e) {
            Log.e("PillDetail", "Fehler beim Laden der TTS-Datei", e);
            return null;
        }
    }

    /** Verbindung zum TTSService */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            ttsService = ((TTSService.LocalBinder)binder).getService();
            // sofort die aktuelle Sprache setzen
            SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
            ttsService.setLanguage(prefs.getString("selected_language", "en"));

            SharedPreferences speechPrefs =
                    getSharedPreferences("tts_prefs", MODE_PRIVATE);
            float savedRate = speechPrefs.getFloat("speech_rate", 1.0f);
            ttsService.setSpeechRate(savedRate);
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            ttsService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsService != null) unbindService(serviceConnection);
    }
}
