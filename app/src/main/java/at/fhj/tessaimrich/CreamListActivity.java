package at.fhj.tessaimrich;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        if (db.medicationDao().findByCategory("cream").isEmpty()) {
            db.medicationDao().insertAll(
                    // Englisch
                    new Medication("Protopic", "cream", "en", "protopic", "protopic_en.txt", "Protopic_EN.pdf"),
                    // Slowenisch
                    new Medication("Protopic", "cream", "sl", "protopic", "protopic_sl.txt", "Protopic_SL.pdf"),
                    // Kroatisch
                    new Medication("Protopic", "cream", "hr", "protopic", "protopic_hr.txt", "Protopic_HR.pdf"),
                    // Italienisch
                    new Medication("Protopic", "cream", "it", "protopic", "protopic_it.txt", "Protopic_IT.pdf"),
                    // Spanisch
                    new Medication("Protopic", "cream", "es", "protopic", "protopic_es.txt", "Protopic_ES.pdf"),
                    // Französisch
                    new Medication("Protopic", "cream", "fr", "protopic", "protopic_fr.txt", "Protopic_FR.pdf")
            );
            Log.d("DB", "Test-Creams wurden eingefügt");
        }
        List<Medication> meds = db.medicationDao().findByCategory("cream");
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
                intent.putExtra("cream_name", creams.get(selectedPos));
                startActivity(intent);
            }
        });
    }
}
