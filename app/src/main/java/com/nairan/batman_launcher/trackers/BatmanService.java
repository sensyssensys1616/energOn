package com.nairan.batman_launcher.trackers;

/**
 * Created by nrzhang on 9/9/2015.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import com.nairan.batman_launcher.primitives.DbHandlerSingleton;
import com.nairan.batman_launcher.primitives.LoaderSingleton;
import com.nairan.batman_launcher.primitives.LoggerSingleton;
import com.nairan.batman_launcher.primitives.PkgHandlerSingleton;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nrzhang on 4/8/2015.
 */
public class BatmanService extends Service {

    private static final String TAG = "Batman service";
    private static final IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    /**
     *
     */
    private final BroadcastReceiver mBatteryEventsReceiver = new BroadcastReceiver() {

        private int prev_level = -1;
        public void onReceive(Context context, Intent intent) {
            try{
                int reportedLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

                if(reportedLevel != this.prev_level){

                    Log.i(TAG, "Level change. From : " + this.prev_level
                            + " to : " + reportedLevel);
                    /**
                     * Prepare input and start a thread
                     *
                     * 		1: two LinkedList's storing absolute CPU time's of both groups over the last 4 level changes
                     * 		2: current coulomb amounts of two virtual batteries
                     * 		3: Debug: time, physical battery level
                     */

                    EstimatingTask mytask = new EstimatingTask(getApplicationContext(),
                            this.prev_level,
                            reportedLevel);
                    mytask.execute();

                    this.prev_level = reportedLevel;

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        this.unregisterReceiver(this.mBatteryEventsReceiver);
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        super.onLowMemory();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Start service");
        registerReceiver(this.mBatteryEventsReceiver, batteryLevelFilter);

        /*
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask(){

                                  @Override
                                  public void run() {
                                      EstimatingTask mytask = new EstimatingTask(getApplicationContext(),
                                              2,
                                              1);
                                      mytask.execute();
                                  }
                              },
                0,
                60000);
        */
        return START_STICKY;
    }
}

