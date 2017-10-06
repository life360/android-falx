package com.life360.batterytestapp;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.os.PersistableBundle;
import android.util.Log;

/**
 * Created by remon on 10/6/17.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScheduledJobService extends JobService {
    static final int JOB_ID_BATTERY_STATS = 100;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        PersistableBundle bundle = jobParameters.getExtras();

        Log.d("rk-dbg", "onStartJob ID " + jobParameters.getJobId());

        if (jobParameters.getJobId() == JOB_ID_BATTERY_STATS) {
            BatteryStatReporter.sendLogs(this);
            return true;
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d("rk-dbg", "onStopJob ID " + jobParameters.getJobId());
        return false;
    }
}