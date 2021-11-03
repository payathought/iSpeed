package com.example.ispeed.View.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.ispeed.Model.InternetDataModel;
import com.example.ispeed.R;
import com.example.ispeed.View.HomeActivity;
import com.example.ispeed.View.SignUpActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;

import es.dmoral.toasty.Toasty;

public class ViewSaveData extends AppCompatDialogFragment {
    FirebaseFirestore db;
    TextView tv_time,tv_loc,tv_ping,tv_download,tv_upload,tv_isp,tv_stablitiy;
    Button btn_ok,btn_cancel;
    String user_id = "";

    InternetDataModel dataModel;

    private static final String TAG = "ViewUserInfoDialog";
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_save_data_dialog,null);

        btn_ok = view.findViewById(R.id.btn_ok);
        btn_cancel = view.findViewById(R.id.btn_cancel);
        tv_time = view.findViewById(R.id.tv_time);
        tv_loc = view.findViewById(R.id.tv_loc);
        tv_ping = view.findViewById(R.id.tv_ping);
        tv_download = view.findViewById(R.id.tv_download);
        tv_upload = view.findViewById(R.id.tv_upload);
        tv_isp = view.findViewById(R.id.tv_isp);
        tv_stablitiy = view.findViewById(R.id.tv_stablitiy);
        db = FirebaseFirestore.getInstance();
        builder.setView(view);

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aa");

        tv_time.setText( "Time Recorded: " +formatter.format(date));
        tv_loc.setText("Location: " + dataModel.getLocation());
        tv_ping.setText( "Ping: " + dataModel.getPing() + "Ms");
        tv_download.setText("Download: " + dataModel.getDownLoadSpeed() + "Mbps");
        tv_upload.setText("Upload: " + dataModel.getUploadSpeed() + "Mbps");
        tv_isp.setText("ISP: " + dataModel.getIsp());
        try {
            if(Double.parseDouble(dataModel.getPing()) < 100){
                tv_stablitiy.setText("Stability: Stable");
                dataModel.setStability(true);
            }else{
                tv_stablitiy.setText("Stability: Unstable");
                dataModel.setStability(false);

            }
        } catch (NumberFormatException nfe) {
            // Handle the condition when str is not a number.
        }



        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                db.collection(getString(R.string.COLLECTION_INTERNET_SPEED_DATA))
                        .add(dataModel)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                Toasty.success(getActivity(),
                                        "Saved successfully", Toast.LENGTH_LONG)
                                        .show();

                                startActivity(new Intent(getActivity(), HomeActivity.class));
                                dismiss();
                            }
                        });


            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });





        return builder.create();

    }

    public ViewSaveData( InternetDataModel dataModel ) {
        this.dataModel = dataModel;
    }
}
