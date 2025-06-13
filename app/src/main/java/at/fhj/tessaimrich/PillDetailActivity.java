
package at.fhj.tessaimrich;


import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;
import java.util.Objects;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PillDetailActivity extends BaseDrawerActivity {

    private TextToSpeech tts;
    private String selectedLanguageCode;

    //CODE IST NUR ERSTER VERSUCH GEWESEN - WIRD NOCH GEÄNDERT

    /* in der Zwischenzeit auskommentiert
    public static final String EXTRA_PILL_ID = "pill_id";
    private int[] audioQ1 = {
            R.raw.amlodipine_q1, R.raw.cymbalta_q1, R.raw.nilemdo_q1, R.raw.qtern_q1, R.raw.eliquis_q1
    };
    private int[] audioQ2 = {
            R.raw.amlodipine_q2, R.raw.cymbalta_q2, R.raw.nilemdo_q2, R.raw.qtern_q2, R.raw.eliquis_q2
    };
    private int[] audioQ3 = {
            R.raw.amlodipine_q3, R.raw.cymbalta_q3, R.raw.nilemdo_q3, R.raw.qtern_q3, R.raw.eliquis_q3
    };
    */
    private String[] pdfAssets = {      //Asset = eine PDF-Datei im assets/-Ordner, die man per getAssets() in der App laden kann.
            "amlodipine.pdf",
            "cymbalta.pdf",
            "nilemdo.pdf",
            "qtern.pdf",
            "eliquis.pdf"
    };
    private String[] pillNames;

    private boolean ttsReady = false;

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
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale locale = new Locale(selectedLanguageCode);
                int result = tts.setLanguage(locale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsReady = false;
                } else {
                    ttsReady = true;
                }
            } else {
                ttsReady = false;
            }
        });

        ImageButton btnAudio = findViewById(R.id.btnAudio1);
        btnAudio.setOnClickListener(v -> {
            String name = getIntent().getStringExtra("pill_name");
            if (name != null) {
                startTextToSpeechForPill(name);
            }
        });

        // Pillen-Name lesen und setzen
        //int pillId = getIntent().getIntExtra(EXTRA_PILL_ID, 0);
        pillNames = getResources().getStringArray(R.array.pill_names);
        TextView tvName = findViewById(R.id.tvPillName);
        //tvName.setText(pillNames[pillId]);

        // Audio-Buttons
        /*setupAudio(findViewById(R.id.btnAudio1), audioQ1[pillId]);
        setupAudio(findViewById(R.id.btnAudio2), audioQ2[pillId]);
        setupAudio(findViewById(R.id.btnAudio3), audioQ3[pillId]);

        // PDF-Buttons
        setupPdf(findViewById(R.id.btnPdf1), pdfAssets[pillId]);
        setupPdf(findViewById(R.id.btnPdf2), pdfAssets[pillId]);
        setupPdf(findViewById(R.id.btnPdf3), pdfAssets[pillId]);
        */

        // Home-Button: zurück zur CategoryActivity
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(PillDetailActivity.this, CategoryActivity.class);
            // Optional: vorhandene Instanz wiederverwenden und Stack bereinigen
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();  // eigene Activity schließen
        });
    }

    private void startTextToSpeechForPill(String pillNameRaw) {
        // Name bereinigen → z. B. "1. Amlodipine Valsartan Mylan" → "amlodipin"
        String pillKey = pillNameRaw.toLowerCase().contains("amlodipine") ? "amlodipin" : ""; // hier ggf. Mapping-Tabelle einsetzen

        if (pillKey.isEmpty()) return;

        // Textdateiname vorbereiten
        String filename = "tts.pills/" + pillKey + "_" + selectedLanguageCode + ".txt";

        try {
            InputStreamReader isr = new InputStreamReader(getAssets().open(filename));
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder textBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                textBuilder.append(line).append("\n");
            }
            reader.close();

            String ttsText = textBuilder.toString().trim();

            // TTS starten
            if (tts != null && ttsReady) {
                tts.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null, "tts_id");
            } else {
                Toast.makeText(this, "Sprachausgabe noch nicht bereit", Toast.LENGTH_SHORT).show();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupAudio(ImageButton btn, int audioResId) {
        btn.setOnClickListener(v -> {
            MediaPlayer mp = MediaPlayer.create(this, audioResId);
            mp.setOnCompletionListener(m -> m.release());
            mp.start();
        });
    }

    private void setupPdf(ImageButton btn, String assetName) {
        btn.setOnClickListener(v -> {
            try {
                // PDF aus assets in Cache kopieren
                File outFile = new File(getCacheDir(), assetName);
                if (!outFile.exists()) {
                    InputStream is = getAssets().open(assetName);
                    FileOutputStream fos = new FileOutputStream(outFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
                    fos.close();
                    is.close();
                }
                // über FileProvider öffnen
                Uri uri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        outFile
                );
                Intent pdfIntent = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(uri, "application/pdf")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(pdfIntent);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

}

