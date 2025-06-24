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

public abstract class BaseMedicationDetailActivity extends BaseDrawerActivity {
    protected Medication med;
    protected String currentLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sprache laden
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String raw = prefs.getString("language", Locale.getDefault().getLanguage());
        currentLang = raw.split("-")[0].toLowerCase();

        // Layout & Title laden
        getLayoutInflater().inflate(getLayoutResource(),
                findViewById(R.id.content_frame),
                true);
        TextView title = findViewById(getTitleViewId());

        // Name aus Intent
        String medName = getIntent().getStringExtra("med_name");
        if (medName == null) {
            Toast.makeText(this, "Medikament nicht angegeben", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        title.setText(medName);

        // DB-Abfrage
        med = AppDatabase.getInstance(this)
                .medicationDao()
                .findByNameAndLanguage(medName, currentLang);
        if (med == null) {
            Toast.makeText(this, "Medikament nicht gefunden", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
// Home-Button einmal zentral setzen
        ImageButton btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            // springe zurück in die jeweils passende List-Activity
            Class<?> parent = getParentActivityClass();
            startActivity(new Intent(this, parent)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        });

        // Hook für TTS/PDF in Subklassen
        onMedicationLoaded(med);
    }
    protected abstract Class<?> getParentActivityClass();
    /** Liefert das Layout-Ressourcen-ID der Subklasse zurück */
    protected abstract int getLayoutResource();

    /** Liefert die ID des Titel-TextViews zurück (z.B. R.id.tvPillName) */
    protected abstract int getTitleViewId();

    /** Wird aufgerufen, nachdem `med` und `currentLang` gesetzt wurden */
    protected abstract void onMedicationLoaded(Medication med);
}
