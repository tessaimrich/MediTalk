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

public class PillDetailActivity extends BaseMedicationDetailActivity {
    private ImageButton btnAudio, btnPdf;
    // Näherungssensor
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximityListener;

    // AudioManager für Lautstärke
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

    @Override
    protected void onMedicationLoaded(Medication med) {
        // TTS-Button
        btnAudio = findViewById(R.id.btnAudio1);
        final boolean[] isSpeaking = {false};
        btnAudio.setOnClickListener(v -> {
            if (ttsService == null || !ttsService.isTTSReady()) {
                Toast.makeText(this, "Sprachausgabe nicht bereit", Toast.LENGTH_SHORT).show();
                return;
            }if (isSpeaking[0]) {
                        // gerade am Sprechen → stoppen
                        ttsService.stop();
                        isSpeaking[0] = false;
                        Toast.makeText(this, "Wiedergabe gestoppt", Toast.LENGTH_SHORT).show();
                        // ggf. Icon zurücksetzen: btnAudio.setImageResource(R.drawable.ic_play);
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

        // PDF-Button
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
    private void setupAudioAndSensor() {
        // AudioManager initialisieren und Lautstärke speichern
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (originalVolume < maxVol / 2) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol - 1, 0);
            Toast.makeText(this, "Basis-Lautstärke erhöht", Toast.LENGTH_SHORT).show();
        }

        // Näherungssensor initialisieren
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
    // Utility aus BaseMedicationDetailActivity oder kopieren
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Sensor-Listener abmelden
        if (sensorManager != null && proximityListener != null) {
            sensorManager.unregisterListener(proximityListener);
        }
        // Lautstärke zurücksetzen
        if (audioManager != null && isVolumeAdjusted) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
        }
    }
}
