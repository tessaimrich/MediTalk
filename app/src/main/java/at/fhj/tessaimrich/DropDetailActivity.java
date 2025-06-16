package at.fhj.tessaimrich;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import at.fhj.tessaimrich.data.AppDatabase;
import at.fhj.tessaimrich.data.Medication;

public class DropDetailActivity extends BaseDrawerActivity {

    private TTSService ttsService;
    private ImageButton btnAudio;
    private String dropName;
    private boolean isSpeaking = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(
                R.layout.activity_drop_detail,
                findViewById(R.id.content_frame),
                true
        );
        //  Gewählten Tropfen-Namen auslesen
        dropName = getIntent().getStringExtra("drop_name");
        ((TextView) findViewById(R.id.tvDropName))
                .setText(dropName != null ? dropName : "");

        // Audio-Button referenzieren
        btnAudio = findViewById(R.id.btnAudio1);

        //pdf-Button

        ImageButton btnHome = findViewById(R.id.btnHome);
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, CategoryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
}
