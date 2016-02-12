package com.whitfield.james.simplenetworkspeedmonitor.manager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.whitfield.james.simplenetworkspeedmonitor.R;

/**
 * Created by jwhit on 11/02/2016.
 */
public class AppMonitorHelpDialogFragment extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.app_monitor_help);
        builder.setTitle("Help");

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
