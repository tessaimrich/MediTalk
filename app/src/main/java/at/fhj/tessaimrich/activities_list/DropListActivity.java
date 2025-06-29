package at.fhj.tessaimrich.activities_list;

import android.content.Intent;
import android.content.SharedPreferences;
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

import at.fhj.tessaimrich.activities_core_function.CategoryActivity;
import at.fhj.tessaimrich.activities_detail.DropDetailActivity;
import at.fhj.tessaimrich.R;
import at.fhj.tessaimrich.base.BaseDrawerActivity;
import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

/**
 * DropListActivity zeigt eine Liste von Medikamenten der Kategorie "drops" an.
 * Nutzer:innen können ein Medikament auswählen und zur Drop-Detailansicht wechseln.
 * Die Sprache wird über SharedPreferences bestimmt. Daten stammen aus Room-Datenbank.
 */

public class DropListActivity extends BaseDrawerActivity {

    private ListView lvDrops;
    private ImageButton btnHome, btnNext;
    private List<String> drops;
    private int selectedPos = -1;

    /**
     * Initialisiert die Aktivität, lädt Medikamente aus der Datenbank
     * und setzt Adapter, Click Listener sowie Spracheinstellungen.
     * @param savedInstanceState gespeicherter Zustand der Aktivität
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_drop_list,
                findViewById(R.id.content_frame),
                true
        );
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs.getString("language", Locale.getDefault().getLanguage());

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        List<Medication> existing = db.medicationDao()
                .findByCategoryAndLanguage("drops", lang);
        if (existing.isEmpty()) {
            db.medicationDao().insertAll(
                    new Medication(
                            "Catiolanze",
                            "drops",
                            lang,
                            "catiolanze",
                            "",
                            "Catiolanze_" + lang.toUpperCase() + ".pdf"
                    ),
                    new Medication(
                            "Simbrinza",
                            "drops",
                            lang,
                            "simbrinza",
                            "",
                            "Simbrinza_" + lang.toUpperCase() + ".pdf"
                    )
            );
            Log.d("DB", "Drops für Sprache " + lang + " eingefügt");
        }
        List<Medication> meds = db.medicationDao()
                .findByCategoryAndLanguage("drops", lang);
        drops = new ArrayList<>();
        for (Medication m : meds) {
            drops.add(m.getName());
        }
            lvDrops  = findViewById(R.id.lvDrops);
        btnHome  = findViewById(R.id.btnHome);
        btnNext  = findViewById(R.id.btnNext);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drops) {
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
        lvDrops.setAdapter(adapter);

        lvDrops.setOnItemClickListener((parent, view, position, id) -> {
            selectedPos = position;
            adapter.notifyDataSetChanged();
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(DropListActivity.this, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnNext.setOnClickListener(v -> {
            if (selectedPos >= 0) {
                Intent intent = new Intent(DropListActivity.this, DropDetailActivity.class);
                intent.putExtra("med_name", drops.get(selectedPos));
                startActivity(intent);
            }
        });
    }
}
