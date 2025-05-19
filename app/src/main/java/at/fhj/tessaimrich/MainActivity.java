package at.fhj.tessaimrich;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//"Weiter"-Button verknüpfen
        Button btnWeiter = findViewById(R.id.btnWeiter);
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





}
