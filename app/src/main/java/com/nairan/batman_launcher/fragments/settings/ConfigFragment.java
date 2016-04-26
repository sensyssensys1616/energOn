package com.nairan.batman_launcher.fragments.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nairan.batman_launcher.Batman_Launcher;
import com.nairan.batman_launcher.R;
import com.nairan.batman_launcher.activities.MainActivity;
import com.nairan.batman_launcher.activities.SettingActivity;
import com.nairan.batman_launcher.viewsandadapters.DynamicGridView;

/**
 * Created by nrzhang on 11/8/2015.
 */
public class ConfigFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "Configuration page";
    private NumberPicker np = null;
    private RadioGroup rg = null;
    private Button save = null;
    private int chargingPolicy;
    private SharedPreferences preference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.setting_config, container, false);
        ((TextView) v.findViewById(R.id.configprompt1)).setText(Batman_Launcher.FRACTIONPROMPT);
        ((TextView) v.findViewById(R.id.configprompt3)).setText(Batman_Launcher.CHARGINGPROMPT);

        preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        chargingPolicy = Integer.parseInt(preference.getString(Batman_Launcher.PREFERENCE_CHARGING_POLICY, ""));
        {
            if(chargingPolicy == 1)
                ((RadioButton) v.findViewById(R.id.radio_proportional)).setChecked(true);
            if(chargingPolicy == 2)
                ((RadioButton) v.findViewById(R.id.radio_equal)).setChecked(true);
            if(chargingPolicy == 3)
                ((RadioButton) v.findViewById(R.id.radio_strict)).setChecked(true);
            if(chargingPolicy == 4)
                ((RadioButton) v.findViewById(R.id.radio_leastpercent)).setChecked(true);
        }

        // Fraction picker
        String portion = preference.getString(Batman_Launcher.PREFERENCE_PORTION, "");
        np = (NumberPicker) v.findViewById(R.id.numberPicker);
        np.setMaxValue(100);
        np.setMinValue(0);
        np.setValue(Integer.parseInt(portion));
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal == 0 || newVal == 100)
                    picker.setValue(newVal);
                else
                    picker.setValue((newVal < oldVal) ? oldVal - 10 : oldVal + 10);
            }

        });

        // Charging policy selection
        v.findViewById(R.id.radio_proportional).setOnClickListener(this);
        v.findViewById(R.id.radio_equal).setOnClickListener(this);
        v.findViewById(R.id.radio_strict).setOnClickListener(this);
        v.findViewById(R.id.radio_leastpercent).setOnClickListener(this);

        // Done!
        save = (Button) v.findViewById(R.id.saveConfigBtn);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = preference.edit();
                editor.putString(Batman_Launcher.PREFERENCE_PORTION, String.valueOf(np.getValue()));
                editor.putString(Batman_Launcher.PREFERENCE_CHARGING_POLICY, String.valueOf(chargingPolicy));
                editor.commit();
                Activity parent = getActivity();
                if(parent instanceof MainActivity){
                    ((MainActivity) parent).showMainFragment();
                }
                if(parent instanceof SettingActivity){
                    parent.finish();
                }
            }
        });

        Log.w(TAG, "within configuration page");
        return v;
    }

    @Override
    public void onClick(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_proportional:
                if (checked)
                    chargingPolicy = 1;
                break;
            case R.id.radio_equal:
                if (checked)
                    chargingPolicy = 2;
                break;
            case R.id.radio_strict:
                if (checked)
                    chargingPolicy = 3;
                break;
            case R.id.radio_leastpercent:
                if (checked)
                    chargingPolicy = 4;
                break;
            default:
                chargingPolicy = 1;
        }
    }
}
