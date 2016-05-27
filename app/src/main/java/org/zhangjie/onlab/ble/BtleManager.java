package org.zhangjie.onlab.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/26.
 */
public class BtleManager {
    private final String TAG = "Onlab.BtleManager";

    private static BtleManager instance = null;

    static {
        instance = new BtleManager();
    }

    private BtleManager() {
    }

    public static BtleManager getInstance() {
        return instance;
    }

    private Context mContext;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mIsBtleConnected = false;
    private String mBtleAddress = null;
    private Handler mBtleHandler;
    private static final long SCAN_PERIOD = 10000;
    private final String BLE_HEAD = "onLab";
    private BtleListener mBtleListener = null;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings mScanSettings;

    private final ServiceConnection mBtleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "BLE service connected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            final String deviceName = device.getName();
            final String deviceAddr = device.getAddress();

            Log.d(TAG, "name = " + deviceName);
            Log.d(TAG, "addr = " + deviceAddr);
            if(deviceName.startsWith(BLE_HEAD)) {
                //connect to the device
                mBluetoothLeService.connect(deviceAddr);
                scan(false);
            }
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            Log.i(TAG, String.valueOf(callbackType));
//            Log.i(TAG, result.toString());
            BluetoothDevice btDevice = result.getDevice();
            Log.d(TAG, "name = " + btDevice.getName());
            Log.d(TAG, "addr = " + btDevice.getAddress());
//            connectToDevice(btDevice);
            if(btDevice.getName() != null && (btDevice.getName().startsWith(BLE_HEAD))) {
                //connect to the device
                mBluetoothLeService.connect(btDevice.getAddress());
                scan(false);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
//                Log.i(TAG, sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Error Code: " + errorCode);
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mIsBtleConnected = true;
                mBtleListener.onDeviceConnected();
                Log.d(TAG, "device connected");
//                mHandler.obtainMessage(DEVICE_CONNECT_STATE,
//                        DEVICE_CONNECT_STATE_CONNECTED, -1).sendToTarget();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                mIsBtleConnected = false;
                mBtleListener.onDeviceDisconnected();

                Log.d(TAG, "device disconnected");

//                mHandler.obtainMessage(DEVICE_CONNECT_STATE,
//                        DEVICE_CONNECT_STATE_DISCONNECTED, -1).sendToTarget();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
            } else if (BluetoothLeService.ACTION_DEVICE_FIND.equals(action)) {

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // process data
                byte data[] = intent
                        .getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
//                if (handleData(data)) {
//                    processData();
//                    resetData();
//                }
            }
        }
    };

    public void scan(final boolean enable) {
        if (enable) {
            mBtleHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mIsBtleConnected) {
                        if (Build.VERSION.SDK_INT < 21) {
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        } else {
                            mLEScanner.stopScan(mScanCallback);
                        }
                    } else {
                        Log.d(TAG, "already connected");
                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.d(TAG, "stop scan");
            } else {
                mLEScanner.stopScan(mScanCallback);
                Log.d(TAG, "stop scan");
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter
                .addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DEVICE_FIND);
        intentFilter.addAction(BluetoothLeService.EXTRA_DATA);

        return intentFilter;
    }

    public void init(Context context) {
        Log.d(TAG, "init");
        mContext = context;
        mBtleHandler = new Handler();
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mScanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(3000)
                    .build();
        }

        Intent gattServiceIntentFilter = new Intent(mContext,
                BluetoothLeService.class);
        mContext.bindService(gattServiceIntentFilter, mBtleServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    public void register(BtleListener listener) {
        Log.d(TAG, "register");
        mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        mBtleListener = listener;
    }

    public void unregister() {
        Log.d(TAG, "unregister");
        mContext.unregisterReceiver(mGattUpdateReceiver);
    }

    public void release() {
        Log.d(TAG, "release");
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mBluetoothLeService.disconnect();
        mContext.unbindService(mBtleServiceConnection);
    }

}
