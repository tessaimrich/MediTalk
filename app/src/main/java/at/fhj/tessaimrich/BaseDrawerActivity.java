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

public abstract class BaseDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    public TTSService ttsService;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        /**
         * Verbindung zum TTSService
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            ttsService = ((TTSService.LocalBinder) binder).getService();
            // aktuelle Sprache setzen
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(BaseDrawerActivity.this);
            String lang = prefs.getString("language", Locale.getDefault().getLanguage());
            ttsService.setLanguage(lang);
            // ggf. Rate setzen, falls du das auch hier zentralisieren möchtest
            float rate = getSharedPreferences("tts_prefs", MODE_PRIVATE)
                    .getFloat("speech_rate", 1.0f);
            ttsService.setSpeechRate(rate);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            ttsService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        //Toolbar als ActionBar setzen
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //DrawerToggle (Hamburger-Icon) koppeln
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //NavigationItem‐Listener setzen
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        Intent ttsIntent = new Intent(this, TTSService.class);
        startService(ttsIntent);
        bindService(ttsIntent, serviceConnection, BIND_AUTO_CREATE);

    }

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
    /** Drawer‐Menü‐Clicks auswerten */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        int id = item.getItemId();

        if (id == R.id.nav_language) {
            showLanguageSelectionDialog();
            return true;

        } else if (id == R.id.nav_display) {
            startActivity(new Intent(this, SettingsActivity.class));

        } else if (id == R.id.nav_info) {
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

    /** Back‐Button schließt den Drawer, falls geöffnet */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
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

                    //Activity neu starten, damit UI (Header, PillList etc.) aktualisiert wird
                    recreate();
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsService != null) {
            unbindService(serviceConnection);
        }
    }

}