package com.nairan.batman_launcher.trackers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.nairan.batman_launcher.Batman_Launcher;
import com.nairan.batman_launcher.primitives.DbHandlerSingleton;
import com.nairan.batman_launcher.primitives.LoaderSingleton;
import com.nairan.batman_launcher.primitives.LoggerSingleton;
import com.nairan.batman_launcher.primitives.ProcessLog;

import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * In this class, we conduct algorithm computation
 * 		We take as input per-group CPU time absolute consumption over last 4 physical battery levels
 * 		In this class, we read current CPU time absolute numbers, then we have 4 windows to fit a new model
 *
 * @author nrzhang
 *
 */
public class EstimatingTask extends AsyncTask<Void, Void, String> {

    public static final String TAG = "Batman Thread";

    public static final String CSVSEP           = ",";
    public static final String FIRSTDELIMIT     = "FFF";
    public static final String SECONDDELIMIT    = "SSS";
    public static final String THIRDDELIMIT     = "TTT";
    public static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("dd-M-yy HH:mm:ss");

    public static final String PROC_DIR         = "/proc/";
    public static final String STAT             = "/stat";
    public static final String CMDLINE          = "/cmdline";

    private static final int HEADCUT            = 5;
    private static final double PERLEVEL_CHARGE = 50000;

    /**
     * Inputs
     */
    private final int mPrevlevel;
    private final int mCurrlevel;
    private final int mBattlevelChange;
    private final boolean mCharging;

    private Context mCtx;

    private final SharedPreferences mPreferences;
    public HashMap<String, LinkedList<Double>> mPastLists;
    private double mPortion = -1;
    private int mCharging_policy = 0;
    private HashMap<String, Double> mLevels;

    private String all;

    public EstimatingTask(Context ctx,
                          int prev_level,
                          int curr_level) {

        this.mCtx = ctx;
        all = System.currentTimeMillis() + CSVSEP + DATEFORMAT.format(new Date(System.currentTimeMillis())) + CSVSEP;

        this.mPrevlevel = prev_level;
        this.mCurrlevel = curr_level;
        int change = prev_level - curr_level;
        if (change > 0) {
            this.mCharging = false;
        } else {
            this.mCharging = true;
        }
        this.mBattlevelChange = Math.abs(change);
        all += this.mPrevlevel + CSVSEP + this.mCurrlevel + CSVSEP + this.mCharging + CSVSEP;

        /**
         * pull out the current states
         *      1: portion number
         *      2: charging policy
         *      3: current virtual battery levels, in hashmap
         *      4: past cpu time, in hashmap
         */
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(this.mCtx);

        String str_portion          = this.mPreferences.getString(Batman_Launcher.PREFERENCE_PORTION, "");
        String str_charging_policy  = this.mPreferences.getString(Batman_Launcher.PREFERENCE_CHARGING_POLICY, "");
        String str_vb_levels        = this.mPreferences.getString(Batman_Launcher.PREFERENCE_VBLEVELS, "");
        String str_pastlists        = this.mPreferences.getString(Batman_Launcher.PREFERENCE_PASTLIST, "");

        Log.w(TAG, "Portion : " + str_portion);
        Log.w(TAG, "Charging policy : " + str_charging_policy);
        Log.w(TAG, "VB levels : " + str_vb_levels);
        Log.w(TAG, "Past list : " + str_pastlists);
        all += str_portion + CSVSEP + str_vb_levels + CSVSEP + str_pastlists + CSVSEP;

        if(!str_portion.equals("")){
            this.mPortion = Double.parseDouble(str_portion);
        }

        if(!str_charging_policy.equals("")) {
            this.mCharging_policy = Integer.parseInt(str_charging_policy);
        }

        if(!str_vb_levels.equals("")) {
            String[] levels_fields = str_vb_levels.split(FIRSTDELIMIT);
            this.mLevels = new HashMap<String, Double>();
            for (int i = 0; i < levels_fields.length; ++i) {
                String[] levels_fields_inner = levels_fields[i].split(SECONDDELIMIT);
                this.mLevels.put(levels_fields_inner[0], Double.parseDouble(levels_fields_inner[1]));
            }
        }

        if(!str_pastlists.equals("")){

            String[] lists_fields = str_pastlists.split(FIRSTDELIMIT);
            this.mPastLists = new HashMap<String, LinkedList<Double>>();
            for (int i = 0; i < lists_fields.length; ++i) {
                String[] lists_fields_inner = lists_fields[i].split(SECONDDELIMIT);
                String groupName = lists_fields_inner[0];
                if(lists_fields_inner.length > 1) {
                    String list = lists_fields_inner[1];
                    String[] numbers = list.split(THIRDDELIMIT);
                    LinkedList<Double> pastNumbers = new LinkedList<Double>();
                    for (int j = 0; j < numbers.length; ++j) {
                        pastNumbers.add(Double.parseDouble(numbers[j]));
                    }
                    this.mPastLists.put(groupName, pastNumbers);
                } else {
                    this.mPastLists.put(groupName, new LinkedList<Double>());
                }
            }
        }
    }

