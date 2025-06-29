package at.fhj.tessaimrich.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import at.fhj.tessaimrich.R;


/**
 * Die NavHeader-Activity ist für die Darstellung des Navigationsmenü-Headers zuständig.
 * Sie zeigt den Benutzer:innen die aktuell gewählte Sprache an.
 * Die Sprache wird aus den {@link SharedPreferences} gelesen und als Text angezeigt.
 */
public class NavHeader extends AppCompatActivity {

    /**
     * Wird beim Starten der Activity aufgerufen.
     * Hier wird das Layout gesetzt, die gespeicherte Sprache geladen und im TextView angezeigt.
     *
     * @param savedInstanceState Zustand der Activity bei Wiederherstellung (z.B. nach Rotation)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.nav_header_drawer);
        TextView languageTextView = findViewById(R.id.tvLanguage);

        SharedPreferences sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);

        String selectedLanguage = sharedPreferences.getString("selected_language", "en");

        languageTextView.setText("Sprache: " + selectedLanguage);
    }
}