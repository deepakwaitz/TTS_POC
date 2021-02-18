package com.example.myapplication;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText textToSpeak;
    TextView progressView;
    TextToSpeech googleTTS;
    ListView voiceListView;
    SwipeRefreshLayout swipeRefreshLayout;
    Long timeOfSpeakRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToSpeak = findViewById(R.id.textToSpeak);
        textToSpeak.setText("In Android Studio we need to install Gherkin plugin. This plugin provides Gherkin language support. Gherkin is the language that Cucumber uses to define test cases (test scenarios). It has been designed to be non-technical and human readable"
        );
        progressView = findViewById(R.id.progressView);
        voiceListView = findViewById(R.id.voiceListView);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);


        // Create the TTS and wait until it's initialized to do anything else
        if (isGoogleEngineInstalled()) {
            createGoogleTTS();
        } else {
            Log.i("XXX", "onCreate(): Google not installed -- nothing done.");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                assignFullSetOfVoicesToVoiceListView();
            }
        });

    }

    // this is where the program really begins (when the TTS is initialized)
    private void onTTSInitialized() {

        setUpWhatHappensWhenAVoiceItemIsClicked();
        setUtteranceProgressListenerOnTheTTS();
        assignFullSetOfVoicesToVoiceListView();

    }

    // FACTORED/EXTRACTED METHODS ----------------------------------------------------------------
    // These are just pulled out to make onCreate() easier to read and the basic sequence
    // of events more obvious.

    private void createGoogleTTS() {

        googleTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    Log.i("XXX", "Google tts initialized");
                    onTTSInitialized();
                } else {
                    Log.i("XXX", "Internal Google engine init error.");
                }
            }
        }, "com.google.android.tts");

    }

    private void setUpWhatHappensWhenAVoiceItemIsClicked() {
        voiceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Voice desiredVoice = (Voice) parent.getAdapter().getItem(position);
                // if (setting the desired voice is "successful")...
                // in the case of google engine, this does not necessarily mean the voice you
                // want will actually be used. :(
                if (googleTTS.setVoice(desiredVoice) == 0) {
                    Log.i("XXX", "Speech voice set to: " + desiredVoice.toString());
                    // TTS did may "auto-downgrade" voice selection
                    // due to internal reason such as no data
                    // Unfortunately it will not tell you, and there seems to be no
                    // way of checking whether the presently selected voice (getVoice()) "equals"
                    // the desired voice.
                    speak();
                }
            }
        });
    }

    private void setUtteranceProgressListenerOnTheTTS() {

        UtteranceProgressListener blurp = new UtteranceProgressListener() {

            @Override // MIN API 15
            public void onStart(String s) {
                long timeSinceSpeakCall = System.currentTimeMillis() - timeOfSpeakRequest;
                Log.i("XXX", "progress.onStart() callback.  "
                        + timeSinceSpeakCall + " millis since speak() was called.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressView.setTextColor(Color.GREEN);
                        progressView.setText("PROGRESS: STARTED");
                    }
                });
            }

            @Override // MIN API 15
            public void onDone(String s) {
                long timeSinceSpeakCall = System.currentTimeMillis() - timeOfSpeakRequest;
                Log.i("XXX", "progress.onDone() callback.  "
                        + timeSinceSpeakCall + " millis since speak() was called.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressView.setTextColor(Color.GREEN);
                        progressView.setText("PROGRESS: DONE");
                    }
                });
            }

            // Getting an error can simply mean that the particular voice is not available
            // to the device yet... and still needs to be downloaded / is still downloading
            @Override // MIN API 15 (depracated at API 21)
            public void onError(String s) {
                long timeSinceSpeakCall = System.currentTimeMillis() - timeOfSpeakRequest;
                Log.i("XXX", "progress.onERROR() callback.  "
                        + timeSinceSpeakCall + " millis since speak() was called.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressView.setTextColor(Color.RED);
                        progressView.setText("PROGRESS: ERROR");
                    }
                });

            }
        };
        googleTTS.setOnUtteranceProgressListener(blurp);

    }

    // must happens AFTER tts is initialized
    private void assignFullSetOfVoicesToVoiceListView() {

        googleTTS.stop();

        List<Voice> tempVoiceList = new ArrayList<>();

        for (Voice v : googleTTS.getVoices()) {
            if (v.getLocale().getLanguage().contains("en")) { // only English voices
                tempVoiceList.add(v);
            }
        }

        // Sort the list alphabetically by name
        Collections.sort(tempVoiceList, new Comparator<Voice>() {
            @Override
            public int compare(Voice v1, Voice v2) {
                Log.i("XXX", "comparing item");
                return (v2.getName().compareToIgnoreCase(v1.getName()));
            }
        });

        VoiceAdapter tempAdapter = new VoiceAdapter(this, tempVoiceList);

        voiceListView.setAdapter(tempAdapter);
        swipeRefreshLayout.setRefreshing(false);
        progressView.setTextColor(Color.BLACK);
        progressView.setText("PROGRESS: ...");

    }

    private void speak() {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "merp");
        timeOfSpeakRequest = System.currentTimeMillis();
        googleTTS.speak(textToSpeak.getText().toString(), TextToSpeech.QUEUE_FLUSH, map);
    }

    // Checks if Google Engine is installed
    // ... (and gives more info in Logs).
    // The version number is going to dictate the quality of voices available
    private boolean isGoogleEngineInstalled() {

        final Intent ttsIntent = new Intent();
        ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> list = pm.queryIntentActivities(ttsIntent, PackageManager.GET_META_DATA);

        boolean googleIsInstalled = false;

        for (int i = 0; i < list.size(); i++) {

            ResolveInfo resolveInfoUnderScrutiny = list.get(i);
            String engineName = resolveInfoUnderScrutiny.activityInfo.applicationInfo.packageName;

            if (engineName.equals("com.google.android.tts")) {
                String version = "null";
                try {
                    version = pm.getPackageInfo(engineName,
                            PackageManager.GET_META_DATA).versionName;
                } catch (Exception e) {
                    Log.i("XXX", "Error getting google engine verion: " + e.toString());
                }
                Log.i("XXX", "Google engine version " + version + " is installed!");
                googleIsInstalled = true;
            } else {
                Log.i("XXX", "Google Engine is not installed!");
            }

        }
        return googleIsInstalled;
    }
}