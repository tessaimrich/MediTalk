package at.fhj.tessaimrich;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class CategoryActivity extends BaseDrawerActivity {

    private MaterialAutoCompleteTextView searchInput;
    //in der Zwischenzeit auskommentiert
   // private AppDatabase db;  // Room-Datenbank

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_category,        // dein existierendes Layout
                findViewById(R.id.content_frame),  // container aus app_bar_drawer.xml
                true
    );

        // Room-Instanz holen (Singleton)
        /*für Zwischenzeit und AppStart auskommentiert
        db = AppDatabase.getInstance(getApplicationContext());
        */

        // Suchfeld: Enter/Search löst Suche aus
        searchInput = findViewById(R.id.search_input);

        //Nur zum Testen, dann mit Room DB bald verbunden etc
        String[] suggestions = {"Amlodipin", "Cymbalta", "Eliquis", "Nilemdo", "Qtern"}; // z. B. Dummy-Daten
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                suggestions
        );
        searchInput.setAdapter(adapter);
        searchInput.setThreshold(1); // ab 1 Buchstabe zeigen


        // Kategorie-Icons: führen zu ListActivities
        findViewById(R.id.ivPill).setOnClickListener(v ->
                startActivity(new Intent(this, PillListActivity.class))
        );
        findViewById(R.id.ivDrops).setOnClickListener(v ->
                startActivity(new Intent(this, DropListActivity.class))
        );
        findViewById(R.id.ivCreams).setOnClickListener(v ->
                startActivity(new Intent(this, CreamListActivity.class))
        );
        findViewById(R.id.ivInhalations).setOnClickListener(v ->
                startActivity(new Intent(this, InhalationListActivity.class))
        );

        // Home-Button:
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            //nothing to do
        });

    }

    /**
     * Führt die Suche in der Room-DB durch.
     * Datenzugriff läuft in einem Hintergrund-Thread, damit die UI nicht blockiert wird
     * Bei Fund: Start der passenden Detail-Activity, ansonsten Toast.
     */
    /*In der Zwischenzeit auskommentiert
    private void performSearch(String name) {
        new Thread(() -> {
            Medication med = db.medDao().findByName(name);
            runOnUiThread(() -> {
                if (med == null) {
                    Toast.makeText(this, "Nicht gefunden", Toast.LENGTH_SHORT).show();
                } else {
                    //  Intent-Ziel je nach Med-Typ wählen
                    Class<?> target;
                    switch (med.getType()) {
                        case "PILL":       target = PillDetailActivity.class;      break;
                        case "CREAM":      target = CreamDetailActivity.class;     break;
                        case "DROP":       target = DropDetailActivity.class;      break;
                        case "INHALATION": target = InhalationDetailActivity.class;break;
                        default:
                            Toast.makeText(this, "Unbekannter Typ", Toast.LENGTH_SHORT).show();
                            return;
                    }
                    Intent intent = new Intent(this, target);
                    intent.putExtra("item_id", med.getId());  // Extra fürs Laden der Details
                    startActivity(intent);
                }
            });
        }).start();
    }
    */
}
