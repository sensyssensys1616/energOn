package com.nairan.batman_launcher.fragments.main;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nairan.batman_launcher.Batman_Launcher;
import com.nairan.batman_launcher.R;
import com.nairan.batman_launcher.primitives.DbHandlerSingleton;
import com.nairan.batman_launcher.primitives.PkgHandlerSingleton;
import com.nairan.batman_launcher.viewsandadapters.BaseCompatableAdapter;

import java.util.HashSet;

/**
 * Created by nrzhang on 4/8/2015.
 */
public class SettingListViewFragment extends Fragment {

    private static final String TAG = "BMGrouping";

    private ListView mApplv;
    private AppsAdapter mAdapter;
    private String mGroupLabel;
    private HashSet<String> mTmpSecondary = new HashSet<String>();
    private TextView mTv;
    private Button mBtn;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.main_listview, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mGroupLabel = bundle.getString(Batman_Launcher.GROUPLABEL);
        }

        this.mApplv = (ListView) v.findViewById(R.id.appList);
        this.mAdapter = new AppsAdapter();
        this.mAdapter.setData(DbHandlerSingleton
                .getInstance(getActivity().getApplicationContext())
                .getAGroupResolveInfo(mGroupLabel, false));
        this.mApplv.setAdapter(this.mAdapter);

        this.mBtn = (Button) v.findViewById(R.id.addBtn);
        this.mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (String str : mTmpSecondary) {
                    DbHandlerSingleton
                            .getInstance(getActivity().getApplicationContext())
                            .changeGroup(mGroupLabel, str);
                }

                DbHandlerSingleton.getInstance(getActivity().getApplicationContext())
                        .finishUpdate(PkgHandlerSingleton.getInstance(getActivity().getApplicationContext()).getInstalledApps());

                getFragmentManager().popBackStackImmediate();
            }
        });

        return v;
    }

    protected class AppsAdapter extends BaseCompatableAdapter<ResolveInfo> {

        @Override
        public View getView(int position, View convertView, ViewGroup container){
            final ResolveInfo curr = getItem(position);
            if (convertView == null) // have to create a new view
                convertView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.row_installedapp, null);

            if (curr != null){
                final String name = curr.activityInfo.packageName;
                final Drawable icon = curr.loadIcon(getActivity().getPackageManager());
                final CharSequence label = curr.loadLabel(getActivity().getPackageManager());

                ((TextView) convertView.findViewById(R.id.app_name)).setText(label);
                ((ImageView) convertView.findViewById(R.id.app_icon)).setImageDrawable(icon);
                ((ImageView) convertView.findViewById(R.id.app_icon)).setScaleType(ImageView.ScaleType.FIT_CENTER);

                final CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkthisapp);

                cb.setOnCheckedChangeListener(null);
                if(mTmpSecondary.contains(name)){
                    cb.setChecked(true);
                } else {
                    cb.setChecked(false);
                }

                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if (isChecked) {
                            mTmpSecondary.add(name);
                        } else {
                            mTmpSecondary.remove(name);
                        }
                    }
                });
            }

            return convertView;
        }
    }
}
