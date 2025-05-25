/*
package at.fhj.tessaimrich;
 */

import android.content.Intent;
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




/*
public class PillDetailActivity extends AppCompatActivity {
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
    private String[] pdfAssets = {      //Asset = eine PDF-Datei im assets/-Ordner, die man per getAssets() in der App laden kann.
            "amlodipine.pdf",
            "cymbalta.pdf",
            "nilemdo.pdf",
            "qtern.pdf",
            "eliquis.pdf"
    };
    private String[] pillNames;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_detail);

        // Pillen-Name lesen und setzen
        int pillId = getIntent().getIntExtra(EXTRA_PILL_ID, 0);
        pillNames = getResources().getStringArray(R.array.pill_names);
        TextView tvName = findViewById(R.id.tvPillName);
        tvName.setText(pillNames[pillId]);

        // 2) Audio-Buttons
        setupAudio(findViewById(R.id.btnAudio1), audioQ1[pillId]);
        setupAudio(findViewById(R.id.btnAudio2), audioQ2[pillId]);
        setupAudio(findViewById(R.id.btnAudio3), audioQ3[pillId]);

        // 3) PDF-Buttons
        setupPdf(findViewById(R.id.btnPdf1), pdfAssets[pillId]);
        setupPdf(findViewById(R.id.btnPdf2), pdfAssets[pillId]);
        setupPdf(findViewById(R.id.btnPdf3), pdfAssets[pillId]);

        // 4) Home-Button
        findViewById(R.id.btnHome).setOnClickListener(v -> finish());
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
                // über FileProvider öffnen (bitte in Manifest + res/xml/file_paths.xml konfigurieren)
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
}

*/