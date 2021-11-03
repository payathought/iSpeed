package com.example.ispeed.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ispeed.BuildConfig;
import com.example.ispeed.FunctionMethod.FunctionMethod;
import com.example.ispeed.Model.InternetDataModel;
import com.example.ispeed.Model.SignUpModel;
import com.example.ispeed.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class ExportActivity extends AppCompatActivity {
    BarChart bc_downloadSpeed,bc_ping,bc_uploadSpeed;
    ArrayList<InternetDataModel> mDataModel;
    FirebaseFirestore db;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser firebaseUser;
    TextView tv_ds_label,tv_us_label,tv_ping_label,tv_export_name,tv_export_email,tv_export_date;
    CardView cv_uploadSpeed,cv_downloadSpeed,cv_ping;
    private static final String TAG = "DashboardMainActivity";
    Button btn_settings,btn_export;
    String filterDay = "";

    String filterStartingHour = "";
    String filterEndingHour = "";

    FunctionMethod method;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        bc_ping = findViewById(R.id.bc_ping);
        bc_uploadSpeed = findViewById(R.id.bc_uploadSpeed);
        bc_downloadSpeed = findViewById(R.id.bc_downloadSpeed);
        tv_ds_label = findViewById(R.id.tv_ds_label);
        tv_ping_label = findViewById(R.id.tv_ping_label);
        tv_us_label = findViewById(R.id.tv_us_label);
        tv_export_name = findViewById(R.id.tv_export_name);
        tv_export_email = findViewById(R.id.tv_export_email);
        tv_export_date = findViewById(R.id.tv_export_date);


        btn_settings = findViewById(R.id.btn_settings);
        btn_export = findViewById(R.id.btn_export);

        cv_downloadSpeed = findViewById(R.id.cv_downloadSpeed);
        cv_ping = findViewById(R.id.cv_ping);
        cv_uploadSpeed = findViewById(R.id.cv_uploadSpeed);
        method = new FunctionMethod();
        method.takeScreenShot(this);
        verifyStoragePermission(this);

        getUserInfo();

        if (!getIntent().getStringExtra("date").equals("") &&
                !getIntent().getStringExtra("start") .equals("") &&
                !getIntent().getStringExtra("end") .equals("")){
            filterDay = getIntent().getStringExtra("date");
            filterStartingHour = getIntent().getStringExtra("start");
            filterEndingHour = getIntent().getStringExtra("end");
            getDataWithFilters();

        }else{
            getData();
        }

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
               captureScreen(getWindow().getDecorView().getRootView());
            }
        }, 3000);
    }

    public void getData(){

        db.collection(getString(R.string.COLLECTION_INTERNET_SPEED_DATA))
                .whereEqualTo("user_id",firebaseUser.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, FirebaseFirestoreException error) {
                        mDataModel = new ArrayList<>();
                        for (DocumentSnapshot document : value.getDocuments())
                        {
                            InternetDataModel dataModel = document.toObject(InternetDataModel.class);
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                            if (filterDay.equals("")){
                                filterDay =  formatter.format(new Date());

                            }

                            if (filterDay.equals(formatter.format(dataModel.getTime()))){
                                mDataModel.add(dataModel);
                            }


                        }

                        ArrayList<BarEntry> barEntries = new ArrayList<>();
                        ArrayList<BarEntry> barEntriesUS = new ArrayList<>();
                        ArrayList<BarEntry> barEntriesPing = new ArrayList<>();

                        ArrayList<String> theTimes = new ArrayList<>();
                        barEntriesUS.add(new BarEntry(0,0));
                        barEntries.add(new BarEntry(0,0));
                        barEntriesPing.add(new BarEntry(0,0));
                        theTimes.add("");

                        for (int i = 0; i < mDataModel.size(); i++){
                            Float val = Float.valueOf(mDataModel.get(i).getDownLoadSpeed());
                            Float valUS = Float.valueOf(mDataModel.get(i).getUploadSpeed());
                            Float valPing = Float.valueOf(mDataModel.get(i).getPing());

                            barEntries.add(new BarEntry(val,1+i));
                            barEntriesUS.add(new BarEntry(valUS,1+i));
                            barEntriesPing.add(new BarEntry(valPing,1+i));
                            Date date = mDataModel.get(i).getTime();
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm aa");

                            Log.d(TAG, "timeOfData: " + formatter.format(date));
                            theTimes.add(formatter.format(date));

                        }

                        Date date = new Date();
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                        tv_export_date.setText("Date: "+ simpleDateFormat.format(date));
                        tv_ds_label.setText("Download Speed (" + simpleDateFormat.format(date) + ")" );
                        tv_ping_label.setText("Ping (" + simpleDateFormat.format(date) + ")" );
                        tv_us_label.setText("Upload Speed (" + simpleDateFormat.format(date) + ")" );


                        BarDataSet dataSet = new BarDataSet(barEntries, "Mbps");
                        BarData data = new BarData(theTimes,dataSet );
                        bc_downloadSpeed.setData(data);
                        bc_downloadSpeed.setTouchEnabled(true);
                        bc_downloadSpeed.setDragEnabled(true);
                        bc_downloadSpeed.setScaleEnabled(true);
                        // set the data and list of labels into chart
                        bc_downloadSpeed.setDescription("");  // set the description
                        bc_downloadSpeed.animateY(2000);


                        BarDataSet  dataSetUS = new BarDataSet(barEntriesUS, "Mbps");
                        BarData dataUS = new BarData(theTimes,dataSetUS );
                        bc_uploadSpeed.setData(dataUS);
                        bc_uploadSpeed.setTouchEnabled(true);
                        bc_uploadSpeed.setDragEnabled(true);
                        bc_uploadSpeed.setScaleEnabled(true);
                        // set the data and list of labels into chart
                        bc_uploadSpeed.setDescription("");  // set the description
                        bc_uploadSpeed.animateY(2000);



                        BarDataSet  dataSetPing = new BarDataSet(barEntriesPing, "Ms");
                        BarData dataPing= new BarData(theTimes,dataSetPing );
                        bc_ping.setData(dataPing);
                        bc_ping.setTouchEnabled(true);
                        bc_ping.setDragEnabled(true);
                        bc_ping.setScaleEnabled(true);
                        // set the data and list of labels into chart
                        bc_ping.setDescription("");  // set the description
                        bc_ping.animateY(2000);



                    }
                });
    }

    public void getDataWithFilters(){
        Timestamp timestamp;
        Date dateStart, dateEnd;
        String  parsers = filterDay + " " + filterStartingHour + ":00";
        String  parserEnd = filterDay + " " + filterEndingHour + ":00";
        try {
            dateStart = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(parsers);
            dateEnd = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(parserEnd);
            db.collection(getString(R.string.COLLECTION_INTERNET_SPEED_DATA))
                    .whereEqualTo("user_id",firebaseUser.getUid())
                    .whereGreaterThanOrEqualTo("time",dateStart)
                    .whereLessThanOrEqualTo("time",dateEnd)
                    .orderBy("time", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable  QuerySnapshot value, FirebaseFirestoreException error) {
                            mDataModel = new ArrayList<>();

                            if (error != null){
                                Log.d(TAG, "onEvent: " + error.getMessage());
                            }else{
                                for (DocumentSnapshot document : value.getDocuments())
                                {
                                    InternetDataModel dataModel = document.toObject(InternetDataModel.class);
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                                    if (filterDay.equals("")){
                                        filterDay =  formatter.format(new Date());

                                    }


                                    Log.d(TAG, "onEvent: " + filterDay );
                                    Log.d(TAG, "onEvent: " + formatter.format(dataModel.getTime()));
                                    mDataModel.add(dataModel);

                                }

                                ArrayList<BarEntry> barEntries = new ArrayList<>();
                                ArrayList<BarEntry> barEntriesUS = new ArrayList<>();
                                ArrayList<BarEntry> barEntriesPing = new ArrayList<>();

                                ArrayList<String> theTimes = new ArrayList<>();
                                barEntriesUS.add(new BarEntry(0,0));
                                barEntries.add(new BarEntry(0,0));
                                barEntriesPing.add(new BarEntry(0,0));
                                theTimes.add("");

                                for (int i = 0; i < mDataModel.size(); i++){
                                    Float val = Float.valueOf(mDataModel.get(i).getDownLoadSpeed());
                                    Float valUS = Float.valueOf(mDataModel.get(i).getUploadSpeed());
                                    Float valPing = Float.valueOf(mDataModel.get(i).getPing());

                                    barEntries.add(new BarEntry(val,1+i));
                                    barEntriesUS.add(new BarEntry(valUS,1+i));
                                    barEntriesPing.add(new BarEntry(valPing,1+i));
                                    Date date = mDataModel.get(i).getTime();
                                    @SuppressLint("SimpleDateFormat")
                                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm aa");

                                    theTimes.add(formatter.format(date));

                                }

                                Date date = mDataModel.get(0).getTime();
                                @SuppressLint("SimpleDateFormat")
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");

                                tv_export_date.setText("Date: " + simpleDateFormat.format(date));
                                tv_ds_label.setText("Download Speed (" + simpleDateFormat.format(date) + ")" );
                                tv_ping_label.setText("Ping (" + simpleDateFormat.format(date) + ")" );
                                tv_us_label.setText("Upload Speed (" + simpleDateFormat.format(date) + ")" );


                                BarDataSet  dataSet = new BarDataSet(barEntries, "Mbps");
                                BarData data = new BarData(theTimes,dataSet );
                                bc_downloadSpeed.setData(data);
                                bc_downloadSpeed.setTouchEnabled(true);
                                bc_downloadSpeed.setDragEnabled(true);
                                bc_downloadSpeed.setScaleEnabled(true);
                                // set the data and list of labels into chart
                                bc_downloadSpeed.setDescription("");  // set the description
                                bc_downloadSpeed.animateY(2000);


                                BarDataSet  dataSetUS = new BarDataSet(barEntriesUS, "Mbps");
                                BarData dataUS = new BarData(theTimes,dataSetUS );
                                bc_uploadSpeed.setData(dataUS);
                                bc_uploadSpeed.setTouchEnabled(true);
                                bc_uploadSpeed.setDragEnabled(true);
                                bc_uploadSpeed.setScaleEnabled(true);
                                // set the data and list of labels into chart
                                bc_uploadSpeed.setDescription("");  // set the description
                                bc_uploadSpeed.animateY(2000);



                                BarDataSet  dataSetPing = new BarDataSet(barEntriesPing, "Ms");
                                BarData dataPing= new BarData(theTimes,dataSetPing );
                                bc_ping.setData(dataPing);
                                bc_ping.setTouchEnabled(true);
                                bc_ping.setDragEnabled(true);
                                bc_ping.setScaleEnabled(true);
                                // set the data and list of labels into chart
                                bc_ping.setDescription("");  // set the description
                                bc_ping.animateY(2000);

                            }



                        }
                    });



        } catch (ParseException e) {
            e.printStackTrace();
        }


