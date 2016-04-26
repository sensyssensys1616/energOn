package com.nairan.batman_launcher.primitives;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nrzhang on 25/8/2015.
 */
public class PkgHandlerSingleton {

    private static PkgHandlerSingleton instance;

    private List<ResolveInfo> mInstalledUserApps;
    private ArrayList<String> mPkgNames;

    public static PkgHandlerSingleton getInstance(Context ctx){
        if (instance == null){
            // Create the instance
            instance = new PkgHandlerSingleton(ctx);
        }

        // Return the instance
        return instance;
    }

    private PkgHandlerSingleton(Context ctx){
        // Constructor hidden because this is a singleton
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = ctx.getPackageManager().queryIntentActivities(mainIntent, 0);

        this.mInstalledUserApps = new ArrayList<ResolveInfo>();
        this.mPkgNames = new ArrayList<String>();
        for(int i = 0; i < apps.size(); ++i){
            if(!apps.get(i).activityInfo.packageName.contains("nairan") &&
                    !apps.get(i).activityInfo.packageName.contains("com.android.settings")){
                this.mInstalledUserApps.add(apps.get(i));
                this.mPkgNames.add(apps.get(i).activityInfo.packageName);
            }
        }
    }

    public void reset(Context ctx){
        this.mInstalledUserApps.clear();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = ctx.getPackageManager().queryIntentActivities(mainIntent, 0);

        this.mInstalledUserApps = new ArrayList<ResolveInfo>();
        this.mPkgNames = new ArrayList<String>();
        for(int i = 0; i < apps.size(); ++i){
            if(!apps.get(i).activityInfo.packageName.contains("nairan") &&
                    !apps.get(i).activityInfo.packageName.contains("com.android.settings")){
                this.mInstalledUserApps.add(apps.get(i));
                this.mPkgNames.add(apps.get(i).activityInfo.packageName);
            }
        }
    }

    public List<ResolveInfo> getInstalledApps(){
        return this.mInstalledUserApps;
    }

    public ArrayList<String> getInstalledPkgNames(){
        return this.mPkgNames;
    }

    /**
     * check if an installed package
     *
     * @param processName: process name, maybe simplified version of full package name
     * @return: full package name
     */
    public String isIstalled(String processName) {
        Iterator<ResolveInfo> iter = this.mInstalledUserApps.iterator();
        while(iter.hasNext()){
            String formalName = iter.next().activityInfo.packageName;
            if(formalName.contains(processName))
                return formalName;
        }

        return null;
    }
}
