package at.fhj.tessaimrich;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PillDetailActivity extends BaseDrawerActivity implements TTSService.TTSListener {

    private TTSService ttsService;
    private String selectedLanguageCode;

    private boolean ttsReady = false;
    private boolean isSpeaking = false;  // Flag, das anzeigt, ob TTS gerade spricht

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_pill_detail,
                findViewById(R.id.content_frame),
                true
        );

        // Sprache aus SharedPreferences lesen
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        selectedLanguageCode = prefs.getString("selected_language", "en");  // fallback: en

        // Pillenname aus Intent holen
        String pillName = getIntent().getStringExtra("pill_name");
        if (pillName != null) {
            ((TextView) findViewById(R.id.tvPillName)).setText(pillName);
        }

        // TTSService starten
        Intent serviceIntent = new Intent(this, TTSService.class);
        startService(serviceIntent);

        // Hole die Service-Instanz (falls sie schon läuft)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        // Audio-Button
        ImageButton btnAudio = findViewById(R.id.btnAudio1);
        btnAudio.setOnClickListener(v -> {
            String name = getIntent().getStringExtra("pill_name");
            if (name != null) {
                if (isSpeaking) {
                    // Wenn die Wiedergabe läuft, stoppe sie
                    ttsService.stop();
                    isSpeaking = false;
                    Toast.makeText(this, "Wiedergabe gestoppt", Toast.LENGTH_SHORT).show();
                } else {
                    // Wenn keine Wiedergabe läuft, starte sie
                    startTextToSpeechForPill(name);
                    isSpeaking = true;
                }
            }
        });

        // Home-Button: zurück zur CategoryActivity
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(PillDetailActivity.this, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();  // eigene Activity schließen
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (ttsService != null && ttsService.isTTSReady()) {
            String languageCode = PreferenceManager.getDefaultSharedPreferences(this).getString("language", "de");
            ttsService.setLanguage(languageCode); // Sprache neu setzen
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TTSService.LocalBinder binder = (TTSService.LocalBinder) service;
            ttsService = binder.getService();
            ttsService.setTTSListener(PillDetailActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ttsService = null;
        }
    };

    @Override
    public void onTTSInitialized(boolean isReady) {
        if (isReady) {
            // TTS wird nur initialisiert, aber nicht automatisch abgespielt
            Log.d("TTS", "TTS erfolgreich initialisiert, bereit zur Wiedergabe");
        } else {
            Toast.makeText(this, "TTS konnte nicht initialisiert werden", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTextToSpeechForPill(String pillNameRaw) {
        String pillKey = getPillKeyFromName(pillNameRaw);  // Hole den pillKey aus dem Namen
        if (pillKey.isEmpty()) return;

        // Der Dateiname basiert auf dem pillKey und der Sprache
        String filename = "tts/pills/" + pillKey + "_" + selectedLanguageCode + ".txt"; // Richtig!
        Log.d("TTS", "Dateiname für TTS: " + filename);  // Überprüfe den Dateinamen im Log

        try {
            // Lade die Datei aus den Assets
            InputStreamReader isr = new InputStreamReader(getAssets().open(filename));
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder textBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                textBuilder.append(line).append("\n");
            }
            reader.close();

            String ttsText = textBuilder.toString().trim();

            // Debugging: Zeige den geladenen Text
            Log.d("TTS", "Text: " + ttsText);  // Log-Ausgabe
            Toast.makeText(this, "Text: " + ttsText, Toast.LENGTH_LONG).show();  // Toast

            // TTS nur sprechen, wenn es bereit ist
            if (ttsService != null && ttsService.isTTSReady()&& !isSpeaking) {
                ttsService.speak(ttsText);
                isSpeaking = true;
            } else {
                if (isSpeaking) {
                    // Stoppe TTS, wenn es schon läuft
                    ttsService.stop();
                    isSpeaking = false;  // Flag zurücksetzen
                    Toast.makeText(this, "Wiedergabe gestoppt", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Sprachausgabe noch nicht bereit", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (FileNotFoundException e) {
            Log.e("TTS", "Datei nicht gefunden: " + filename);  // Log für den Fehler
            e.printStackTrace();
            Toast.makeText(this, "Datei nicht gefunden: " + filename, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private String getPillKeyFromName(String pillNameRaw) {
        // Mappe verschiedene Namen auf einen einheitlichen pillKey
        if (pillNameRaw.toLowerCase().contains("amlodipin")) {
            return "amlodipin";
        } else if (pillNameRaw.toLowerCase().contains("valsartan")) {
            return "valsartan";
        } else if (pillNameRaw.toLowerCase().contains("cymbalta")) {
            return "cymbalta";
        } else if (pillNameRaw.toLowerCase().contains("eliquis")) {
            return "eliquis";
        } else if (pillNameRaw.toLowerCase().contains("nilemdo")) {
            return "nilemdo";
        } else if (pillNameRaw.toLowerCase().contains("qtern")) {
            return "qtern";
        }
        return "";  // Falls der pillName nicht zugeordnet werden konnte
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsService != null) {
            unbindService(serviceConnection);
        }
    }
}
