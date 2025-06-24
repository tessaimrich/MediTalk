package at.fhj.tessaimrich;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import at.fhj.tessaimrich.data.Medication;

public class CreamDetailActivity extends BaseMedicationDetailActivity {
    private ImageButton btnAudio, btnPdf;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximityListener;
    private AudioManager audioManager;
    private int originalVolume;
    private boolean isVolumeAdjusted = false;
    @Override protected Class<?> getParentActivityClass() {
        return CreamListActivity.class;
    }
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_cream_detail;
    }

    @Override
    protected int getTitleViewId() {
        return R.id.tvCreamName;
    }

    @Override
    protected void onMedicationLoaded(Medication med) {
        // TTS-Button
        btnAudio = findViewById(R.id.btnAudioCream);
        final boolean[] isSpeaking = {false};
        btnAudio.setOnClickListener(v -> {
            if (ttsService == null || !ttsService.isTTSReady()) {
                Toast.makeText(this, "Sprachausgabe nicht bereit", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isSpeaking[0]) {
                // stoppen
                ttsService.stop();
                isSpeaking[0] = false;
                Toast.makeText(this, "Wiedergabe gestoppt", Toast.LENGTH_SHORT).show();
                // optional: btnAudio.setImageResource(R.drawable.ic_play);
            } else {
                // starten
                String assetPath = "tts/pills/cream/"
                        + med.getTtsText() + "_" + currentLang + ".txt";
                String text = loadAssetText(assetPath);
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
        btnPdf = findViewById(R.id.btnPdfCreamPdf);
        btnPdf.setOnClickListener(v -> {
            String asset = med.getPdfAsset();
            if (asset == null|| asset.isEmpty()) {
                Toast.makeText(this, "Keine PDF verfügbar", Toast.LENGTH_SHORT).show();
                return;
            }
            openPdf("pdfs/" + asset.trim());
        });
        setupAudioAndProximity();
        // Home-Button: automatisch von BaseDrawerActivity
    }
    private void setupAudioAndProximity() {
        // AudioManager initialisieren & Basislautstärke merken
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
                if (ttsService != null) {
                    if (distance < proximitySensor.getMaximumRange() && !isVolumeAdjusted) {
                        int reduced = Math.max(1, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 3);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, reduced, 0);
                        isVolumeAdjusted = true;
                        Toast.makeText(CreamDetailActivity.this,
                                "Ohr erkannt → Lautstärke reduziert",
                                Toast.LENGTH_SHORT).show();
                    } else if (distance >= proximitySensor.getMaximumRange() && isVolumeAdjusted) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                        isVolumeAdjusted = false;
                        Toast.makeText(CreamDetailActivity.this,
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
    // Hilfsmethode aus der Basisklasse kopiert oder ggf. in Utility auslagern
    private String loadAssetText(String assetPath) {
        try (InputStream in = getAssets().open(assetPath)) {
            byte[] buf = new byte[in.available()];
            in.read(buf);
            return new String(buf);
        } catch (IOException e) {
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
                    this, getPackageName() + ".fileprovider", out
            );
            startActivity(new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/pdf")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
        } catch (IOException e) {
            Toast.makeText(this, "Fehler beim Öffnen der PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
