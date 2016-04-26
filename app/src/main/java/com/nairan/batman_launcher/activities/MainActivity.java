package com.nairan.batman_launcher.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.nairan.batman_launcher.Batman_Launcher;
import com.nairan.batman_launcher.fragments.main.MainFragment;
import com.nairan.batman_launcher.fragments.main.NewAppPickGroupFragment;
import com.nairan.batman_launcher.fragments.main.PerGroupFragment;
import com.nairan.batman_launcher.fragments.settings.ConfigFragment;
import com.nairan.batman_launcher.primitives.DbHandlerSingleton;
import com.nairan.batman_launcher.R;
import com.nairan.batman_launcher.primitives.PkgHandlerSingleton;
import com.nairan.batman_launcher.trackers.BatmanService;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "Batman MainActivity";
    public static final int REQUESTCODE_CONFIG = 999;
    public static final String REGROUP_TAG = "regroup tag";
    public static final String GROUP_TAG = "Per-group : ";
    public static final String HOME_FRAGMENT = "Home";
    private static final String NEW_APP_ALERT = "new app group dialog";

    private boolean mShowInstall = false;
    private boolean mShowUninstall = false;
    private String mInstalledApp;
    private String mUninstalledApp;

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                Uri data = intent.getData();
                mInstalledApp = data.getEncodedSchemeSpecificPart();

                if(!mInstalledApp.equals("com.nairan.batman_launcher")) {
                    mShowInstall = true;
                }
            }

            if (action.equals(Intent.ACTION_PACKAGE_FULLY_REMOVED)) {
                Uri data = intent.getData();
                mUninstalledApp = data.getEncodedSchemeSpecificPart();
                Log.w(TAG, "Fully removed : " + mUninstalledApp);
                if(!mUninstalledApp.equals("com.nairan.batman_launcher")) {
                    mShowUninstall = true;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        intentFilter.addDataScheme("package");
        registerReceiver(br, intentFilter);

        startService(new Intent(this, BatmanService.class));

        if(DbHandlerSingleton.getInstance(this.getApplicationContext())
                .getGroups()
                .get(Batman_Launcher.SECONDGROUP) == null){
            Intent i = new Intent(this, SettingActivity.class);
            startActivity(i);
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        if (mShowInstall) {
            mShowInstall = false;

            if(!DbHandlerSingleton.getInstance(this.getApplicationContext()).getMap().containsKey(mInstalledApp)) {
                // Show only if is necessary, otherwise FragmentManager will take care
                if (getSupportFragmentManager().findFragmentByTag(NEW_APP_ALERT) == null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("packageName", mInstalledApp);
                    NewAppPickGroupFragment newAppPickGroupFragment = new NewAppPickGroupFragment();
                    newAppPickGroupFragment.setArguments(bundle);
                    newAppPickGroupFragment.show(getSupportFragmentManager(), NEW_APP_ALERT);
                }
            }

        } else if(mShowUninstall) {
            mShowUninstall = false;

            String groupLabel = DbHandlerSingleton.getInstance(getApplicationContext())
                    .getMap().get(mUninstalledApp);
            PkgHandlerSingleton.getInstance(getApplicationContext()).reset(getApplicationContext());
            DbHandlerSingleton.getInstance(getApplicationContext())
                    .delMember(mUninstalledApp);
            DbHandlerSingleton.getInstance(getApplicationContext())
                    .finishUpdate(PkgHandlerSingleton.getInstance(getApplicationContext()).getInstalledApps());

            showPerGroup(groupLabel);
        } else {
            showMainFragment();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(br);
        } catch(IllegalArgumentException e) {

        }
    }

    public void showPerGroup(String groupLabel) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        PerGroupFragment perGroupFragment = new PerGroupFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Batman_Launcher.GROUPLABEL, groupLabel);
        perGroupFragment.setArguments(bundle);

        fragmentTransaction.replace(R.id.fragment_container_main, new MainFragment());
        fragmentTransaction.replace(R.id.fragment_container_main, perGroupFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(HOME_FRAGMENT);
        if(!mainFragment.isVisible())
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_config, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.batman_config:
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                ConfigFragment configFragment = new ConfigFragment();
                fragmentTransaction.replace(R.id.fragment_container_main, configFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showMainFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        MainFragment mainFragment = new MainFragment();
        fragmentTransaction.replace(R.id.fragment_container_main, mainFragment, HOME_FRAGMENT);
        fragmentTransaction.commit();
    }

    public void setNewAppGroup(String label, String pkgName) {
        PkgHandlerSingleton.getInstance(getApplicationContext())
                .reset(getApplicationContext());
        DbHandlerSingleton.getInstance(getApplicationContext())
                .addMember(label, pkgName);
        DbHandlerSingleton.getInstance(getApplicationContext())
                .finishUpdate(PkgHandlerSingleton.getInstance(getApplicationContext()).getInstalledApps());

        showMainFragment();
    }
}