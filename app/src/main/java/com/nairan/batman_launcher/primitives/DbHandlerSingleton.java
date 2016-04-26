package com.nairan.batman_launcher.primitives;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nairan.batman_launcher.Batman_Launcher;
import com.nairan.batman_launcher.utils.DBOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by nrzhang on 25/8/2015.
 */
public class DbHandlerSingleton {

    private static final String TAG = "Db manager";
    /**
     * Singleton and constructor
     */
    private static DbHandlerSingleton instance;

    private DBOpenHelper mDBHelper;
    private HashMap<String, String> mMap; // key: member, value: group
    private HashMap<String, ArrayList<ResolveInfo>> mGroups;
    private HashMap<String, Integer> mSorted;

    public static DbHandlerSingleton getInstance(Context ctx){

        if (instance == null){
            // Create the instancec
            instance = new DbHandlerSingleton(ctx);
        }

        // Return the instance
        return instance;
    }

    private DbHandlerSingleton(Context ctx){
        // Constructor hidden because this is a singleton
        this.mMap = new HashMap<String, String>();
        this.mGroups = new HashMap<String, ArrayList<ResolveInfo>>();

        File myDb = ctx.getDatabasePath(Batman_Launcher.DATABASE_NAME);
        if(!myDb.exists()) {
            this.mDBHelper = new DBOpenHelper(ctx);
            installMapAndGroup(PkgHandlerSingleton.getInstance(ctx).getInstalledApps(),
                    PkgHandlerSingleton.getInstance(ctx).getInstalledPkgNames());

        } else {
            this.mDBHelper = new DBOpenHelper(ctx);
            loadMapAndGroupFromDb(PkgHandlerSingleton.getInstance(ctx).getInstalledApps());
        }
    }

    private void loadMapAndGroupFromDb(List<ResolveInfo> installedApps) {
        SQLiteDatabase db = this.mDBHelper.getReadableDatabase();
        db.beginTransaction();
        try{
            Cursor cursor = db.rawQuery("select * from apps", null);
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    String pkgName = cursor.getString(1);
                    String group = cursor.getString(2);

                    this.mMap.put(pkgName, group);

                } while (cursor.moveToNext());
            }

            cursor.close();

            finishUpdate(installedApps);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Interface methods
     */

    public boolean isEmpty() {
        return this.mMap.isEmpty();
    }

    public HashMap<String, String> getMap() {
        return this.mMap;
    }

    private void installMapAndGroup(List<ResolveInfo> installedApps, ArrayList<String> installedPkgNames) {
        for(int i = 0; i < installedPkgNames.size(); ++i){
            addMember(Batman_Launcher.FIRSTGROUP, installedPkgNames.get(i));
        }
        this.mGroups.put(Batman_Launcher.FIRSTGROUP, (ArrayList<ResolveInfo>) installedApps);
    }

    public void addMember(String group, String member){

        if(this.mMap.containsKey(member) && this.mMap.get(member).equals(group))
            return;

        SQLiteDatabase db = this.mDBHelper.getWritableDatabase();
        db.beginTransaction();
        try{
            db.execSQL("insert into apps (appName, appGroup) values (?, ?)",
                    new Object[]{member, group});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        this.mMap.put(member, group);
    }

    public void changeGroup(String destGroup, String pkgName) {
        SQLiteDatabase db = this.mDBHelper.getWritableDatabase();
        db.beginTransaction();
        try{
            db.execSQL("update apps set appGroup = '" + destGroup + "' where appName = '" + pkgName + "'");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        this.mMap.put(pkgName, destGroup);
    }

    public void delMember(String pkgName) {
        SQLiteDatabase db = this.mDBHelper.getWritableDatabase();
        db.beginTransaction();
        try{
            db.execSQL("delete from apps where appName=?", new String[]{pkgName});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        this.mMap.remove(pkgName);

    }

    private void deleteAll(){
        SQLiteDatabase db = this.mDBHelper.getWritableDatabase();
        db.beginTransaction();
        try{
            db.execSQL("delete from apps");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Load the mapping from the db
     * Install the global data structure HashMap<groupLabel, ArrayList<ResolveInfo>>
     */
    public void finishUpdate(List<ResolveInfo> installedApps) {
        this.mGroups.clear();
        for(int i = 0; i < installedApps.size(); ++i){
            String name = installedApps.get(i).activityInfo.packageName;
            String groupLabel = this.mMap.get(name);

            if(groupLabel == null){
                groupLabel = Batman_Launcher.FIRSTGROUP;
                this.addMember(groupLabel, name);
            }

            if(!mGroups.containsKey(groupLabel)){
                ArrayList<ResolveInfo> tmp = new ArrayList<ResolveInfo>();
                tmp.add(installedApps.get(i));
                mGroups.put(groupLabel, tmp);
            } else {
                mGroups.get(groupLabel).add(installedApps.get(i));
            }
        }
    }

    public HashMap<String, ArrayList<ResolveInfo>> getGroups() {
        for(Map.Entry<String, ArrayList<ResolveInfo>> entry : this.mGroups.entrySet()){
            if(entry.getKey() == null){
                for(int i = 0; i < entry.getValue().size(); ++i) {
                    String pkgName = entry.getValue().get(i).activityInfo.packageName;
                    changeGroup(Batman_Launcher.FIRSTGROUP, pkgName);
                    mGroups.get(Batman_Launcher.FIRSTGROUP).add(entry.getValue().get(i));
                }
            }
        }

        /**
         * Sorting
         */
        if(mSorted != null){
            for(Map.Entry<String, ArrayList<ResolveInfo>> entry : this.mGroups.entrySet()){
                ArrayList<ResolveInfo> raw = entry.getValue();
                Collections.sort(raw, new Comparator<ResolveInfo>() {
                    @Override
                    public int compare(ResolveInfo ri1, ResolveInfo ri2) {
                        int l = (mSorted.get(ri1.activityInfo.packageName) == null) ? 0 : mSorted.get(ri1.activityInfo.packageName);
                        int r = (mSorted.get(ri2.activityInfo.packageName) == null) ? 0 : mSorted.get(ri2.activityInfo.packageName);

                        if(l == r)
                            return 0;

                        return (l > r) ? -1 : 1;
                    }
                });
            }
        }

        return this.mGroups;
    }

    public ArrayList<String> getAGroupMemberNames(String groupLabel) {
        ArrayList<ResolveInfo> group = this.mGroups.get(groupLabel);

        if(group == null)
            return null;

        ArrayList<String> ret = new ArrayList<String>();
        for(int i = 0; i < group.size(); ++i){
            ret.add(group.get(i).activityInfo.packageName);
        }

        return ret;
    }

    public List<ResolveInfo> getAGroupResolveInfo(String groupLabel, boolean thisGroup) {
        ArrayList<ResolveInfo> group = new ArrayList<ResolveInfo>();
        if(!thisGroup){
            for(Map.Entry<String, ArrayList<ResolveInfo>> entry : this.mGroups.entrySet()){
                if(!entry.getKey().equals(groupLabel)){
                    group.addAll(entry.getValue());
                }
            }
        } else {
            group = this.mGroups.get(groupLabel);
        }

        return group;
    }

    public void setSortedMap(HashMap<String, Integer> sorted) {
        this.mSorted = sorted;
    }
}
