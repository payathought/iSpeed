package com.example.ispeed.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
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

import com.example.ispeed.FunctionMethod.FunctionMethod;
import com.example.ispeed.GetSpeedTestHostsHandler;
import com.example.ispeed.MainActivity;
import com.example.ispeed.Model.InternetDataModel;
import com.example.ispeed.R;
import com.example.ispeed.View.Dialog.ViewSaveData;
import com.example.ispeed.test.HttpDownloadTest;
import com.example.ispeed.test.HttpUploadTest;
import com.example.ispeed.test.PingTest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class GeoMapActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private Location currentLocation;

    String currentLocForSavingData;
    private static final String TAG = "GeoMapActivityGeoMapActivity";
    private CameraPosition initialPosition;
    FloatingActionButton myLocationButton;
    LocationComponent locationComponent;
    GetSpeedTestHostsHandler getSpeedTestHostsHandler = null;
    Button btn_measureNow;
    static int position = 0;
    static int lastPosition = 0;
    HashSet<String> tempBlackList;

    InternetDataModel dataModel;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser firebaseUser;

    TextView tv_userInternet, tv_isp, tv_download, tv_upload, tv_ping, tv_stablitiy, tv_timeRecorded;

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    FunctionMethod method;
    private static final int REQUEST_LOCATION = 199;
    GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = getSharedPreferences(getString(R.string.USERPREF), Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        tempBlackList = new HashSet<>();
        mFirebaseAuth = FirebaseAuth.getInstance();
        dataModel = new InternetDataModel();

        Mapbox.getInstance(this, getString(R.string.mapbox_maps_api_key));

        setContentView(R.layout.activity_geo_map);
        final DecimalFormat dec = new DecimalFormat("#.##");
        mapView = findViewById(R.id.mb_user);
        myLocationButton = findViewById(R.id.myLocationButton);
        tv_userInternet = findViewById(R.id.tv_userInternet);
        tv_isp = findViewById(R.id.tv_isp);
        tv_download = findViewById(R.id.tv_download);
        tv_upload = findViewById(R.id.tv_upload);
        tv_ping = findViewById(R.id.tv_ping);
        tv_stablitiy = findViewById(R.id.tv_stablitiy);
        tv_timeRecorded = findViewById(R.id.tv_timeRecorded);
        btn_measureNow = findViewById(R.id.btn_measureNow);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        method = new FunctionMethod();
        if (googleApiClient == null) {
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d(TAG, "onSuccess: " + location.getLatitude());
                            try {
                                if (ActivityCompat.checkSelfPermission(
                                        GeoMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                        GeoMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(GeoMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                                } else {
//                                        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                    double lat = location.getLatitude();
                                    double longi = location.getLongitude();

                                    Geocoder geo = new Geocoder(GeoMapActivity.this, Locale.getDefault());
                                    List<Address> addresses = geo.getFromLocation(lat, longi, 1);

                                    currentLocForSavingData = addresses.get(0).getLocality();

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });


        if (sharedpreferences.getAll().isEmpty())
        {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }else{
            tv_userInternet.setText("User: " + sharedpreferences.getAll().get(getString(R.string.FIRSTNAME)).toString() + " " + sharedpreferences.getAll().get(getString(R.string.LASTNAME)).toString());
        }

        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mapboxMap.getLocationComponent() != null) { // Check to ensure coordinates aren't null, probably a better way of doing this...
                    enableLocationComponent(mapboxMap.getStyle());
                    locationComponent.zoomWhileTracking(14);

                }
            }
        });


        btn_measureNow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btn_measureNow.setEnabled(false);

                //Restart test icin eger baglanti koparsa
                if (getSpeedTestHostsHandler == null) {
                    getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
                    getSpeedTestHostsHandler.start();

                }





                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                startButton.setText("Selecting best server based on ping...");
                            }
                        });

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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "No Connection...", Toast.LENGTH_LONG).show();
                                        btn_measureNow.setEnabled(true);
                                        btn_measureNow.setTextSize(16);
                                    }
                                });
                                getSpeedTestHostsHandler = null;
                                return;
                            }
                        }

                        //Find closest server
                        HashMap<Integer, String> mapKey = getSpeedTestHostsHandler.getMapKey();
                        HashMap<Integer, List<String>> mapValue = getSpeedTestHostsHandler.getMapValue();
                        double selfLat = getSpeedTestHostsHandler.getSelfLat();
                        double selfLon = getSpeedTestHostsHandler.getSelfLon();
                        // Get System TELEPHONY service reference
                        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                        Log.d(TAG, "mapValue: " + getSpeedTestHostsHandler.getIspName());
                        tv_isp.setText("ISP: " + getSpeedTestHostsHandler.getIspName());

