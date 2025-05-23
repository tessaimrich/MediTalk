package at.fhj.tessaimrich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;



public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME   = "app_settings";
    private static final String KEY_LANGUAGE = "selected_language";
    // Wird gesetzt, wenn der Nutzer eine Flagge wählt
    private String selectedLanguage = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Edge-to-Edge und eigenes App-Icon (in styles.xml als <item name="android:windowBackground">@drawable/app_icon_background</item>)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Wenn bereits eine Sprache gespeichert ist, direkt zur CategoryActivity und MainActivity beenden
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.contains(KEY_LANGUAGE)) {
            startActivity(new Intent(this, CategoryActivity.class));
            finish();
            return;
        }

        // Padding für SystemBars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });




// Flaggen-Buttons mit Beschriftung
        // verbindet das Bild, das Label, den Sprachcode und das Preferences-Objekt
        //"en": Sprachcode aus ISO 639-1: internationale Norm für Namen von Sprachen
        // prefs: SharedPreferences-Objekt, in das die Auswahl gespeichert werden soll
        setupFlag(findViewById(R.id.btnFlagEnglish),   findViewById(R.id.tvLangEnglish),   "en", prefs);
        setupFlag(findViewById(R.id.btnFlagSlovenian),    findViewById(R.id.tvLangSlovenian),    "sl", prefs);
        setupFlag(findViewById(R.id.btnFlagSpanish),    findViewById(R.id.tvLangSpanish),    "es", prefs);
        setupFlag(findViewById(R.id.btnFlagCroatian),   findViewById(R.id.tvLangCroatian),   "hr", prefs);
        setupFlag(findViewById(R.id.btnFlagItalian), findViewById(R.id.tvLangItalian), "it", prefs);
        setupFlag(findViewById(R.id.btnFlagFrench),  findViewById(R.id.tvLangFrench),  "fr", prefs);


//"Weiter"-Button verknüpfen
        ImageButton btnWeiter = findViewById(R.id.btnWeiter);
        btnWeiter.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
            startActivity(intent);
        });


// (Optional) Sprache speichern, wenn eine Flagge geklickt wird:
        ImageButton flagEng = findViewById(R.id.btnFlagEnglish);
        flagEng.setOnClickListener(v -> {
            // z. B. SharedPreferences oder ein Feld setzen
            // selectedLanguage = "en";
        });
        // … ebenso für die anderen Flaggen
    }



    /**
     * Initialisiert einen Flaggen-Button mit Beschriftung
     * und aktiviert den Weiter-Button, sobald eine Auswahl getroffen wurde.
     */
    private void setupFlag(ImageButton flagBtn, TextView flagLabel, String langCode, SharedPreferences prefs) {
        // Beschriftung unterhalb der Flagge
        flagLabel.setText(getLanguageDisplayName(langCode));

        flagBtn.setOnClickListener(v -> {
            selectedLanguage = langCode;
            // visuelles Feedback: Label fett
            flagLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            // Weiter-Button aktivieren
            findViewById(R.id.btnWeiter).setEnabled(true);
        });
    }


    /**
     * Gibt den Sprachnamen für den Code zurück.
     */
    private String getLanguageDisplayName(String code) {
        switch (code) {
            case "en": return "English";
            case "de": return "Deutsch";
            case "fr": return "Français";
            case "es": return "Español";
            case "sl": return "Slovenščina";
            case "hr": return "Hrvatski";
            default:   return code;
        }
    }
}





}