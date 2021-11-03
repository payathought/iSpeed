package com.example.ispeed.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ispeed.FunctionMethod.FunctionMethod;
import com.example.ispeed.MainActivity;
import com.example.ispeed.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class HomeActivity extends AppCompatActivity {

    CardView cv_speedTest,cv_geomap,cv_stab;
    FunctionMethod method;
    private static final int REQUEST_LOCATION = 199 ;
    GoogleApiClient googleApiClient;
    Toolbar toolbar;
    TextView txtUserToolbar;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        cv_speedTest = findViewById(R.id.cv_speedTest);
        cv_geomap = findViewById(R.id.cv_geomap);
        cv_stab = findViewById(R.id.cv_stab);

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
            editor = sharedpreferences.edit();
            editor.clear();
            editor.apply();
            Log.d("Logout", "logout: " + editor);
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
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