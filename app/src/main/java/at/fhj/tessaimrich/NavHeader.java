package at.fhj.tessaimrich;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


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
        // Aktiviert automatische Anpassung für Status- und Navigationsleiste (Edge-to-Edge)
        EdgeToEdge.enable(this);
        // Setzt das Layout dieser Activity auf den Navigations-Header
        setContentView(R.layout.nav_header_drawer);
        // TextView im Layout suchen, das die Sprache anzeigen soll
        TextView languageTextView = findViewById(R.id.tvLanguage);

        // Zugriff auf die gespeicherten App-Einstellungen im SharedPreferences-Objekt
        // Achtung: Es wird hier "app_settings" verwendet – das sollte zur restlichen App passen
        SharedPreferences sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);

        // Holt den gespeicherten Sprachcode, z.B. "en"
        // Falls keiner gespeichert ist, wird Englisch als Standard verwendet
        String selectedLanguage = sharedPreferences.getString("selected_language", "en");

        // Zeigt die gewählte Sprache im TextView an
        languageTextView.setText("Sprache: " + selectedLanguage);
    }
}