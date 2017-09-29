package com.life360.batterytestapp.google;

/**
 * Created by remon on 9/19/17.
 */

import android.content.Context;
import android.text.format.DateUtils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.life360.falx.FalxApi;
import com.life360.falx.network.FalxInterceptor;

import junit.framework.Assert;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit interfaces to call google platoform endpoints (maps, places, directions etc.).
 *
 * Created by Remon Karim on 4/25/16.
 */
public class GooglePlatform {

    private static final String GEOCODE_URL = "maps/api/geocode/json";
    public interface Api {
        @GET(GEOCODE_URL)
        Call<GeocodeResponse> reverseGeocode(
                @Query("latlng") String latLng,     // %f,%f
                @Query("language") String language
        );

        @GET(GEOCODE_URL)
        Call<GeocodeResponse> geocode(
                @Query("address") String address,
                @Query("language") String language
        );

    }

    private static final String GOOGLE_MAPS_API_BASE_URL = "https://maps.googleapis.com/";
    private static final int CACHE_SIZE = 16 * 1024 * 1024;     // 16 MB
    private static final GooglePlatform INSTANCE = new GooglePlatform();
    private Api apiInterfaceImpl;

    private static GooglePlatform getInstance() {
        return INSTANCE;
    }

    // Made constructor private to hide it from outside classes
    private GooglePlatform() { }

    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
//            if (AndroidUtils.hasInternetConnection(Life360BaseApplication.getContext())) {
                long maxAge = DateUtils.HOUR_IN_MILLIS; // read from cache for 1 hr
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
//            } else {
//                long maxStale = DateUtils.DAY_IN_MILLIS * 28; // tolerate 4-weeks stale
//                return originalResponse.newBuilder()
//                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
//                        .build();
//            }
        }
    };

    /*
     * Used to limit the number of queries within the 10/sec free limit. Used only for reverse geocoding and geocoding.
     */
    private static final Interceptor THROTTLING_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            if (chain.request().url().toString().contains(GEOCODE_URL)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            return chain.proceed(chain.request());
        }
    };

    private Api createGoogleMapsInterfaceImpl(Context context) {
        File cacheDir = context.getCacheDir();
        Cache responseCache = new Cache(cacheDir, CACHE_SIZE);

        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.addInterceptor(THROTTLING_INTERCEPTOR);
        okHttpClientBuilder.addNetworkInterceptor(FalxApi.getInstance(context).getInterceptor());      // remon.add

        okHttpClientBuilder.networkInterceptors().add(REWRITE_CACHE_CONTROL_INTERCEPTOR);
        okHttpClientBuilder.cache(responseCache);

//        if (!BuildConfig.IS_PRODUCTION) {
//            okHttpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
//        }

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GOOGLE_MAPS_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .client(okHttpClientBuilder.build())
                .build();

        return retrofit.create(Api.class);
    }

    /**
     * Get Life360Api instance that includes location data in the headers.
     * @param context
     * @return an authorized ApiInterface for retrofit calls
     */
    public static Api getInterface(final Context context) {
        final GooglePlatform googleApis = getInstance();
        Assert.assertNotNull(context);

        if (context != null) {
            if (googleApis.apiInterfaceImpl == null) {
                googleApis.apiInterfaceImpl = googleApis.createGoogleMapsInterfaceImpl(context);
            }
        }

        return googleApis.apiInterfaceImpl;
    }

    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return builder.create();
    }
}
