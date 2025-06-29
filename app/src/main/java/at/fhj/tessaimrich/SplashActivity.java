package at.fhj.tessaimrich;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * SplashActivity ist die Startbildschirm-Activity der App.
 * Sie zeigt beim Start für 2 Sekunden einen Splash Screen an
 * und wechselt anschließend automatisch zur MainActivity.
 * Außerdem wird der Nachtmodus dauerhaft aktiviert-Design Grund.
 */
public class SplashActivity extends ComponentActivity {
    /**
     * Wird aufgerufen, wenn die Activity erstellt wird.
     * Setzt das Layout auf den Splash Screen, aktiviert den Nachtmodus
     * und startet nach 2 Sekunden die MainActivity.
     * @param savedInstanceState gespeicherter Zustand der Activity, falls vorhanden
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}