package biz.lightwell.sentiremobile.sensorMgmt;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import biz.lightwell.sentiremobile.R;
import biz.lightwell.sentiremobile.myUtil.C;


// TODO: 3/23/2017 need to research bluetooth profiles - these are associated to the board ID and it caches the gatt attributes; profile must be removed if gatt attributes are changed...

public class SensorManagementActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mConnectedGatt;
    private SparseArray<BluetoothDevice> mDevices;
    private BluetoothLeScanner mBluetoothLeScanner;
    // private BluetoothGattCallback mGattCallback;
    // private Handler mHandler;
    private ScanSettings mSettings;
    private List<ScanFilter> mFilters;

    private ProgressDialog mProgress;
    private TextView mScanStatus, mDeviceStatus, mSensorData;


    // ------------------------------------------------ OVERRIDE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (C.LOGGING) { Log.d(C.LOGTAG, "SensorManagementActivity - onCreate"); }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_management);

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        mDevices = new SparseArray<BluetoothDevice>();

        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);

        mScanStatus     = (TextView) findViewById(R.id.scanStatus);
        mDeviceStatus   = (TextView) findViewById(R.id.deviceStatus);
        mSensorData     = (TextView) findViewById(R.id.sensorData);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled - request to get enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        } else {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            mFilters = new ArrayList<ScanFilter>();            
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mProgress.dismiss();
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        stopScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add the "scan" option to the menu
        getMenuInflater().inflate(R.menu.sensor_menu, menu);
        //Add any device elements we've discovered to the overflow menu
        for (int i=0; i < mDevices.size(); i++) {
            BluetoothDevice device = mDevices.valueAt(i);
            menu.add(0, mDevices.keyAt(i), 0, device.getName());
        }        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                mDevices.clear();
                startScan();
                return true;
            default:
                //Obtain the discovered device to connect with
                BluetoothDevice device = mDevices.get(item.getItemId());
                Log.i(C.LOGTAG, "Connecting to "+device.getName());
                /*
                 * Make a connection with the device using the special LE-specific
                 * connectGatt() method, passing in a callback for GATT events
                 */
                mConnectedGatt = device.connectGatt(this, false, mGattCallback);
                //Display progress UI
                //mHandler.sendMessage(Message.obtain(null, C.MSG_CONNECTDEVICE, "Connecting to "+device.getName()+"..."));
                return super.onOptionsItemSelected(item);
        }
    }

    // ------------------------------------------------ METHODS & BUTTON HANDLERS
    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    private void startScan() {
        setProgressBarIndeterminateVisibility(true);
        mHandler.postDelayed(mStopRunnable, 10000);
        mBluetoothLeScanner.startScan(mFilters, mSettings, mScanCallback);
        mHandler.sendMessage(Message.obtain(null, C.MSG_STARTSCAN, "Started Scanning..."));
    }

    private void stopScan() {
        setProgressBarIndeterminateVisibility(false);
        mBluetoothLeScanner.stopScan(mScanCallback);
        mHandler.sendMessage(Message.obtain(null, C.MSG_STOPSCAN, "Stopped Scanning..."));
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(C.LOGTAG, String.valueOf(callbackType));
            Log.i(C.LOGTAG, result.toString());
            BluetoothDevice device = result.getDevice();
            if(C.DEVICE_NAME.equals(device.getName())) {
                mDevices.put(device.hashCode(), device);
                //Update the overflow menu
                invalidateOptionsMenu();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }

    };




    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        private int lState = 0;
        private void reset() { lState = 0; }
        private void advance() { lState++; }

        private void enableNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (lState) {
                case 0:
                    // TBD
                    Log.i(C.LOGTAG, "Sensor MQ-2 to - single run...");
                    characteristic = gatt.getService(C.MQ2_SERVICE).getCharacteristic(C.MQ2_CONFIG_CHAR);
                    characteristic.setValue(new byte[] {0x01});
                    break;
                default:
                    Log.i(C.LOGTAG, "All senors enabled...");
                    return;
            }
            gatt.writeCharacteristic(characteristic);
        }

        private void readNextSensor(BluetoothGatt gatt) {
            Log.i(C.LOGTAG, "Sensor to read...");
            BluetoothGattCharacteristic characteristic;
            switch (lState) {
                case 0:
                    Log.i(C.LOGTAG, "Reading ...");
                    characteristic = gatt.getService(C.MQ2_SERVICE).getCharacteristic(C.MQ2_DATA_CHAR);
                    break;
                default:
                    Log.i(C.LOGTAG, "All senors read...");
                    return;
            }
            gatt.readCharacteristic(characteristic);
        }

        //setNotify...

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(C.LOGTAG, "Connection state change: " + status + " -> " + newState);
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                mHandler.sendMessage(Message.obtain(null, C.MSG_CONNECTDEVICE, "Connecting Device..." + gatt.getDevice().getName() + " " + gatt.getDevice().toString()));
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                mHandler.sendEmptyMessage(C.MSG_CLEAR);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(C.LOGTAG, "services discovered: " + status);
            //mHandler.sendMessage(Message.obtain(null, C.MSG_PROGRESS, "Enabling sensors..."));
            reset();
            enableNextSensor(gatt);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (characteristic.getUuid().equals(C.MQ2_DATA_CHAR)) {
                mHandler.sendMessage(Message.obtain(null, C.MSG_MQ2, characteristic));
            }

            //setNotifyNextSensor(gatt);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //After writing the enable flag, next we read the initial value
            readNextSensor(gatt);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            /*
             * After notifications are enabled, all updates from the device on characteristic
             * value changes will be posted here.  Similar to read, we hand these up to the
             * UI thread to update the display.
             */
            if (C.HUMIDITY_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, C.MSG_HUMIDITY, characteristic));
            }
            if (C.PRESSURE_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, C.MSG_PRESSURE, characteristic));
            }
            if (C.PRESSURE_CAL_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, C.MSG_PRESSURE_CAL, characteristic));
            }
            if (C.MQ2_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, C.MSG_MQ2, characteristic));
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Once notifications are enabled, we move to the next sensor and start over with enable
            advance();
            enableNextSensor(gatt);
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(C.LOGTAG, "Remote RSSI: "+rssi);
        }

        private String connectionState(int status) {
            switch (status) {
                case BluetoothProfile.STATE_CONNECTED:
                    return "Connected";
                case BluetoothProfile.STATE_DISCONNECTED:
                    return "Disconnected";
                case BluetoothProfile.STATE_CONNECTING:
                    return "Connecting";
                case BluetoothProfile.STATE_DISCONNECTING:
                    return "Disconnecting";
                default:
                    return String.valueOf(status);
            }
        }

    };  
    




    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            BluetoothGattCharacteristic characteristic;
            switch (msg.what) {
                // build up UI message handling here
                case C.MSG_STARTSCAN:
                    mScanStatus.setText("Scanning....");
                    break;
                case C.MSG_STOPSCAN:
                    mScanStatus.setText("Stopped....");
                    break;
                case C.MSG_MQ2:
                    if (C.LOGGING) { Log.d(C.LOGTAG, "SensorManagementActivity - mHandler Data Read: MQ2 - " + msg.toString()); }
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(C.LOGTAG, "Error obtaining MQ2 value");
                        return;
                    }
                    mSensorData.setText(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0).toString());


                    break;
                case C.MSG_CONNECTDEVICE:
                    mDeviceStatus.setText(msg.obj.toString());
                case C.MSG_PROGRESS:
                    //mDeviceStatus.setText(msg.toString());
                    break;
                case C.MSG_DISMISS:
                    break;
                case C.MSG_CLEAR:
                    mScanStatus.setText("TBD...");
                    mDeviceStatus.setText("TBD...");
                    mSensorData.setText("TBD...");
                    break;
                default:
                    if (C.LOGGING) { Log.d(C.LOGTAG, "SensorManagementActivity - mHandler: " + msg.toString()); }
            }
        }
    };



    // ------------------------------------------------ PRIVATE CLASSES

}
