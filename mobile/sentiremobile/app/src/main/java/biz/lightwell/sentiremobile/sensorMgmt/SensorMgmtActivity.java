package biz.lightwell.sentiremobile.sensorMgmt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orm.query.Select;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import biz.lightwell.sentiremobile.myUtil.C;
import biz.lightwell.sentiremobile.R;

public class SensorMgmtActivity extends AppCompatActivity implements View.OnClickListener {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mConnectedGatt;
    private SparseArray<BluetoothDevice> mDevices;
    private BluetoothLeScanner mBluetoothLeScanner;
    // private BluetoothGattCallback mGattCallback;
    // private Handler mHandler;
    private ScanSettings mSettings;
    private List<ScanFilter> mFilters;

    private Button mBtnMQ2, mBtnTemperature, mBtnMQ2Read, mBtnTemperatureRead, mBtnSendJSON;
    private TextView mScanStatus, mDeviceStatus, mMQ2Status, mTemperatureStatus;
    private ListView mListofSensorDataPts;
    private SensorListAdapter adapter;


    // ------------------------------------------------ OVERRIDE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_mgmt);

        mBtnMQ2 = (Button) findViewById(R.id.btn_mq2);
        mBtnTemperature = (Button) findViewById(R.id.btn_temperature);
        mBtnMQ2Read = (Button) findViewById(R.id.btn_mq2Read);
        mBtnTemperatureRead = (Button) findViewById(R.id.btn_temperatureRead);
        mBtnSendJSON = (Button) findViewById(R.id.btn_sendJSON);

        mBtnMQ2.setOnClickListener(this);
        mBtnTemperature.setOnClickListener(this);
        mBtnMQ2Read.setOnClickListener(this);
        mBtnTemperatureRead.setOnClickListener(this);
        mBtnSendJSON.setOnClickListener(this);

        mScanStatus = (TextView) findViewById(R.id.scanStatus);
        mDeviceStatus = (TextView) findViewById(R.id.deviceStatus);
        mMQ2Status = (TextView) findViewById(R.id.mq2Status);
        mTemperatureStatus = (TextView) findViewById(R.id.temperatureStatus);

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mDevices = new SparseArray<BluetoothDevice>();

        List<sensorDataObj> mListofSensorDataPts = (List) sensorDataObj.find(sensorDataObj.class,"sensor_data_key != ?", "''");
        adapter = new SensorListAdapter(this, mListofSensorDataPts);
        ListView listView = (ListView) findViewById(R.id.sensorDataList);
        listView.setAdapter(adapter);


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


