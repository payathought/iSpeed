package com.example.ispeed.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ispeed.FunctionMethod.FunctionMethod;
import com.example.ispeed.MainActivity;
import com.example.ispeed.R;
import com.example.ispeed.service.AutoMaticTrackingActivity;
import com.example.ispeed.service.MyService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class HomeActivity extends AppCompatActivity {

    CardView cv_speedTest,cv_geomap,cv_stab,cv_tracking;
    FunctionMethod method;
    private static final int REQUEST_LOCATION = 199 ;
    GoogleApiClient googleApiClient;
    Toolbar toolbar;
    TextView txtUserToolbar;
    SharedPreferences sharedpreferences;

    SharedPreferences.Editor editor;
//    Button bnt_startTracking,btn_stopTracking;
    private FusedLocationProviderClient fusedLocationClient;

    String currentLocation;
    private static final String TAG = "HomeActivity";
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        cv_speedTest = findViewById(R.id.cv_speedTest);
        cv_geomap = findViewById(R.id.cv_geomap);
        cv_stab = findViewById(R.id.cv_stab);
        cv_tracking = findViewById(R.id.cv_tracking);
//        bnt_startTracking = findViewById(R.id.bnt_startTracking);
//        btn_stopTracking = findViewById(R.id.btn_stopTracking);

        method = new FunctionMethod();
        toolbar  = findViewById(R.id.toolBar);
        txtUserToolbar  = findViewById(R.id.txtUserToolbar);


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        sharedpreferences = getSharedPreferences(getString(R.string.USERPREF), Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        if (sharedpreferences.getAll().isEmpty())
        {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }else{
            txtUserToolbar.setText(sharedpreferences.getAll().get(getString(R.string.FIRSTNAME)).toString() + " " + sharedpreferences.getAll().get(getString(R.string.LASTNAME)).toString());
        }

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
                        public void onConnectionFailed(@NonNull  ConnectionResult connectionResult) {

                        }


                    }).build();
            googleApiClient.connect();
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



        cv_speedTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
            }
        });
        cv_geomap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, GeoMapActivity.class));
            }
        });

        cv_stab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, DashboardMainActivity.class));
            }
        });
        cv_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, AutoMaticTrackingActivity.class));
            }
        });

//        bnt_startTracking.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent startServiceIntent = new Intent(HomeActivity.this, MyService.class);
//                startServiceIntent.putExtra("loc",currentLocation);
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    startForegroundService(startServiceIntent);
//                }else {
//                    startService(startServiceIntent);
//                }
//                editor.putBoolean("isStart", true);
//                btn_stopTracking.setVisibility(View.VISIBLE);
//                bnt_startTracking.setVisibility(View.GONE);
//            }
//        });
//        btn_stopTracking.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent stopServiceIntent = new Intent(HomeActivity.this, MyService.class);
//                stopServiceIntent.putExtra("loc",currentLocation);
//                stopService(stopServiceIntent);
//                editor.putBoolean("isStart", false);
//                btn_stopTracking.setVisibility(View.GONE);
//                bnt_startTracking.setVisibility(View.VISIBLE);
//            }
//        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        method.gpsChecker(this, googleApiClient, REQUEST_LOCATION);
        if (sharedpreferences.getAll().isEmpty())
        {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }else{
            txtUserToolbar.setText(sharedpreferences.getAll().get(getString(R.string.FIRSTNAME)).toString() + " " + sharedpreferences.getAll().get(getString(R.string.LASTNAME)).toString());
            if(sharedpreferences.getAll().get("isStart") != null){
                boolean isTrue = Boolean.parseBoolean(sharedpreferences.getAll().get("isStart").toString());
                if (isTrue){
                    Log.d(TAG, "onResume: " + isTrue);
//                    btn_stopTracking.setVisibility(View.VISIBLE);
//                    bnt_startTracking.setVisibility(View.GONE);
                }else {
                    Log.d(TAG, "onResume: " + isTrue);
//                    btn_stopTracking.setVisibility(View.GONE);
//                    bnt_startTracking.setVisibility(View.VISIBLE);
                }
            }

        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.logout) {

            editor.clear();
            editor.apply();
            Log.d("Logout", "logout: " + editor);
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signOut();
            Toasty.info(getApplicationContext(), "Logging Out", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void onBackPressed() {
        super.onBackPressed();

        if (sharedpreferences.getAll().isEmpty())
        {
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }
        else if (!sharedpreferences.getAll().isEmpty())
        {
                startActivity(new Intent(getApplicationContext(),HomeActivity.class));

        }
        moveTaskToBack(true);

    }
}