//        Date date = null;
//        try {
//            date = new Date(String.valueOf(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(parsers)));
//            Timestamp timestamp = new Timestamp(date);
//            Log.d(TAG, "getData: " + timestamp);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }


    }

    public void getUserInfo(){
        db.collection(getString(R.string.COLLECTION_USER_INFORMATION))
                .whereEqualTo("user_id", firebaseUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if (!querySnapshot.isEmpty()) {
                            SignUpModel signUpModel = querySnapshot.getDocuments().get(0).toObject(SignUpModel.class);

                            tv_export_name.setText("Name: " + signUpModel.getFirstname() + " " + signUpModel.getLastname());
                            tv_export_email.setText("Email: " +  firebaseUser.getEmail());




                        }
                    }
                });
    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = ExportActivity.this.getExternalFilesDir
                    (Environment.DIRECTORY_SCREENSHOTS) + "/" + "SCREEN"
                    + System.currentTimeMillis() + ".jpeg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);
            File imageFile = new File(mPath);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            Log.d(TAG, "takeScreenshot: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public File captureScreen(View view) {
        Date date = new Date();

        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");
        CharSequence sequence = formatter.format(date);
        try {

            String dirPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
            File fileDir = new File(dirPath);

            if (!fileDir.exists()) {
                boolean mkDir = fileDir.mkdir();
            }

            String path = dirPath + "/" + "SCREEN"
                    + System.currentTimeMillis() + ".png";


            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());

            view.setDrawingCacheEnabled(false);

            File imageFile = new File(path);

            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);

            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

