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


/**
 * Detailansicht für ein Medikament der Kategorie „Cream“.
 * <p>
 * Diese Klasse erweitert {@link BaseMedicationDetailActivity} und implementiert das Layout,
 * TTS-Funktionalität, PDF-Anzeige und die automatische Lautstärkeregelung
 * mithilfe des Näherungssensors für Cremes.
 */
public class CreamDetailActivity extends BaseMedicationDetailActivity {
    private ImageButton btnAudio, btnPdf;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximityListener;
    private AudioManager audioManager;
    private int originalVolume;
    private boolean isVolumeAdjusted = false;
    //getLayoutResource() Gibt das Layout dieser Detailansicht zurück.Wird von der Basisklasse beim Laden verwendet
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_cream_detail;
    }
    // getTitleViewId() Gibt die ID des TextViews zurück, das den Medikamentennamen enthält. Wird in der Basisklasse zur Titelanzeige verwendet.
    @Override
    protected int getTitleViewId() {
        return R.id.tvCreamName;
    }




    /**
     * Wird aufgerufen, sobald das Medikament aus der Datenbank geladen wurde.
     * Hier wird die TTS-Funktion eingerichtet, der PDF-Button aktiviert
     * und der Näherungssensor initialisiert.
     *
     * @param med Das geladene Medikamentenobjekt
     */
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
                // Text aus Assets laden und vorlesen
                String rawKey = med.getTtsText();
                String key = rawKey.toLowerCase(Locale.ROOT);
                String lang = currentLang.toLowerCase(Locale.ROOT);

                String assetPath = "tts/pills/cream/"
                        + key + "_" + lang + ".txt";
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
    /**
     * Initialisiert AudioManager und registiert einen Proximity-Sensor-Listener.
     * <p>
     * • Speichert die aktuelle Musiklautstärke und erhöht sie auf fast Maximum,
     *   wenn sie unter der Hälfte liegt – damit z.B. Text‑to‑Speech gut hörbar ist.
     * • Initialisiert den Näherungssensor (in cm). Erkennt Geräte-Nähe (z.B. <≃5cm),
     *   indem gemessen wird, ob der Wert < proximitySensor.getMaximumRange() ist.
     * • Bei Nähe: Lautstärke auf ⅓ Max setzen, bei Entfernung: auf Original‑Wert zurückholen.
     * • Toast-Meldungen informieren den Nutzer über „Ohr erkannt – Lautstärke reduziert“
     *   oder Rückkehr zu „Lautsprecher“-Modus.
     * </p>
     */
    private void setupAudioAndProximity() {
        // AudioManager initialisieren & Basislautstärke merken
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




    /**
     * Lädt Textdateien aus dem Assets-Ordner, z.B. für TTS-Wiedergabe.
     *
     * @param assetPath Pfad zur Datei im Assets-Ordner
     * @return Der geladene Text oder {@code null}, falls nicht gefunden
     */
    private String loadAssetText(String assetPath) {
        try (InputStream in = getAssets().open(assetPath)) {
            byte[] buf = new byte[in.available()];
            in.read(buf);
            return new String(buf);
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * Öffnet eine PDF-Datei aus dem Assets-Ordner über eine temporäre Datei im internen Speicher.
     *
     * @param assetPath Pfad zur PDF-Datei im Assets-Verzeichnis
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
