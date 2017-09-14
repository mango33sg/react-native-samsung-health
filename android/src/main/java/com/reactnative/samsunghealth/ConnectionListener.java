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

public class ConnectionListener implements
    HealthDataStore.ConnectionListener
{
    private Callback mSuccessCallback;
    private Callback mErrorCallback;
    private SamsungHealthModule mModule;

    private static final String REACT_MODULE = "RNSamsungHealth";

    public Set<PermissionKey> mKeySet;

    public ConnectionListener(SamsungHealthModule module, Callback error, Callback success)
    {
        mModule = module;
        mErrorCallback = error;
        mSuccessCallback = success;
    }

    @Override
    public void onConnected() {
        Log.d(REACT_MODULE, "Health data service is connected.");
        HealthPermissionManager pmsManager = new HealthPermissionManager(mModule.getStore());
        mKeySet = new HashSet<PermissionKey>();
        mKeySet.add(new PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, PermissionType.READ));
        try {
            Map<PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(mKeySet);
            if (resultMap.containsValue(Boolean.FALSE)) {
                mSuccessCallback.invoke(false);
            } else {
                mSuccessCallback.invoke(true);
            }
        } catch (Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            mErrorCallback.invoke("Permission setting fails");
        }
    }

    @Override
    public void onConnectionFailed(HealthConnectionErrorResult error) {
        Log.d(REACT_MODULE, "Health data service is not available.");
        mErrorCallback.invoke("Health data service is not available.");
    }

    @Override
    public void onDisconnected() {
        Log.d(REACT_MODULE, "Health data service is disconnected.");
    }
};
/* vim :set ts=4 sw=4 sts=4 et : */
