package at.fhj.tessaimrich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.preference.PreferenceManager;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

public class CategoryActivity extends BaseDrawerActivity {

    private MaterialAutoCompleteTextView searchInput;
    private AppDatabase db;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_category,
                findViewById(R.id.content_frame),
                true
        );
        searchInput = findViewById(R.id.search_input);

        db = AppDatabase.getInstance(getApplicationContext());

        // 1) Sprache ermitteln
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs
                .getString("language", Locale.getDefault().getLanguage())
                .split("-")[0]
                .toLowerCase();

        // 2) Falls noch keine Einträge für diese Sprache existieren, Test-Daten einfügen
        List<Medication> pillsExist = db.medicationDao()
                .findByCategoryAndLanguage("pills", lang);
        if (pillsExist.isEmpty()) {

                        // —––––– PILLS –––––—
                        db.medicationDao().insertAll(
                                new Medication("Amlodipine Valsartan Mylan", "pills", lang,
                                        "AmlodipineValsartanMylan", "", "AmlodipineValsartanMylan_" + lang.toUpperCase() + ".pdf"),
                                new Medication("Cymbalta", "pills", lang,
                                        "Cymbalta", "", "Cymbalta_" + lang.toUpperCase() + ".pdf"),
                                new Medication("Eliquis", "pills", lang,
                                        "Eliquis", "", "Eliquis_" + lang.toUpperCase() + ".pdf"),
                                new Medication("Nilemdo", "pills", lang,
                                        "Nilemdo", "", "Nilemdo_" + lang.toUpperCase() + ".pdf"),
                                new Medication("Qtern", "pills", lang,
                                        "Qtern", "", "Qtern_" + lang.toUpperCase() + ".pdf")
              );
                    }
        AppDatabase.databaseWriteExecutor.execute(() -> {
                List<Medication> dropsExist = db.medicationDao()
                            .findByCategoryAndLanguage("drops", lang);
            if (dropsExist.isEmpty()) {
                db.medicationDao().insertAll(
                        new Medication("Catiolanze", "drops", lang,
                                "Catiolanze", "", "Catiolanze_" + lang.toUpperCase() + ".pdf"),
                        new Medication("Simbrinza", "drops", lang,
                                "Simbrinza", "", "Simbrinza_" + lang.toUpperCase() + ".pdf")
                );
            }
                // —––––– CREAMS –––––—
            List<Medication> creamsExist = db.medicationDao()
                    .findByCategoryAndLanguage("cream", lang);
            if (creamsExist.isEmpty()) {
                db.medicationDao().insertAll(
                        new Medication("Protopic", "cream", lang,
                                "Protopic", "", "Protopic_" + lang.toUpperCase() + ".pdf")
                );
            }
                // —––––– INHALATIONS –––––—
            List<Medication> inhExist = db.medicationDao()
                    .findByCategoryAndLanguage("inhalation", lang);
            if (inhExist.isEmpty()) {
                db.medicationDao().insertAll(
                        new Medication("Enerzair", "inhalation", lang,
                                "Enerzair", "", "Enerzair_" + lang.toUpperCase() + ".pdf"),
                        new Medication("Ultibro", "inhalation", lang,
                                "Ultibro", "", "Ultibro_" + lang.toUpperCase() + ".pdf")
                );
            }
                // nach Insert: Adapter updaten
                List<String> updated = db.medicationDao().getAllNames(lang);
                runOnUiThread(() -> {
                    adapter.clear();
                    adapter.addAll(updated);
                    adapter.notifyDataSetChanged();
                });


        });

        // 3) Adapter mit allen Namen dieser Sprache
        List<String> suggestions = db.medicationDao().getAllNames(lang);
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                suggestions
        );

        searchInput.setAdapter(adapter);
        searchInput.setThreshold(1);

        // 4) Enter/Search
        searchInput.setOnEditorActionListener((v, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                String input = searchInput.getText().toString().trim();
                if (!input.isEmpty()) performSearch(input, lang);
                else Toast.makeText(this,
                        "Bitte Suchbegriff eingeben",
                        Toast.LENGTH_SHORT
                ).show();
                return true;
            }
            return false;
        });

        // 5) Klick auf Vorschlag
        searchInput.setOnItemClickListener((parent, view, pos, id) -> {
            String sel = adapter.getItem(pos);
            if (sel != null) performSearch(sel, lang);
        });

        // 6) Icons → ListActivities
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
    }

    /** regex-basierte Suche über alle Medikamente der aktuellen Sprache */
    private void performSearch(String pattern, String lang) {
        new Thread(() -> {
            try {
                List<Medication> meds = db.medicationDao().getAll(lang);
                Pattern rx = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                Medication found = null;
                for (Medication m : meds) {
                    if (rx.matcher(m.getName()).find()) {
                        found = m;
                        break;
                    }
                }
                Medication result = found;
                runOnUiThread(() -> {
                    if (result == null) {
                        Toast.makeText(this, "Nicht gefunden: " + pattern, Toast.LENGTH_SHORT).show();
                        Log.w("Search", "Kein Medikament gefunden für Regex: " + pattern);
                    } else {
                        Class<?> target;
                        switch (result.getCategory().toLowerCase(Locale.ROOT)) {
                            case "pills":      target = PillDetailActivity.class;       break;
                            case "drops":      target = DropDetailActivity.class;       break;
                            case "cream":      target = CreamDetailActivity.class;      break;
                            case "inhalation": target = InhalationDetailActivity.class; break;
                            default:
                                Toast.makeText(this,
                                        "Unbekannter Typ: " + result.getCategory(),
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                        }
                        Intent intent = new Intent(this, target);
                        intent.putExtra("med_name", result.getName());
                        startActivity(intent);
                    }
                });
            } catch (Exception e) {
                Log.e("Search", "Fehler bei performSearch(): ", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Fehler bei der Suche", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
