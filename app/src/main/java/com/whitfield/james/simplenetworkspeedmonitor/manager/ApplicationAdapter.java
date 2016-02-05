package com.whitfield.james.simplenetworkspeedmonitor.manager;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.objects.PackageNetwork;
import com.whitfield.james.simplenetworkspeedmonitor.util.Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jwhit on 05/02/2016.
 */
public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    public static final String TAG = "AppMonitorAdapter";
    private ArrayList<PackageNetwork> mDataset;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tvName;
        public TextView tvTransmitted;
        public TextView tvReceived;
        public ImageView ivIcon;

        public ViewHolder(View v) {
            super(v);

            tvName = (TextView) v.findViewById(R.id.tvName);
            tvTransmitted = (TextView) v.findViewById(R.id.tvTransmitted);
            tvReceived = (TextView) v.findViewById(R.id.tvReceived);
            ivIcon = (ImageView) v.findViewById(R.id.ivIcon);

        }
    }

    public ApplicationAdapter(ArrayList<PackageNetwork> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public ApplicationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.application_list_details, parent, false);
        // set the view's size, margins, paddings and layout parameters


        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        PackageNetwork packageNetwork = mDataset.get(position);


        holder.ivIcon.setImageDrawable(packageNetwork.getIcon());
        holder.tvName.setText(packageNetwork.getNameOrPackage());


        String transmittedKbs = Common.bytesToKbs(packageNetwork.getBytesTransmitted());
        String receivedKbs = Common.bytesToKbs(packageNetwork.getBytesReceievd());

        holder.tvTransmitted.setText(Common.stringUpNotificationOutput(packageNetwork.getBytesTransmitted()));
        holder.tvReceived.setText(Common.stringDownNotificationOutput(packageNetwork.getBytesReceievd()));

        Log.i(TAG, packageNetwork.getNameOrPackage() + " - " + "Down: " + receivedKbs + " Kbs Up: " + transmittedKbs + " Kbs");
    }

    public int getItemCount() {
        return mDataset.size();
    }
}
