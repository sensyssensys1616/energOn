package com.nairan.batman_launcher.fragments.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.nairan.batman_launcher.R;
import com.nairan.batman_launcher.activities.MainActivity;
import com.nairan.batman_launcher.activities.SettingActivity;
import com.nairan.batman_launcher.primitives.DbHandlerSingleton;

import java.util.Set;

/**
 * Created by nrzhang on 11/8/2015.
 */
public class NewAppPickGroupFragment extends DialogFragment {

    private String msg;
    private static final String TAG = "New app pick group";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        this.msg = getArguments().getString("packageName");

        Set<String> allLabels = DbHandlerSingleton.getInstance(getActivity().getApplicationContext())
                .getGroups().keySet();
        final CharSequence[] names = allLabels.toArray(new String[allLabels.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick a group for " + this.msg)
                .setItems(names, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String label = names[which].toString();
                        ((MainActivity) getActivity()).setNewAppGroup(label, msg);
                    }
                });
        return builder.create();
    }
}
