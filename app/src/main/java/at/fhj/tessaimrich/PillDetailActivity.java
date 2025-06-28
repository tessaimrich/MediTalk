package at.fhj.tessaimrich;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import at.fhj.tessaimrich.data.Medication;

/**
 * Diese Activity zeigt die Detailinformationen zu einer Tablette an.
 * <p>
 * Sie ermöglicht:
 * <ul>
 *   <li>Wiedergabe eines Textes via Text-to-Speech (TTS)</li>
 *   <li>Anzeige einer PDF-Datei</li>
 *   <li>Automatische Lautstärkeregelung bei Näherung</li>
 * </ul>
 * Die Activity basiert auf {@link BaseMedicationDetailActivity}, welche die gemeinsame Logik übernimmt.
 */
public class PillDetailActivity extends BaseMedicationDetailActivity {
    private ImageButton btnAudio, btnPdf;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximityListener;
    private AudioManager audioManager;
    private int originalVolume;
    private boolean isVolumeAdjusted = false;
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_pill_detail;
    }
    @Override
    protected int getTitleViewId() {
        return R.id.tvPillName;
    }


    /**
     * Wird aufgerufen, sobald die Medikamentendaten geladen sind.
     * Initialisiert TTS-Button, PDF-Button und die automatische Lautstärkeregelung.
     *
     * @param med Das geladene Medikamentenobjekt
     */
    @Override
    protected void onMedicationLoaded(Medication med) {
        btnAudio = findViewById(R.id.btnAudio1);
        final boolean[] isSpeaking = {false};
        btnAudio.setOnClickListener(v -> {
            if (ttsService == null || !ttsService.isTTSReady()) {
                Toast.makeText(this, "Sprachausgabe nicht bereit", Toast.LENGTH_SHORT).show();
                return;
            }if (isSpeaking[0]) {
                        ttsService.stop();
                        isSpeaking[0] = false;
                        Toast.makeText(this, "Wiedergabe gestoppt", Toast.LENGTH_SHORT).show();
                    } else {


                String rawKey = med.getTtsText();
                if (rawKey.equalsIgnoreCase("amlodipinevalsartanmylan")) {
                    rawKey = "amlodipin";
                }
                String key = rawKey.toLowerCase(Locale.ROOT);
                String lang = currentLang.toLowerCase(Locale.ROOT);


                String path = "tts/pills/" + key + "_" + lang + ".txt";


                String text = loadAssetText(path);

                if (text != null && !text.isEmpty()) {
                    ttsService.speak(text);
                    isSpeaking[0] = true;
                    Toast.makeText(this, "Wiedergabe gestartet", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Text nicht gefunden", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnPdf = findViewById(R.id.btnPdf1);
        btnPdf.setOnClickListener(v -> {
            String pdfAsset = med.getPdfAsset();
            if (pdfAsset == null) {
                Toast.makeText(this, "Keine PDF verfügbar", Toast.LENGTH_SHORT).show();
                return;
            }


            String pdfName = pdfAsset.trim().replaceAll("\\s+","");
            openPdf("pdfs/" + pdfName);
        });
        setupAudioAndSensor();
    }



    /**
     * Initialisiert AudioManager und Näherungssensor.
     * Bei erkannter Nähe wird die Lautstärke reduziert.
     */
    private void setupAudioAndSensor() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (originalVolume < maxVol / 2) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol - 1, 0);
            Toast.makeText(this, "Basis-Lautstärke erhöht", Toast.LENGTH_SHORT).show();
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor != null) {
            proximityListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float distance = event.values[0];
                    if (ttsService != null && audioManager != null) {
                        if (distance < proximitySensor.getMaximumRange() && !isVolumeAdjusted) {
                            int reduced = Math.max(1, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 3);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, reduced, 0);
                            isVolumeAdjusted = true;
                            Toast.makeText(PillDetailActivity.this,
                                    "Ohr erkannt → Lautstärke reduziert",
                                    Toast.LENGTH_SHORT).show();
                        } else if (distance >= proximitySensor.getMaximumRange() && isVolumeAdjusted) {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                            isVolumeAdjusted = false;
                            Toast.makeText(PillDetailActivity.this,
                                    "Lautsprecher", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };
            sensorManager.registerListener(proximityListener, proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }



    /**
     * Lädt eine Textdatei aus dem Assets-Ordner.
     * Wird für TTS-Textverarbeitung verwendet.
     *
     * @param path Pfad zur Datei
     * @return Inhalt als String oder {@code null} bei Fehler
     */
    private String loadAssetText(String path) {
        try (InputStream in = getAssets().open(path)) {
            byte[] buf = new byte[in.available()];
            in.read(buf);
            return new String(buf);
        } catch (IOException e) {
            Log.e("TESSA", "Fehler beim Öffnen der Datei: " + path, e);
            return null;
        }
    }


    /**
     * Öffnet eine PDF-Datei aus dem Assets-Ordner.
     * Die Datei wird in den internen Speicher kopiert und dann angezeigt.
     *
     * @param assetPath Pfad zur PDF-Datei im Assets-Ordner
     */
    private void openPdf(String assetPath) {
        try (InputStream in = getAssets().open(assetPath)) {
            File out = new File(getFilesDir(), new File(assetPath).getName());
            try (FileOutputStream fos = new FileOutputStream(out)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) fos.write(buf, 0, len);
            }
            Uri uri = FileProvider.getUriForFile(
                    this, getPackageName()+".fileprovider", out
            );
            startActivity(new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/pdf")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
        } catch (IOException e) {
            Toast.makeText(this, "PDF nicht gefunden", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Wird beim Beenden der Activity aufgerufen.
     * Der SensorListener wird abgemeldet und die Lautstärke zurückgesetzt.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null && proximityListener != null) {
            sensorManager.unregisterListener(proximityListener);
        }
        if (audioManager != null && isVolumeAdjusted) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
        }
    }
}
