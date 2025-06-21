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

import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class PillListActivity extends BaseDrawerActivity {

    private ListView lvPills;
    private ImageButton btnHome, btnNext;
    private List<Medication> pillList;
    private int selectedPos = -1;  // aktuell ausgewählter Eintrag




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_pill_list,
                findViewById(R.id.content_frame),
                true
        );

        //Sprache ermitteln
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs.getString("language", Locale.getDefault().getLanguage());

        // DB-Instanz holen
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        // Testdaten für Pills nur einmal pro Sprache anlegen
        List<Medication> existing = db.medicationDao()
                .findByCategoryAndLanguage("Pills", lang);
        if (existing.isEmpty()) {
            db.medicationDao().insertAll(
                    new Medication("Amlodipine Valsartan Mylan", "Pills", lang, "amlodipin", "amlodipin_" + lang + ".txt", "AmlodipineValsartanMylan_" + lang.toUpperCase() + ".pdf"),
                    new Medication("Cymbalta",                  "Pills", lang, "cymbalta",    "cymbalta_"  + lang + ".txt", "Cymbalta_"                + lang.toUpperCase() + ".pdf"),
                    new Medication("Eliquis",                   "Pills", lang, "eliquis",     "eliquis_"   + lang + ".txt", "Eliquis_"                 + lang.toUpperCase() + ".pdf"),
                    new Medication("Nilemdo",                   "Pills", lang, "nilemdo",     "nilemdo_"   + lang + ".txt", "Nilemdo_"                 + lang.toUpperCase() + ".pdf"),
                    new Medication("Qtern",                     "Pills", lang, "qtern",       "qtern_"     + lang + ".txt", "Qtern_"                   + lang.toUpperCase() + ".pdf")
            );
            Log.d("DB", "Pill-Testdaten für Sprache " + lang + " eingefügt");
        }

        // Alle Pills (als Objekte) in pillList laden
        pillList = db.medicationDao()
                .findByCategoryAndLanguage("Pills", lang);
        // Dann nur die Namen für den Adapter extrahieren
        List<String> pillNames = new ArrayList<>();
        for (Medication m : pillList) {
            pillNames.add(m.getName());
        }

        lvPills  = findViewById(R.id.lvPills);
        btnHome  = findViewById(R.id.btnHome);
        btnNext  = findViewById(R.id.btnNext);


        // Adapter, der im getView() das ausgewählte Item fettgedruckt macht
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                pillNames        // <-- nicht mehr "pills", sondern "pillNames"
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.med_text_darkgray));
                if (position == selectedPos) {
                    tv.setTypeface(null, Typeface.BOLD);
                } else {
                    tv.setTypeface(null, Typeface.NORMAL);
                }
                return view;
            }
        };
        lvPills.setAdapter(adapter);

        // Und direkt **danach** den neuen Item-Click-Listener:
        lvPills.setOnItemClickListener((parent, view, position, id) -> {
            Medication selected = pillList.get(position);
            Intent i = new Intent(PillListActivity.this, PillDetailActivity.class);
            i.putExtra("med_id", selected.getId());
            startActivity(i);
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
            if (selectedPos >= 0) {
                Medication sel = pillList.get(selectedPos);
                Intent intent = new Intent(PillListActivity.this, PillDetailActivity.class);
                intent.putExtra("med_id", sel.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Bitte ein Medikament auswählen", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
