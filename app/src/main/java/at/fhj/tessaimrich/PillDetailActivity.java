package at.fhj.tessaimrich;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;



public class PillDetailActivity extends BaseDrawerActivity {

    private TTSService ttsService;
    private ImageButton btnAudio;
    private ImageButton btnPdf;
    private String pillName;
    private boolean isSpeaking = false;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximityListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_pill_detail,
                findViewById(R.id.content_frame),
                true
        );

        // Pillenname holen und anzeigen
        pillName = getIntent().getStringExtra("pill_name");
        ((TextView) findViewById(R.id.tvPillName)).setText(pillName != null ? pillName : "");


        // Audio-Button referenzieren
        btnAudio = findViewById(R.id.btnAudio1);

        // Button-Logik: einmal Play, nächstes Mal Stop
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

        // für pdf:
        btnPdf = findViewById(R.id.btnPdf1);
        btnPdf.setOnClickListener(v -> {
            //gewählte Sprache aus SharedPreferences holen
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String lang = prefs.getString("language", Locale.getDefault().getLanguage());

            // Jetzt gezielt nach Name+Sprache fragen
            Medication med = AppDatabase
                    .getInstance(getApplicationContext())
                    .medicationDao()
                    .findByNameAndLanguage(pillName, lang);

            if (med == null || med.getPdfAsset() == null) {
                Toast.makeText(this, "Keine PDF verfügbar („" + lang + "“)", Toast.LENGTH_SHORT).show();
                return;
            }
            // PDF öffnen
            openPdfFromAssets(med.getPdfAsset());
        });


        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(PillDetailActivity.this, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });


        // TTS-Service starten und binden
        Intent intent = new Intent(this, TTSService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);


        //für Näherungssensor:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor != null) {
            proximityListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float distance = event.values[0];
                    if (ttsService != null) {
                        if (distance < proximitySensor.getMaximumRange()) {
                            // Handy am Ohr
                            ttsService.useEarpieceOutput();
                            Toast.makeText(PillDetailActivity.this, "Ohr erkannt → Earpiece", Toast.LENGTH_SHORT).show();
                        } else {
                            // Handy weiter weg
                            ttsService.useSpeakerOutput();
                            Toast.makeText(PillDetailActivity.this, "Lautsprecher", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };
            sensorManager.registerListener(proximityListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }





    }








    /**
     * Lädt den TTS-Text aus DB + Assets
     */
    private String loadTtsTextForPill(String name) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs.getString("language", Locale.getDefault().getLanguage());

        Medication med = AppDatabase
                .getInstance(getApplicationContext())
                .medicationDao()
                .findByNameAndLanguage(name,lang);
        if (med == null) return null;

        String key      = med.getTtsText();                  // z.B. "cymbalta"
        String filelang     = med.getLanguage();                 // z.B. "fr"
        String requested= key + "_" + filelang + ".txt";         // "cymbalta_fr.txt"
        String file     = resolveAssetFilename("tts/pills", requested);
        if (file == null) return null;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open("tts/pills/" + file))
        )) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(" ");
            }
            return sb.toString().trim();
        } catch (IOException e) {
            Log.e("PillDetail", "Fehler beim Laden der TTS-Datei", e);
            return null;
        }
    }



    /**
     * Kopiert die PDF aus assets in den App-Files-Ordner
     * und öffnet sie dann mit einem externen PDF-Viewer.
     */


    private void openPdfFromAssets(String rawAssetName) {
        // rawAssetName kommt direkt aus der DB, z.B. "Cymbalta_EN.pdf"
        String assetFileName = resolveAssetFilename("pdfs", rawAssetName);
        if (assetFileName == null) {
            Toast.makeText(this,
                    "PDF nicht gefunden für „" + pillName + "“",
                    Toast.LENGTH_LONG).show();
            return;
        }

        try (InputStream in = getAssets().open("pdfs/" + assetFileName)) {
            File outFile = new File(getFilesDir(), assetFileName);
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    outFile
            );
            startActivity(new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/pdf")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            );

        } catch (FileNotFoundException fnf) {
            Log.e("PillDetail", "PDF nicht gefunden: " + assetFileName, fnf);
            Toast.makeText(this,
                    "PDF nicht gefunden: " + assetFileName,
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("PillDetail", "Fehler beim Öffnen der PDF", e);
            Toast.makeText(this,
                    "Fehler beim Öffnen der PDF",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private String resolveAssetFilename(String dir, String requestedName) {
        try {
            String[] files = getAssets().list(dir);
            if (files != null) {
                //exakter case-insensitive Match
                for (String f : files) {
                    if (f.equalsIgnoreCase(requestedName)) {
                        return f;
                    }
                }
                //Fallback: Matching auf Suffix (z.B. "_EN.pdf" oder "_de.txt")
                String lowerReq = requestedName.toLowerCase(Locale.ROOT);
                String suffix   = lowerReq.substring(lowerReq.lastIndexOf('_'));
                for (String f : files) {
                    if (f.toLowerCase(Locale.ROOT).endsWith(suffix)) {
                        return f;
                    }
                }
            }
        } catch (IOException e) {
            Log.e("PillDetail", "Fehler beim Listen von assets/" + dir, e);
        }
        return null;
    }
    /**
     * Verbindung zum TTSService
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            ttsService = ((TTSService.LocalBinder) binder).getService();
            // sofort die aktuelle Sprache setzen
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(PillDetailActivity.this);
            String lang = prefs.getString("language", Locale.getDefault().getLanguage());
            ttsService.setLanguage(lang);
            SharedPreferences speechPrefs =
                    getSharedPreferences("tts_prefs", MODE_PRIVATE);
            float savedRate = speechPrefs.getFloat("speech_rate", 1.0f);
            ttsService.setSpeechRate(savedRate);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ttsService = null;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsService != null) unbindService(serviceConnection);    //trennt die Verbindung zum TTSService
        if (sensorManager != null && proximityListener != null) {    //deaktiviert den Näherungssensor, um unnötige Hintergrundaktivität zu vermeiden
            sensorManager.unregisterListener(proximityListener);
        }
    }


}
