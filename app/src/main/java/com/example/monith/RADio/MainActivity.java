package com.example.monith.RADio;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import io.rmiri.buttonloading.ButtonLoading;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
//import com.agilie.volumecontrol.animation.controller.ControllerImpl;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BluetoothAdapter mBluetoothAdapter;

    private ButtonLoading buttonLoading;

    private boolean deviceFound;
    private static final String DEVICE_NAME = "RADIO001";


    private GifDrawable gifDrawable;

    public static final String PREFS_NAME = "STATIONS_APP";
    public static final String STATIONS = "saved_stations";

    BluetoothConnectionService mBluetoothConnection;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private BluetoothDevice mBTDevice;

    ListView list ;



    // Begin BT Functions
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {


            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON\n");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("finish")){
//                finish();
            }
        }
    };

    //Bluetooth Functions End

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        registerReceiver(broadcastReceiver, new IntentFilter("finish"));

    //Bluetooth Code Below

        //Button btnONOFF = (Button) findViewById(R.id.btnONOFF);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

//

        final GifImageView gifImageView = (GifImageView) findViewById(R.id.gif);

        try {
            gifDrawable = new GifDrawable(getResources(), R.drawable.artificial_intelligence_product_rokid);
            gifImageView.setImageDrawable(gifDrawable);
        } catch (Resources.NotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        gifImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(gifDrawable.isRunning()) {
//                    gifDrawable.stop();
//                }else{
//                    gifDrawable.start();
//                }
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
    }

  /*
     * starting service method
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