    @Override
    protected String doInBackground(Void... arg0) {
        // TODO Auto-generated method stub

        String ret = this.mPreferences.getString(Batman_Launcher.PREFERENCE_VBLEVELS, "");
        if(this.mPrevlevel != -1) { // just turn on the phone
            HashMap<String, ProcessLog> prev = LoaderSingleton.getInstance(this.mCtx).getPrevSnapshot();
            HashMap<String, ProcessLog> curr = LoaderSingleton.getInstance(this.mCtx).loadonce();
            int[] changes = getSnapshotChange(curr, prev);
            LoaderSingleton.getInstance(this.mCtx).setPrevSnapshot(curr);

            Log.d(TAG, "Primary cpu change this round : " + changes[0]
                    + " while secondary cpu change this round : " + changes[1]);
            all += changes[0] + CSVSEP + changes[1] + CSVSEP;

            /**
             * Context
             */
            try {
                int curBrightnessValue = Settings.System.getInt(this.mCtx.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS);
                ConnectivityManager manager = (ConnectivityManager) this.mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);

                boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                        .isConnectedOrConnecting();

                boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                        .isConnectedOrConnecting();

                all += curBrightnessValue + CSVSEP + is3g + CSVSEP + isWifi + CSVSEP;
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            ret = compute(changes);
        }

        return ret;
    }

