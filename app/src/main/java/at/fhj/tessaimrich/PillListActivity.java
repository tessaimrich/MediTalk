package at.fhj.tessaimrich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

/**
 * Die PillListActivity zeigt alle Medikamente der Kategorie "pills" in der aktuell gewählten Sprache an.
 * Diese Aktivity lädt die entsprechenden Medikamente aus der Datenbank und zeigt sie in einer Liste an.
 * Nutzer:innen können ein Medikament auswählen, dadurch dann zur Detailansicht des gewählten Medikaments wechseln
 * oder zur CategoryActivity zurückkehren
 * Diese Klasse erbt von BaseDrawerActivity, damit das Navigationsmenü verwendet werden kann.
 */
public class PillListActivity extends BaseDrawerActivity {

    private ListView lvPills;
    private ImageButton btnHome, btnNext;
    private List<String> pills;
    private int selectedPos = -1;

/**
 * Wird beim Start der Aktivität aufgerufen.
 * Diese Methode lädt die Benutzeroberfläche, liest die aktuell gewählte Sprache,
 * initialisiert bei Bedarf Testdaten für Medikamente und zeigt die Liste aller
 * Medikamente der Kategorie "pills" an. Außerdem werden die Schaltflächen zum
 * Zurücknavigieren und Weiterklicken eingerichtet.
 * @param savedInstanceState Zustand bei erneutem Erstellen, zum Beispiel nach dem Drehen des Bildschirms
 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_pill_list,
                findViewById(R.id.content_frame),
                true
        );

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs.getString("language", Locale.getDefault().getLanguage());

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        List<Medication> existing = db.medicationDao()
                .findByCategoryAndLanguage("pills", lang);
        if (existing.isEmpty()) {
            db.medicationDao().insertAll(
                    new Medication("Amlodipine Valsartan Mylan", "pills", lang, "amlodipin", "amlodipin_" + lang + ".txt", "AmlodipineValsartanMylan_" + lang.toUpperCase() + ".pdf"),
                    new Medication("Cymbalta",                  "pills", lang, "cymbalta",    "cymbalta_"  + lang + ".txt", "Cymbalta_"                + lang.toUpperCase() + ".pdf"),
                    new Medication("Eliquis",                   "pills", lang, "eliquis",     "eliquis_"   + lang + ".txt", "Eliquis_"                 + lang.toUpperCase() + ".pdf"),
                    new Medication("Nilemdo",                   "pills", lang, "nilemdo",     "nilemdo_"   + lang + ".txt", "Nilemdo_"                 + lang.toUpperCase() + ".pdf"),
                    new Medication("Qtern",                     "pills", lang, "qtern",       "qtern_"     + lang + ".txt", "Qtern_"                   + lang.toUpperCase() + ".pdf")
            );
            Log.d("DB", "Pill-Testdaten für Sprache " + lang + " eingefügt");
        }

        List<Medication> meds = db.medicationDao()
                .findByCategoryAndLanguage("pills", lang);

        pills = new ArrayList<>();
        for (Medication m : meds) {
            pills.add(m.getName());
        }
        lvPills  = findViewById(R.id.lvPills);
        btnHome  = findViewById(R.id.btnHome);
        btnNext  = findViewById(R.id.btnNext);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pills) {
        /**
         * Liefert die Ansicht für jedes Listenelement.
         * Das aktuell ausgewählte Element wird fett dargestellt.
         * @param position Position des Elements in der Liste
         * @param convertView Alte Ansicht (falls vorhanden) zum Wiederverwenden
         * @param parent Übergeordnete Ansicht
         * @return Die fertige Ansicht für das Listenelement.
         */
         @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
             if (tv != null) {
                 tv.setTextColor(ContextCompat.getColor(getContext(), R.color.med_text_darkgray));
                 if (position == selectedPos) {
                     tv.setTypeface(null, Typeface.BOLD);
                 } else {
                     tv.setTypeface(null, Typeface.NORMAL);
                 }
             } else {
                 Log.w("Adapter", "TextView nicht gefunden");
             }

                return view;
            }
        };
        lvPills.setAdapter(adapter);

        lvPills.setOnItemClickListener((parent, view, position, id) -> {
            selectedPos = position;
            adapter.notifyDataSetChanged();
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(PillListActivity.this, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnNext.setOnClickListener(v -> {
            if (selectedPos >= 0) {
                Intent intent = new Intent(PillListActivity.this, PillDetailActivity.class);
                intent.putExtra("med_name", pills.get(selectedPos));
                startActivity(intent);
            }
        });
    }
}
