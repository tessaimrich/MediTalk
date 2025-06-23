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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;


public class CreamDetailActivity extends BaseDrawerActivity {

    private ImageButton btnAudio;
    private ImageButton btnPdf;
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

        // Audio-Button referenzieren
        btnAudio = findViewById(R.id.btnAudioCream);

        // Play/Stop-Logik
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

        // PDF-Button
        btnPdf = findViewById(R.id.btnPdfCreamPdf);
        btnPdf.setOnClickListener(v -> {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            String rawLang = prefs.getString("language",
                    Locale.getDefault().getLanguage());
            String lang = rawLang.split("-")[0].toLowerCase();
            Log.d("CreamDetail", "rawLang=" + rawLang + " → lang=" + lang);
            // Prüfen, ob für Kroatisch (hr) Einträge in der Datenbank vorhanden sind
            if (lang.equals("hr")) {
                Medication med = AppDatabase
                        .getInstance(getApplicationContext())
                        .medicationDao()
                        .findByNameAndLanguage(creamName, "hr");
                if (med == null) {
                    Log.d("CreamDetail", "Keine Einträge für Kroatisch (hr) in der Datenbank.");
                } else {
                    Log.d("CreamDetail", "Eintrag für Kroatisch (hr) gefunden.");
                }
            }
            Medication med = AppDatabase
                    .getInstance(getApplicationContext())
                    .medicationDao()
                    .findByNameAndLanguage(creamName,lang);
            if (med != null) {
                Log.d("CreamDetail", "DAO findByNameAndLanguage liefert pdfAsset=" + med.getPdfAsset());
            } else {
                Log.w("CreamDetail", "DAO findByNameAndLanguage liefert null für lang=" + lang);
            }
            if (med == null) {
                med = AppDatabase
                        .getInstance(getApplicationContext())
                        .medicationDao()
                        .findByName(creamName);
                if (med != null) {
                    Log.d("CreamDetail", "DAO findByName liefert pdfAsset=" + med.getPdfAsset());
                }
            }
            if (med == null || med.getPdfAsset() == null) {
                Toast.makeText(this, "Keine PDF verfügbar", Toast.LENGTH_SHORT).show();
                return;
            }
            openPdfFromAssets(med.getPdfAsset());
        });

        // Home-Button und TTS-Service
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private String loadTtsTextForCream(String name) {
        try {
            Medication med = AppDatabase
                    .getInstance(getApplicationContext())
                    .medicationDao()
                    .findByName(name);
            if (med == null) return null;

            String key = med.getTtsText();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String lang = prefs.getString("language", Locale.getDefault().getLanguage());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /** kopiert PDFs aus assets/pdfs/ und öffnet sie */
    private void openPdfFromAssets(String assetFileName) {
        AssetManager am = getAssets();
        try (InputStream in = am.open("pdfs/" + assetFileName)) {
            File outFile = new File(getFilesDir(), assetFileName);
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    outFile
            );
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

        } catch (IOException e) {
            Toast.makeText(this, "Fehler beim Öffnen der PDF", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }





}
