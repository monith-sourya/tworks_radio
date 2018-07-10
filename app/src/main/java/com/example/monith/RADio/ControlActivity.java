package com.example.monith.RADio;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.agilie.volumecontrol.animation.controller.ControllerImpl;
//import com.agilie.volomecontrolview.R.id.controllerView;
import com.agilie.volumecontrol.view.VolumeControlView;
//import com.agilie.controller.R.id.value
import com.github.shchurov.horizontalwheelview.HorizontalWheelView;
import com.google.gson.Gson;

import java.util.Locale;

import io.rmiri.buttonloading.ButtonLoading;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


public class ControlActivity extends AppCompatActivity implements StationsDialogFragment.StationSelectedListener {

    private static final String TAG = "ControlActivity";

    private BluetoothAdapter mBluetoothAdapter;

    //Start Connection Button
    private ButtonLoading buttonLoading;

    private VolumeControlView controllerView ;

    private boolean deviceFound;
    private static final String DEVICE_NAME = "RADIO002";

    BluetoothConnectionService mBluetoothConnection;

    private boolean Connected = false;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice mBTDevice;

    // For Frequency Selection
    private HorizontalWheelView horizontalWheelView;
    private TextView tvAngle;

    public static final String PREFS_NAME = "RADIO_APP";
    public static final String STATIONS = "saved_stations";

    // For Stations Dialog Fragment
    private ArrayList<Float> savedStations ;

    //Refresh Button
    private GifDrawable gifDrawableRefresh;