//                        Log.d(TAG, "run: " + getSpeedTestHostsHandler.getHostName());
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

                        Log.d(TAG, "mapVal: " + testAddr);
                        final double distance = dist;

                        if (info == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn_measureNow.setTextSize(12);
                                    btn_measureNow.setText("There was a problem in getting Host Location. Try again later.");
                                }
                            });
                            return;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                startButton.setTextSize(13);
//                                startButton.setText(String.format("Host Location: %s [Distance: %s km]", info.get(2), new DecimalFormat("#.##").format(distance / 1000)));
                            }
                        });

                        //Init Ping graphic
                        final LinearLayout chartPing = (LinearLayout) findViewById(R.id.chartPing);
                        XYSeriesRenderer pingRenderer = new XYSeriesRenderer();
                        XYSeriesRenderer.FillOutsideLine pingFill = new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ALL);
                        pingFill.setColor(Color.parseColor("#4d5a6a"));
                        pingRenderer.addFillOutsideLine(pingFill);
                        pingRenderer.setDisplayChartValues(false);
                        pingRenderer.setShowLegendItem(false);
                        pingRenderer.setColor(Color.parseColor("#4d5a6a"));
                        pingRenderer.setLineWidth(5);
                        final XYMultipleSeriesRenderer multiPingRenderer = new XYMultipleSeriesRenderer();
                        multiPingRenderer.setXLabels(0);
                        multiPingRenderer.setYLabels(0);
                        multiPingRenderer.setZoomEnabled(false);
                        multiPingRenderer.setXAxisColor(Color.parseColor("#647488"));
                        multiPingRenderer.setYAxisColor(Color.parseColor("#2F3C4C"));
                        multiPingRenderer.setPanEnabled(true, true);
                        multiPingRenderer.setZoomButtonsVisible(false);
                        multiPingRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
                        multiPingRenderer.addSeriesRenderer(pingRenderer);

                        //Init Download graphic
                        final LinearLayout chartDownload = (LinearLayout) findViewById(R.id.chartDownload);
                        XYSeriesRenderer downloadRenderer = new XYSeriesRenderer();
                        XYSeriesRenderer.FillOutsideLine downloadFill = new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ALL);
                        downloadFill.setColor(Color.parseColor("#4d5a6a"));
                        downloadRenderer.addFillOutsideLine(downloadFill);
                        downloadRenderer.setDisplayChartValues(false);
                        downloadRenderer.setColor(Color.parseColor("#4d5a6a"));
                        downloadRenderer.setShowLegendItem(false);
                        downloadRenderer.setLineWidth(5);
                        final XYMultipleSeriesRenderer multiDownloadRenderer = new XYMultipleSeriesRenderer();
                        multiDownloadRenderer.setXLabels(0);
                        multiDownloadRenderer.setYLabels(0);
                        multiDownloadRenderer.setZoomEnabled(false);
                        multiDownloadRenderer.setXAxisColor(Color.parseColor("#647488"));
                        multiDownloadRenderer.setYAxisColor(Color.parseColor("#2F3C4C"));
                        multiDownloadRenderer.setPanEnabled(false, false);
                        multiDownloadRenderer.setZoomButtonsVisible(false);
                        multiDownloadRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
                        multiDownloadRenderer.addSeriesRenderer(downloadRenderer);

                        //Init Upload graphic
                        final LinearLayout chartUpload = (LinearLayout) findViewById(R.id.chartUpload);
                        XYSeriesRenderer uploadRenderer = new XYSeriesRenderer();
                        XYSeriesRenderer.FillOutsideLine uploadFill = new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ALL);
                        uploadFill.setColor(Color.parseColor("#4d5a6a"));
                        uploadRenderer.addFillOutsideLine(uploadFill);
                        uploadRenderer.setDisplayChartValues(false);
                        uploadRenderer.setColor(Color.parseColor("#4d5a6a"));
                        uploadRenderer.setShowLegendItem(false);
                        uploadRenderer.setLineWidth(5);
                        final XYMultipleSeriesRenderer multiUploadRenderer = new XYMultipleSeriesRenderer();
                        multiUploadRenderer.setXLabels(0);
                        multiUploadRenderer.setYLabels(0);
                        multiUploadRenderer.setZoomEnabled(false);
                        multiUploadRenderer.setXAxisColor(Color.parseColor("#647488"));
                        multiUploadRenderer.setYAxisColor(Color.parseColor("#2F3C4C"));
                        multiUploadRenderer.setPanEnabled(false, false);
                        multiUploadRenderer.setZoomButtonsVisible(false);
                        multiUploadRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
                        multiUploadRenderer.addSeriesRenderer(uploadRenderer);

                        //Reset value, graphics
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_ping.setText("0 ms");
                                tv_download.setText("0 Mbps");
                                tv_upload.setText("0 Mbps");
                                tv_stablitiy.setText("Internet Stability: ");
                                tv_timeRecorded.setText("Time Recorded: ");
                                tv_isp.setText("ISP: " + getSpeedTestHostsHandler.getIspName());

                            }
                        });
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
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tv_ping.setText("Ping: "+dec.format(pingTest.getAvgRtt()) + " ms");
                                            dataModel.setPing(dec.format(pingTest.getAvgRtt()));
                                            if(pingTest.getAvgRtt() >= 100){
                                                tv_stablitiy.setText("Stability: Unstable");
                                            }else{
                                                tv_stablitiy.setText("Stability: Stable");
                                            }
                                        }
                                    });
                                }
                            } else {
                                pingRateList.add(pingTest.getInstantRtt());

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_ping.setText("Ping: "+ dec.format(pingTest.getInstantRtt()) + " ms");
                                        dataModel.setPing(dec.format(pingTest.getAvgRtt()));
                                    }
                                });

                                //Update chart
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Creating an  XYSeries for Income
                                        XYSeries pingSeries = new XYSeries("");
                                        pingSeries.setTitle("");

                                        int count = 0;
                                        List<Double> tmpLs = new ArrayList<>(pingRateList);
                                        for (Double val : tmpLs) {
                                            pingSeries.add(count++, val);
                                        }

                                        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                        dataset.addSeries(pingSeries);



                                    }
                                });
                            }


                            //Download Test
                            if (pingTestFinished) {
                                if (downloadTestFinished) {
                                    //Failure
                                    if (downloadTest.getFinalDownloadRate() == 0) {
                                        System.out.println("Download error...");
                                    } else {
                                        //Success
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                tv_download.setText( "Download Speed: "+ dec.format(downloadTest.getFinalDownloadRate()) + " Mbps");
                                                dataModel.setDownLoadSpeed(dec.format(downloadTest.getFinalDownloadRate()));
                                            }
                                        });
                                    }
                                } else {
                                    //Calc position
                                    double downloadRate = downloadTest.getInstantDownloadRate();
                                    downloadRateList.add(downloadRate);
                                    position = getPositionByRate(downloadRate);

                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {

                                            tv_download.setText("Download Speed: "+dec.format(downloadTest.getInstantDownloadRate()) + " Mbps");
                                            dataModel.setDownLoadSpeed(dec.format(downloadTest.getFinalDownloadRate()));
                                        }

                                    });
                                    lastPosition = position;

                                    //Update chart
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Creating an  XYSeries for Income
                                            XYSeries downloadSeries = new XYSeries("");
                                            downloadSeries.setTitle("");

                                            List<Double> tmpLs = new ArrayList<>(downloadRateList);
                                            int count = 0;
                                            for (Double val : tmpLs) {
                                                downloadSeries.add(count++, val);
                                            }

                                            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                            dataset.addSeries(downloadSeries);

                                        }
                                    });

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
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Date date = new Date();
                                                SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aa");
                                                tv_upload.setText("Upload Speed: " + dec.format(uploadTest.getFinalUploadRate()) + " Mbps");
                                                tv_timeRecorded.setText( "Time Recorded: " +formatter.format(date));
                                                dataModel.setUploadSpeed(dec.format(uploadTest.getFinalUploadRate()));
                                            }
                                        });
                                    }
                                } else {
                                    //Calc position
                                    double uploadRate = uploadTest.getInstantUploadRate();
                                    uploadRateList.add(uploadRate);
                                    position = getPositionByRate(uploadRate);

                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            Date date = new Date();
                                            SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aa");

                                            tv_upload.setText( "Upload Speed: " +dec.format(uploadTest.getInstantUploadRate()) + " Mbps");
                                            tv_timeRecorded.setText("Time Recorded: " + formatter.format(date));
                                            dataModel.setUploadSpeed(dec.format(uploadTest.getFinalUploadRate()));
                                        }

                                    });
                                    lastPosition = position;

                                    //Update chart
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Creating an  XYSeries for Income
                                            XYSeries uploadSeries = new XYSeries("");
                                            uploadSeries.setTitle("");

                                            int count = 0;
                                            List<Double> tmpLs = new ArrayList<>(uploadRateList);
                                            for (Double val : tmpLs) {
                                                if (count == 0) {
                                                    val = 0.0;
                                                }
                                                uploadSeries.add(count++, val);
                                            }

                                            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                            dataset.addSeries(uploadSeries);

                                        }
                                    });

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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                firebaseUser = mFirebaseAuth.getCurrentUser();
                                dataModel.setIsp(getSpeedTestHostsHandler.getIspName());
                                dataModel.setLocation(currentLocForSavingData);
                                dataModel.setTime(null);
                                dataModel.setUser_id(firebaseUser.getUid());

                                ViewSaveData dialog = new ViewSaveData(dataModel);
                                dialog.show(getSupportFragmentManager(), "iSpeed");

                                btn_measureNow.setEnabled(true);
                                btn_measureNow.setTextSize(16);
                                btn_measureNow.setText("Measure Now");
                            }
                        });


                    }
                }).start();
            }
        });

    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        this.mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {

            @Override
            public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                    }
                });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component

            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            locationComponent.zoomWhileTracking(14);

            Location lastKnownLocation = locationComponent.getLastKnownLocation();
            if (lastKnownLocation != null)
            {
                currentLocation = lastKnownLocation;
                Log.d(TAG, "Current Location: " +currentLocation.getLatitude()+" "+ currentLocation.getLongitude());

                initialPosition = new CameraPosition.Builder()
                        .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                        .zoom(15)
                        .build();
            }
// Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
        getSpeedTestHostsHandler.start();

        method.gpsChecker(this, googleApiClient, REQUEST_LOCATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public int getPositionByRate(double rate) {
        if (rate <= 1) {
            return (int) (rate * 30);

        } else if (rate <= 10) {
            return (int) (rate * 6) + 30;

        } else if (rate <= 30) {
            return (int) ((rate - 10) * 3) + 90;

        } else if (rate <= 50) {
            return (int) ((rate - 30) * 1.5) + 150;

        } else if (rate <= 100) {
            return (int) ((rate - 50) * 1.2) + 180;
        }

        return 0;
    }
}