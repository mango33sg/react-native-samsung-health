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
import com.facebook.react.bridge.WritableArray;
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
import java.util.TimeZone;
import java.util.Date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class StepCountResultListener implements
    HealthResultHolder.ResultListener<ReadResult>
{
    private static final String REACT_MODULE = "RNSamsungHealth";

    private Callback mSuccessCallback;
    private Callback mErrorCallback;
    private SamsungHealthModule mModule;

    public StepCountResultListener(SamsungHealthModule module, Callback error, Callback success)
    {
        mSuccessCallback = success;
        mErrorCallback = error;
        mModule = module;
    }

    @Override
    public void onResult(ReadResult result) {
        WritableArray resultSet = Arguments.createArray();
        //String resultSet = "[";

        Cursor c = null;

        HealthDeviceManager deviceManager = new HealthDeviceManager(mModule.getStore());

        try {
            c = result.getResultCursor();

            Log.d(REACT_MODULE, "Column Names" + Arrays.toString(c.getColumnNames()));

            if (c != null) {
                String deviceName = "";
                String deviceManufacturer = "";
                String deviceModel = "";
                String groupName="";
                Integer deviceGroup;
                byte[] dataText = null;

                while (c.moveToNext()) {

                    deviceName = deviceManager.getDeviceByUuid(c.getString(c.getColumnIndex(HealthConstants.StepCount.DEVICE_UUID))).getCustomName();
                    deviceManufacturer = deviceManager.getDeviceByUuid(c.getString(c.getColumnIndex(HealthConstants.StepCount.DEVICE_UUID))).getManufacturer();
                    deviceModel = deviceManager.getDeviceByUuid(c.getString(c.getColumnIndex(HealthConstants.StepCount.DEVICE_UUID))).getModel();
                    deviceGroup = deviceManager.getDeviceByUuid(c.getString(c.getColumnIndex(HealthConstants.StepCount.DEVICE_UUID))).getGroup();

                    if (deviceName == null) {
                        deviceName = "";
                    }

                    if (deviceManufacturer == null) {
                        deviceManufacturer = "";
                    }

                    if (deviceModel == null) {
                        deviceModel = "";
                    }
                    switch(deviceGroup){
                        case HealthDevice.GROUP_MOBILE:
                            groupName = "mobileDevice";
                            break;
                        case HealthDevice.GROUP_EXTERNAL:
                            groupName = "peripheral";
                            break;
                        case HealthDevice.GROUP_COMPANION:
                            groupName = "wearable";
                            break;
                        case HealthDevice.GROUP_UNKNOWN:
                            groupName = "unknown";
                            break;
                    }

                    WritableMap map = Arguments.createMap();

                    long t_offset = c.getLong(c.getColumnIndex(HealthConstants.StepCount.TIME_OFFSET));
                    long t_start = t_offset + c.getLong(c.getColumnIndex(HealthConstants.StepCount.START_TIME));
                    long t_end = t_offset + c.getLong(c.getColumnIndex(HealthConstants.StepCount.END_TIME));

                    Date dt_start = new Date(t_start);
                    Date dt_end = new Date(t_end);
                    DateFormat dt_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    dt_format.setTimeZone(TimeZone.getDefault());

                    map.putDouble("start_ts", (double)t_start);
                    map.putDouble("end_ts", (double)t_end);

                    map.putString("start", dt_format.format(dt_start));
                    map.putString("end", dt_format.format(dt_end));

                    map.putInt("step", c.getInt(c.getColumnIndex(HealthConstants.StepCount.COUNT)));

                    map.putString("deviceName", deviceName);
                    map.putString("deviceManufacturer", deviceManufacturer);
                    map.putString("deviceModel", deviceModel);
                    map.putString("deviceGroup", groupName);

                    resultSet.pushMap(map);
                }

            } else {
                Log.d(REACT_MODULE, "The cursor is null.");
            }
        }
        catch(Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            mErrorCallback.invoke(e.getClass().getName() + " - " + e.getMessage());
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
        //resultSet += "]";

        Log.d(REACT_MODULE, "Steps Results");
        mSuccessCallback.invoke(resultSet);
    }
}

/* vim :set ts=4 sw=4 sts=4 et : */
