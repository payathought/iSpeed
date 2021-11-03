package com.example.ispeed;

import android.Manifest;
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
import com.example.ispeed.Model.InternetDataModel;
import com.example.ispeed.View.Dialog.ViewSaveData;
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


public class MainActivity extends AppCompatActivity {
    static int position = 0;
    static int lastPosition = 0;
    GetSpeedTestHostsHandler getSpeedTestHostsHandler = null;
    HashSet<String> tempBlackList;
    private static final String TAG = "MainActivity";
    LocationManager locationManager;
    String currentLocation;
    private static final int REQUEST_LOCATION = 1;

    InternetDataModel dataModel;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser firebaseUser;

    FunctionMethod functionMethod;
    private static final int REQUEST_LOCATION_V2 = 199 ;
    GoogleApiClient googleApiClient;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onResume() {
        super.onResume();

        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
        getSpeedTestHostsHandler.start();

        functionMethod.gpsChecker(this, googleApiClient, REQUEST_LOCATION_V2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();
        final Button startButton = (Button) findViewById(R.id.startButton);
        final DecimalFormat dec = new DecimalFormat("#.##");
        startButton.setText("Begin Test");

        tempBlackList = new HashSet<>();
        dataModel = new InternetDataModel();
        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
        getSpeedTestHostsHandler.start();

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
                                            MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                            MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                                    } else {
//                                        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                        double lat = location.getLatitude();
                                        double longi = location.getLongitude();

                                        Geocoder geo = new Geocoder(MainActivity.this, Locale.getDefault());
                                        List<Address> addresses = geo.getFromLocation(lat, longi, 1);

                                        currentLocation = addresses.get(0).getLocality();

                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                        }
                    }
                });

//        try {
//            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                OnGPS();
//            } else {
//                if (ActivityCompat.checkSelfPermission(
//                        MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                        MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
//                } else {
//                    Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    if (locationGPS != null) {
//                        double lat = locationGPS.getLatitude();
//                        double longi = locationGPS.getLongitude();
//
//                        Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
//                        List<Address> addresses = geo.getFromLocation(lat, longi, 1);
//
//                        Log.d(TAG, "onCreate: "  + addresses);
//
//                    } else {
//                        Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startButton.setEnabled(false);

                //Restart test icin eger baglanti koparsa
                if (getSpeedTestHostsHandler == null) {
                    getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
                    getSpeedTestHostsHandler.start();
                }

                new Thread(new Runnable() {
                    RotateAnimation rotate;
                    ImageView barImageView = (ImageView) findViewById(R.id.barImageView);
                    TextView pingTextView = (TextView) findViewById(R.id.pingTextView);
                    TextView downloadTextView = (TextView) findViewById(R.id.downloadTextView);
                    TextView uploadTextView = (TextView) findViewById(R.id.uploadTextView);

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
                                        startButton.setEnabled(true);
                                        startButton.setTextSize(16);
                                        startButton.setText("Restart Test");
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startButton.setTextSize(12);
                                    startButton.setText("There was a problem in getting Host Location. Try again later.");
                                }
                            });
                            return;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startButton.setTextSize(13);
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
                                pingTextView.setText("0 ms");
                                chartPing.removeAllViews();
                                downloadTextView.setText("0 Mbps");
                                chartDownload.removeAllViews();
                                uploadTextView.setText("0 Mbps");
                                chartUpload.removeAllViews();
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
                                            pingTextView.setText(dec.format(pingTest.getAvgRtt()) + " ms");
                                            dataModel.setPing(dec.format(pingTest.getAvgRtt()));
                                        }
                                    });
                                }
                            } else {
                                pingRateList.add(pingTest.getInstantRtt());

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pingTextView.setText(dec.format(pingTest.getInstantRtt()) + " ms");
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

                                        GraphicalView chartView = ChartFactory.getLineChartView(getBaseContext(), dataset, multiPingRenderer);
                                        chartPing.addView(chartView, 0);

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
                                                downloadTextView.setText(dec.format(downloadTest.getFinalDownloadRate()) + " Mbps");
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
                                            rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                            rotate.setInterpolator(new LinearInterpolator());
                                            rotate.setDuration(100);
                                            barImageView.startAnimation(rotate);
                                            downloadTextView.setText(dec.format(downloadTest.getInstantDownloadRate()) + " Mbps");

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

                                            GraphicalView chartView = ChartFactory.getLineChartView(getBaseContext(), dataset, multiDownloadRenderer);
                                            chartDownload.addView(chartView, 0);
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
                                                uploadTextView.setText(dec.format(uploadTest.getFinalUploadRate()) + " Mbps");
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
                                            rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                            rotate.setInterpolator(new LinearInterpolator());
                                            rotate.setDuration(100);
                                            barImageView.startAnimation(rotate);
                                            uploadTextView.setText(dec.format(uploadTest.getInstantUploadRate()) + " Mbps");
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

                                            GraphicalView chartView = ChartFactory.getLineChartView(getBaseContext(), dataset, multiUploadRenderer);
                                            chartUpload.addView(chartView, 0);
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
                                dataModel.setLocation(currentLocation);
                                dataModel.setTime(null);
                                dataModel.setUser_id(firebaseUser.getUid());

                                ViewSaveData dialog = new ViewSaveData(dataModel);
                                dialog.show(getSupportFragmentManager(), "iSpeed");
                                startButton.setEnabled(true);
                                startButton.setTextSize(16);
                                startButton.setText("Restart Test");
                            }
                        });


                    }
                }).start();
            }
        });
    }


    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

