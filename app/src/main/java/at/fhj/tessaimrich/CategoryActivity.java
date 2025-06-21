package at.fhj.tessaimrich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

public class CategoryActivity extends BaseDrawerActivity {

    private MaterialAutoCompleteTextView searchInput;
    private AppDatabase db;  // Room-Datenbank
    private String lang;   // aktuell gewählte Sprache

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_category,        // existierendes Layout
                findViewById(R.id.content_frame),  // container aus app_bar_drawer.xml
                true
    );

        // Room-Instanz holen (Singleton)
        db = AppDatabase.getInstance(getApplicationContext());

        // Sprache lesen (die in MainActivity gesetzt)
        SharedPreferences prefs = getSharedPreferences("MediTalkPrefs", MODE_PRIVATE);
        lang = prefs.getString("language", "en");   // Default: Englisch

        // Suchfeld: Enter/Search löst Suche aus
        searchInput = findViewById(R.id.search_input);
        searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String input = searchInput.getText().toString().trim();
                if (!input.isEmpty()) {
                    performSearch(input);
                } else {
                    Toast.makeText(this, "Bitte Suchbegriff eingeben", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

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




//METHODEN

    /**
     * Führt die Suche in der Room-DB durch.
     * Datenzugriff läuft in einem Hintergrund-Thread, damit die UI nicht blockiert wird
     * Bei Fund: Start der passenden Detail-Activity, ansonsten Toast.
     */
    private void performSearch(String name) {
        // das DAO in einem Background-Thread aufrufen
        new Thread(() -> {
            Medication med = db.medicationDao().findByNameAndLanguage(name, lang);
            runOnUiThread(() -> {
                if (med == null) {
                    Toast.makeText(this, "Nicht gefunden: " + name, Toast.LENGTH_SHORT).show();
                } else {
                    // DetailActivity anhand der Kategorie wählen
                    Class<?> target;
                    switch (med.getCategory().toLowerCase()) {
                        case "pill":       target = PillDetailActivity.class;       break;
                        case "cream":      target = CreamDetailActivity.class;      break;
                        case "drop":       target = DropDetailActivity.class;       break;
                        case "inhalation": target = InhalationDetailActivity.class; break;
                        default:
                            Toast.makeText(this, "Unbekannter Typ: " + med.getCategory(), Toast.LENGTH_SHORT).show();
                            return;
                    }

                    // Intent: ID mitgeben (statt Name)
                    Intent intent = new Intent(this, target);
                    intent.putExtra("med_id", med.getId());
                    startActivity(intent);
                }
            });
        }).start();

    }

}
