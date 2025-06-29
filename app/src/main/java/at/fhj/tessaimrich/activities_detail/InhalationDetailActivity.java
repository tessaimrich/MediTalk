package at.fhj.tessaimrich.activities_detail;

import android.content.Intent;
import android.net.Uri;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import at.fhj.tessaimrich.R;
import at.fhj.tessaimrich.base.BaseMedicationDetailActivity;
import at.fhj.tessaimrich.data.Medication;

public class InhalationDetailActivity extends BaseMedicationDetailActivity {
    private ImageButton btnPdf;
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_inhalation_detail;
    }

    @Override
    protected int getTitleViewId() {
        return R.id.tvInhalationName;
    }

    @Override
    protected void onMedicationLoaded(Medication med) {
        btnPdf = findViewById(R.id.btnPdfinhalation);
        btnPdf.setOnClickListener(v -> {
            String original = med.getPdfAsset();
            if (original == null || original.isEmpty()) {
                Toast.makeText(this, "Keine PDF verfügbar", Toast.LENGTH_SHORT).show();
                return;
            }

            // Basisschlüssel extrahieren (alles vor dem Unterstrich)
            int u = original.lastIndexOf('_');
            String base = (u > 0)
                    ? original.substring(0, u)
                    : original.replaceAll("\\.pdf$", "");

            // PDF-Name mit aktueller Sprache zusammensetzen
            String pdfName = base + "_" + currentLang.toUpperCase(Locale.ROOT) + ".pdf";
            openPdfFromAssets("pdfs/" + pdfName);
        });
        // Home-Button kommt automatisch aus BaseDrawerActivity
    }

    /** Kopiert die PDF aus assets und öffnet sie */
    private void openPdfFromAssets(String assetPath) {
        try (InputStream in = getAssets().open(assetPath)) {
            File outFile = new File(getFilesDir(), new File(assetPath).getName());
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
            startActivity(new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/pdf")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
        } catch (IOException e) {
            Toast.makeText(this, "Fehler beim Öffnen der PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
