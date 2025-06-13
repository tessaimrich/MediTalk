package at.fhj.tessaimrich;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
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
                Locale locale = new Locale("en");  // Wähle die korrekte Sprache hier
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
        if (ttsReady && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
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
