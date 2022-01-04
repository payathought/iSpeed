package com.example.ispeed.service;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ispeed.FunctionMethod.FunctionMethod;
import com.example.ispeed.MainActivity;
import com.example.ispeed.Model.InternetDataModel;
import com.example.ispeed.View.Dialog.ViewSaveData;
import com.example.ispeed.View.HomeActivity;
import com.example.ispeed.test.HttpDownloadTest;
import com.example.ispeed.test.HttpUploadTest;
import com.example.ispeed.test.PingTest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.LogDescriptor;
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

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import es.dmoral.toasty.Toasty;

public class AutoMaticTrackingActivity extends AppCompatActivity {
    final DecimalFormat dec = new DecimalFormat("#.##");
    String currentLocation;
    private static final int REQUEST_LOCATION = 1;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser firebaseUser;

    FunctionMethod functionMethod;
    private static final int REQUEST_LOCATION_V2 = 199 ;
    GoogleApiClient googleApiClient;
    FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    Button btn_startTracking,btn_stopTracking;
    private static final String TAG = "AutoMaticTrackingActivi";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_matic_tracking);

        btn_stopTracking = findViewById(R.id.btn_stopTracking);
        btn_startTracking = findViewById(R.id.btn_startTracking);
        functionMethod = new FunctionMethod();
        if(googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        }


                    }).build();
            googleApiClient.connect();
        }
        db = FirebaseFirestore.getInstance();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d(TAG, "onSuccess: " + location.getLatitude());
                            try {
                                if (ActivityCompat.checkSelfPermission(
                                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                        getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions( (Activity) getApplicationContext() , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                                } else {
//                                        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                    double lat = location.getLatitude();
                                    double longi = location.getLongitude();

                                    Geocoder geo = new Geocoder(getBaseContext(), Locale.getDefault());
                                    List<Address> addresses = geo.getFromLocation(lat, longi, 1);

                                    currentLocation = addresses.get(0).getLocality();

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });

        btn_startTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent startServiceIntent = new Intent(AutoMaticTrackingActivity.this, MyService.class);
//                startServiceIntent.putExtra("loc",currentLocation);
//                startService(startServiceIntent);


                Intent startServiceIntent = new Intent(AutoMaticTrackingActivity.this, MyService.class);
                startServiceIntent.putExtra("loc",currentLocation);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(startServiceIntent);
                }else {
                    startService(startServiceIntent);
                }
            }
        });
        btn_stopTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent stopServiceIntent = new Intent(AutoMaticTrackingActivity.this, MyService.class);
                stopServiceIntent.putExtra("loc",currentLocation);
                stopService(stopServiceIntent);
            }
        });
    }
}