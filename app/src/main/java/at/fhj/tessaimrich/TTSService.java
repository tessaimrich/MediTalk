package at.fhj.tessaimrich;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
                SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
                String languageCode = prefs.getString("selected_language", "en");
                Locale locale = new Locale(languageCode);

                int result = tts.setLanguage(locale);
                if (result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED) {

                    // Engine ist bereit
                    ttsReady = true;

                    // UtteranceProgressListener nur einmal registrieren
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            if (listener != null) listener.onSpeakStart();
                        }
                        @Override
                        public void onDone(String utteranceId) {
                            if (listener != null) listener.onSpeakDone();
                        }
                        @Override
                        public void onError(String utteranceId) {
                            if (listener != null) listener.onSpeakError();
                        }
                    });

                    Log.d("TTS", "TextToSpeech initialized successfully.");
                    if (listener != null) listener.onTTSInitialized(true);
                }
            } else {
                ttsReady = false;
                Log.e("TTS", "TextToSpeech initialization failed.");
                if (listener != null) listener.onTTSInitialized(false);
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

    public void speak(String text) {
        if (!ttsReady || tts == null || text == null || text.isEmpty()) return;

        String utteranceId = "TTS_" + System.currentTimeMillis();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void stop() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
    }

    public void setLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTSService", "Language not supported: " + languageCode);
        } else {
            Log.d("TTSService", "Language set to: " + languageCode);
        }
    }

    public boolean isTTSReady() {
        return ttsReady;
    }

    public boolean isSpeaking() {
        return tts != null && tts.isSpeaking();
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        TTSService getService() {
            return TTSService.this;
        }
    }

    public interface TTSListener {
        void onTTSInitialized(boolean isReady);
        void onSpeakStart();
        void onSpeakDone();
        void onSpeakError();
    }
}
