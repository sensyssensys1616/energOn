package com.nairan.batman_launcher.fragments.settings;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nairan.batman_launcher.Batman_Launcher;
import com.nairan.batman_launcher.R;
import com.nairan.batman_launcher.activities.SettingActivity;
import com.nairan.batman_launcher.fragments.main.PerGroupFragment;
import com.nairan.batman_launcher.primitives.DbHandlerSingleton;
import com.nairan.batman_launcher.primitives.PkgHandlerSingleton;
import com.nairan.batman_launcher.viewsandadapters.BaseDynamicGridAdapter;
import com.nairan.batman_launcher.viewsandadapters.DynamicGridView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by nrzhang on 31/8/2015.
 */
public class AppGridFragment extends Fragment
        /*implements AbsListView.MultiChoiceModeListener*/ {

    private static final String TAG         = "Batman grid UI";

    private AppsAdapter mAdapter; // Adapter to draw children views in a gridview
    private DynamicGridView mAppgv;

    /***********************************************************************************************
     *
     * Overrides inherited methods -  as a fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.setting_grid, container, false);
        this.mAppgv = (DynamicGridView) v.findViewById(R.id.grid);
        this.mAdapter = new AppsAdapter(getActivity(), getResources().getInteger(R.integer.column_count));
        this.mAppgv.setAdapter(this.mAdapter);
        return v;
    }

    /**
     * It can be used to do final initialization
     * This is called after onCreateView
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        reDraw();

        /*
        this.mAppgv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getActivity(), "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
        */

        this.mAppgv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Log.w(TAG, "I hear a long click: " + ((TextView) view.findViewById(R.id.label)).getText());

                CharSequence pos = String.valueOf(position);
                ClipData.Item item = new ClipData.Item(pos);
                String[] types = new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData dragData = new ClipData(pos, types, item);
                DragShadow ds = new DragShadow(view);

                ((SettingActivity) getActivity()).showDragListener();

                view.startDrag(dragData, ds, view, 0);

                return true;
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private class DragShadow extends View.DragShadowBuilder{

        private Drawable shadow;

        @Override
        public void onDrawShadow(Canvas canvas) {
            shadow.draw(canvas);
        }

        @Override
        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {

            View v = getView();
            int height = v.getHeight();
            int width = v.getWidth();

            shadow.setBounds(0, 0, width, height);

            shadowSize.set(width, height);
            shadowTouchPoint.set(width, height);
        }

        public DragShadow(View view) {
            super(view);
            shadow = ((ImageView) view.findViewById(R.id.icon)).getDrawable();
        }
    }

    public void reDraw() {
        this.mAdapter.set(DbHandlerSingleton
                .getInstance(getActivity().getApplicationContext())
                .getGroups().get(Batman_Launcher.FIRSTGROUP));
    }

    /***********************************************************************************************
     *
     * Implements abstract methods -   contexual action bar

     @Override
     public boolean onCreateActionMode(ActionMode mode, Menu menu) {
     // Inflate the menu for the CAB
     MenuInflater inflater = mode.getMenuInflater();
     inflater.inflate(R.menu.menu_cab, menu);

     return true;
     }

     @Override
     public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
     // Here you can perform updates to the CAB due to
     // an invalidate() request
     return false;
     }

     @Override
     public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
     // Respond to clicks on the actions in the CAB
     return false;
     }

     @Override
     public void onDestroyActionMode(ActionMode mode) {
     // Here you can make any necessary updates to the activity when
     // the CAB is removed. By default, selected items are deselected/unchecked.
     // Create fragment and give it an argument specifying the article it should show

     for (String s : this.mTmpSecondary) {
     Log.i(TAG, "who is this: " + s);
     if (!DbHandlerSingleton.getInstance(getActivity().getApplicationContext())
     .getMap().get(s).equals(Batman_Launcher.SECONDGROUP))
     DbHandlerSingleton.getInstance(getActivity().getApplicationContext())
     .changeGroup(Batman_Launcher.SECONDGROUP, s);
     }

     DbHandlerSingleton.getInstance(getActivity().getApplicationContext())
     .finishUpdate(PkgHandlerSingleton.getInstance(getActivity().getApplicationContext())
     .getInstalledApps());

     AllocatingFragment newFragment = new AllocatingFragment();
     newFragment.show(getFragmentManager(), "dialog");
     }

     @Override
     public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
     int firstVisible = this.mAppgv.getFirstVisiblePosition();
     int childPosition = position - firstVisible;

     View v = this.mAppgv.getChildAt(childPosition);
     if(v != null) {
     ResolveInfo ri = PkgHandlerSingleton.getInstance(getActivity().getApplicationContext())
     .getInstalledApps().get(position);
     String pkgName = ri.activityInfo.packageName;
     if (!this.mTmpSecondary.contains(pkgName)) {
     this.mTmpSecondary.add(pkgName);
     v.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
     } else {
     this.mTmpSecondary.remove(pkgName);
     v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
     }
     }
     }
     */
    /***********************************************************************************************
     * Custom adapter
     */
    private class AppsAdapter extends BaseDynamicGridAdapter<ResolveInfo> {

        public AppsAdapter(Context context, int columnCount) {
            super(context, columnCount);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            final ResolveInfo curr = getItem(position);
            if (convertView == null) {// have to create a new view
                LayoutInflater layoutInflater
                        = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.icon_label, container, false);
            }

            if (curr != null) {
                String name = curr.activityInfo.packageName;
                CharSequence label = curr.loadLabel(getActivity().getPackageManager());
                Drawable icon = curr.loadIcon(getActivity().getPackageManager());
                ((TextView) convertView.findViewById(R.id.label)).setText(label);
                ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(icon);
                ((ImageView) convertView.findViewById(R.id.icon)).setScaleType(ImageView.ScaleType.FIT_CENTER);

                //mAppgv.setMultiChoiceModeListener(null);
                //if(mTmpSecondary.contains(name)){
                //    convertView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                //} else {
                //    convertView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                //}
                //mAppgv.setMultiChoiceModeListener(GroupingFragment.this);
            }

            return convertView;
        }
    }
}
