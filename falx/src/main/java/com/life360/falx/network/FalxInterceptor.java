package com.life360.falx.network;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.life360.falx.FalxApi;
import com.life360.falx.dagger.DaggerUtilComponent;
import com.life360.falx.dagger.LoggerModule;
import com.life360.falx.dagger.UtilComponent;
import com.life360.falx.model.NetworkActivity;
import com.life360.falx.util.Logger;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.Observer;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by remon on 9/19/17.
 *
 * Provides integration with <a href="http://square.github.io/okhttp/">OkHttp</a>
 * using <a href="https://github.com/square/okhttp/wiki/Interceptors">Interceptor</a>.
 * Usage:
 * <pre>
 *   OkHttpClient client = new OkHttpClient.Builder()
 *       .addNetworkInterceptor(new FalxInterceptor())
 *       .build();
 * </pre>
 */

public class FalxInterceptor implements Interceptor {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Inject
    Logger logger;

    private Observer<NetworkActivity> networkActivityObserver;


    public FalxInterceptor(@NonNull Observer<NetworkActivity> networkActivityObserver) {
        UtilComponent utilComponent = DaggerUtilComponent.builder()
                // list of modules that are part of this component need to be created here too
                .loggerModule(new LoggerModule())
                .build();

        utilComponent.inject(this);

        this.networkActivityObserver = networkActivityObserver;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long t1 = System.nanoTime();

        logger.i(FalxApi.TAG, String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        Response response = chain.proceed(request);
        long t2 = System.nanoTime();

        final HttpUrl httpUrl = response.request().url();
        logger.i(FalxApi.TAG, String.format("Received response for %s in %.1fms%n%s",
                httpUrl, (t2 - t1) / 1e6d, response.headers()));

        // Note: Do not consume the response body here.

        int bytesReceived = 0;
        String contentLength = response.header("content-length");
        if (contentLength != null) {
            bytesReceived = Integer.valueOf(contentLength);
        }

        logger.i(FalxApi.TAG, "Response length: (bytes) = " + bytesReceived);

        // Publish result to the Observable
        networkActivityObserver.onNext(new NetworkActivity(1, bytesReceived, httpUrl.toString()));

        return response;
    }
}
