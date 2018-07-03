package com.example.monith.test;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import android.view.MotionEvent;
import android.view.GestureDetector;
import android.support.v4.view.GestureDetectorCompat;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import com.example.monith.test.BluetoothConnectionService;
//import com.agilie.volumecontrol.animation.controller.ControllerImpl;

import com.example.monith.test.SharedPreference;
import com.example.monith.test.CustomList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
//
//    private TextView view ;
//    private GestureDetectorCompat gestureDetector;
    private BluetoothAdapter mBluetoothAdapter;

    private TextView logView;
    private ScrollView logScroll;

    private Button btnStartConnection;

    private boolean deviceFound;
    private static final String DEVICE_NAME = "RADIO001";


    public static final String PREFS_NAME = "STATIONS_APP";
    public static final String STATIONS = "saved_stations";

//    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
//    public DeviceListAdapter mDeviceListAdapter;
//    ListView lvNewDevices;

    BluetoothConnectionService mBluetoothConnection;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private BluetoothDevice mBTDevice;

    ListView list ;

    private void scrollToBottom()
    {
        logScroll.post(new Runnable()
        {
            public void run()
            {
                logScroll.smoothScrollTo(0, logView.getBottom());
            }
        });
    }



    // Begin BT Functions
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {


            logView = (TextView)findViewById(R.id.logText);
            logScroll = (ScrollView) findViewById(R.id.ScrollPane);


            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        logView.append("Bluetooth Off\n");
                        scrollToBottom();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        logView.append("Bluetooth Turning Off\n");
                        scrollToBottom();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON\n");
                        logView.append("Bluetooth On\n");
                        scrollToBottom();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        logView.append("Bluetooth Turning On\n");
                        scrollToBottom();
                        break;
                }
            }
        }
    };

//    /**
//     * Broadcast Receiver for listing devices that are not yet paired
//     * -Executed by btnDiscover() method.
//     */
//    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//
//            logView = (TextView)findViewById(R.id.logText);
//            logScroll = (ScrollView) findViewById(R.id.ScrollPane);
//
//            logView.append("onReceive: ACTION FOUND.\n");
//            scrollToBottom();
//
//            if (action.equals(BluetoothDevice.ACTION_FOUND)){
//                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
//                mBTDevices.add(device);
//                logView.append("onReceive: " + device.getName() + ": " + device.getAddress()+"\n");
//                scrollToBottom();
//                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
//                lvNewDevices.setAdapter(mDeviceListAdapter);
//            }
//        }
//    };
//
//    private BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//             final String action = intent.getAction();
//
//             logView = (TextView)findViewById(R.id.logText);
//             logScroll = (ScrollView) findViewById(R.id.ScrollPane);
//
//             if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
//                 BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//
//                 if(mDevice.getBondState()== BluetoothDevice.BOND_BONDED){
//                    logView.append("Device Bonded. \n");
//                    scrollToBottom();
//
//                    mBTDevice = mDevice;
//                 }
//                 if(mDevice.getBondState()==BluetoothDevice.BOND_BONDING){
//                     logView.append("Device bonding. \n");
//                     scrollToBottom();
//                 }
//                 if(mDevice.getBondState()== BluetoothDevice.BOND_NONE){
//                    logView.append("BOND_None. \n");
//                    scrollToBottom();
//                 }
//             }
//        }
//    };


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("finish")){
                finish();
            }
        }
    };

//    registerReceiver(broadcastReceiver, new IntentFilter("finish"));

    //    registerReceiver(broadcast_reciever, new IntentFilter("finish"));

//    public final void enableDisableBT(){
//        if(mBluetoothAdapter== null){
//            Log.d(TAG, "enableDisableBT: Does not have bluetooth capabilities");
//        }
//        if(!mBluetoothAdapter.enable()){
//            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivity(enableBTIntent);
//
//            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//            registerReceiver(mBroadcastReceiver1, BTIntent);
//        }
//        if(mBluetoothAdapter.enable()){
//            mBluetoothAdapter.disable();
//
//            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//            registerReceiver(mBroadcastReceiver1, BTIntent);
//        }
//    }

    //Bluetooth Functions End

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
//        mBTDevices = new ArrayList<>();
        btnStartConnection = (Button) findViewById(R.id.btnStartConn);
//        btnSend = (Button) findViewById(R.id.btnSend);
//
//        etSend = (EditText) findViewById(R.id.etSend);

        logView = (TextView) findViewById(R.id.logText);
        logScroll = (ScrollView) findViewById(R.id.ScrollPane);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


