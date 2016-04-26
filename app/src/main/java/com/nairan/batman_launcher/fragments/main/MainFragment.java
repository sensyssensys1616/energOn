package com.nairan.batman_launcher.fragments.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nairan.batman_launcher.Batman_Launcher;
import com.nairan.batman_launcher.R;
import com.nairan.batman_launcher.activities.MainActivity;
import com.nairan.batman_launcher.primitives.DbHandlerSingleton;
import com.nairan.batman_launcher.trackers.AsyncResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nrzhang on 5/9/2015.
 */
public class MainFragment extends Fragment {

    private static final String TAG = "Main fragment";

    private int hash(String key) {
        int hash = 7;
        for (int i = 0; i < key.length(); i++) {
            hash = hash*31 + key.charAt(i);
        }

        return hash;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout v = (RelativeLayout) inflater.inflate(R.layout.main, container, false);

        int i = 0;
        Log.w(TAG, " The number of groups : " + DbHandlerSingleton.getInstance(getActivity().getApplicationContext()).getGroups().size());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String[] levels = preferences.getString(Batman_Launcher.PREFERENCE_VBLEVELS, "").split(Batman_Launcher.FIRSTDELIMIT);
        final HashMap<String, Double> vblevels = new HashMap<String, Double>();
        for(int j = 0; j < levels.length; ++j){
            vblevels.put(levels[j].split(Batman_Launcher.SECONDDELIMIT)[0],
                    Double.parseDouble(levels[j].split(Batman_Launcher.SECONDDELIMIT)[1]));
        }

        for(Map.Entry<String, ArrayList<ResolveInfo>> entry : DbHandlerSingleton.getInstance(getActivity().getApplicationContext()).getGroups().entrySet()){

            final String groupLabel = entry.getKey();
            View groupFolder = inflater.inflate(R.layout.icon_label, null);
            Log.w(TAG, "\tGroup : " + groupLabel + " with level : " + vblevels.get(groupLabel));

            groupFolder.setId(hash(entry.getKey()));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            if(i == 0) {
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                i++;
                //lastId = hash(entry.getKey());
            } else {
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            }

            // fill in any details dynamically here
            ImageView icon = (ImageView) groupFolder.findViewById(R.id.icon);
            icon.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.drawer));
            TextView label = (TextView) groupFolder.findViewById(R.id.label);
            label.setText(entry.getKey());

            groupFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(vblevels.get(groupLabel).compareTo((double) 0) <= 0){
                        Log.w(TAG, "Yes set unclickable");
                        v.setEnabled(false);
                    } else {
                        Log.w(TAG, "Still clickable");
                        PerGroupFragment fragment = new PerGroupFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(Batman_Launcher.GROUPLABEL, groupLabel);
                        fragment.setArguments(bundle);

                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container_main, fragment, MainActivity.GROUP_TAG + groupLabel);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                }
            });

            v.addView(groupFolder, params);
        }

        return v;
    }
}
