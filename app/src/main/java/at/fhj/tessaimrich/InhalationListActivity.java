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
 * Diese Activity zeigt eine Liste aller Inhalationsmedikamente an,
 * die in der Datenbank für die gewählte Sprache gespeichert sind.
 * Die Nutzer:innen können ein Medikament auswählen und zur Detailansicht weitergehen.
 * Es gibt außerdem einen Home-Button zurück zur Kategorieübersicht.
 */
public class InhalationListActivity extends BaseDrawerActivity {

    private ListView lvInhalations;
    private ImageButton btnHome, btnNext;
    private List<String> inhalations;
    private int selectedPos = -1;

    /**
     * Wird beim Starten der Activity aufgerufen.
     * Initialisiert die Datenbank, lädt Medikamentendaten,
     * bereitet die Ansicht vor und definiert das Verhalten der Buttons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_inhalation_list,
                findViewById(R.id.content_frame),
                true
        );
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs.getString("language", Locale.getDefault().getLanguage());

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        List<Medication> existing = db.medicationDao()
                .findByCategoryAndLanguage("inhalation", lang);
        if (existing.isEmpty()) {
            db.medicationDao().insertAll(
                    new Medication(
                            "Enerzair",
                            "inhalation",
                            lang,
                            "enerzair",
                            "",
                            "Enerzair_" + lang.toUpperCase() + ".pdf"
                    ),
                    new Medication(
                            "Ultibro",
                            "inhalation",
                            lang,
                            "ultibro",
                            "",
                            "Ultibro_" + lang.toUpperCase() + ".pdf"
                    )
            );
            Log.d("DB", "Inhalation-Testdaten für Sprache " + lang + " eingefügt");
        }

        List<Medication> meds = db.medicationDao()
                .findByCategoryAndLanguage("inhalation", lang);

        inhalations = new ArrayList<>();
        for (Medication m : meds) {
            inhalations.add(m.getName());
        }
        lvInhalations  = findViewById(R.id.lvInhalations);
        btnHome  = findViewById(R.id.btnHome);
        btnNext  = findViewById(R.id.btnNext);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, inhalations) {
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
        lvInhalations.setAdapter(adapter);

        lvInhalations.setOnItemClickListener((parent, view, position, id) -> {
            selectedPos = position;
            adapter.notifyDataSetChanged();
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(InhalationListActivity.this, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnNext.setOnClickListener(v -> {
            if (selectedPos >= 0) {
                Intent intent = new Intent(InhalationListActivity.this, InhalationDetailActivity.class);
                intent.putExtra("med_name", inhalations.get(selectedPos));
                startActivity(intent);
            }
        });
    }
}
