package at.fhj.tessaimrich;

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

import at.fhj.tessaimrich.data.Medication;


/**
 * Die {@code DropDetailActivity} zeigt die Detailansicht eines Medikaments der Kategorie "Tropfen".
 * <p>
 * Diese Klasse bietet die Anzeige einer PDF-Datei in der gewünschten Sprache.
 * <p>
 * Die Klasse erbt von {@link BaseMedicationDetailActivity}, wodurch UI-Struktur, Sprachlogik
 * und Home-Button automatisch verfügbar sind.
 */
public class DropDetailActivity extends BaseMedicationDetailActivity {
    private ImageButton btnPdf;
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_drop_detail;
    }

    @Override
    protected int getTitleViewId() {
        return R.id.tvDropName;
    }

    /**
     * Wird automatisch aufgerufen, sobald die Medikamentendaten aus der Datenbank geladen wurden.
     *
     * @param med Das geladene Medikamentenobjekt
     */
    @Override
    protected void onMedicationLoaded(Medication med) {
        // PDF-Button initialisieren
        btnPdf = findViewById(R.id.btnPdfdrop);
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
                    : original.replaceAll("\\.pdf$", "");   // falls kein Unterstrich vorhanden

            // PDF-Name mit aktueller Sprache zusammensetzen
            String pdfName = base + "_" + currentLang.toUpperCase(Locale.ROOT) + ".pdf";
            openPdfFromAssets("pdfs/" + pdfName);
        });

        // Home-Button kommt automatisch aus BaseDrawerActivity
    }


    /**
     * Öffnet eine PDF-Datei aus dem Assets-Ordner.
     * Dazu wird die Datei temporär in den internen Speicher kopiert und über einen PDF-Viewer geöffnet.
     *
     * @param assetPath Pfad zur PDF-Datei im assets-Verzeichnis
     */
    private void openPdfFromAssets(String assetPath) {
        try (InputStream in = getAssets().open(assetPath)) {
            // Ziel-Datei im internen App-Speicher
            File outFile = new File(getFilesDir(), new File(assetPath).getName());
            // Datei byteweise kopieren
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            // Datei als URI bereitstellen über FileProvider
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    outFile
            );
            // PDF-Viewer starten
            startActivity(new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/pdf")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            );
        } catch (IOException e) {
            Toast.makeText(this, "Fehler beim Öffnen der PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
