package com.nairan.batman_launcher.fragments.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.nairan.batman_launcher.R;
import com.nairan.batman_launcher.activities.MainActivity;
import com.nairan.batman_launcher.activities.SettingActivity;

/**
 * Created by nrzhang on 11/8/2015.
 */
public class ConfirmFragment extends DialogFragment {

    private String msg;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        this.msg = getArguments().getString("packageName");

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(this.msg)
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((SettingActivity) getActivity()).confirmMove(msg, true);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((SettingActivity) getActivity()).confirmMove(msg, false);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