    private String compute(int[] changes) {

        int primary_change = changes[0];
        int secondary_change = changes[1];

        double[] vb_updates = new double[]{this.mLevels.get(Batman_Launcher.FIRSTGROUP), this.mLevels.get(Batman_Launcher.SECONDGROUP)};
        double[] portions = new double[]{100 - this.mPortion, this.mPortion};

        if(this.mCharging){
            return chargingCalculation(this.mPrevlevel, this.mCurrlevel, vb_updates, portions, this.mCharging_policy);
        }

        /**
         * Discharging
         */
        for(int i = 0; i < vb_updates.length; ++i){
            if(vb_updates[i] <= 0){
                vb_updates[1-i] -= (this.mPrevlevel - this.mCurrlevel) * 100.0 / portions[1-i];
                all += vb_updates[0] + CSVSEP + vb_updates[1] + CSVSEP + CSVSEP + CSVSEP;
                return Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + vb_updates[0] + FIRSTDELIMIT
                        + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + vb_updates[1];
            }
        }

        boolean firstsmall      = (primary_change < 50) ? true : false;
        boolean secondsmall     = (secondary_change < 50) ? true : false;
        boolean firstbig        = (primary_change > 2000) ? true : false;
        boolean secondbig       = (secondary_change > 2000) ? true : false;
        if(primary_change == 0){
            primary_change++;
        }
        if(secondary_change == 0){
            secondary_change++;
        }
        double ratio = (primary_change > secondary_change) ?
                primary_change / secondary_change : secondary_change / primary_change;
        int bigone = (primary_change > secondary_change) ? 0 : 1;
        int smallone = (primary_change > secondary_change) ? 1 : 0;
        double sum = primary_change + secondary_change;

        /**
         * Logic
         */

        /*
        if ( (firstsmall && secondsmall) || (firstbig && secondbig) ) {
            vb_updates[0] -= (this.mPrevlevel - this.mCurrlevel);
            vb_updates[1] -= (this.mPrevlevel - this.mCurrlevel);
            all += vb_updates[0] + CSVSEP + vb_updates[1] + CSVSEP + CSVSEP + CSVSEP;
            return Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + vb_updates[0] + FIRSTDELIMIT
                    + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + vb_updates[1];
        } else {

            if (ratio >= 5) {
                vb_updates[bigone] -= (this.mPrevlevel - this.mCurrlevel) * 100.0 / portions[bigone];
                all += vb_updates[0] + CSVSEP + vb_updates[1] + CSVSEP + CSVSEP + CSVSEP;
                return Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + vb_updates[0] + FIRSTDELIMIT
                        + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + vb_updates[1];
            } else { */
                this.mPastLists.get(Batman_Launcher.FIRSTGROUP).add((double) primary_change);
                this.mPastLists.get(Batman_Launcher.SECONDGROUP).add((double)secondary_change);
                if(this.mPastLists.get(Batman_Launcher.FIRSTGROUP).size() < 4){
                    writeListPreferences(this.mPastLists);

                    vb_updates[0] -= (this.mPrevlevel - this.mCurrlevel);
                    vb_updates[1] -= (this.mPrevlevel - this.mCurrlevel);
                    all += vb_updates[0] + CSVSEP + vb_updates[1] + CSVSEP + CSVSEP + CSVSEP;
                    return Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + vb_updates[0] + FIRSTDELIMIT
                            + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + vb_updates[1];
                } else {
                    //model computation
                    vb_updates = estimating(vb_updates, ratio, bigone, portions);
                }
            /*}
        }*/

        return Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + vb_updates[0] + FIRSTDELIMIT
                + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + vb_updates[1];
    }

    /*********
     * Discharging
     *
     * @param vb_updates
     * @param ratio
     * @param bigone
     * @param pors
     * @return
     *********/
    private double[] estimating(double[] vb_updates, double ratio, int bigone, double[] pors) {
        int size = this.mPastLists.get(Batman_Launcher.FIRSTGROUP).size();
        while(size > 6){
            this.mPastLists.get(Batman_Launcher.FIRSTGROUP).pollFirst();
            this.mPastLists.get(Batman_Launcher.SECONDGROUP).pollFirst();
            size--;
        }

        writeListPreferences(this.mPastLists);

        double[] y = new double[size];
        Arrays.fill(y, PERLEVEL_CHARGE);

        double[][] x = new double[size][];
        for(int i = 0; i < size; ++i){
            //Debug.println("cpu1 : " + list1.get(i) + " cpu2 : " + list2.get(i));
            x[i] = new double[]{this.mPastLists.get(Batman_Launcher.FIRSTGROUP).get(i),
                    this.mPastLists.get(Batman_Launcher.SECONDGROUP).get(i)};
        }

        OLSMultipleLinearRegression ls = new OLSMultipleLinearRegression();
        ls.setNoIntercept(true);
        ls.newSampleData(y, x);
        double[] beta = ls.estimateRegressionParameters();

        Log.w(TAG, "beta1 : " + beta[0] + " beta2 : " + beta[1]);
        if(beta[0] <= 0 || beta[1] <= 0){
            if(ratio > 5){
                vb_updates[bigone] -= (this.mPrevlevel - this.mCurrlevel) * 100.0 / pors[bigone];
            } else {
                vb_updates[0] -= (this.mPrevlevel - this.mCurrlevel);
                vb_updates[1] -= (this.mPrevlevel - this.mCurrlevel);
            }
            all += vb_updates[0] + CSVSEP + vb_updates[1] + CSVSEP + CSVSEP + CSVSEP;
            return vb_updates;
        }

        vb_updates[0] -= beta[0]*this.mPastLists.get(Batman_Launcher.FIRSTGROUP).getLast() / (PERLEVEL_CHARGE * pors[0] / 100);
        vb_updates[1] -= beta[1]*this.mPastLists.get(Batman_Launcher.SECONDGROUP).getLast() / (PERLEVEL_CHARGE * pors[1] / 100);
        all += vb_updates[0] + CSVSEP + vb_updates[1] + CSVSEP + beta[0] + CSVSEP + beta[1] + CSVSEP;

        return vb_updates;
    }

