package at.fhj.tessaimrich;

import android.os.Bundle;

public class SettingsActivity extends BaseDrawerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Drawer-Layout ist bereits in BaseDrawerActivity gesetzt.
        // 2) Wir blasen nur noch unser settings-Layout in den content_frame:
        getLayoutInflater().inflate(
                R.layout.activity_settings,
                findViewById(R.id.content_frame),
                true
        );

        // NICHTS ANDERES – keine Edge-to-Edge, kein findViewById(R.id.main) etc.
    }
}