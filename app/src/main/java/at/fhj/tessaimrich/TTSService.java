package at.fhj.tessaimrich;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TTSService extends Service {

    private TextToSpeech tts;
    private boolean ttsReady = false;
    private TTSListener listener;
    private final IBinder binder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                SharedPreferences sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);
                String languageCode = sharedPreferences.getString("selected_language", "en");
                Locale locale = new Locale(languageCode);  // Wähle die korrekte Sprache hier

                int result = tts.setLanguage(locale);
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsReady = true;
                    Log.d("TTS", "TextToSpeech initialized successfully.");
                    if (listener != null) {
                        listener.onTTSInitialized(ttsReady); // Benachrichtige den Listener
                    }
                }
            } else {
                ttsReady = false;
                Log.e("TTS", "TextToSpeech initialization failed.");
                if (listener != null) {
                    listener.onTTSInitialized(ttsReady); // Benachrichtige den Listener
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setTTSListener(TTSListener listener) {
        this.listener = listener;
    }

    // Die Methode, um Text zu sprechen
    public void speak(String text) {
        if (text == null || text.isEmpty() || tts == null) return;

        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String langCode = prefs.getString("selected_language", "de");

        tts.setLanguage(new Locale(langCode));
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }


    public void setLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTSService", "Language not supported: " + languageCode);
        } else {
            Log.d("TTSService", "Language set to: " + languageCode);
        }
    }

    public boolean isTTSReady() {
        return ttsReady;
    }

    public class LocalBinder extends Binder {
        TTSService getService() {
            return TTSService.this;
        }
    }

    public interface TTSListener {
        void onTTSInitialized(boolean isReady);
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
    public void stop() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
    }

}
