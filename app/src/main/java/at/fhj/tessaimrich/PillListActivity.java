package at.fhj.tessaimrich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class PillListActivity extends BaseDrawerActivity {

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
        getLayoutInflater().inflate(
                R.layout.activity_pill_list,
                findViewById(R.id.content_frame),
                true
        );

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
