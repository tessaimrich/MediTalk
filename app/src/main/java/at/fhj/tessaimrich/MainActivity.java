package at.fhj.tessaimrich;



import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.preference.PreferenceManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Die MainActivity ist der Einstiegspunkt der App.
 * Sie erlaubt es den Nutzer:innen, eine Sprache auszuwählen, und navigiert anschließend zur CategoryActivity.
 * Die Sprachwahl wird in den SharedPreferences gespeichert.
 */
public class MainActivity extends AppCompatActivity {

    /** Schlüssel zur Übergabe der Sprache an die nächste Activity */
    private static final String KEY_LANGUAGE = "language";
    /** Vom Benutzer gewählte Sprache im ISO-639-1-Format (z.B. "en") */
    private String selectedLanguage = null;



    /**
     * Initialisiert die Activity,
     * setzt das Layout und das Statusleisten-Padding,
     * verbindet Buttons mit Aktionen zur Sprachauswahl und Navigation.
     *
     * @param savedInstanceState Zustand der Activity bei erneutem Erstellen
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Padding für SystemBars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

    // Sprach-Buttons initialisieren
        // verbindet das Bild, das Label, den Sprachcode und das Preferences-Objekt
        //"en": Sprachcode aus ISO 639-1: internationale Norm für Namen von Sprachen
        // prefs: SharedPreferences-Objekt, in das die Auswahl gespeichert werden soll
        setupFlag(findViewById(R.id.btnFlagEnglish),   findViewById(R.id.tvLangEnglish),   "en", prefs);
        setupFlag(findViewById(R.id.btnFlagSlovenian),    findViewById(R.id.tvLangSlovenian),    "sl", prefs);
        setupFlag(findViewById(R.id.btnFlagSpanish),    findViewById(R.id.tvLangSpanish),    "es", prefs);
        setupFlag(findViewById(R.id.btnFlagCroatian),   findViewById(R.id.tvLangCroatian),   "hr", prefs);
        setupFlag(findViewById(R.id.btnFlagItalian), findViewById(R.id.tvLangItalian), "it", prefs);
        setupFlag(findViewById(R.id.btnFlagFrench),  findViewById(R.id.tvLangFrench),  "fr", prefs);


        // Weiter-Button vorbereiten
        ImageButton btnWeiter = findViewById(R.id.btnWeiter);
        btnWeiter.setEnabled(true);
        TextView tvWeiter = findViewById(R.id.tvWeiterLabel);
        tvWeiter.setText("Weiter");

        btnWeiter.setOnClickListener(v -> {
            if (selectedLanguage == null) {
                Toast.makeText(MainActivity.this, "Keine Sprache gewählt. Bitte tippen Sie auf eine Flagge.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sprachname speichern
            prefs.edit()
                    .putString("language", selectedLanguage)
                    .apply();
            // Zur CategoryActivity wechseln, Sprache mitgeben
            Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
            intent.putExtra(KEY_LANGUAGE, selectedLanguage);
            startActivity(intent);
            finish();
        });


        // Logout-Button: schließt die App
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            finishAffinity();      // Alle Activities beenden → App geschlossen
        });

    }  //Ende onCreate()






    /**
     * Verknüpft einen Flaggen-Button mit seiner zugehörigen Textanzeige und einer Sprache.
     * Setzt die gewählte Sprache und hebt die entsprechende Beschriftung fett hervor.
     *
     * @param flagBtn   Button mit Flaggenbild
     * @param flagLabel TextView mit Sprachbezeichnung
     * @param langCode  Sprachcode
     * @param prefs     SharedPreferences zur späteren Speicherung
     */
    private void setupFlag(ImageButton flagBtn, TextView flagLabel, String langCode, SharedPreferences prefs) {

        flagBtn.setOnClickListener(v -> {
            selectedLanguage = langCode;
            // Alle Markierungen zurücksetzen
            resetFlagLabels();
            // Diese Flagge hervorheben
            flagLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            // Weiter-Button aktivieren
            findViewById(R.id.btnWeiter).setEnabled(true);
        });
    }




    /**
     * Setzt alle Sprachlabels (unterhalb der Flaggen) auf normale Schrift.
     * Dient dazu, die Hervorhebung bei Sprachauswahl zurückzusetzen.
     */
    private void resetFlagLabels() {
        int[] labelIds = {
                R.id.tvLangEnglish,
                R.id.tvLangSlovenian,
                R.id.tvLangSpanish,
                R.id.tvLangCroatian,
                R.id.tvLangItalian,
                R.id.tvLangFrench
        };
        for (int id : labelIds) {
            TextView label = findViewById(id);
            label.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }



}