    @Override
    public void onClick(View v) {
        Log.i(C.LOGTAG, "SensorMgmtActivity - onClick");
        byte[] msg = new byte[] {0x00};
        UUID service = null;
        UUID characteristic = null;
        String action = "";


        int id = v.getId();
        Button b = (Button) findViewById(id);
        String s = b.getText().toString().substring(0,5);
        String e = b.getText().toString().substring(5);

        if (s.equals("START")) {
            msg = new byte[] {0x01};
            action = "write";
            b.setText("STOP " + e);
        } else if (s.equals("STOP ")) {
            msg = new byte[] {0x00};
            action = "write";
            b.setText("START" + e);
        } else if (s.equals("READ ")) {
            action = "read";
        }

        switch (id) {
            case R.id.btn_mq2:
                service = C.MQ2_SERVICE;
                characteristic = C.MQ2_CONFIG_CHAR;
                break;
            case R.id.btn_mq2Read:
                service = C.MQ2_SERVICE;
                characteristic = C.MQ2_DATA_CHAR;
                break;
            case R.id.btn_temperature:
                service = C.TMP_SERVICE;
                characteristic = C.TMP_CONFIG_CHAR;
                break;
            case R.id.btn_temperatureRead:
                service = C.TMP_SERVICE;
                characteristic = C.TMP_DATA_CHAR;
                break;
            case R.id.btn_sendJSON:
                JSONArray jsonArray = getJSONArraydata();
                Toast.makeText(getApplicationContext(), jsonArray.toString(), Toast.LENGTH_LONG).show();
        }
        if (service != null) {
            sendBLEMessage(service, characteristic, action, msg);
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
        mHandler.postDelayed(mStopRunnable, 10000);
        mBluetoothLeScanner.startScan(mFilters, mSettings, mScanCallback);
        mHandler.sendMessage(Message.obtain(null, C.MSG_STARTSCAN, "Started Scanning..."));
    }

    private void stopScan() {
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
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (characteristic.getUuid().equals(C.MQ2_DATA_CHAR)) {
                mHandler.sendMessage(Message.obtain(null, C.MSG_MQ2, characteristic));
            } else if  (characteristic.getUuid().equals(C.TMP_DATA_CHAR)) {
                mHandler.sendMessage(Message.obtain(null, C.MSG_TMP, characteristic));
            }
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

    private void sendBLEMessage(UUID service, UUID characteristic, String action, byte[] msg) {
        Log.i(C.LOGTAG, "SensorMgmtActivity - sendBLEMessage");
        BluetoothGattCharacteristic c;
        c = mConnectedGatt.getService(service).getCharacteristic(characteristic);
        if (action.equals("read")) {
            mConnectedGatt.readCharacteristic(c);
        } else if (action.equals("write")){
            c.setValue(msg);
            mConnectedGatt.writeCharacteristic(c);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            BluetoothGattCharacteristic characteristic;
            switch (msg.what) {
                case C.MSG_STARTSCAN:
                    mScanStatus.setText("Scanning ...");
                    break;
                case C.MSG_STOPSCAN:
                    mScanStatus.setText("Stopped");
                    break;
                case C.MSG_CONNECTDEVICE:
                    mDeviceStatus.setText(msg.obj.toString());
                    break;
                case C.MSG_MQ2:
                    if (C.LOGGING) { Log.d(C.LOGTAG, "SensorManagementActivity - mHandler Data Read: MQ2 - " + msg.toString()); }
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(C.LOGTAG, "Error obtaining MQ2 value");
                        return;
                    }
                    mMQ2Status.setText(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0).toString());
                    saveData("MQ2", characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0).toString(), "Int");
                    getLatLong();
                    break;
                case C.MSG_TMP:
                    if (C.LOGGING) { Log.d(C.LOGTAG, "SensorManagementActivity - mHandler Data Read: TEMP - " + msg.toString()); }
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(C.LOGTAG, "Error obtaining TMP value");
                        return;
                    }
                    int v = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                    Double voltage = (v / 1024.0) * 5.0;
                    Double temperature = (voltage - .5) * 100;
                    mTemperatureStatus.setText(temperature.toString());
                    saveData("TEMP", temperature.toString(), "Double");
                    getLatLong();
                    break;
                case C.MSG_CLEAR:
                    mScanStatus.setText("TBD");
                    mDeviceStatus.setText("TBD");
                    mMQ2Status.setText("TBD");
                    mTemperatureStatus.setText("TBD");
                    break;
                default:
                    if (C.LOGGING) { Log.d(C.LOGTAG, "SensorMgmtActivity - mHandler: " + msg.toString()); }
            }
        }

        private void saveData(String dataKey, String dataValue, String dataType) {
            ArrayList<sensorDataObj> listofSensorDataPts;

            Calendar c = Calendar.getInstance();
            // TODO: 3/28/2017 replace with call from database
            String m_androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            sensorDataObj sensorData = new sensorDataObj(dataKey, dataValue, dataType, c, m_androidId);
            sensorData.save();

            adapter.reload();



        }

        private void getLatLong(){
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitude = location.getLongitude();
            double latitude  = location.getLatitude();
            double altitude  = location.getAltitude();

            saveData("LAT", String.valueOf(latitude), "double");
            saveData("LONG", String.valueOf(longitude), "double");
            saveData("ALT", String.valueOf(altitude), "double");

        }
    };


    private JSONArray getJSONArraydata() {
        JSONArray jsonArray = new JSONArray();
        List<sensorDataObj> listof =(List) sensorDataObj.find(sensorDataObj.class, "sensor_data_key != ?", "''");
        if (listof.size() > 0) {
            for (int i = 0; i < listof.size(); i++) {;
                jsonArray.put(listof.get(i).getJSON());
            }
        }
        return jsonArray;
    }

    // ------------------------------------------------ PRIVATE CLASSES

}
