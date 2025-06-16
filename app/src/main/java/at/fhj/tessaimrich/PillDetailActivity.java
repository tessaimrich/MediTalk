package at.fhj.tessaimrich;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
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

public class PillDetailActivity extends BaseDrawerActivity {

    private TTSService ttsService;
    private ImageButton btnAudio;
    private ImageButton btnPdf;
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
            // Aus DB den pdfAsset-Namen holen
            Medication med = AppDatabase
                    .getInstance(getApplicationContext())
                    .medicationDao()
                    .findByName(pillName);
            if (med == null || med.getPdfAsset() == null) {
                Toast.makeText(this, "Keine PDF verfügbar", Toast.LENGTH_SHORT).show();
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
    }

    /**
     * Lädt den TTS-Text aus DB + Assets
     */
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

    /**
     * Verbindung zum TTSService
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            ttsService = ((TTSService.LocalBinder) binder).getService();
            // sofort die aktuelle Sprache setzen
            SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
            ttsService.setLanguage(prefs.getString("selected_language", "en"));

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
        if (ttsService != null) unbindService(serviceConnection);
    }


    /**
     * Kopiert die PDF aus assets in den App-Files-Ordner
     * und öffnet sie dann mit einem externen PDF-Viewer.
     */

    private void logAllAssets() {
        AssetManager am = getAssets();
        try {
            // 1) Was liegt direkt im assets-Ordner?
            String[] roots = am.list("");
            Log.d("PillDetail", "assets/ root enthält: " + Arrays.toString(roots));
            // 2) Was liegt im Unterordner pdfs/ ?
            String[] pdfs = am.list("pdfs");
            Log.d("PillDetail", "assets/pdfs enthält: " + Arrays.toString(pdfs));
        } catch (IOException e) {
            Log.e("PillDetail", "Fehler beim Listen der Assets", e);
        }
    }
    private void openPdfFromAssets(String rawAssetName) {
        logAllAssets();
        AssetManager am = getAssets();

        // 1) Liste alle PDFs im Ordner assets/pdfs/
        String[] files = new String[0];
        try {
            files = am.list("pdfs");
            Log.d("PillDetail", "assets/pdfs enthält: " + Arrays.toString(files));
        } catch (IOException e) {
            Log.e("PillDetail", "Fehler beim Listen von assets/pdfs", e);
        }

        // 2) Baue den gewünschten Namen (mit .pdf-Endung)
        String desired = rawAssetName;
        if (!desired.toLowerCase().endsWith(".pdf")) {
            desired = desired + ".pdf";
        }
        Log.d("PillDetail", "Gewünschter Name (case-insensitive): " + desired);

        // 3) Suche in files[] nach einem Eintrag, der equalsIgnoreCase(desired) ist
        String actualFile = null;
        for (String f : files) {
            if (f.equalsIgnoreCase(desired)) {
                actualFile = f;
                break;
            }
        }
        if (actualFile == null) {
            Log.e("PillDetail", "Kein Asset gefunden, das '" + desired + "' entspricht");
            Toast.makeText(this, "PDF nicht gefunden: " + desired, Toast.LENGTH_LONG).show();
            return;
        }
        Log.d("PillDetail", "Gefundenes Asset (exakt): " + actualFile);

        // 4) Öffne das gefundene Asset und kopiere es ins interne Verzeichnis
        try (InputStream in = am.open("pdfs/" + actualFile)) {
            File outFile = new File(getFilesDir(), actualFile);
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }

            // 5) Erzeuge URI via FileProvider und starte den externen Viewer
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    outFile
            );
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/pdf")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

        } catch (FileNotFoundException fnf) {
            Log.e("PillDetail", "Asset nicht gefunden beim Öffnen: " + actualFile, fnf);
            Toast.makeText(this, "PDF nicht gefunden: " + actualFile, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("PillDetail", "Fehler beim Kopieren/Öffnen der PDF", e);
            Toast.makeText(this, "Fehler beim Öffnen der PDF", Toast.LENGTH_SHORT).show();
        }
    }

}