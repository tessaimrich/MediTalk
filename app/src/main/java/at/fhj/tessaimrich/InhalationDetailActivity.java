package at.fhj.tessaimrich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;


public class InhalationDetailActivity extends BaseDrawerActivity {
    private ImageButton btnPdf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_inhalation_detail,
                findViewById(R.id.content_frame),
                true
        );
        // Gewählten Inhalations-Namen auslesen
        String inhalationName = getIntent().getStringExtra("inhalation_name");
        ((TextView) findViewById(R.id.tvInhalationName))
                .setText(inhalationName != null ? inhalationName : "");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String rawLang = prefs.getString("language", Locale.getDefault().getLanguage());
        // nur Basis-Code, alles nach '-' verwerfen:
        String lang = rawLang.split("-")[0].toLowerCase();
        Log.d("InhalationDetail", "Current language code: '" + lang + "'");

        // PDF-Button initialisieren
        btnPdf = findViewById(R.id.btnPdfinhalation);
        btnPdf.setOnClickListener(v -> {
            // PDF-Asset aus der DB abrufen
            Medication med = AppDatabase
                    .getInstance(getApplicationContext())
                    .medicationDao()
                    .findByNameAndLanguage(inhalationName, lang);
            String assetName = med.getPdfAsset();
            Log.d("InhalationDetail", "DB liefert pdfAsset = '" + assetName + "'");
            if (med == null || med.getPdfAsset() == null) {
                Toast.makeText(this, "Keine PDF verfügbar", Toast.LENGTH_SHORT).show();
                return;
            }
            openPdfFromAssets(med.getPdfAsset());
        });
        ImageButton btnHome = findViewById(R.id.btnHome);
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, CategoryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
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

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    outFile
            );
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

        } catch (IOException e) {
            Toast.makeText(this, "Fehler beim Öffnen der PDF", Toast.LENGTH_SHORT).show();
            Log.e("InhalationDetail", "PDF open error", e);
        }
    }
}

