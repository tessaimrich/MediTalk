package at.fhj.tessaimrich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

public class PillListActivity extends BaseDrawerActivity {

    private ListView lvPills;
    private ImageButton btnHome, btnNext;
    private String[] pills = {
            "Amlodipine Valsartan Mylan",
            "Cymbalta",
            "Eliquis ",
            "Nilemdo",
            "Qtern"
    };
    private int selectedPos = -1;  // aktuell ausgewählter Eintrag



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_pill_list,
                findViewById(R.id.content_frame),
                true
        );

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        if (db.medicationDao().getAll("en").isEmpty()) {
            db.medicationDao().insertAll(
                    // Englisch
                    new Medication("Amlodipine Valsartan Mylan", "Pills", "en", "amlodipin", "amlodipin_en.txt", "AmlodipineValsartanMylan_EN.pdf"),
                    new Medication("Cymbalta",                  "Pills", "en", "cymbalta",  "cymbalta_en.txt", "Cymbalta_EN.pdf"),
                    new Medication("Eliquis",                   "Pills", "en", "eliquis",   "eliquis_en.txt", "Eliquis_EN.pdf"),
                    new Medication("Nilemdo",                   "Pills", "en", "nilemdo",   "nilemdo_en.txt", "Nilemdo_EN.pdf"),
                    new Medication("Qtern",                     "Pills", "en", "qtern",     "qtern_en.txt", "Qtern_EN.pdf"),
                    // Slowenisch
            new Medication("Amlodipine Valsartan Mylan", "Pills", "sl", "amlodipin", "amlodipin_sl.txt", "AmlodipineValsartanMylan_SL.pdf"),
                    new Medication("Cymbalta",                  "Pills", "sl", "cymbalta",  "cymbalta_sl.txt", "Cymbalta_SL.pdf"),
                    new Medication("Eliquis",                   "Pills", "sl", "eliquis",   "eliquis_sl.txt", "Eliquis_SL.pdf"),
                    new Medication("Nilemdo",                   "Pills", "sl", "nilemdo",   "nilemdo_sl.txt", "Nilemdo_SL.pdf"),
                    new Medication("Qtern",                     "Pills", "sl", "qtern",     "qtern_sl.txt", "Qtern_SL.pdf"),

                    // Kroatisch
                    new Medication("Amlodipine Valsartan Mylan", "Pills", "hr", "amlodipin", "amlodipin_hr.txt", "AmlodipineValsartanMylan_HR.pdf"),
                    new Medication("Cymbalta",                  "Pills", "hr", "cymbalta",  "cymbalta_hr.txt", "Cymbalta_HR.pdf"),
                    new Medication("Eliquis",                   "Pills", "hr", "eliquis",   "eliquis_hr.txt", "Eliquis_HR.pdf"),
                    new Medication("Nilemdo",                   "Pills", "hr", "nilemdo",   "nilemdo_hr.txt", "Nilemdo_HR.pdf"),
                    new Medication("Qtern",                     "Pills", "hr", "qtern",     "qtern_hr.txt", "Qtern_HR.pdf"),

                    // Italienisch
                    new Medication("Amlodipine Valsartan Mylan", "Pills", "it", "amlodipin", "amlodipin_it.txt", "AmlodipineValsartanMylan_IT.pdf"),
                    new Medication("Cymbalta",                  "Pills", "it", "cymbalta",  "cymbalta_it.txt", "Cymbalta_IT.pdf"),
                    new Medication("Eliquis",                   "Pills", "it", "eliquis",   "eliquis_it.txt", "Eliquis_IT.pdf"),
                    new Medication("Nilemdo",                   "Pills", "it", "nilemdo",   "nilemdo_it.txt", "Nilemdo_IT.pdf"),
                    new Medication("Qtern",                     "Pills", "it", "qtern",     "qtern_it.txt", "Qtern_IT.pdf"),

                    // Spanisch
                    new Medication("Amlodipine Valsartan Mylan", "Pills", "es", "amlodipin", "amlodipin_es.txt", "AmlodipineValsartanMylan_ES.pdf"),
                    new Medication("Cymbalta",                  "Pills", "es", "cymbalta",  "cymbalta_es.txt", "Cymbalta_ES.pdf"),
                    new Medication("Eliquis",                   "Pills", "es", "eliquis",   "eliquis_es.txt", "Eliquis_ES.pdf"),
                    new Medication("Nilemdo",                   "Pills", "es", "nilemdo",   "nilemdo_es.txt", "Nilemdo_ES.pdf"),
                    new Medication("Qtern",                     "Pills", "es", "qtern",     "qtern_es.txt", "Qtern_ES.pdf"),

                    // Französisch
                    new Medication("Amlodipine Valsartan Mylan", "Pills", "fr", "amlodipin", "amlodipin_fr.txt", "AmlodipineValsartanMylan_FR.pdf"),
                    new Medication("Cymbalta",                  "Pills", "fr", "cymbalta",  "cymbalta_fr.txt", "Cymbalta_FR.pdf"),
                    new Medication("Eliquis",                   "Pills", "fr", "eliquis",   "eliquis_fr.txt", "Eliquis_FR.pdf"),
                    new Medication("Nilemdo",                   "Pills", "fr", "nilemdo",   "nilemdo_fr.txt", "Nilemdo_FR.pdf"),
                    new Medication("Qtern",                     "Pills", "fr", "qtern",     "qtern_fr.txt", "Qtern_FR.pdf")
            );
            Log.d("DB", "Testmedikamente wurden eingefügt");
        }
        lvPills  = findViewById(R.id.lvPills);
        btnHome  = findViewById(R.id.btnHome);
        btnNext  = findViewById(R.id.btnNext);

        // Adapter, der im getView() das ausgewählte Item fettgedruckt macht
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pills) {
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
        lvPills.setAdapter(adapter);



        // Tippen auf Medikamentennamen: Auswahl merken
        lvPills.setOnItemClickListener((parent, view, position, id) -> {
            selectedPos = position;
            adapter.notifyDataSetChanged();
        });


        // Home-Button: zurück zur CategoryActivity
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(PillListActivity.this, CategoryActivity.class);
            // damit keine doppelten CategoryActivity-Instanzen im Back-Stack landen
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();  // schließt die PillListActivity
        });


        // Weiter-Button
        btnNext.setOnClickListener(v -> {
            if (selectedPos >= 0) {         // Nur weiter wenn ein Medikament ausgewählt ist
                Intent intent = new Intent(PillListActivity.this, PillDetailActivity.class);
                intent.putExtra("pill_name", pills[selectedPos]);
                startActivity(intent);
            }
        });
    }
}
