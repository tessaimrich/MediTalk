package at.fhj.tessaimrich.activities_list;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import at.fhj.tessaimrich.activities_core_function.CategoryActivity;
import at.fhj.tessaimrich.activities_detail.CreamDetailActivity;
import at.fhj.tessaimrich.R;
import at.fhj.tessaimrich.base.BaseDrawerActivity;
import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Activity zur Anzeige einer Liste von verfügbaren Cremen (Medikamenten-Kategorie "cream").
 * Die Daten werden aus einer lokalen Room-Datenbank geladen, wobei Sprachpräferenzen berücksichtigt werden.
 * Die Nutzer:innen können eine Creme auswählen und zur Detailansicht navigieren.
 */
public class CreamListActivity extends BaseDrawerActivity {

    private ListView lvCreams;
    private ImageButton btnHome, btnNext;
    private List<String> creams;

    private int selectedPos = -1;

    /**
     * Wird beim Starten der Activity aufgerufen. Lädt und zeigt die Cremen basierend auf der
     * eingestellten Sprache, erlaubt die Auswahl einer Creme und ermöglicht Navigation zur Detailansicht.
     * @param savedInstanceState Zustand der Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_cream_list,
                findViewById(R.id.content_frame),
                true
        );
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs.getString("language", Locale.getDefault().getLanguage());

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        List<Medication> existing = db.medicationDao()
                .findByCategoryAndLanguage("cream", lang);
        if (existing.isEmpty()) {
            db.medicationDao().insertAll(
                    new Medication(
                            "Protopic",
                            "cream",
                            lang,
                            "protopic",
                            "protopic_" + lang + ".txt",
                            "Protopic_" + lang.toUpperCase() + ".pdf"
                    )
            );
            Log.d("DB", "Cream Protopic für Sprache " + lang + " eingefügt");
        }
        List<Medication> meds = db.medicationDao()
                .findByCategoryAndLanguage("cream", lang);

        creams = new ArrayList<>();
        for (Medication m : meds) {
            creams.add(m.getName());
        }
        lvCreams = findViewById(R.id.lvCreams);
        btnHome  = findViewById(R.id.btnHome);
        btnNext  = findViewById(R.id.btnNext);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                creams) {
            /**
             * Liefert die Ansicht für jedes Listenelement.
             * Das aktuell ausgewählte Element wird fett dargestellt.
             * @param position Position des Elements in der Liste
             * @param convertView Alte Ansicht (falls vorhanden) zum Wiederverwenden
             * @param parent Übergeordnete Ansicht
             * @return Die fertige Ansicht für das Listenelement.
             */
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
                if (tv != null) {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.med_text_darkgray));
                    tv.setTypeface(null, position == selectedPos
                            ? Typeface.BOLD
                            : Typeface.NORMAL);
                } else {
                    Log.w("Adapter", "TextView nicht gefunden");
                }


                return view;
            }
        };
        lvCreams.setAdapter(adapter);

        lvCreams.setOnItemClickListener((parent, view, position, id) -> {
            selectedPos = position;
            adapter.notifyDataSetChanged();
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(CreamListActivity.this, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnNext.setOnClickListener(v -> {
            if (selectedPos >= 0) {
                Intent intent = new Intent(CreamListActivity.this, CreamDetailActivity.class);
                intent.putExtra("med_name", creams.get(selectedPos));
                startActivity(intent);
            }
        });
    }
}
