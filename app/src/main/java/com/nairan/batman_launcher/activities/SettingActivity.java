package com.nairan.batman_launcher.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.nairan.batman_launcher.Batman_Launcher;
import com.nairan.batman_launcher.R;
import com.nairan.batman_launcher.fragments.settings.AppGridFragment;
import com.nairan.batman_launcher.fragments.settings.ConfigFragment;
import com.nairan.batman_launcher.fragments.settings.ConfirmFragment;
import com.nairan.batman_launcher.fragments.settings.GroupingFragment;
import com.nairan.batman_launcher.primitives.DbHandlerSingleton;
import com.nairan.batman_launcher.primitives.PkgHandlerSingleton;

import java.util.ArrayList;

public class SettingActivity extends FragmentActivity {

    private static final String TAG = "Batman Setting";

    private static final String GROUP_TAG = "group tag";
    private static final String GRID_TAG = "grid tag";

    private AppGridFragment appGridFragment;
    private GroupingFragment groupingFragment;
    private ConfirmFragment confirmFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container_setting) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            showAppGridFragment();
        }
    }

    public void showAppGridFragment(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        appGridFragment = new AppGridFragment();
        fragmentTransaction.replace(R.id.fragment_container_setting, appGridFragment).commit();
    }

    public void showDragListener() {
        groupingFragment = (GroupingFragment) getSupportFragmentManager().findFragmentByTag(GROUP_TAG);
        if(groupingFragment != null && groupingFragment.isVisible())
            return;

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        groupingFragment = new GroupingFragment();
        appGridFragment = new AppGridFragment();
        fragmentTransaction.replace(R.id.fragment_container_setting, groupingFragment, GROUP_TAG);
        fragmentTransaction.add(R.id.fragment_container_setting, appGridFragment, GRID_TAG);
        fragmentTransaction.commit();
    }

    public void showConfirmDialog(int position) {
        ArrayList<ResolveInfo> firstGroup = DbHandlerSingleton
                .getInstance(getApplicationContext())
                .getGroups()
                .get(Batman_Launcher.FIRSTGROUP);
        Bundle bundle = new Bundle();
        bundle.putString("packageName", firstGroup.get(position).activityInfo.packageName);
        confirmFragment = new ConfirmFragment();
        confirmFragment.setArguments(bundle);
        confirmFragment.show(getSupportFragmentManager(), "confirm dialog");
    }

    public void confirmMove(String msg, boolean b) {
        if(b) {
            DbHandlerSingleton.getInstance(getApplicationContext()).changeGroup(Batman_Launcher.SECONDGROUP, msg);
            DbHandlerSingleton.getInstance(getApplicationContext()).finishUpdate(PkgHandlerSingleton.getInstance(getApplicationContext()).getInstalledApps());

            reDraw();
        }
    }

    public void reDraw() {
        appGridFragment.reDraw();
    }

    public void showConfigPage() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ConfigFragment configFragment = new ConfigFragment();
        fragmentTransaction.remove(getSupportFragmentManager().findFragmentByTag(GRID_TAG));
        fragmentTransaction.remove(getSupportFragmentManager().findFragmentByTag(GROUP_TAG));
        fragmentTransaction.add(R.id.fragment_container_setting, configFragment);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}