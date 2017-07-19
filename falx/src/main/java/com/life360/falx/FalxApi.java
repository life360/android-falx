package com.life360.falx;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.format.DateUtils;
import android.util.Log;

import com.life360.falx.dagger.DaggerUtilComponent;
import com.life360.falx.dagger.DateTimeModule;
import com.life360.falx.dagger.LoggerModule;
import com.life360.falx.dagger.UtilComponent;
import com.life360.falx.util.Clock;
import com.life360.falx.util.Logger;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

/**
 * Created by remon on 6/27/17.
 */
public class FalxApi {

    public static FalxApi getInstance(Context context) {
        if (falxApi == null) {
            synchronized (FalxApi.class) {
                if (falxApi == null) {
                    falxApi = new FalxApi(context);
                }
            }
        }

        return falxApi;
    }

    public static void init(@NonNull final Context context) {
        Log.d(TAG, "init Falx");
        getInstance(context);
    }

    /**
     * Marks start of a session, call when a Activity comes to the foreground (Activity.onStart)
     * @param activity
     * @return
     */
    public boolean startSession(Activity activity) {
        // Is a session in progress?
        if (startTime == 0) {
            startTime = clock.currentTimeMillis();

            logger.d(TAG, "Session started... at: " + startTime);
        } else {
            logger.d(TAG, "Session already in progress, started at: " + startTime);

            // reset session end timer if there is one
            if (sessionEndTimer != null) {
                sessionEndTimer.cancel();
                sessionEndTimer = null;
            }
        }

        return true;
    }

    /**
     * * Marks start of a session, call when a Activity is removed from the foreground (Activity.onStop)
     * @param activity
     * @return
     */
    public boolean endSession(Activity activity) {
        endTime = clock.currentTimeMillis();

        // Start a timer to end the session in TIME_BETWEEN_ACTIVITY_TRANSITION milliseconds
        if (sessionEndTimer != null) {
            sessionEndTimer.cancel();
            sessionEndTimer = null;
        }

        sessionEndTimer = new Timer();

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                onSessionEnded();
            }
        };

        sessionEndTimer.schedule(task, TIME_BETWEEN_ACTIVITY_TRANSITION);

        return true;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    void onSessionEnded() {
        if (endTime < startTime) {
            logger.e(TAG, "Likeley error in timer schedule to mark end of a session");

            endTime = startTime;
        }

        logger.d(TAG, "Session completed, duration (seconds): " + ((endTime - startTime) / 1000));

        lastSessionData = new SessionData(startTime, (endTime - startTime));

        startTime = 0;
        endTime = 0;

        if (sessionEndTimer != null) {
            sessionEndTimer.cancel();
            sessionEndTimer = null;
        }
    }

    private static final String TAG = "FalxApi";

    private static volatile FalxApi falxApi = null;

    @VisibleForTesting
    static final long TIME_BETWEEN_ACTIVITY_TRANSITION = 5 * DateUtils.SECOND_IN_MILLIS;

    private Context application;        // Application application

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Inject Clock clock;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Inject Logger logger;

    private long startTime;
    private long endTime;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    Timer sessionEndTimer;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    SessionData lastSessionData;

    private UtilComponent utilComponent;

    private FalxApi(@NonNull Context context) {
        // Create our UtilComponent module, since it will be only used by FalxApi
        UtilComponent utilComponent = DaggerUtilComponent.builder()
                // list of modules that are part of this component need to be created here too
                .dateTimeModule(new DateTimeModule())
                .loggerModule(new LoggerModule())
                .build();

        init(context, utilComponent);
    }

    /**
     * Test code can pass in a TestUtilsComponent to this special constructor to inject
     * fake objects instead of what is provided by UtilComponent
     * @param context
     * @param utilComponent
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    FalxApi(@NonNull Context context, @NonNull UtilComponent utilComponent) {
        init(context, utilComponent);
    }

    private void init(Context context, UtilComponent utilComponent) {
        this.application = context.getApplicationContext();

        // Create our UtilComponent module, since it will be only used by FalxApi
        this.utilComponent = utilComponent;
        this.utilComponent.inject(this);
    }

}