    @Override
    protected void onResume() {
        super.onResume();

        if(Connected) {
            testConnection();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // utility for Volume Control View
        initController();

        // Broadcast Receiver for Incoming Messages. -> From BluetoothConnectionService
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        // Broadcast receiver for Successfull Connection. From BluetoothConnectionService
        LocalBroadcastManager.getInstance(this).registerReceiver(connectionBroadcastReceiver,new IntentFilter("connectionMessage"));

        // For Connection State Change
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver1, filter);

        // Utility Functions for Horizontal Wheel View
        initViews();
        setupListeners();
        updateUi();

        // Start Connection
        buttonLoading = (ButtonLoading) findViewById(R.id.buttonLoading);
        buttonLoading.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
                findDevice();

                if (mBluetoothAdapter.getState()==BluetoothAdapter.STATE_ON) {
                    if (deviceFound){
                        startConnection();

                    }else{
                        Toast.makeText(getApplicationContext(), "Device Not Found in Paired List.", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Please Turn on Bluetooth", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStart() {
                //...
            }

            @Override
            public void onFinish() {
                if(Connected) {
                    buttonLoading.setVisibility(View.GONE);
                }else{
                    Toast.makeText(getApplicationContext(), "Can't Find Device.", Toast.LENGTH_LONG).show();
                }
            }
        });



        // Gif/Button for Refreshing
        final GifImageView gifImageView = (GifImageView) findViewById(R.id.refreshGIF);

        try {
            gifDrawableRefresh = new GifDrawable(getResources(), R.drawable.loading);
            gifImageView.setImageDrawable(gifDrawableRefresh);
        } catch (Resources.NotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Initializing to still image
        gifDrawableRefresh.stop();

        // Starts Rotation upon click and calls refresh method
        gifImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gifDrawableRefresh.start();
                refresh();
            }
        });

    }

    // Utility function for Initializing Controllers for Volume Control
    private void initController(){

        controllerView = (VolumeControlView) findViewById(R.id.controllerView);


        controllerView.getController().setOnTouchControllerListener( new ControllerImpl.OnTouchControllerListener(){

            @Override
            public void onControllerDown(int angle, int percent){
            }

            @Override
            public void onControllerMove(int angle, int percent){
            }

            @Override
            public void onAngleChange(int angle, int percent){
                if (Connected) {
                    percent/=2;

                    byte[] bytes = toByteArray(percent);

                    // Writes Volume to Bluetooth
                    if(percent!=0) {
                        mBluetoothConnection.write(bytes);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();
                    buttonLoading.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //Broadcast Receiver for Incoming Bluetooth Messages. -> From BluetoothConnectionService
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");

            float incoming = Float.parseFloat(text);

            TextView frequencyView = (TextView)findViewById(R.id.frequencyView);
            TextView volumeView = (TextView) findViewById(R.id.volumeView);

            // For Volume (less than 50)
            if(incoming<=(float)50){
                gifDrawableRefresh.stop();
                volumeView.setText(String.format(Locale.US, "%.0f", incoming));
            }

            // Connection Tester.
            else if(incoming==(float)424.6){

                Connected = true;
                firstRefresh();
            }

            // For Frequency
            else{
                frequencyView.setText(String.format(Locale.US, "%.01f", incoming));

                double fraction = (double)incoming;
                fraction-=88;
                fraction/=20;

                horizontalWheelView.setCompleteTurnFraction(fraction);
                updateUi();
                gifDrawableRefresh.stop();
            }
        }
    };

    // Receiver for first Connection confirmation.
    BroadcastReceiver connectionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("connection");

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testConnection();
            finishLoading();
        }
    };

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            //Device found

                Toast.makeText(getApplicationContext(),"Device Found!", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

                Toast.makeText(getApplicationContext(),"Device Successfully Connected", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Toast.makeText(getApplicationContext(), "Device Not Found!", Toast.LENGTH_LONG).show();
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            //Device is about to disconnect
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            //Device has disconnected
            }
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
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        unregisterReceiver(mReceiver1);
        super.onDestroy();
    }



    //Checks if Device is in Paired List before Attempting to start Connection
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

    private void initViews() {
        horizontalWheelView = (HorizontalWheelView) findViewById(R.id.horizontalWheelView);
        tvAngle = (TextView) findViewById(R.id.tvAngle);

        horizontalWheelView.setOnlyPositiveValues(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupListeners() {
        horizontalWheelView.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {

                if (Connected) {
                    updateUi();
//                writedata();
                } else {
                }
            }

            //            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_UP) {
//                    // release
//                    logView = (TextView)findViewById(R.id.logTextControl);
//                    logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);
//
//                    float frequency = (float)horizontalWheelView.getCompleteTurnFraction();
//                    frequency *= (float)20.0;
//                    frequency += (float) 88.0;
//                    DecimalFormat value = new DecimalFormat("#.#");
//                    value.format(frequency);
//
//                    frequency*=10;
//
//                    int x = (int)frequency;
//
//                    byte[] bytes = toByteArray(x);
//
//                    //mBluetoothConnection.write(bytes);
//
//                    logView.append("Sending Message: "+ x +"\n");
//                    scrollToBottom();
//                    return false;
//                } else if(event.getAction() == MotionEvent.ACTION_DOWN) {
//                    // pressed
//
//                    return true;
//                }
//                //return super.onTouchEvent(event);
//                return false;
//            }
        });

        horizontalWheelView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    // release
                    if (Connected) {
//                        writedata();

                        float frequency = Float.parseFloat(tvAngle.getText().toString());
                        StationChange(frequency);
                    } else {
                        Toast.makeText(getApplicationContext(), "Please connect to device.", Toast.LENGTH_LONG).show();
                        buttonLoading.setVisibility(View.VISIBLE);
                    }
                    return false;
                } else if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    // pressed

                    return false;
                }
                //return super.onTouchEvent(event);
                return false;
            }
        });
    }

    // Utilities for Horizontal Wheel View
    private void updateUi() {
        updateText();
    }

    private void writedata(){

        float frequency = (float)horizontalWheelView.getCompleteTurnFraction();
        frequency *= (float)20.0;
        frequency += (float) 88.0;

        frequency*=10;

        int x = (int)frequency;

        byte[] bytes = toByteArray(x);

        mBluetoothConnection.write(bytes);

    }

    private void updateText() {
        float frequency = (float)horizontalWheelView.getCompleteTurnFraction();
        frequency *= (float)20.0;
        frequency += (float) 88.0;
        DecimalFormat value = new DecimalFormat("#.#");
        value.format(frequency);

        String text = String.format(Locale.US, "%.01f", frequency);
        tvAngle.setText(text);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateUi();
    }
    // End utilities

    // Searches for the next Lower Station from saved Stations list.
    public void leftClick(View view){
        if (Connected) {
            savedStations = getStations(getApplicationContext());
            Float current = Float.parseFloat(tvAngle.getText().toString());

            if(savedStations.get(0).equals(current)){
                Toast.makeText(getApplicationContext(), "This is the first station.", Toast.LENGTH_SHORT).show();
                return;
            }
            for(int i=(savedStations.size()-1);i>=0;i--){
                if(savedStations.get(i)<current){
                    StationChange(savedStations.get(i));
                    return;
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();
            buttonLoading.setVisibility(View.VISIBLE);
        }
    }

    // Searches for the next Greater Station from saved Stations list.

    public void rightClick(View view){

        if (Connected) {

            savedStations = getStations(getApplicationContext());
            Float current = Float.parseFloat(tvAngle.getText().toString());

            if(savedStations.get(savedStations.size()-1).equals(current)){
                Toast.makeText(getApplicationContext(), "This is the last station.", Toast.LENGTH_SHORT).show();
                return;
            }
            for(int i=0;i<savedStations.size();i++){
                if(savedStations.get(i)>current){
                    StationChange(savedStations.get(i));
                    return;
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();
            buttonLoading.setVisibility(View.VISIBLE);
        }
        return;

    }

    // Opens Stations Dialog Fragment
    public void savedStationsClick(View view){
        FragmentManager fm = getFragmentManager();
        StationsDialogFragment dialogFragment = new StationsDialogFragment();
        dialogFragment.show(fm, "Sample Fragment");

    }

    // Method refreshes Current Frequency and Volume values
    public void refresh(){

        if (Connected) {
            int x = 105;
            byte[] bytes = toByteArray(x);

            mBluetoothConnection.write(bytes);
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();

            buttonLoading.setVisibility(View.VISIBLE);
        }
        finishRefresh();
        return;
    }
    public void firstRefresh(){
        if (true) {
            int x = 105;
            byte[] bytes = toByteArray(x);

            mBluetoothConnection.write(bytes);

        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();

            buttonLoading.setVisibility(View.VISIBLE);
        }
    }


    // Saves Current frequency value to the Saved Stations List
    public void saveClick(View view){
        if (Connected) {
            final Context context = getApplicationContext();
            String val = tvAngle.getText().toString();
            Float frequency = Float.parseFloat(val);

            addStation(context, frequency);

            Toast.makeText(getApplicationContext(), String.format(Locale.US, "%.01f Saved", frequency), Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();
            buttonLoading.setVisibility(View.VISIBLE);
        }
    }

    // Aux Func
    public byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value};
    }

    //Aux Func
    public static byte[] toByteArray(float value) {
        byte[] bytes = new byte[4];
        ByteBuffer.wrap(bytes).putFloat(value);
        return bytes;
    }
    public void saveStations(Context context, List<Float> stations) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(stations);

        editor.putString(STATIONS, jsonFavorites);

        editor.apply();
    }

    // Adds Float Value to Shared Preferences using GSON objects.
    public void addStation(Context context, Float value) {
        List<Float> stations = getStations(context);
        if (stations == null)
            stations = new ArrayList<Float>();

        boolean found = false;

            for(int i=0; i< stations.size();i++){
                if (stations.get(i).equals(value))
                    found = true;
            }
            if (found) {
                Toast.makeText(getApplicationContext(), "Station Already Saved.", Toast.LENGTH_SHORT).show();
            } else {
                stations.add(value);
                Collections.sort(stations);
                saveStations(context, stations);
            }
    }

    // Gets Arraylist of saved float values from Shared Preferences using GSON objects.
    public ArrayList<Float> getStations(Context context) {
        SharedPreferences settings;
        List<Float> stations;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(STATIONS)) {
            String jsonFavorites = settings.getString(STATIONS, null);
            Gson gson = new Gson();
            Float[] favoriteItems = gson.fromJson(jsonFavorites,
                    Float[].class);

            stations = Arrays.asList(favoriteItems);
            stations = new ArrayList<Float>(stations);
        } else
            return null;

        return (ArrayList<Float>) stations;
    }


    // When a Station is selected in saved stations list.
    @Override
    public void onStationClick(Float frequency) {
        StationChange(frequency);
    }

    // Changes Station to given value.
    public void StationChange(Float frequency){

        if (Connected) {
            double fraction = (double)frequency;
            fraction-=88;
            fraction/=20;

            horizontalWheelView.setCompleteTurnFraction(fraction);
            writedata();
            updateUi();
            Toast.makeText(getApplicationContext(),"Station Changed to: "+frequency,Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();
            buttonLoading.setVisibility(View.VISIBLE);
        }
    }

    // Aux for buttonloading
    void finishLoading() {
        //call setProgress(false) after 5 second
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                buttonLoading.setProgress(false);
            }
        }, 500);

    }


    // Listener for lack of response from device upon test
    public void testConnection(){
        Connected = false;
        int x = 106;
        byte[] bytes = toByteArray(x);

        mBluetoothConnection.write(bytes);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!Connected) {
                    Toast.makeText(getApplicationContext(),"Device is not Connected", Toast.LENGTH_SHORT).show();

                    buttonLoading.setVisibility(View.VISIBLE);
                }
            }
        }, 1000);

    }


    // Listener for a lack of response from Device on refresh.
    void finishRefresh(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(gifDrawableRefresh.isRunning()){
                    Connected = false;

                    gifDrawableRefresh.stop();

                    Toast.makeText(getApplicationContext(),"Please Reconnect to Device.", Toast.LENGTH_SHORT).show();

                    buttonLoading.setVisibility(View.VISIBLE);
                }
            }
        }, 3000);
    }
}
