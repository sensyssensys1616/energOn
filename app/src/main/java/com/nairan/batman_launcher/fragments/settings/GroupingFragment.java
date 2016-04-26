package com.nairan.batman_launcher.fragments.settings;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.nairan.batman_launcher.Batman_Launcher;
import com.nairan.batman_launcher.R;
import com.nairan.batman_launcher.activities.SettingActivity;
import com.nairan.batman_launcher.primitives.DbHandlerSingleton;
import com.nairan.batman_launcher.primitives.PkgHandlerSingleton;

import java.util.ArrayList;
import java.util.HashSet;

public class GroupingFragment extends Fragment {

    private static final String TAG         = "Drag-drop";

    ImageView groupingBtn;
    ImageButton saveBtn;

    /**
     * Temporary hashset (package name. e.g., com.google.android.youtube) to store secondary apps
     *  1) load database to get current secondary apps
     *  2) add/remove according to user selections
     *  3) commit changes to the database
     *
     */
    private HashSet<String> mTmpSecondary;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.setting_groups, container, false);

        groupingBtn = (ImageView) v.findViewById(R.id.groupingView);
        groupingBtn.setOnDragListener(new View.OnDragListener() {

            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                switch (action) {

                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        // Gets the item containing the dragged data
                        ClipData.Item item = event.getClipData().getItemAt(0);
                        int position = Integer.parseInt(item.getText().toString());
                        ((SettingActivity) getActivity()).showConfirmDialog(position);

                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        ((SettingActivity) getActivity()).reDraw();
                        return true;

                    default:
                        Log.e(TAG, "Unknown action type received by OnDragListener.");
                        return true;
                }
            }
        });

        saveBtn = (ImageButton) v.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "Start configuration page");
                ((SettingActivity) getActivity()).showConfigPage();
            }
        });

        /**
         * Create the hashset and load existing secondary apps
         */
        this.mTmpSecondary = new HashSet<String>();
        ArrayList<String> existingSecondary
                = DbHandlerSingleton
                .getInstance(getActivity().getApplicationContext())
                .getAGroupMemberNames(Batman_Launcher.SECONDGROUP);
        if(existingSecondary != null)
            this.mTmpSecondary.addAll(existingSecondary);

        return v;
    }
}