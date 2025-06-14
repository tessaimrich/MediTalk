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
                    new Medication("Amlodipine Valsartan Mylan", "Pills", "en", "amlodipin", "amlodipin_en.txt"),
                    new Medication("Cymbalta",                  "Pills", "en", "cymbalta",  "cymbalta_en.txt"),
                    new Medication("Eliquis",                   "Pills", "en", "eliquis",   "eliquis_en.txt"),
                    new Medication("Nilemdo",                   "Pills", "en", "nilemdo",   "nilemdo_en.txt"),
                    new Medication("Qtern",                     "Pills", "en", "qtern",     "qtern_en.txt"),
                    // Slowenisch
            new Medication("Amlodipine Valsartan Mylan", "Pills", "sl", "amlodipin", "amlodipin_sl.txt"),
                    new Medication("Cymbalta",                  "Pills", "sl", "cymbalta",  "cymbalta_sl.txt"),
                    new Medication("Eliquis",                   "Pills", "sl", "eliquis",   "eliquis_sl.txt"),
                    new Medication("Nilemdo",                   "Pills", "sl", "nilemdo",   "nilemdo_sl.txt"),
                    new Medication("Qtern",                     "Pills", "sl", "qtern",     "qtern_sl.txt"),

                    // Kroatisch
                    new Medication("Amlodipine Valsartan Mylan", "Pills", "hr", "amlodipin", "amlodipin_hr.txt"),
                    new Medication("Cymbalta",                  "Pills", "hr", "cymbalta",  "cymbalta_hr.txt"),
                    new Medication("Eliquis",                   "Pills", "hr", "eliquis",   "eliquis_hr.txt"),
                    new Medication("Nilemdo",                   "Pills", "hr", "nilemdo",   "nilemdo_hr.txt"),
                    new Medication("Qtern",                     "Pills", "hr", "qtern",     "qtern_hr.txt"),

                    // Italienisch
                    new Medication("Amlodipine Valsartan Mylan", "Pills", "it", "amlodipin", "amlodipin_it.txt"),
                    new Medication("Cymbalta",                  "Pills", "it", "cymbalta",  "cymbalta_it.txt"),
                    new Medication("Eliquis",                   "Pills", "it", "eliquis",   "eliquis_it.txt"),
                    new Medication("Nilemdo",                   "Pills", "it", "nilemdo",   "nilemdo_it.txt"),
                    new Medication("Qtern",                     "Pills", "it", "qtern",     "qtern_it.txt"),

                    // Spanisch
                    new Medication("Amlodipine Valsartan Mylan", "Pills", "es", "amlodipin", "amlodipin_es.txt"),
                    new Medication("Cymbalta",                  "Pills", "es", "cymbalta",  "cymbalta_es.txt"),
                    new Medication("Eliquis",                   "Pills", "es", "eliquis",   "eliquis_es.txt"),
                    new Medication("Nilemdo",                   "Pills", "es", "nilemdo",   "nilemdo_es.txt"),
                    new Medication("Qtern",                     "Pills", "es", "qtern",     "qtern_es.txt"),

                    // Französisch
                    new Medication("Amlodipine Valsartan Mylan", "Pills", "fr", "amlodipin", "amlodipin_fr.txt"),
                    new Medication("Cymbalta",                  "Pills", "fr", "cymbalta",  "cymbalta_fr.txt"),
                    new Medication("Eliquis",                   "Pills", "fr", "eliquis",   "eliquis_fr.txt"),
                    new Medication("Nilemdo",                   "Pills", "fr", "nilemdo",   "nilemdo_fr.txt"),
                    new Medication("Qtern",                     "Pills", "fr", "qtern",     "qtern_fr.txt")
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
