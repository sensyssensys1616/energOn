package com.nairan.batman_launcher.fragments.main;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nairan.batman_launcher.Batman_Launcher;
import com.nairan.batman_launcher.R;
import com.nairan.batman_launcher.activities.MainActivity;
import com.nairan.batman_launcher.primitives.DbHandlerSingleton;
import com.nairan.batman_launcher.primitives.PkgHandlerSingleton;
import com.nairan.batman_launcher.viewsandadapters.BaseDynamicGridAdapter;
import com.nairan.batman_launcher.viewsandadapters.DynamicGridView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by nrzhang on 13/8/2015.
 */
public class PerGroupFragment extends Fragment {

    private static final String TAG = "per-group fragment";

    private List<ResolveInfo> mGroupApps;
    private GroupAppsAdapter mAdapter;
    private DynamicGridView mGroupAppsGv;
    private String mGroupLabel;
    private TextView mTv;
    private Button mBtn;
    private Context mAppCtx;
    private ViewGroup mInsertPoint;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mGroupLabel = bundle.getString(Batman_Launcher.GROUPLABEL);
        }

        final View grid = inflater.inflate(R.layout.groupgrid, null);
        mInsertPoint = (ViewGroup) grid.findViewById(R.id.pergroup);

        this.mGroupAppsGv = (DynamicGridView) grid.findViewById(R.id.group);
        this.mAdapter = new GroupAppsAdapter(getActivity(), getResources().getInteger(R.integer.column_count));
        this.mGroupApps = DbHandlerSingleton.getInstance(mAppCtx)
                .getGroups().get(mGroupLabel);
        this.mAdapter.set(this.mGroupApps);
        this.mGroupAppsGv.setAdapter(this.mAdapter);

        this.mGroupAppsGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                String pkgName = DbHandlerSingleton.getInstance(mAppCtx)
                        .getGroups().get(mGroupLabel).get(position).activityInfo.packageName;
                Intent i = getActivity().getPackageManager().getLaunchIntentForPackage(pkgName);
                getActivity().startActivity(i);
            }
        });

        this.mGroupAppsGv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String pkgName = DbHandlerSingleton.getInstance(mAppCtx)
                        .getGroups().get(mGroupLabel).get(position).activityInfo.packageName;

                CharSequence groupAndpkg = mGroupLabel + "##" + pkgName;
                ClipData.Item item = new ClipData.Item(groupAndpkg);
                String[] types = new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData dragData = new ClipData(groupAndpkg, types, item);
                DragShadow ds = new DragShadow(view);

                if(mInsertPoint.findViewById(R.id.regrouping) == null) {
                    Log.d(TAG, "show drag listener!");
                    showDragListener(mInsertPoint);
                }

                view.startDrag(dragData, ds, view, 0);

                return true;
            }
        });

        this.mBtn = (Button) grid.findViewById(R.id.addBtn);
        this.mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingListViewFragment fragment = new SettingListViewFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Batman_Launcher.GROUPLABEL, mGroupLabel);
                fragment.setArguments(bundle);

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container_main, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String[] levels = preferences.getString(Batman_Launcher.PREFERENCE_VBLEVELS, "").split(Batman_Launcher.FIRSTDELIMIT);
        HashMap<String, Double> vblevels = new HashMap<String, Double>();
        for(int j = 0; j < levels.length; ++j){
            vblevels.put(levels[j].split(Batman_Launcher.SECONDDELIMIT)[0],
                    Double.parseDouble(levels[j].split(Batman_Launcher.SECONDDELIMIT)[1]));
        }
        this.mTv = (TextView) grid.findViewById(R.id.groupLabel);
        this.mTv.setText(mGroupLabel + " : " + vblevels.get(mGroupLabel).intValue() + "%");
        /*
        if(mGroupLabel.equals(Batman_Launcher.FIRSTGROUP)) {
            this.mTv.setText(mGroupLabel + " : 18%");
        } else {
            this.mTv.setText(mGroupLabel + " : 48%");
        }
        */
        return grid;
    }

    private void showDragListener(final ViewGroup container) {

        LayoutInflater inflater = (LayoutInflater) mAppCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.main_regrouping, container, false);
        v.findViewById(R.id.uninstallView)
                .setOnDragListener(new View.OnDragListener() {

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

                                container.removeAllViews();

                                // Gets the item containing the dragged data
                                ClipData.Item item = event.getClipData().getItemAt(0);
                                String groupAndpkg = item.getText().toString();
                                String pkg = groupAndpkg.split("##")[1];
                                Intent i = new Intent(Intent.ACTION_DELETE);
                                i.setData(Uri.parse("package:" + pkg));
                                startActivity(i);
                                return true;

                            case DragEvent.ACTION_DRAG_ENDED:
                                return true;

                            default:
                                Log.e(TAG, "Unknown action type received by OnDragListener.");
                                return true;
                        }
                    }
                });

        v.findViewById(R.id.regroupingView)
                .setOnDragListener(new View.OnDragListener() {

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
                                String groupAndpkg = item.getText().toString();
                                Log.d(TAG, " Change group : " + groupAndpkg);
                                String groupLabel = groupAndpkg.split("##")[0];
                                String pkg = groupAndpkg.split("##")[1];
                                groupLabel = (groupLabel.equals(Batman_Launcher.FIRSTGROUP)) ? Batman_Launcher.SECONDGROUP : Batman_Launcher.FIRSTGROUP;
                                DbHandlerSingleton
                                        .getInstance(mAppCtx)
                                        .changeGroup(groupLabel, pkg);

                                DbHandlerSingleton.getInstance(mAppCtx)
                                        .finishUpdate(PkgHandlerSingleton.getInstance(mAppCtx).getInstalledApps());

                                return true;
                            case DragEvent.ACTION_DRAG_ENDED:
                                ((MainActivity) getActivity()).showPerGroup(mGroupLabel);
                                //Log.d(TAG, "Drag ends : " + container.getChildCount());
                                //container.removeView(v);
                                //container.removeView((View) v.getParent());
                                //Log.d(TAG, "Drag ends now : " + container.getChildCount());
                                //mAdapter.set(DbHandlerSingleton
                                //        .getInstance(mAppCtx)
                                //        .getGroups().get(mGroupLabel));
                                return true;

                            default:
                                Log.e(TAG, "Unknown action type received by OnDragListener.");
                                return true;
                        }
                    }
                });

        container.addView(v, 0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mAppCtx = activity.getApplicationContext();
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

    /***********************************************************************************************
     * Custom adapter
     */
    private class GroupAppsAdapter extends BaseDynamicGridAdapter<ResolveInfo> {

        public GroupAppsAdapter(Context context, int columnCount) {
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
                final String label = curr.loadLabel(getActivity().getPackageManager()).toString();
                final Drawable icon = curr.loadIcon(getActivity().getPackageManager());
                ((TextView) convertView.findViewById(R.id.label)).setText(label);
                ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(icon);
                ((ImageView) convertView.findViewById(R.id.icon)).setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            return convertView;
        }
    }
}