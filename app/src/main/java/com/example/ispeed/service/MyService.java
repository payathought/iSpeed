package com.example.ispeed.service;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.ispeed.FunctionMethod.FunctionMethod;
import com.example.ispeed.MainActivity;
import com.example.ispeed.Model.InternetDataModel;
import com.example.ispeed.View.Dialog.ViewSaveData;
import com.example.ispeed.View.HomeActivity;
import com.example.ispeed.View.LoginActivity;
import com.example.ispeed.test.HttpDownloadTest;
import com.example.ispeed.test.HttpUploadTest;
import com.example.ispeed.test.PingTest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.LogDescriptor;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ispeed.GetSpeedTestHostsHandler;
import com.example.ispeed.R;
import com.example.ispeed.View.Dialog.ViewSaveData;
import com.example.ispeed.test.HttpDownloadTest;
import com.example.ispeed.test.HttpUploadTest;
import com.example.ispeed.test.PingTest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import es.dmoral.toasty.Toasty;

public class MyService extends Service {
    private static final String TAG = "MyService";
    GetSpeedTestHostsHandler getSpeedTestHostsHandler = null;
    HashSet<String> tempBlackList;
    InternetDataModel dataModel;
    final DecimalFormat dec = new DecimalFormat("#.##");
    String currentLocation;
    private static final int REQUEST_LOCATION = 1;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser firebaseUser;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    FunctionMethod functionMethod = new FunctionMethod();
    private static final int REQUEST_LOCATION_V2 = 199 ;
    GoogleApiClient googleApiClient;
    FirebaseFirestore db;
    int delay = 10000;
    int period = 180000;
    Timer timer = new Timer();
    int x = 0;

