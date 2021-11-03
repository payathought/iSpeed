package com.example.ispeed.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


import com.example.ispeed.Model.InternetDataModel;
import com.example.ispeed.Model.TimeAndSpeedModel;
import com.example.ispeed.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class DashBoardDataAdapter extends RecyclerView.Adapter<DashBoardDataAdapter.ViewHolder> {
    private Context mContext;
    ArrayList<TimeAndSpeedModel> mTimeAndSpeedModels = new ArrayList<>();

    private static final String TAG = "DashBoardDataAdapter";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public DashBoardDataAdapter(Context context, ArrayList<TimeAndSpeedModel> mTimeAndSpeedModels) {
        this.mContext = context;
        this.mTimeAndSpeedModels = mTimeAndSpeedModels;


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_dashboard_data_list_item, parent,false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        TimeAndSpeedModel model = mTimeAndSpeedModels.get(position);

        holder.tv_rv_speed.setText(model.getSpeed());
        holder.tv_rv_time.setText(model.getTime());


    }

    @Override
    public int getItemCount() {
        return mTimeAndSpeedModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        TextView tv_rv_time,tv_rv_speed;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_rv_time = itemView.findViewById(R.id.tv_rv_time);
            tv_rv_speed = itemView.findViewById(R.id.tv_rv_speed);

        }
    }



}