//            openScreenshot(imageFile);

            Toasty.info(this,
                    "Check Exported Image on your gallery.", Toast.LENGTH_LONG)
                    .show();
            Log.d(TAG, "captureScreen: " + imageFile.toString());
            return imageFile;


        } catch (FileNotFoundException e) {
            e.printStackTrace();

            Log.d(TAG, "captureScreen: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "captureScreen: " + e.getMessage());
        }

        return  null;

    }
    private void openScreenshot(File imageFile) {

                Uri uri=
                FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                        BuildConfig.APPLICATION_ID + ".provider", imageFile);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_VIEW);
//
//        Uri uri=
//                FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
//                        BuildConfig.APPLICATION_ID + ".provider", imageFile);
////        Uri uri = Uri.fromFile(imageFile);
//        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }


//    // verifying if storage permission is given or not
//    public static void verifystoragepermissions(Activity activity) {
//
//        int permissions = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//        // If storage permission is not given then request for External Storage Permission
//        if (permissions != PackageManager.PERMISSION_GRANTED) {
//
//        }
//    }


    private static final int  REQUEST_EXTERNAL_STORAGE = 1;

    private String[] PERMISION_STORAGE = {

        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,

    };

    public void verifyStoragePermission(Activity activity){

        int permission = ActivityCompat.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,
                    PERMISION_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
                    );
        }

    }

}