package at.fhj.tessaimrich;

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
import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class CreamListActivity extends BaseDrawerActivity {

    private ListView lvCreams;
    private ImageButton btnHome, btnNext;
    private List<String> creams;

    private int selectedPos = -1;  // aktuell ausgewählter Eintrag




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_cream_list,
                findViewById(R.id.content_frame),
                true
        );

        //Sprache ermitteln
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs.getString("language", Locale.getDefault().getLanguage());

        // DB-Instanz
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        // Nur EINEN Cream-Eintrag pro Sprache einfügen
        List<Medication> existing = db.medicationDao()
                .findByCategoryAndLanguage("cream", lang);
        if (existing.isEmpty()) {
            db.medicationDao().insertAll(
                    new Medication(
                            "Protopic",            // Name
                            "cream",               // Kategorie
                            lang,                  // Sprache
                            "protopic",            // resourceKey
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
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.med_text_darkgray));
                tv.setTypeface(null, position == selectedPos
                        ? Typeface.BOLD
                        : Typeface.NORMAL);
                return view;
            }
        };
        lvCreams.setAdapter(adapter);

        // Tippen auf Medikamentennamen: Auswahl merken
        lvCreams.setOnItemClickListener((parent, view, position, id) -> {
            selectedPos = position;
            adapter.notifyDataSetChanged();
        });


        // Home-Button: zurück zur CategoryActivity
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(CreamListActivity.this, CategoryActivity.class);
            // damit keine doppelten CategoryActivity-Instanzen im Back-Stack landen
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();  // schließt die CreamListActivity
        });


        // Weiter-Button
        btnNext.setOnClickListener(v -> {
            if (selectedPos >= 0) {         // Nur weiter wenn ein Medikament ausgewählt ist
                Intent intent = new Intent(CreamListActivity.this, CreamDetailActivity.class);
                intent.putExtra("med_name", creams.get(selectedPos));
                startActivity(intent);
            }
        });
    }
}
