package at.fhj.tessaimrich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.IOException;
import java.util.Locale;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;

public class PillDetailActivity extends BaseDrawerActivity {
    private ImageButton btnAudio;
    private ImageButton btnPdf;
    private String pillName;
    private boolean isSpeaking = false;

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximityListener;

    private AudioManager audioManager;
    private int originalVolume;
    private boolean isVolumeAdjusted = false;

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

        // Audio-Button referenzieren und Logik setzen
        btnAudio = findViewById(R.id.btnAudio1);
        btnAudio.setOnClickListener(v -> {
            if (this.ttsService == null || !this.ttsService.isTTSReady()) {
                Toast.makeText(this, "Sprachausgabe nicht bereit", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isSpeaking) {
                ttsService.stop();
                isSpeaking = false;
                Toast.makeText(this, "Wiedergabe gestoppt", Toast.LENGTH_SHORT).show();
            } else {
                String text = loadTtsTextForPill(pillName);
                if (text != null && !text.isEmpty()) {
                    ttsService.speak(text);
                    isSpeaking = true;
                } else {
                    Toast.makeText(this, "Text nicht gefunden", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // PDF-Button referenzieren und Logik setzen
        btnPdf = findViewById(R.id.btnPdf1);
        btnPdf.setOnClickListener(v -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String lang = prefs.getString("language", Locale.getDefault().getLanguage());

            Medication med = AppDatabase
                    .getInstance(getApplicationContext())
                    .medicationDao()
                    .findByNameAndLanguage(pillName, lang);

            if (med == null || med.getPdfAsset() == null) {
                Toast.makeText(this, "Keine PDF verfügbar („" + lang + "“)", Toast.LENGTH_SHORT).show();
                return;
            }

            String rawPdf = med.getPdfAsset();
            String cleanPdf = rawPdf.replaceAll("\\s+", "");

            openPdfFromAssets(cleanPdf);
        });

        // Home-Button zurück zur Kategorie
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(PillDetailActivity.this, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // AudioManager init und Basis-Lautstärke setzen
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (originalVolume < maxVolume / 2) {
            originalVolume = maxVolume - 1;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
            Toast.makeText(this, "Basis-Lautstärke erhöht für optimale Sprachausgabe", Toast.LENGTH_SHORT).show();
        }

        // Näherungs-Sensor einrichten
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor != null) {
            proximityListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float distance = event.values[0];
                    if (ttsService != null && audioManager != null) {
                        if (distance < proximitySensor.getMaximumRange()) {
                            if (!isVolumeAdjusted) {
                                int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                int reducedVolume = Math.max(1, maxVol / 3);
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, reducedVolume, 0);
                                isVolumeAdjusted = true;
                                Toast.makeText(PillDetailActivity.this, "Ohr erkannt → Lautstärke reduziert", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (isVolumeAdjusted) {
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                                isVolumeAdjusted = false;
                                Toast.makeText(PillDetailActivity.this, "Lautsprecher", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
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

        String key = med.getTtsText();
        String filelang = med.getLanguage();
        String requested = key + "_" + filelang + ".txt";
        String file = resolveAssetFilename("tts/pills", requested);
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
        String cleanAssetName = rawAssetName.replaceAll("\\s+", "");
        String assetFileName = resolveAssetFilename("pdfs", cleanAssetName);

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

    /**
     * Sucht eine Datei im angegebenen Asset-Ordner (case-insensitive, mit verschiedenen Matching-Strategien).
     */
    private String resolveAssetFilename(String dir, String requestedName) {
        try {
            String[] files = getAssets().list(dir);
            if (files != null) {
                String cleanRequestedName = requestedName.replaceAll("\\s+", "");
                for (String f : files) {
                    if (f.equalsIgnoreCase(cleanRequestedName)) {
                        return f;
                    }
                }
                for (String f : files) {
                    String cleanFileName = f.replaceAll("\\s+", "");
                    if (cleanFileName.equalsIgnoreCase(cleanRequestedName)) {
                        return f;
                    }
                }
                if (cleanRequestedName.toLowerCase().contains("_en.")) {
                    String upperCaseRequest = cleanRequestedName.replaceAll("_en\\.", "_EN.");
                    for (String f : files) {
                        String cleanFileName = f.replaceAll("\\s+", "");
                        if (cleanFileName.equalsIgnoreCase(upperCaseRequest)) {
                            return f;
                        }
                    }
                }
                String lowerReq = cleanRequestedName.toLowerCase(Locale.ROOT);
                if (lowerReq.contains("_")) {
                    String baseName = lowerReq.substring(0, lowerReq.lastIndexOf('_'));
                    String langPart = lowerReq.substring(lowerReq.lastIndexOf('_'));
                    for (String f : files) {
                        String cleanFileName = f.replaceAll("\\s+", "").toLowerCase();
                        if (cleanFileName.startsWith(baseName) &&
                                (cleanFileName.contains(langPart) ||
                                        cleanFileName.contains(langPart.toUpperCase()))) {
                            return f;
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e("PillDetail", "Fehler beim Listen von assets/" + dir, e);
        }
        return null;
    }

    /**
     * Wird aufgerufen, wenn die PillDetailActivity endgültig zerstört wird.
     * Diese Methode ruft lediglich den Super-Aufruf auf, da alle dienstspezifischen
     * Aufräum- und Unbind-Operationen (z. B. TTS-Unbinding) bereits zentral in
     * BaseDrawerActivity.onDestroy() durchgeführt werden.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
