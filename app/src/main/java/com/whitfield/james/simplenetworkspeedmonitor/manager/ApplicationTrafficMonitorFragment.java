package com.whitfield.james.simplenetworkspeedmonitor.manager;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.application.ApplicationController;
import com.whitfield.james.simplenetworkspeedmonitor.home.HomeActivityInterface;
import com.whitfield.james.simplenetworkspeedmonitor.objects.PackageNetwork;
import com.whitfield.james.simplenetworkspeedmonitor.util.Common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jwhit on 05/02/2016.
 */
public class ApplicationTrafficMonitorFragment extends android.support.v4.app.Fragment {

    public static final String TAG = "AppMonitorFragment";
    private ActionBar actionBar;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutmanager;
    private RecyclerView.Adapter adapter;
    private ProgressWheel wheel;

    ArrayList<PackageNetwork> appsInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appsInfo = new ArrayList<>();
        adapter = new ApplicationAdapter(appsInfo);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;

        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setSubtitle("Application Monitor");

        view = inflater.inflate(R.layout.fragment_list,container,false);

        recyclerView = (RecyclerView) view.findViewById(R.id.lvRecords);
        recyclerView.setHasFixedSize(true);
        layoutmanager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutmanager);

        wheel = (ProgressWheel) view.findViewById(R.id.pwResponse);
        wheel.setBarColor(getResources().getColor(R.color.colorAccent));

        recyclerView.setAdapter(adapter);

        setViewValues();
        return view;
    }

    private void setViewValues() {

        setupData();

    }

    private void setupData() {


        new ApplicationInfoTask().execute();

    }

    private void startSpin(){

        recyclerView.setVisibility(View.GONE);
        wheel.setVisibility(View.VISIBLE);
        wheel.spin();

    }

    private void stopSpin(){


        recyclerView.setVisibility(View.VISIBLE);
        wheel.setVisibility(View.GONE);
        wheel.stopSpinning();
    }

    public class ApplicationInfoTask extends AsyncTask<Void, Integer, String> {




        @Override
        protected String doInBackground(Void... params) {

            PackageManager packageManager = getContext().getPackageManager();
            ArrayList<ApplicationInfo> applicationInfos = (ArrayList<ApplicationInfo>) packageManager
                    .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

            for (ApplicationInfo applicationInfo : applicationInfos){
                appsInfo.add(new PackageNetwork(applicationInfo,packageManager));
            }

            Comparator<PackageNetwork> comparatorReceived = new Comparator<PackageNetwork>() {
                @Override
                public int compare(PackageNetwork lhs, PackageNetwork rhs) {

                    Long receivedRhs = rhs.getBytesReceievd();
                    Long receivedLhs = lhs.getBytesReceievd();

                    if(receivedLhs > receivedRhs){
                        return 1;
                    }else if(receivedLhs < receivedRhs){
                        return -1;
                    }else {
                        return 0;
                    }
                }
            };
            Collections.sort(appsInfo,comparatorReceived);
            Collections.reverse(appsInfo);

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            appsInfo.clear();
            startSpin();

        }


        @Override
        protected void onPostExecute(String state) {

            adapter.notifyDataSetChanged();
            stopSpin();



            super.onPostExecute(null);
        }


    }
}
