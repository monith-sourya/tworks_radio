package com.example.monith.RADio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class PairActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "PairActivity";


    private BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    private TextView logView;
    private ScrollView logScroll;

    private BluetoothDevice mBTDevice;
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

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            logView = (TextView)findViewById(R.id.logText);
            logScroll = (ScrollView) findViewById(R.id.ScrollPane);

            logView.append("onReceive: ACTION FOUND.\n");
            scrollToBottom();

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                logView.append("onReceive: " + device.getName() + ": " + device.getAddress()+"\n");
                scrollToBottom();
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            logView = (TextView)findViewById(R.id.logText);
            logScroll = (ScrollView) findViewById(R.id.ScrollPane);

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(mDevice.getBondState()== BluetoothDevice.BOND_BONDED){
                    logView.append("Device Bonded. \n");
                    scrollToBottom();

                    mBTDevice = mDevice;
                }
                if(mDevice.getBondState()==BluetoothDevice.BOND_BONDING){
                    logView.append("Device bonding. \n");
                    scrollToBottom();
                }
                if(mDevice.getBondState()== BluetoothDevice.BOND_NONE){
                    logView.append("BOND_None. \n");
                    scrollToBottom();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        logView = (TextView)findViewById(R.id.logText);
        logScroll = (ScrollView) findViewById(R.id.ScrollPane);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Intent i = new Intent(PairActivity.this, MainActivity.class);
//                startActivity(i);
                finish();
            }
        });
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        lvNewDevices.setOnItemClickListener(PairActivity.this);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        Discover();
    }

    public void Discover(){
        logView = (TextView)findViewById(R.id.logText);
        logScroll = (ScrollView) findViewById(R.id.ScrollPane);

        if(mBluetoothAdapter.getState()!=BluetoothAdapter.STATE_ON){
            logView.append("Please Turn on Bluetooth.\n");
            scrollToBottom();
        }else {
            logView.append("btnDiscover: Looking for unpaired devices.\n");
            scrollToBottom();

            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
                logView.append("btnDiscover: Canceling discovery.\n");
                scrollToBottom();

                //check BT permissions in manifest
                checkBTPermissions();

                mBluetoothAdapter.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                logView.append("Starting Discovery again.\n");
                scrollToBottom();
            }
            if (!mBluetoothAdapter.isDiscovering()) {

                //check BT permissions in manifest
                checkBTPermissions();

                mBluetoothAdapter.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
            }
        }
    }
    public void btnDiscover(View view) {

        Discover();
    }

    public void btnCancelDiscover(View view){

        logView = (TextView)findViewById(R.id.logText);
        logScroll = (ScrollView) findViewById(R.id.ScrollPane);

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            logView.append("Cancelled Discovery");
            scrollToBottom();
        }
    }
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        logView = (TextView)findViewById(R.id.logText);
        logScroll = (ScrollView) findViewById(R.id.ScrollPane);

        mBluetoothAdapter.cancelDiscovery();
        String deviceName = mBTDevices.get(position).getName();
        String deviceAddress = mBTDevices.get(position).getAddress();

        logView.append("Device Selected. \n");
        scrollToBottom();
        logView.append("Device Name: "+ deviceName+"\n");
        scrollToBottom();
        logView.append("Device Address: " + deviceAddress+ "\n");
        scrollToBottom();

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            logView.append("Trying to Pair with Device: "+deviceName+"\n");
            scrollToBottom();
            mBTDevices.get(position).createBond();

            mBTDevice = mBTDevices.get(position);

            //mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);

        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");

        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);

        mBluetoothAdapter.cancelDiscovery();
        super.onDestroy();
    }
}
