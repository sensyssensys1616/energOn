package com.nairan.batman_launcher.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.nairan.batman_launcher.primitives.UserApp;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by nrzhang on 8/8/2015.
 */

public class UserAppsLoader extends AsyncTaskLoader<ArrayList<UserApp>> {

    private static final String TAG             = "Batman loader";
    private Context mCtx;
    private ArrayList<UserApp> mInstalledApps;
    private PackageIntentReceiver mPackageObserver;

    public UserAppsLoader(Context context) {
        super(context);
        this.mCtx = context;
    }

    @Override
    public ArrayList<UserApp> loadInBackground() {
        // retrieve the list of installed applications
        List<ApplicationInfo> apps = this.mCtx.getPackageManager().getInstalledApplications(0);

        if(apps == null) {
            apps = new ArrayList<ApplicationInfo>();
        }

        // create corresponding apps and load their labels
        ArrayList<UserApp> items = new ArrayList<UserApp>(apps.size());
        for (int i = 0; i < apps.size(); i++) {
            String pkg = apps.get(i).packageName;

            // only apps which are launchable
            if (this.mCtx.getPackageManager().getLaunchIntentForPackage(pkg) != null) {
                Log.d(TAG, "app: " + pkg);
                UserApp app = new UserApp(this.mCtx, apps.get(i));
                app.loadLabel();
                items.add(app);
            }
        }

        // sort the list
        Collections.sort(items, ALPHA_COMPARATOR);

        return items;
    }

    @Override
    public void deliverResult(ArrayList<UserApp> apps) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (apps != null) {
                onReleaseResources(apps);
            }
        }

        ArrayList<UserApp> oldApps = apps;
        mInstalledApps = apps;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(apps);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldApps != null) {
            onReleaseResources(oldApps);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mInstalledApps != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mInstalledApps);
        }

        // watch for changes in app install and uninstall operation
        if (mPackageObserver == null) {
            mPackageObserver = new PackageIntentReceiver(this);
        }

        if (takeContentChanged() || mInstalledApps == null ) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(ArrayList<UserApp> apps) {
        super.onCanceled(apps);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(apps);
    }

    @Override
    protected void onReset() {
        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mInstalledApps != null) {
            onReleaseResources(mInstalledApps);
            mInstalledApps = null;
        }

        // Stop monitoring for changes.
        if (mPackageObserver != null) {
            getContext().unregisterReceiver(mPackageObserver);
            mPackageObserver = null;
        }
    }

    /**
     * Helper method to do the cleanup work if needed, for example if we're
     * using Cursor, then we should be closing it here
     *
     * @param apps
     */
    protected void onReleaseResources(ArrayList<UserApp> apps) {
        // do nothing
    }


    /**
     * Perform alphabetical comparison of application entry objects.
     */
    public static final Comparator<UserApp> ALPHA_COMPARATOR = new Comparator<UserApp>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(UserApp object1, UserApp object2) {
            return sCollator.compare(object1.getLabel(), object2.getLabel());
        }
    };
}