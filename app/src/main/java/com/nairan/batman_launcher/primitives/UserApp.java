package com.nairan.batman_launcher.primitives;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import java.io.File;

/**
 * Created by nrzhang on 8/8/2015.
 */

public class UserApp {

    private final Context mCtx;
    private final ApplicationInfo mInfo;
    private final File mApkFile;

    private String mAppLabel;
    private Drawable mIcon;
    private boolean mMounted;

    public UserApp(Context context, ApplicationInfo info) {
        this.mCtx = context;
        this.mInfo = info;
        this.mApkFile = new File(info.sourceDir);
    }

    public ApplicationInfo getAppInfo() {
        return this.mInfo;
    }

    public String getApplicationPackageName() {
        return getAppInfo().packageName;
    }

    public String getLabel() {
        return this.mAppLabel;
    }

    public Drawable getIcon() {
        if(this.mIcon == null) {
            if (this.mApkFile.exists()) {
                this.mIcon = this.mInfo.loadIcon(this.mCtx.getPackageManager());
                return this.mIcon;
            } else {
                this.mMounted = false;
            }
        } else if(!this.mMounted) {
            // If the app wasn't mounted but is now mounted, reload
            // its icon.
            if (this.mApkFile.exists()) {
                this.mMounted = true;
                this.mIcon = this.mInfo.loadIcon(this.mCtx.getPackageManager());
                return this.mIcon;
            }
        } else {
            return this.mIcon;
        }

        return ContextCompat.getDrawable(this.mCtx, android.R.drawable.sym_def_app_icon);
    }


    public void loadLabel() {
        if (this.mAppLabel == null || !this.mMounted) {
            if (!this.mApkFile.exists()) {
                this.mMounted = false;
                this.mAppLabel = this.mInfo.packageName;
            } else {
                this.mMounted = true;
                CharSequence label = this.mInfo.loadLabel(this.mCtx.getPackageManager());
                this.mAppLabel = label != null ? label.toString() : this.mInfo.packageName;
            }
        }
    }
}