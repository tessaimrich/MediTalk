package at.fhj.tessaimrich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

/**
 * Abstrakte Basisklasse für Medikament-Detail-Activities.
 * Diese Klasse lädt ein Medikament anhand seines Namens und der Spracheinstellungen der Nutzer:innen
 * aus der Datenbank und stellt die Grundstruktur für die Activities bereit.
 * Die Klasse soll eine richtige Weiterleitung zur passenden Detail-Activity ermöglichen.
 */
public abstract class BaseMedicationDetailActivity extends BaseDrawerActivity {
    protected Medication med;
    protected String currentLang;

    /**
     * Initialisiert die Activity, lädt die Sprache, das Layout, Medikament
     * und setzt den Home-Button für alle Detail-Activities
     * @param savedInstanceState gespeicherter Zustand der Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String raw = prefs.getString("language", Locale.getDefault().getLanguage());
        currentLang = raw.split("-")[0].toLowerCase();

        getLayoutInflater().inflate(getLayoutResource(),
                findViewById(R.id.content_frame),
                true);
        TextView title = findViewById(getTitleViewId());

        String medName = getIntent().getStringExtra("med_name");
        if (medName == null) {
            Toast.makeText(this, "Medikament nicht angegeben", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        title.setText(medName);

        med = AppDatabase.getInstance(this)
                .medicationDao()
                .findByNameAndLanguage(medName, currentLang);
        if (med == null) {
            Toast.makeText(this, "Medikament nicht gefunden", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        ImageButton btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Class<?> parent = getParentActivityClass();
            startActivity(new Intent(this, parent)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        });

        onMedicationLoaded(med);
    }

    /**
     * Gibt die Klasse der Eltern-Activity zurück, zu der bei Klick auf den Home-Button navigiert wird.
     * Standardmäßig bei uns ist es die CategoryActivity.
     * @return Elternklasse der Activity
     */
    protected Class<?>getParentActivityClass(){
       return CategoryActivity.class;
    }

    /**
     *  Muss von der Unterklasse implementiert werden, um das spezifische Layout bereitzustellen.
     * @return Layout-Resource z.B R.layout.activity_pill_detail
     */
    protected abstract int getLayoutResource();

    /** Muss von der Unterklasse implementiert werden, um den Titel-TextView zu identifizieren.
     * @return liefert die ID des Titel-TextViews zurück (z.B. R.id.tvPillName)
     */
    protected abstract int getTitleViewId();

    /** Wird aufgerufen, nachdem `med` und `currentLang` aus der DB geladen wurde.
     * @param med das geladene Medikament-Objekt
     */
    protected abstract void onMedicationLoaded(Medication med);
}