    /********
     * Charging
     *
     * @param prevlevel
     * @param currlevel
     * @param vbs
     * @param pors
     * @param mCharging_policy
     * @return
     ********/
    private String chargingCalculation(int prevlevel, int currlevel, double[] vbs, double[] pors, int mCharging_policy) {

        if(currlevel >= 100){
            return Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + 100 + FIRSTDELIMIT
                    + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + 100;
        }

        /**
         * Check charging policy, we implement proportional (1) & least percent (4)
         */
        for(int i = 0; i < vbs.length; ++i){
            if(vbs[i] >= 100){
                if(vbs[1-i] >= 100){
                    return Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + 100 + FIRSTDELIMIT
                            + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + 100;
                } else {
                    vbs[1-i] -= (prevlevel - currlevel) * 100.0 / pors[1-i];
                    all += vbs[0] + CSVSEP + vbs[1] + CSVSEP + CSVSEP + CSVSEP;
                    return Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + vbs[0] + FIRSTDELIMIT
                            + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + vbs[1];
                }
            }
        }

        if(mCharging_policy == 4){
            // least percent
            int pri = new Double(vbs[0]).intValue();
            int sec = new Double(vbs[1]).intValue();
            if(pri < sec){
                pri -= (prevlevel - currlevel) * 100.0 / pors[0];
                return Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + pri + FIRSTDELIMIT
                        + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + sec;
            } else if(pri > sec){
                sec -= (prevlevel - currlevel) * 100.0 / pors[1];
                return Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + pri + FIRSTDELIMIT
                        + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + sec;
            } else {
                // equal percent, follow through
            }
        }

        // Default : proportional
        vbs[0] -= (prevlevel - currlevel);
        vbs[1] -= (prevlevel - currlevel);
        all += vbs[0] + CSVSEP + vbs[1] + CSVSEP + CSVSEP + CSVSEP;
        return Batman_Launcher.FIRSTGROUP + SECONDDELIMIT + vbs[0] + FIRSTDELIMIT
                + Batman_Launcher.SECONDGROUP + SECONDDELIMIT + vbs[1];
    }