    private FusedLocationProviderClient fusedLocationClient;
    public MyService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ispeedlogo)
                .setPriority(1)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle("Tracking Internet")
                .build();

        startForeground(1, notification);
        sharedpreferences = getSharedPreferences(getString(R.string.USERPREF), Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        Intent intent = new Intent();
        Bundle extras = intent.getExtras();
        if(extras != null){
            currentLocation = String.valueOf(extras.getString("loc"));
        }
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setSslEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toasty.info(getApplicationContext(),
                "Tracking on background...", Toast.LENGTH_LONG)
                .show();


        try {
            timer.schedule(new TimerTask()
            {
                public void run()
                {
                    Log.d(TAG, "data");


                    if (functionMethod.haveNetworkConnected(getApplication()))
                    {
                        trackInternet();
                    }else{
                        Toasty.error(getApplicationContext(),
                                "Tracking Stops. Check internet Connection", Toast.LENGTH_LONG)
                                .show();
                        onDestroy();
                    }
                }
            }, delay, period);
        }catch (Exception e){
            Log.d(TAG, "Exception: " + e.getMessage());
            onDestroy();
        }


        return START_REDELIVER_INTENT;





    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        timer.cancel();
        Toasty.error(getApplicationContext(),
                "Tracking Stops", Toast.LENGTH_LONG)
                .show();

        editor.putBoolean("isStart", false);
    }


    public void trackInternet(){




        tempBlackList = new HashSet<>();
        dataModel = new InternetDataModel();
        mFirebaseAuth = FirebaseAuth.getInstance();
        //Restart test icin eger baglanti koparsa

        //Restart test icin eger baglanti koparsa
        if (getSpeedTestHostsHandler == null) {
            getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
            getSpeedTestHostsHandler.start();
        }

        //Get egcodes.speedtest hosts
        int timeCount = 600; //1min
        while (!getSpeedTestHostsHandler.isFinished()) {
            timeCount--;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.d(TAG, "run: " + e.getMessage());
            }
            if (timeCount <= 0) {

                getSpeedTestHostsHandler = null;
                return;
            }
        }

        //Find closest server
        HashMap<Integer, String> mapKey = getSpeedTestHostsHandler.getMapKey();
        HashMap<Integer, List<String>> mapValue = getSpeedTestHostsHandler.getMapValue();
        double selfLat = getSpeedTestHostsHandler.getSelfLat();
        double selfLon = getSpeedTestHostsHandler.getSelfLon();
        double tmp = 19349458;
        double dist = 0.0;
        int findServerIndex = 0;
        for (int index : mapKey.keySet()) {
            if (tempBlackList.contains(mapValue.get(index).get(5))) {
                continue;
            }

            Location source = new Location("Source");
            source.setLatitude(selfLat);
            source.setLongitude(selfLon);

            List<String> ls = mapValue.get(index);
            Location dest = new Location("Dest");
            dest.setLatitude(Double.parseDouble(ls.get(0)));
            dest.setLongitude(Double.parseDouble(ls.get(1)));

            double distance = source.distanceTo(dest);
            if (tmp > distance) {
                tmp = distance;
                dist = distance;
                findServerIndex = index;
            }
        }
        String testAddr = mapKey.get(findServerIndex).replace("http://", "https://");
        final List<String> info = mapValue.get(findServerIndex);
        final double distance = dist;

        if (info == null) {

            return;
        }


        //Reset value, graphics
        final List<Double> pingRateList = new ArrayList<>();
        final List<Double> downloadRateList = new ArrayList<>();
        final List<Double> uploadRateList = new ArrayList<>();
        Boolean pingTestStarted = false;
        Boolean pingTestFinished = false;
        Boolean downloadTestStarted = false;
        Boolean downloadTestFinished = false;
        Boolean uploadTestStarted = false;
        Boolean uploadTestFinished = false;

        //Init Test
        final PingTest pingTest = new PingTest(info.get(6).replace(":8080", ""), 3);
        final HttpDownloadTest downloadTest = new HttpDownloadTest(testAddr.replace(testAddr.split("/")[testAddr.split("/").length - 1], ""));
        final HttpUploadTest uploadTest = new HttpUploadTest(testAddr);


        //Tests
        while (true) {
            if (!pingTestStarted) {
                pingTest.start();
                pingTestStarted = true;
            }
            if (pingTestFinished && !downloadTestStarted) {
                downloadTest.start();
                downloadTestStarted = true;
            }
            if (downloadTestFinished && !uploadTestStarted) {
                uploadTest.start();
                uploadTestStarted = true;
            }


            //Ping Test
            if (pingTestFinished) {
                //Failure
                if (pingTest.getAvgRtt() == 0) {
                    System.out.println("Ping error...");
                } else {
                    //Success
                    dataModel.setPing(dec.format(pingTest.getAvgRtt()));

                }
            } else {
                pingRateList.add(pingTest.getInstantRtt());
                dataModel.setPing(dec.format(pingTest.getAvgRtt()));


                //Update chart

            }


            //Download Test
            if (pingTestFinished) {
                if (downloadTestFinished) {
                    //Failure
                    if (downloadTest.getFinalDownloadRate() == 0) {
                        System.out.println("Download error...");
                    } else {
                        //Success
                        dataModel.setDownLoadSpeed(dec.format(downloadTest.getFinalDownloadRate()));

                    }
                } else {
                    //Calc position
                    double downloadRate = downloadTest.getInstantDownloadRate();
                    downloadRateList.add(downloadRate);

                }
            }


            //Upload Test
            if (downloadTestFinished) {
                if (uploadTestFinished) {
                    //Failure
                    if (uploadTest.getFinalUploadRate() == 0) {
                        System.out.println("Upload error...");
                    } else {
                        //Success
                        dataModel.setUploadSpeed(dec.format(uploadTest.getFinalUploadRate()));
                    }
                } else {
                    //Calc position
                    double uploadRate = uploadTest.getInstantUploadRate();
                    uploadRateList.add(uploadRate);

                    dataModel.setUploadSpeed(dec.format(uploadTest.getFinalUploadRate()));


                    //Update chart

                }
            }

            //Test bitti
            if (pingTestFinished && downloadTestFinished && uploadTest.isFinished()) {
                break;
            }

            if (pingTest.isFinished()) {
                pingTestFinished = true;
            }
            if (downloadTest.isFinished()) {
                downloadTestFinished = true;
            }
            if (uploadTest.isFinished()) {
                uploadTestFinished = true;
            }

            if (pingTestStarted && !pingTestFinished) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Log.d(TAG, "run: " + e.getMessage());
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.d(TAG, "run: " + e.getMessage());
                }
            }
        }

        //Thread bitiminde button yeniden aktif ediliyor

        firebaseUser = mFirebaseAuth.getCurrentUser();
        dataModel.setIsp(getSpeedTestHostsHandler.getIspName());
        dataModel.setLocation(currentLocation);
        dataModel.setTime(null);
        dataModel.setUser_id(firebaseUser.getUid());

        db.collection("Internet Speed Info")
                .add(dataModel)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        Toasty.success(getApplicationContext(),
                                "Saved successfully", Toast.LENGTH_LONG)
                                .show();
                        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
                    }
                });




}
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "my_service_channelid";
        String channelName = "My Foreground Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }
}