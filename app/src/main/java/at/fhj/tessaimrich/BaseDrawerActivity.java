package at.fhj.tessaimrich;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Locale;



/**
 * Abstrakte Basisklasse für Activities mit Navigation Drawer.
 * Implementiert das Grundlayout (Drawer, Toolbar, NavigationView)
 * und die Integration des TTSService.
 * Alle untergeordneten Activities (List-, Detail- ..) können so einheitlich
 * Navigation und Spracheinstellungen nutzen.
 */
public abstract class BaseDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Hauptcontainer für den Navigation Drawer
    protected DrawerLayout drawer;
    // Öffnet/schließt den Drawer über das Hamburger-Menü in der Toolbar
    private ActionBarDrawerToggle toggle;
    public TTSService ttsService;
    /**
     * Attribut vom Typ ServiceConnection,
     * das als anonymes Objekt deklariert und sofort initialisiert wird.
     * Es ist eine Verbindung zum {@link TTSService}, um ihn zu starten,
     * ein Objekt vom Typ TTSService zu speichern und Sprache und Sprechgeschwindigkeit zu setzen.
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            // Zugriff auf laufenden TTSService über seinen LocalBinder
            ttsService = ((TTSService.LocalBinder) binder).getService();
            // // Sprache aus den App-Einstellungen holen
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(BaseDrawerActivity.this);
            String lang = prefs.getString("language", Locale.getDefault().getLanguage());
            // Sprache und Sprechgeschwindigkeit im TTS-Service setzen
            ttsService.setLanguage(lang);
            float rate = getSharedPreferences("tts_prefs", MODE_PRIVATE)
                    .getFloat("speech_rate", 1.0f);
            ttsService.setSpeechRate(rate);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ttsService = null;
        }
    };



    /**
     * Initialisiert das Layout (Drawer, Toolbar, NavigationView)
     * und bindet den {@link TTSService}.
     *
     * @param savedInstanceState Zustand beim erneuten Starten
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        //Toolbar als ActionBar setzen
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer und Toggle (Hamburger-Menü) initialisieren
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();             // Sync zwischen Drawer und Toolbar

        //NavigationItem‐Listener setzen
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        // TTS-Service starten und binden
        Intent ttsIntent = new Intent(this, TTSService.class);
        startService(ttsIntent);
        bindService(ttsIntent, serviceConnection, BIND_AUTO_CREATE);

    }





    /**
     * Aktualisiert das Sprachlabel im Drawer-Header, wenn die Activity wieder sichtbar wird.
     */
    @Override
    protected void onResume() {
        super.onResume();

        NavigationView navView = findViewById(R.id.nav_view);
        if (navView != null) {
            // Zugriff auf das Header-Layout im Drawer
            android.view.View headerView = navView.getHeaderView(0);

            // Zugriff auf das Sprach-TextView im Header
            TextView tvLanguage = headerView.findViewById(R.id.tvLanguage);

            if (tvLanguage != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String language = prefs.getString("language", "Deutsch");
                tvLanguage.setText("Sprache: " + language);
            }
        }
    }




    /**
     * Reagiert auf Klicks im Navigation Drawer.
     *
     * @param item Das gewählte Menüelement
     * @return true, wenn das Event verarbeitet wurde
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        int id = item.getItemId();

        if (id == R.id.nav_language) {
            showLanguageSelectionDialog();   // Sprache ändern
            return true;

        } else if (id == R.id.nav_display) {
            startActivity(new Intent(this, SettingsActivity.class));    // Anzeigeeinstellungen öffnen

        } else if (id == R.id.nav_info) {
            // App-Info anzeigen
                new AlertDialog.Builder(this)
                    .setTitle("Info zur App")
                    .setMessage("MediTalk\nVersion: " + getString(R.string.app_version))
                    .setPositiveButton("OK", null)
                    .show();
        }
        else if (id == R.id.nav_home) {
            // Home: schließt diese Activity (SettingsActivity oder jede andere)
            // und kehrt zur vorherigen (z.B. CategoryActivity) zurück
            finish();
        }
        return true;
    }



    /**
     * Wenn der Drawer geöffnet ist, wird dieser beim Drücken des Zurück-Buttons geschlossen,
     * anstatt die Activity zu beenden.
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }




    /**
     * Zeigt einen Dialog zur Sprachauswahl an.
     * Die neue Sprache wird gespeichert und auf den {@link TTSService} angewendet.
     * Danach wird die Activity neu gestartet, um die UI zu aktualisieren.
     */
    private void showLanguageSelectionDialog() {
        final String[] languages = {"English", "Kroatisch", "Slowenisch", "Italienisch", "Spanisch", "Französisch"};
        final String[] codes     = {"en",      "hr",        "sl",         "it",         "es",       "fr"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sprache wählen")
                .setItems(languages, (dialog, which) -> {
                    String selectedCode  = codes[which];      // wird gespeichert
                    //String selectedLabel = languages[which];  // nur zur Anzeige

                    // ISO-Sprachcode speichern
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    prefs.edit().putString("language", selectedCode).apply();

                    if (ttsService != null && ttsService.isTTSReady()) {
                        ttsService.setLanguage(selectedCode);
                    }

                    Toast.makeText(this,
                            "Sprache geändert: " + languages[which],
                            Toast.LENGTH_SHORT).show();

                    recreate();     // UI neu laden
                })
                .show();
    }




    /**
     * Trennt beim Beenden die Verbindung zum {@link TTSService},
     * um Ressourcen freizugeben und Leaks zu vermeiden.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsService != null) {
            unbindService(serviceConnection);
        }
    }



}