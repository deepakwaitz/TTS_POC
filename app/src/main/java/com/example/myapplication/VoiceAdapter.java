package com.example.myapplication;
import android.content.Context;
import android.graphics.Color;
import android.speech.tts.Voice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class VoiceAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Voice> mDataSource;

    public VoiceAdapter(Context context, List<Voice> voicesToDisplay) {
        mContext = context;
        mDataSource = voicesToDisplay;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // In a real app this method is not efficient,
        // and "View Holder Pattern" shoudl be used instead.
        View rowView = mInflater.inflate(R.layout.list_item_voice, parent, false);

        if (position%2 == 0) {
            rowView.setBackgroundColor(Color.rgb(245,245,245));
        }

        Voice voiceUnderScrutiny = mDataSource.get(position);

        // example output of Voice.toString() :
        // "Voice[Name: pt-br-x-afs#male_2-local, locale: pt_BR, quality: 400, latency: 200,
        // requiresNetwork: false, features: [networkTimeoutMs, notInstalled, networkRetriesCount]]"

        // Get title element
        TextView voiceTitleTextView =
                (TextView) rowView.findViewById(R.id.voice_title);

        TextView qualityTextView =
                (TextView) rowView.findViewById(R.id.voice_quality);

        TextView networkRequiredTextView =
                (TextView) rowView.findViewById(R.id.voice_network);

        TextView isInstalledTextView =
                (TextView) rowView.findViewById(R.id.voice_installed);

        TextView featuresTextView =
                (TextView) rowView.findViewById(R.id.voice_features);

        voiceTitleTextView.setText("VOICE NAME: " + voiceUnderScrutiny.getName());

        // Voice Quality...
        // ( https://developer.android.com/reference/android/speech/tts/Voice.html )
        // 100 = Very Low, 200 = Low, 300 = Normal, 400 = High, 500 = Very High
        qualityTextView.setText(  "QLTY: " + ((Integer) voiceUnderScrutiny.getQuality()).toString()  );
        if (voiceUnderScrutiny.getQuality() == 500) {
            qualityTextView.setTextColor(Color.GREEN); // set v. high quality to green
        }

        if (!voiceUnderScrutiny.isNetworkConnectionRequired()) {
            networkRequiredTextView.setText("NET_REQ?: NO");
        } else {
            networkRequiredTextView.setText("NET_REQ?: YES");
        }

        if (!voiceUnderScrutiny.getFeatures().contains("notInstalled")) {
            isInstalledTextView.setTextColor(Color.GREEN);
            isInstalledTextView.setText("INSTLLD?: YES");
        } else {
            isInstalledTextView.setTextColor(Color.RED);
            isInstalledTextView.setText("INSTLLD?: NO");
        }

        featuresTextView.setText("FEATURES: " + voiceUnderScrutiny.getFeatures().toString());

        return rowView;
    }
}