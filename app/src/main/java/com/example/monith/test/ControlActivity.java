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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;

public class ControlActivity extends AppCompatActivity {

    private static final String TAG = "ControlActivity";

    private BluetoothAdapter mBluetoothAdapter;

    private TextView logView;
    private ScrollView logScroll;

    private Button btnStartConnection;
    private Button btnSend;
    private EditText etSend;

    private boolean deviceFound;
    private static final String DEVICE_NAME = "HC-05";

    BluetoothConnectionService mBluetoothConnection;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        btnStartConnection = (Button) findViewById(R.id.btnStartConn);
        btnSend = (Button) findViewById(R.id.btnSend);

        etSend = (EditText) findViewById(R.id.etSend);

        logView = (TextView)findViewById(R.id.logTextControl);
        logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //startConnection();

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findDevice();

                if (deviceFound){
                    startConnection();

                    logView.append("Connection Started. \n");
                    scrollToBottom();
                }else{
//
//                    Context context = getApplicationContext();
//                    String text = "Device Not Found in Paired List.";
//                    Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
//                    toast.show();
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                String s = etSend.getText().toString();


                mBluetoothConnection.write(bytes);

                logView.append("Sending Message: "+ s +"\n");
                scrollToBottom();

                etSend.setText("");
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
    }
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");

            logView.append("Incoming Message: "+ text+"\n");
            scrollToBottom();
        }
    };

    public void startConnection(){
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }
    /**
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");


        mBluetoothConnection = new BluetoothConnectionService(ControlActivity.this);


        mBluetoothConnection.startClient(device,uuid);

        Log.d(TAG, "startBTConnection:RFCOM Bluetooth Connection.");
        Context context = getApplicationContext();
        String text = "Successfully Connected to Device.";
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");

        unregisterReceiver(mReceiver);

        super.onDestroy();
    }


    public void leftClick(View view){

        logView = (TextView)findViewById(R.id.logTextControl);
        logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);

        String x = "4";
        byte[] bytes = x.getBytes(Charset.defaultCharset());

        mBluetoothConnection.write(bytes);

        logView.append("Sending Message: "+ x +"\n");
        scrollToBottom();

    }

    public void rightClick(View view){

        logView = (TextView)findViewById(R.id.logTextControl);
        logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);

        String x = "3";
        byte[] bytes = x.getBytes(Charset.defaultCharset());

        mBluetoothConnection.write(bytes);
        logView.append("Sending Message: "+ x +"\n");
        scrollToBottom();

    }


//    /**
//     * This method is required for all devices running API23+
//     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
//     * in the manifest is not enough.
//     *
//     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
//     */
//    private void checkBTPermissions() {
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
//            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
//            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
//            if (permissionCheck != 0) {
//
//                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
//            }
//        }else{
//            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
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
