package at.fhj.tessaimrich;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public abstract class BaseDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // 1) Toolbar als ActionBar setzen
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2) DrawerToggle (Hamburger-Icon) koppeln
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

        // 3) NavigationItem‐Listener setzen
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
    }

    /** Drawer‐Menü‐Clicks auswerten */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        int id = item.getItemId();

        if (id == R.id.nav_language) {
            Toast.makeText(this, "Sprachauswahl folgt…", Toast.LENGTH_SHORT).show();

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
}