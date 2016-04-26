package com.nairan.batman_launcher;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nairan.batman_launcher.primitives.DbHandlerSingleton;
import com.nairan.batman_launcher.primitives.LoaderSingleton;
import com.nairan.batman_launcher.primitives.LoggerSingleton;
import com.nairan.batman_launcher.primitives.PkgHandlerSingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by nrzhang on 4/8/2015.
 */
public class Batman_Launcher extends Application {

    private static final String TAG = "Batman launcher";

    public static final String FIRSTGROUP	= "Primary";
    public static final String SECONDGROUP	= "Secondary";
    public static final String GROUPLABEL   = "group label";
    public static final String DATABASE_NAME   = "local.db";

    public static final String FRACTIONPROMPT = "Designate a portion of the battery";
    public static final String CHARGINGPROMPT = "Select a charging policy";

    /**
     * Preferences
     */
    public static final String PREFERENCE_PORTION           = "Preference portion";
    public static final String PREFERENCE_CHARGING_POLICY   = "Preference charging policy";
    public static final String PREFERENCE_VBLEVELS          = "Preference virtual battery levels";
    public static final String PREFERENCE_PASTLIST          = "Preference past linkedlists";
    public static final String PREFERENCE_PBLEVEL           = "Preference physical battery level";

    /**
     * Format
     */
    public static final String CSVSEP           = ",";
    public static final String FIRSTDELIMIT     = "FFF";
    public static final String SECONDDELIMIT    = "SSS";
    public static final String THIRDDELIMIT     = "TTT";

    public static boolean beyondhundredbug = false;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        DbHandlerSingleton.getInstance(this);
        PkgHandlerSingleton.getInstance(this);
        LoggerSingleton.getInstance(this);
        LoaderSingleton.getInstance(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String str_portion = preferences.getString(Batman_Launcher.PREFERENCE_PORTION, "");
        String str_charging_policy = preferences.getString(Batman_Launcher.PREFERENCE_CHARGING_POLICY, "");
        String str_vb_levels = preferences.getString(Batman_Launcher.PREFERENCE_VBLEVELS, "");
        String str_pastlists = preferences.getString(Batman_Launcher.PREFERENCE_PASTLIST, "");
        String str_pb_level = preferences.getString(Batman_Launcher.PREFERENCE_PBLEVEL, "");

        SharedPreferences.Editor editor = preferences.edit();
        if(str_portion.equals("")){
            editor.putString(Batman_Launcher.PREFERENCE_PORTION, String.valueOf(30));
            editor.commit();
        }
        if(str_charging_policy.equals("")) {
            editor.putString(Batman_Launcher.PREFERENCE_CHARGING_POLICY, String.valueOf(1));
            editor.commit();
        }

        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        if(str_vb_levels.equals("")) {
            /**
             * this is the first time use or not
             */
            String str = Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + level + FIRSTDELIMIT
                    + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + level;
            editor.putString(Batman_Launcher.PREFERENCE_VBLEVELS, str);
            editor.putString(Batman_Launcher.PREFERENCE_PBLEVEL, String.valueOf(level));
            editor.commit();
        } else {

            /**
             * There should be values already over there
             */
            int difference = level - Integer.parseInt(str_pb_level);
            String[] levels_fields = str_vb_levels.split(FIRSTDELIMIT);
            String[] levels_update = new String[levels_fields.length];
            for (int i = 0; i < levels_fields.length; ++i) {
                String[] levels_fields_inner = levels_fields[i].split(SECONDDELIMIT);
                double storedLevel = Double.parseDouble(levels_fields_inner[1]);
                if(storedLevel >= 100 || storedLevel <= 0){
                    Log.d(TAG, "Beyond 100 bug");
                    beyondhundredbug = true;
                    String str = Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + level + FIRSTDELIMIT
                            + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + level;
                    editor.putString(Batman_Launcher.PREFERENCE_VBLEVELS, str);
                    editor.putString(Batman_Launcher.PREFERENCE_PBLEVEL, String.valueOf(level));
                    editor.commit();
                }
                levels_update[i] = String.valueOf(storedLevel + difference);
            }

            if(!beyondhundredbug){
                String str = Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + levels_update[0] + FIRSTDELIMIT
                        + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + levels_update[1];
                editor.putString(Batman_Launcher.PREFERENCE_VBLEVELS, str);
                editor.putString(Batman_Launcher.PREFERENCE_PBLEVEL, String.valueOf(level));
                editor.commit();
                beyondhundredbug = false;
            }
        }

        if(str_pastlists.equals("")) {
            editor.putString(Batman_Launcher.PREFERENCE_PASTLIST, FIRSTGROUP + FIRSTDELIMIT + SECONDGROUP);
            editor.commit();
        }

        /*
        Log.i(TAG, "I got called");

        HashMap<String, ArrayList<ResolveInfo>>
        haha = DbHandlerSingleton.getInstance(this).getGroups();
        Log.i(TAG, "I got called : " + haha.size());
        for(Map.Entry<String, ArrayList<ResolveInfo>> entry : haha.entrySet()){
            Log.i(TAG, "Group : " + entry.getKey());
            if(entry.getKey() == null){
                for(int i = 0; i < entry.getValue().size(); ++i) {
                    Log.i(TAG, "Group member : " + entry.getValue().get(i).activityInfo.packageName);
                }
            }
        }
        */
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        super.onLowMemory();
    }
}

