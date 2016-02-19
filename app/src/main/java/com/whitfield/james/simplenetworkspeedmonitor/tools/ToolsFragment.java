package com.whitfield.james.simplenetworkspeedmonitor.tools;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.home.HomeActivity;
import com.whitfield.james.simplenetworkspeedmonitor.services.MobileDataMonitorService;
import com.whitfield.james.simplenetworkspeedmonitor.services.NetworkIntentService;

/**
 * Created by jwhit on 12/02/2016.
 */
public class ToolsFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener, SwitchCompat.OnCheckedChangeListener {


    public static final String INTENT_SERVICE_TIME_TAG = "TIME";
    private ActionBar actionBar;
    private AppCompatSpinner spinner;
    private SwitchCompat switchCompat;
    private ArrayAdapter<CharSequence> adapter;
    private String unit_minutes;
    private int spinnerPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;

        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setSubtitle("Extra Tools");

        view = inflater.inflate(R.layout.tools_screen,container,false);



        spinner = (AppCompatSpinner) view.findViewById(R.id.spTime);
        switchCompat = (SwitchCompat) view.findViewById(R.id.swMobile);



        adapter = ArrayAdapter.createFromResource(getContext(), R.array.time_units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        if(isMyServiceRunning(MobileDataMonitorService.class)){

            switchCompat.setChecked(true);
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.preferences_key), getActivity().MODE_PRIVATE);

            spinnerPosition = sharedPreferences.getInt(getString(R.string.MDR_key), 0);
            spinner.setSelection(spinnerPosition);
        }

        switchCompat.setOnCheckedChangeListener(this);


        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(position > 0) {
            unit_minutes = (String) parent.getItemAtPosition(position);
            spinnerPosition = position;
        }else{
            unit_minutes = null;
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

        unit_minutes = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(isChecked){
            if(unit_minutes != null){
                //TODO do stuff

                Intent intent = new Intent(getContext(), MobileDataMonitorService.class);
                Bundle bundle = new Bundle();
                int i =  Integer.parseInt(unit_minutes.split(" ")[0]);
                bundle.putInt(INTENT_SERVICE_TIME_TAG,i);
                intent.putExtras(bundle);
                Log.i("Mobile","Service starting...");
                getActivity().startService(intent);

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.preferences_key), Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.MDR_key),spinnerPosition);
                editor.commit();

            }else{
                Toast.makeText(getContext(),"Value required",Toast.LENGTH_SHORT).show();
                switchCompat.setChecked(false);
            }
        }else{
            //TODO end stuff
            Log.i("Mobile","Service Stopping...");
            getActivity().stopService( new Intent(getContext(), MobileDataMonitorService.class));
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
