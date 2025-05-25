package at.fhj.tessaimrich;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.textfield.TextInputEditText;

public class CategoryActivity extends AppCompatActivity {

    private TextInputEditText searchInput;
    private AppDatabase db;  // Room-Datenbank

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Room-Instanz holen (Singleton)
        db = AppDatabase.getInstance(getApplicationContext());

        // Suchfeld konfigurieren: Enter/Search löst performSearch() aus
        searchInput = findViewById(R.id.search_input);
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = v.getText().toString().trim();
                performSearch(query);
                return true;
            }
            return false;
        });

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



        // Home-Button: zurück zur MainActivity mit CLEAR_TOP & SINGLE_TOP
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });



        // Menü-Button: öffnet den Drawer
        findViewById(R.id.btnMenu).setOnClickListener(v -> {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer != null) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * Führt die Suche in der Room-DB (asynchron) aus.
     * Bei Fund: Start der passenden Detail-Activity, ansonsten Toast.
     */
    private void performSearch(String name) {
        new Thread(() -> {
            Medication med = db.medDao().findByName(name);
            runOnUiThread(() -> {
                if (med == null) {
                    Toast.makeText(this, "Nicht gefunden", Toast.LENGTH_SHORT).show();
                } else {
                    //  Intent-Ziel je nach Med-Typ wählen :contentReference[oaicite:5]{index=5}
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
}