//        lvNewDevices.setOnItemClickListener(MainActivity.this);


        registerReceiver(broadcastReceiver, new IntentFilter("finish"));

//Bluetooth Code Below

        //Button btnONOFF = (Button) findViewById(R.id.btnONOFF);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

//
//        btnONOFF.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
////                enableDisableBT();
//            }
//        });


        //Broadcast receiver for bond state change.
//
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        registerReceiver(mBroadcastReceiver4, filter);

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    findDevice();
                    if (deviceFound) {
                        //startConnection();
                        Intent i = new Intent(MainActivity.this, ControlActivity.class);
                        startActivity(i);

                    } else {

                        Context context = getApplicationContext();
                        String text = "Device Not Found in Paired List.";
                        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                        toast.show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please Turn on Bluetooth", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView right;

        right = (ImageView) findViewById(R.id.rightArrowImage);

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logView.append("Clicked");
                scrollToBottom();
            }
        });
    }
//
//    public void startConnection(){
//        startBTConnection(mBTDevice, MY_UUID_INSECURE);
//    }
  /*
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");


        mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);

        
        mBluetoothConnection.startClient(device,uuid);

        Log.d(TAG, "startBTConnection:RFCOM Bluetooth Connection.");
        Context context = getApplicationContext();
        String text = "Successfully Connected to Device.";
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");

        unregisterReceiver(mBroadcastReceiver1);
//        unregisterReceiver(mBroadcastReceiver3);
//        unregisterReceiver(mBroadcastReceiver4);
        unregisterReceiver(broadcastReceiver);

//        mBluetoothAdapter.cancelDiscovery();
        super.onDestroy();
    }

//    public void btnDiscover(View view) {
//
//
//        logView = (TextView)findViewById(R.id.logText);
//        logScroll = (ScrollView) findViewById(R.id.ScrollPane);
//
//        if(mBluetoothAdapter.getState()!=BluetoothAdapter.STATE_ON){
//            logView.append("Please Turn on Bluetooth.\n");
//            scrollToBottom();
//        }else {
//            logView.append("btnDiscover: Looking for unpaired devices.\n");
//            scrollToBottom();
//
//            if (mBluetoothAdapter.isDiscovering()) {
//                mBluetoothAdapter.cancelDiscovery();
//                logView.append("btnDiscover: Canceling discovery.\n");
//                scrollToBottom();
//
//                //check BT permissions in manifest
//                checkBTPermissions();
//
//                mBluetoothAdapter.startDiscovery();
//                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
//                logView.append("Starting Discovery again.\n");
//                scrollToBottom();
//            }
//            if (!mBluetoothAdapter.isDiscovering()) {
//
//                //check BT permissions in manifest
//                checkBTPermissions();
//
//                mBluetoothAdapter.startDiscovery();
//                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
//            }
//        }
//    }
//
//    public void btnCancelDiscover(View view){
//
//        logView = (TextView)findViewById(R.id.logText);
//        logScroll = (ScrollView) findViewById(R.id.ScrollPane);
//
//        if(mBluetoothAdapter.isDiscovering()){
//            mBluetoothAdapter.cancelDiscovery();
//            logView.append("Cancelled Discovery");
//            scrollToBottom();
//        }
//    }

    public void btnPairActivity(View view){
        Intent i = new Intent(this, PairActivity.class);
        startActivity(i);
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//
//        logView = (TextView)findViewById(R.id.logText);
//        logScroll = (ScrollView) findViewById(R.id.ScrollPane);
//
//        mBluetoothAdapter.cancelDiscovery();
//        String deviceName = mBTDevices.get(position).getName();
//        String deviceAddress = mBTDevices.get(position).getAddress();
//
//        logView.append("Device Selected. \n");
//        scrollToBottom();
//        logView.append("Device Name: "+ deviceName+"\n");
//        scrollToBottom();
//        logView.append("Device Address: " + deviceAddress+ "\n");
//        scrollToBottom();
//
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
//            logView.append("Trying to Pair with Device: "+deviceName+"\n");
//            scrollToBottom();
//            mBTDevices.get(position).createBond();
//
//            mBTDevice = mBTDevices.get(position);
//
//            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
//
//        }
//    }

    private void findDevice() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        deviceFound = false;

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(DEVICE_NAME)) {
                    mBTDevice = device;
                    deviceFound = true;
                    break;
                }
            }
        }
        if(!deviceFound){
            Context context = getApplicationContext();
            String text = "Please Pair Device First.";
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.show();
        }

    }


}
