package at.fhj.tessaimrich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

public class InhalationListActivity extends BaseDrawerActivity {

    private ListView lvInhalations;
    private ImageButton btnHome, btnNext;
    private List<String> inhalations;
    private int selectedPos = -1;  // aktuell ausgewählter Eintrag



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_inhalation_list,
                findViewById(R.id.content_frame),
                true
        );
        //Sprache ermitteln
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs.getString("language", Locale.getDefault().getLanguage());

        //DB-Instanz holen
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        // Testdaten für Inhalationen einmal pro Sprache anlegen
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

        // 4) Alle Inhalations-Einträge dieser Sprache aus DB lesen
        List<Medication> meds = db.medicationDao()
                .findByCategoryAndLanguage("inhalation", lang);

        // 5) Nur die Namen extrahieren
        inhalations = new ArrayList<>();
        for (Medication m : meds) {
            inhalations.add(m.getName());
        }
        lvInhalations  = findViewById(R.id.lvInhalations);
        btnHome  = findViewById(R.id.btnHome);
        btnNext  = findViewById(R.id.btnNext);

        // Adapter, der im getView() das ausgewählte Item fettgedruckt macht
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, inhalations) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.med_text_darkgray));

                if (position == selectedPos) {
                    tv.setTypeface(null, Typeface.BOLD);    // fettgedruckt, wenn ausgewählt
                } else {
                    tv.setTypeface(null, Typeface.NORMAL);
                }
                return view;
            }
        };
        lvInhalations.setAdapter(adapter);



        // Tippen auf Medikamentennamen: Auswahl merken
        lvInhalations.setOnItemClickListener((parent, view, position, id) -> {
            selectedPos = position;
            adapter.notifyDataSetChanged();
        });


        // Home-Button: zurück zur CategoryActivity
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(InhalationListActivity.this, CategoryActivity.class);
            // damit keine doppelten CategoryActivity-Instanzen im Back-Stack landen
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();  // schließt die InhalationListActivity
        });


        // Weiter-Button
        btnNext.setOnClickListener(v -> {
            if (selectedPos >= 0) {         // Nur weiter wenn ein Medikament ausgewählt ist
                Intent intent = new Intent(InhalationListActivity.this, InhalationDetailActivity.class);
                intent.putExtra("inhalation_name", inhalations.get(selectedPos));
                startActivity(intent);
            }
        });
    }
}
