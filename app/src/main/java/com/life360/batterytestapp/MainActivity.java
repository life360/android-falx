package com.life360.batterytestapp;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.life360.batterytestapp.google.GeocodeResponse;
import com.life360.batterytestapp.google.GooglePlatform;
import com.life360.falx.FalxApi;
import com.life360.falx.monitor.FalxConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.job.JobInfo.NETWORK_TYPE_ANY;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean logging;
    private FalxApi falxApi;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        falxApi = FalxApi.getInstance(MainActivity.this);
        falxApi.enableLogging(true);
        falxApi.addMonitors(FalxApi.MONITOR_APP_STATE | FalxApi.MONITOR_NETWORK);
        falxApi.addOnOffMonitor(FalxConstants.MONITOR_LABEL_GPS, FalxConstants.EVENT_GPS_ON);
        falxApi.addOnOffMonitor(FalxConstants.MONITOR_LABEL_ACTIVITY_DETECTION, FalxConstants.EVENT_ACTIVITY_DETECTION_ON);

        findViewById(R.id.trigger_stats).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("rk-dbg", "Test aggregate data query...");

                BatteryStatReporter.sendLogs(MainActivity.this);
            }
        });

        findViewById(R.id.get_events_json).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    showLogs(FalxApi.getInstance(MainActivity.this).eventToJSON("falx_logs_test.log"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.trigger_logging).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logging = !logging;
                FalxApi.getInstance(MainActivity.this).enableLogging(logging);
                if (logging) {
                    Toast.makeText(MainActivity.this, "Falx Logging enabled.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Falx Logging disabled.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        JobScheduler jobScheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        List<JobInfo> pendingJobs = jobScheduler.getAllPendingJobs();

        boolean foundBatteryJob = false;
        for (JobInfo info : pendingJobs) {
            if (ScheduledJobService.JOB_ID_BATTERY_STATS == info.getId()) {
                foundBatteryJob = true;
                break;
            }
        }

        if (!foundBatteryJob) {
            JobInfo.Builder builder = new JobInfo.Builder(ScheduledJobService.JOB_ID_BATTERY_STATS, new ComponentName(this, ScheduledJobService.class));
            builder.setPeriodic(1 * DateUtils.MINUTE_IN_MILLIS);
            builder.setRequiredNetworkType(NETWORK_TYPE_ANY);
            JobInfo jobInfo = builder.build();

            int schRes = jobScheduler.schedule(jobInfo);
            Log.d("rk-dbg", "Job scheduling result: " + schRes);
        } else {
            Log.d("rk-dbg", "Battery stat reporter job already scheduled");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        falxApi.startSession(this);
        falxApi.turnedOn(FalxConstants.MONITOR_LABEL_GPS);
        falxApi.turnedOn(FalxConstants.MONITOR_LABEL_ACTIVITY_DETECTION);
    }

    @Override
    protected void onStop() {
        super.onStop();

        falxApi.turnedOff(FalxConstants.MONITOR_LABEL_GPS);
        falxApi.turnedOff(FalxConstants.MONITOR_LABEL_ACTIVITY_DETECTION);
        falxApi.endSession(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                Log.d("rk-dbg", "Map long click rev-geocoding: " + latLng.toString());

                // Try a reverse geocode
                GooglePlatform.getInterface(MainActivity.this)
                        .reverseGeocode(String.format(Locale.US, "%f,%f", latLng.latitude, latLng.longitude), Locale.getDefault().getLanguage())
                        .enqueue(new Callback<GeocodeResponse>() {
                            @Override
                            public void onResponse(Call<GeocodeResponse> call, Response<GeocodeResponse> response) {
                                if (response.isSuccessful()) {
                                    String address = "No address";

                                    GeocodeResponse geocodeResponse = response.body();
                                    if (geocodeResponse.results.size() > 0) {
                                        GeocodeResponse.Results results = geocodeResponse.results.get(0);
                                        address = results.formattedAddress;
                                    }

                                    Log.d("rk-dbg", "Rev geocode response: " + address);
                                    Toast.makeText(MainActivity.this, address, Toast.LENGTH_SHORT).show();
                                } else {
                                    try {
                                        Log.e("rk-dbg", "Error in response: " + response.errorBody().string());
                                    } catch (IOException e) {
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<GeocodeResponse> call, Throwable t) {
                                Log.e("rk-dbg", "Call failure: " + t.toString());
                            }
                        });
            }
        });

    }

    private void showLogs(@NonNull URI uri) throws IOException {

        File file = new File(uri);

        StringBuilder text = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            Log.d("vs-dbg", "file read failed", e.getCause());
        } finally {
            if (br != null) {
                br.close();
            }
        }

        Toast.makeText(this, text.toString(), Toast.LENGTH_LONG).show();

    }
}
