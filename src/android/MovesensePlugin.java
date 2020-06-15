package com.szemafor.plugins.movesense;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Map;
import java.util.HashMap;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.*;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import com.movesense.mds.*;

import java.util.HashMap;

/**
 * This class echoes a string called from JavaScript.
 */
public class MovesensePlugin extends CordovaPlugin {
    private static final String LOG_TAG = "MovesensePlugin";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("scan")) {
            this.scan(callbackContext);
            return true;
        }
        else if (action.equals("stopscan")) {
            //potential error
            this.stopscan(callbackContext);
            return true;
        }
        else if (action.equals("connect")) {
            String macAddress = args.getString(0);
            this.connect(macAddress, callbackContext);
            return true;
        }
        else if (action.equals("disconnect")) {
            String macAddress = args.getString(0);
            this.disconnect(macAddress, callbackContext);
            return true;
        }
        else if (action.equals("get") || action.equals("post") || action.equals("put") || action.equals("delete")) {
            String uri = args.getString(0);
            String body = args.optString(1, null);
            this.action(action, uri, body, callbackContext);
            return true;
        }
        else if (action.equals("subscribe")) {
            String uri = args.getString(0);
            String body = args.optString(1, null);
            String subscriptionId = args.optString(2, "id");
            this.subscribe(uri, body, subscriptionId, callbackContext);
            return true;
        }
        else if (action.equals("unsubscribe")) {
            String subscriptionId = args.optString(0, "id");
            this.unsubscribe(subscriptionId, callbackContext);
            return true;
        }
        return false;
    }

    /**
     * Scan
     */

    static private RxBleClient mBleClient;

    private RxBleClient getBleClient() {
        // Init RxAndroidBle (Ble helper library) if not yet initialized
        if (mBleClient == null)
        {
            Context context=this.cordova.getActivity().getApplicationContext();
            mBleClient = RxBleClient.create(context);
        }

        return mBleClient;
    }

    Disposable mScanSubscription;
    Observable<ScanResult> mScan;

    /**
     * scan action from cordova/js
     *
     * @param callbackContext
     */
    private void scan(CallbackContext callbackContext) {
        mScan = getBleClient().scanBleDevices(
                new ScanSettings.Builder()
                        // .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        // .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build()
                // add filters if needed
        );
        mScan.doOnDispose(() -> {
            Log.d(LOG_TAG,"scan.dispose");
        });
        mScanSubscription = mScan.subscribe(
                scanResult -> {
                    Log.d(LOG_TAG,"scanResult: " + scanResult);
                    if (this.mScanSubscription == null) {
                        throw new RuntimeException("Stop scan");
                        // return;
                    }

                    // Process scan result here. filter movesense devices.
                    if (scanResult.getBleDevice()!=null &&
                            scanResult.getBleDevice().getName() != null &&
                            scanResult.getBleDevice().getName().startsWith("Movesense")) {

                        try {
                            JSONObject item = new JSONObject();

                            item.put("name", scanResult.getBleDevice().getName());
                            item.put("mac", scanResult.getBleDevice().getMacAddress());
                            item.put("rssi", scanResult.getRssi());

                            PluginResult result = new PluginResult(PluginResult.Status.OK, item);
                            result.setKeepCallback(true);
                            callbackContext.sendPluginResult(result);
                        } catch (JSONException e) {
                            callbackContext.error(e.getMessage());
                        }
                    }
                },
                throwable -> {
                    Log.e(LOG_TAG,"scan error: " + throwable);
                    stopScan();
                    callbackContext.error(throwable.getMessage());
                });
    }

    private void stopScan() {
        if (mScanSubscription != null) {
            mScanSubscription.dispose();
            mScanSubscription = null;
            mScan = null;
        }
    }

    private void stopscan(CallbackContext callbackContext) {
        if (mScanSubscription != null) {
            stopScan();
            callbackContext.success();
        } else {
            callbackContext.error("No scan is going on");
        }

    }

    /**
     * Connect/disconnect
     */

    static Mds mMds = null;
    private Mds getMds() {
        // Init RxAndroidBle (Ble helper library) if not yet initialized
        if (mMds == null)
        {
            Context context=this.cordova.getActivity().getApplicationContext();
            mMds =  Mds.builder().build(context);
        }

        return mMds;
    }



    private void connect(String macAddress, CallbackContext callbackContext) {
        RxBleDevice bleDevice = getBleClient().getBleDevice(macAddress);
        Log.i(LOG_TAG, "Connecting to BLE device: " + bleDevice.getMacAddress());

        getMds().connect(bleDevice.getMacAddress(), new MdsConnectionListener() {
            @Override
            public void onConnect(String macAddress) {
                Log.d(LOG_TAG, "onConnect:" + macAddress);

                try {
                    JSONObject item = new JSONObject();

                    item.put("type", "connect");
                    item.put("mac", macAddress);

                    PluginResult result = new PluginResult(PluginResult.Status.OK, item);
                    result.setKeepCallback(true);
                    //callbackContext.sendPluginResult(result);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            }

            @Override
            public void onConnectionComplete(String macAddress, String serial) {
                Log.d(LOG_TAG, "onConnectionComplete:" + macAddress + " - " + serial);

                try {
                    JSONObject item = new JSONObject();

                    item.put("type", "connectComplete");
                    item.put("mac", macAddress);
                    item.put("serial", serial);

                    PluginResult result = new PluginResult(PluginResult.Status.OK, item);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            }

            @Override
            public void onError(MdsException e) {
                Log.e(LOG_TAG, "onError:" + e);

                callbackContext.error(e.getMessage());
            }

            @Override
            public void onDisconnect(String macAddress) {
                Log.d(LOG_TAG, "onDisconnect: " + macAddress);

                try {
                    JSONObject item = new JSONObject();

                    item.put("type", "disconnect");
                    item.put("mac", macAddress);

                    PluginResult result = new PluginResult(PluginResult.Status.OK, item);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void disconnect(String macAddress, CallbackContext callbackContext) {
        getMds().disconnect(macAddress);
        callbackContext.success();
    }

    /**
     * Actions
     */

    private void action(String type, String uri, String body, CallbackContext callbackContext) {
        MdsResponseListener listener = new MdsResponseListener() {
            @Override
            public void onSuccess(String data) {
                Log.i(LOG_TAG, "MDS - " + type + " response "  + data);
                callbackContext.success(data);
            }

            @Override
            public void onError(MdsException e) {
                Log.e(LOG_TAG, "MDS - " + type + " response as error:" + e);
                callbackContext.error(e.getMessage());
            }
        };

        if (type.equals("get")) {
            getMds().get(uri, body, listener);
        } else if (type.equals("put")) {
            getMds().put(uri, body, listener);
        } else if (type.equals("post")) {
            getMds().put(uri, body, listener);
        } else if (type.equals("delete")) {
            getMds().put(uri, body, listener);
        } else {
            callbackContext.error("Unexpected type:"+ type);
        }
    }

    /**
     * Subscriptions
     */

    int subscriptionId = 0;

    Map<String, MdsSubscription> subscriptionMap = new HashMap<String, MdsSubscription>();

    private String createSubscriptionId() {
        subscriptionId += 1;
        return "SID-" + subscriptionId;
    }

    private void subscribe(String uri, String body, String subscriptionId, CallbackContext callbackContext) {
        String subId = subscriptionId;
        if (subscriptionMap.containsKey(subId)) {
            callbackContext.error("Subscription with id:"+subId+"already exists!");
            return;
        }

        MdsSubscription subscription = getMds().subscribe(uri, body, new MdsNotificationListener() {
            @Override
            public void onNotification(String data) {
                Log.i(LOG_TAG, "MDS - notification "  + data);
                /*try {
                     JSONObject item = new JSONObject();

                    item.put("type", "data");
                    item.put("data", data); */

                    PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                    /*
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }*/
            }

            @Override
            public void onError(MdsException e) {
                Log.e(LOG_TAG, "MDS - subscribe error:" + e);
                callbackContext.error(e.getMessage());
            }
        });
        subscriptionMap.put(subId, subscription);
        /*
        try {
            JSONObject item = new JSONObject();

            item.put("type", "subscribed");
            item.put("id", subId);

            PluginResult result = new PluginResult(PluginResult.Status.OK, item);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

        } catch (JSONException e) {
            callbackContext.error(e.getMessage());
        }*/
    }


    private void unsubscribe(String subId, CallbackContext callbackContext) {
        if (subscriptionMap.containsKey(subId)) {
            MdsSubscription sub = subscriptionMap.remove(subId);
            sub.unsubscribe();
            callbackContext.success();
        } else {
            callbackContext.error("No subscription with id:" + subId);
        }
    }
}
