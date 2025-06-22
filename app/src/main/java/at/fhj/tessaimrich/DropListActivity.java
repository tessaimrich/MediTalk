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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

public class DropListActivity extends BaseDrawerActivity {

    private ListView lvDrops;
    private ImageButton btnHome, btnNext;
    private List<String> drops;
    private int selectedPos = -1;  // aktuell ausgewählter Eintrag



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_drop_list,
                findViewById(R.id.content_frame),
                true
        );
        // Sprache aus SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs.getString("language", Locale.getDefault().getLanguage());

        // DB-Instanz holen
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        // Nur einmalig Drops für diese Sprache einfügen
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

        //Nur aktuelle Sprache laden
        List<Medication> meds = db.medicationDao()
                .findByCategoryAndLanguage("drops", lang);
        drops = new ArrayList<>();
        for (Medication m : meds) {
            drops.add(m.getName());
        }
            lvDrops  = findViewById(R.id.lvDrops);
        btnHome  = findViewById(R.id.btnHome);
        btnNext  = findViewById(R.id.btnNext);

        // Adapter, der im getView() das ausgewählte Item fettgedruckt macht
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drops) {
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
        lvDrops.setAdapter(adapter);



        // Tippen auf Medikamentennamen: Auswahl merken
        lvDrops.setOnItemClickListener((parent, view, position, id) -> {
            selectedPos = position;
            adapter.notifyDataSetChanged();
        });


        // Home-Button: zurück zur CategoryActivity
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(DropListActivity.this, CategoryActivity.class);
            // damit keine doppelten CategoryActivity-Instanzen im Back-Stack landen
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();  // schließt die DropListActivity
        });


        // Weiter-Button
        btnNext.setOnClickListener(v -> {
            if (selectedPos >= 0) {         // Nur weiter wenn ein Medikament ausgewählt ist
                Intent intent = new Intent(DropListActivity.this, DropDetailActivity.class);
                intent.putExtra("drop_name", drops.get(selectedPos));
                startActivity(intent);
            }
        });
    }
}
