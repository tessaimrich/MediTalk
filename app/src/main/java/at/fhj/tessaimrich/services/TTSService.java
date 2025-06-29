package at.fhj.tessaimrich.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import java.util.Locale;

/**
 * {@code TTSService} ist ein Android Service zur Verwaltung von Text-to-Speech (TTS).
 * <p>
 * Hauptfunktionen:
 * <ul>
 *   <li>Initialisierung und Konfiguration der TTS-Engine</li>
 *   <li>Sprachausgabe von Texten in verschiedenen Sprachen</li>
 *   <li>Überwachung des Fortschritts durch einen Listener</li>
 *   <li>Verwendung eines Binders zur Interaktion mit Activities</li>
 * </ul>
 */
public class TTSService extends Service {


    private TextToSpeech tts;
    private boolean ttsReady = false;
    private TTSListener listener;
    private final IBinder binder = new LocalBinder();



    /**
     * Wird beim Start des Services aufgerufen. Initialisiert TTS und setzt Sprache.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true;
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

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String languageCode = prefs.getString("language", "en");
                setLanguage(languageCode);

                Log.d("TTS", "Initialized and set language to " + languageCode);
                if (listener != null) listener.onTTSInitialized(true);
            } else {
                ttsReady = false;
                Log.e("TTS", "Initialization failed");
                if (listener != null) listener.onTTSInitialized(false);
            }
        });
    }


    /**
     * Gibt den Binder zurück, damit andere Komponenten (z.B. Activities) mit dem Service kommunizieren können.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    /**
     * Spricht einen Text mit der aktuellen TTS-Konfiguration.
     * @param text Der zu sprechende Text
     */
    public void speak(String text) {
        if (!ttsReady || tts == null || text == null || text.isEmpty()) return;

        String utteranceId = "TTS_" + System.currentTimeMillis();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }


    /**
     * Stoppt die aktuelle Sprachausgabe.
     */
    public void stop() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
    }

    /**
     * Setzt die Sprachgeschwindigkeit.
     * @param rate Geschwindigkeit (1.0 = normal)
     */
    public void setSpeechRate(float rate) {
        if (tts != null && ttsReady) {
            tts.setSpeechRate(rate);
        }
    }

    /**
     * Setzt die Sprache für TTS basierend auf dem Sprachcode.
     * @param languageCode Sprachcode (z.B. "de" für Deutsch)
     */
    public void setLanguage(String languageCode) {
        Locale locale;
        switch (languageCode) {
            case "en":
                locale = Locale.US;
                break;
            case "de":
                locale = Locale.GERMANY;
                break;
            case "fr":
                if (tts.setLanguage(Locale.FRANCE)
                        == TextToSpeech.LANG_MISSING_DATA
                        || tts.setLanguage(Locale.FRANCE)
                        == TextToSpeech.LANG_NOT_SUPPORTED) {
                    locale = Locale.FRENCH;
                } else {
                    Log.d("TTSService","Using fr-FR");
                    return;
                }
                break;
            case "it":
                locale = Locale.ITALY;
                break;
            case "es":
                locale = new Locale("es", "ES");
                break;
            case "hr":
                locale = new Locale("hr", "HR");
                break;
            case "sl":
                locale = new Locale("sl", "SI");
                break;
            default:
                locale = Locale.getDefault();
        }

        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTSService", "Language not supported: " + languageCode);
        } else {
            Log.d("TTSService", "Language set to: " + locale);
        }
    }

    /**
     * Gibt zurück, ob TTS bereit zur Sprachausgabe ist.
     */
    public boolean isTTSReady() {
        return ttsReady;
    }


    /**
     * Beendet den Service und gibt TTS-Ressourcen frei.
     */
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Binder-Klasse für lokale Verbindung mit Clients.
     */
    public class LocalBinder extends Binder {
        public TTSService getService() {
            return TTSService.this;
        }
    }

    /**
     * Schnittstelle für Rückmeldungen zu TTS-Ereignissen.
     */
    public interface TTSListener {
        void onTTSInitialized(boolean isReady);

        void onSpeakStart();

        void onSpeakDone();

        void onSpeakError();
    }

}