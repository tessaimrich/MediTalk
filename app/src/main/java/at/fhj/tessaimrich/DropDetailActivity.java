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
import android.preference.PreferenceManager;
import java.util.Locale;
import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

public class DropDetailActivity extends BaseDrawerActivity {
    private ImageButton btnPdf;
    private String dropName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_drop_detail,
                findViewById(R.id.content_frame),
                true
        );
        //  Gewählten Tropfen-Namen auslesen
        dropName = getIntent().getStringExtra("drop_name");
        ((TextView) findViewById(R.id.tvDropName))
                .setText(dropName != null ? dropName : "");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs.getString("language", Locale.getDefault().getLanguage());

        Log.d("DropDetail", "Current language code: '" + lang + "'");

        //pdf-Button
        btnPdf = findViewById(R.id.btnPdfdrop);
        btnPdf.setOnClickListener(v -> {
            Medication med = AppDatabase
                    .getInstance(getApplicationContext())
                    .medicationDao()
                    .findByNameAndLanguage(dropName,lang);
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

