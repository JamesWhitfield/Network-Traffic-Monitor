package com.whitfield.james.simplenetworkspeedmonitor.tools;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
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

/**
 * Created by jwhit on 12/02/2016.
 */
public class ToolsFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener, SwitchCompat.OnCheckedChangeListener {


    private ActionBar actionBar;
    private AppCompatSpinner spinner;
    private SwitchCompat switchCompat;
    private ArrayAdapter<CharSequence> adapter;
    private String unit_minutes;

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

        switchCompat.setOnCheckedChangeListener(this);

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(position > 0) {
            unit_minutes = (String) parent.getItemAtPosition(position);
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
            }else{
                Toast.makeText(getContext(),"Value required",Toast.LENGTH_SHORT).show();
                switchCompat.setChecked(false);
            }
        }else{
            //TODO end stuff
        }
    }
}