    private int[] getSnapshotChange(HashMap<String, ProcessLog> curr,
                                     HashMap<String, ProcessLog> prev) {
        // TODO Auto-generated method stub
        HashMap<String, Integer> changes = new HashMap<String, Integer>();

        for (Map.Entry<String, ProcessLog> process : curr.entrySet()) {
            String pkgName = process.getKey();
            ProcessLog pl = process.getValue();
            //Log.w(TAG, pkgName + " (no change) : " + pl.getPid() + " ## " + pl.getProcessCpu());
            if (!prev.containsKey(pkgName)) {
                changes.put(pkgName, pl.getProcessCpu());
            } else {
                if (pl.getPid() != prev.get(pkgName).getPid())
                    changes.put(pkgName, pl.getProcessCpu());
                else {
                    //Log.w(TAG, "Same : " + pl.getProcessCpu() + " --- prev : " + prev.get(pkgName).getProcessCpu());
                    changes.put(pkgName, pl.getProcessCpu() - prev.get(pkgName).getProcessCpu());
                    //Log.w(TAG, " size : " + changes.size());
                }
            }
        }

        ArrayList<Map.Entry<String, Integer>> mchanges = new ArrayList<Map.Entry<String, Integer>>(changes.entrySet());

        Collections.sort(mchanges, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> lhs, Map.Entry<String, Integer> rhs) {

                int l = lhs.getValue();
                int r = rhs.getValue();

                if(l == r){
                    return 0;
                }

                return (l > r) ? -1 : 1;
            }
        });

        int smaller = (mchanges.size() <= HEADCUT) ? mchanges.size() : HEADCUT;
        for (int i = 0; i < smaller; ++i) {
            Log.w(TAG, mchanges.get(i).getKey() + " : " + mchanges.get(i).getValue());
        }

        DbHandlerSingleton.getInstance(this.mCtx).setSortedMap(changes);

        int primary_change = 0;
        int secondary_change = 0;

        /**************************************************************************************
         * Logger for debugging
         **************************************************************************************/
        String writeout = System.currentTimeMillis() + CSVSEP + this.mCurrlevel + CSVSEP;
        for (int i = 0; i < smaller; ++i) {
            String pkgName = mchanges.get(i).getKey();
            int cputimeChange = mchanges.get(i).getValue();
            if (DbHandlerSingleton.getInstance(mCtx).getMap().get(pkgName).equals(Batman_Launcher.SECONDGROUP)) {
                secondary_change += cputimeChange;
            } else {
                primary_change += cputimeChange;
            }
            writeout += pkgName + SECONDDELIMIT + cputimeChange + FIRSTDELIMIT;
        }
        LoggerSingleton.getInstance(mCtx).write(LoggerSingleton.LOG, writeout);
        LoggerSingleton.getInstance(mCtx).write(LoggerSingleton.OUTPUT, System.currentTimeMillis() + CSVSEP
                + DATEFORMAT.format(new Date(System.currentTimeMillis())) + CSVSEP
                + this.mCurrlevel + CSVSEP
                + primary_change + CSVSEP
                + secondary_change);
        for (int i = 0; i < this.mBattlevelChange; ++i) {
            LoggerSingleton.getInstance(mCtx).write(LoggerSingleton.CHECK, (double) primary_change / this.mBattlevelChange
                    + CSVSEP
                    + (double) secondary_change / this.mBattlevelChange);
        }

        return new int[]{primary_change, secondary_change};
    }

    private void writeListPreferences(HashMap<String, LinkedList<Double>> pastLists) {
        String ret = Batman_Launcher.FIRSTGROUP + SECONDDELIMIT;
        LinkedList<Double> pri = pastLists.get(Batman_Launcher.FIRSTGROUP);
        for(int i = 0; i < pri.size() - 1; ++i){
            ret += pri.get(i) + THIRDDELIMIT;
        }
        ret += pri.get(pri.size() - 1);
        ret += FIRSTDELIMIT + Batman_Launcher.SECONDGROUP + SECONDDELIMIT;
        LinkedList<Double> sec = pastLists.get(Batman_Launcher.SECONDGROUP);
        for(int i = 0; i < sec.size() - 1; ++i){
            ret += sec.get(i) + THIRDDELIMIT;
        }
        ret += sec.get(sec.size() - 1);

        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.putString(Batman_Launcher.PREFERENCE_PASTLIST, ret);
        editor.commit();
    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.putString(Batman_Launcher.PREFERENCE_VBLEVELS, result);
        editor.putString(Batman_Launcher.PREFERENCE_PBLEVEL, String.valueOf(this.mCurrlevel));
        editor.commit();
        LoggerSingleton.getInstance(this.mCtx).write(LoggerSingleton.ALL, all);
    }
}
