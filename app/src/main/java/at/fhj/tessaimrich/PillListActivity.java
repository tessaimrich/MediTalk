package at.fhj.tessaimrich;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PillListActivity extends AppCompatActivity {

    private ListView lvPills;
    private ImageButton btnHome, btnNext;
    private String[] pills = {
            "1. Amlodipine Valsartan Mylan",
            "2. Cymbalta",
            "3. Eliquis ",
            "4. Nilemdo",
            "5. Qtern"
    };
    private int selectedPos = -1;  // aktuell ausgewählter Eintrag



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_list);

        lvPills  = findViewById(R.id.lvPills);
        btnHome  = findViewById(R.id.btnHome);
        btnNext  = findViewById(R.id.btnNext);

        // Adapter, der im getView() das ausgewählte Item fettgedruckt macht
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pills) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);

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


        // Home-Button: Zurück zur Startseite
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(PillListActivity.this, MainActivity.class);
            // Flags
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // CLEAR_TOP
            //   – Wenn sich schon eine Instanz der MainActivity irgendwo im Back-Stack befindet, werden alle darüberliegenden Activities gelöscht und es wird diese wiederverwendet .
            //   – Ohne dieses Flag hätte man nach ein paar Navigationen mehrere MainActivity-Instanzen in deinem Stack.
            // SINGLE_TOP
            //   - wenn sie schon ganz oben liegt, bekommt bestehende Instanz einfach einen neuen Intent.

            startActivity(intent);
            finish();
        });


        // 4) Weiter-Button
        btnNext.setOnClickListener(v -> {
            if (selectedPos >= 0) {         // Nur weiter wenn ein Medikament ausgewählt ist
                Intent intent = new Intent(PillListActivity.this, PillDetailActivity.class);
                intent.putExtra("pill_name", pills[selectedPos]);
                startActivity(intent);
            }
        });
    }
}
