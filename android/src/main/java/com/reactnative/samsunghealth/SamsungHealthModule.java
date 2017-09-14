package com.reactnative.samsunghealth;

import android.database.Cursor;
import android.util.Log;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.LifecycleEventListener;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataResolver.Filter;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadResult;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthDevice;
import com.samsung.android.sdk.healthdata.HealthDeviceManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionResult;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by firodj on 5/2/17.
 */

@ReactModule(name = "RNSamsungHealth")
public class SamsungHealthModule extends ReactContextBaseJavaModule implements
        LifecycleEventListener {

    private static final String REACT_MODULE = "RNSamsungHealth";

    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";

    private HealthDataStore mStore;
    public Set<PermissionKey> mKeySet;


    public SamsungHealthModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return REACT_MODULE;
    }

    @Override
    public void initialize() {
        super.initialize();

        getReactApplicationContext().addLifecycleEventListener(this);
        initSamsungHealth();
    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {
    }

    private void sendEvent(String eventName,
                           @Nullable WritableMap params) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }


    public void initSamsungHealth() {
        Log.d(REACT_MODULE, "initialize Samsung Health...");
        HealthDataService healthDataService = new HealthDataService();
        try {
            healthDataService.initialize(getReactApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HealthDataStore getStore()
    {
        return mStore;
    }

    public ReactContext getContext()
    {
        return getReactApplicationContext();
    }

    @ReactMethod
    public void connect(Callback error, Callback success)
    {
        mKeySet = new HashSet<PermissionKey>();
        mKeySet.add(new PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, PermissionType.READ));
        if (mStore == null) {
            error.invoke("status permission is false");
            // mStore = new HealthDataStore(getReactApplicationContext(), new ConnectionListener(this, null, success));
        }
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        pmsManager.requestPermissions(mKeySet, this.getContext().getCurrentActivity()).setResultListener(
            new PermissionListener(this, error, success)
        );
    }

    @ReactMethod
    public void statusPermission(Callback success)
    {
        mStore = new HealthDataStore(getReactApplicationContext(), new ConnectionListener(this, null, success));
        mStore.connectService();
    }

    @ReactMethod
    public void disconnect()
    {
        if (mStore != null) {
            Log.d(REACT_MODULE, "disconnectService");
            mStore.disconnectService();
            mStore = null;
        }
    }

    private long getStartTimeOfToday() {
        Calendar today = Calendar.getInstance();

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return today.getTimeInMillis();
    }

    // Read the today's step count on demand
    @ReactMethod
    public void readStepCount(double startDate, double endDate, Callback error, Callback success) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        Log.d(REACT_MODULE, "startDate:" + String.valueOf(startDate));
        Log.d(REACT_MODULE, "endDate:" + String.valueOf(endDate));

        Filter filter = Filter.and(Filter.
                greaterThanEquals(HealthConstants.StepCount.START_TIME, (long)startDate),
                Filter.lessThanEquals(HealthConstants.StepCount.START_TIME, (long)endDate)
                );

        HealthDataResolver.ReadRequest request = new ReadRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .setProperties(new String[]{
                        HealthConstants.StepCount.COUNT,
                        HealthConstants.StepCount.START_TIME,
                        HealthConstants.StepCount.END_TIME,
                        HealthConstants.StepCount.TIME_OFFSET,
                        HealthConstants.StepCount.DEVICE_UUID
                })
                .setFilter(filter)
                .build();


        try {
            resolver.read(request).setResultListener(new StepCountResultListener(this, error, success));
        } catch (Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            Log.e(REACT_MODULE, "Getting step count fails.");
            error.invoke("Getting step count fails.");
        }
    }
}

/* vim :set ts=4 sw=4 sts=4 et : */
