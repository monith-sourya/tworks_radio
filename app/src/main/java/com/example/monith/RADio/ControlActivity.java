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

    private TextView logView;
    private ScrollView logScroll;

    private ButtonLoading buttonLoading;

    private Button btnStartConnection;
    private Button btnSend;
    private EditText etSend;

    private boolean deviceFound;
    private static final String DEVICE_NAME = "RADIO002";

    BluetoothConnectionService mBluetoothConnection;

    private boolean Connected;

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


    private HorizontalWheelView horizontalWheelView;
    private TextView tvAngle;

    public static final String PREFS_NAME = "RADIO_APP";
    public static final String STATIONS = "saved_stations";

    private ArrayList<Float> savedStations ;


    private GifDrawable gifDrawableRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

//        btnStartConnection = (Button) findViewById(R.id.btnStartConn);

        logView = (TextView)findViewById(R.id.logTextControl);
        logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //startConnection();



//        btnStartConnection.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                findDevice();
//
//                if (mBluetoothAdapter.getState()==BluetoothAdapter.STATE_ON) {
//                    if (deviceFound){
//                        startConnection();
//                    }else{
//
//                        Context context = getApplicationContext();
//                        String text = "Device Not Found in Paired List.";
//                        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
//                        toast.show();
//                    }
//                }else {
//                    Toast.makeText(getApplicationContext(),"Please Turn on Bluetooth", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        initController();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
        LocalBroadcastManager.getInstance(this).registerReceiver(connectionBroadcastReceiver,new IntentFilter("connectionMessage"));

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver1, filter);


        initViews();
        setupListeners();
        updateUi();
        //IMPORTANT
        //startConnection();

        buttonLoading = (ButtonLoading) findViewById(R.id.buttonLoading);
        buttonLoading.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
//                Toast.makeText(getApplicationContext(), "onClick", Toast.LENGTH_SHORT).show();
                findDevice();

                if (mBluetoothAdapter.getState()==BluetoothAdapter.STATE_ON) {
                    if (deviceFound){
                        startConnection();
                    }else{

                        Context context = getApplicationContext();
                        String text = "Device Not Found in Paired List.";
                        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                        toast.show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Please Turn on Bluetooth", Toast.LENGTH_SHORT).show();
                }
//                finishLoading();
            }

            @Override
            public void onStart() {
                //...
            }

            @Override
            public void onFinish() {
                //...
            }
        });

        final GifImageView gifImageView = (GifImageView) findViewById(R.id.refreshGIF);

        try {
            gifDrawableRefresh = new GifDrawable(getResources(), R.drawable.loading);
            gifImageView.setImageDrawable(gifDrawableRefresh);
        } catch (Resources.NotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        gifDrawableRefresh.stop();

        gifImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(gifDrawableRefresh.isRunning()) {
//                    gifDrawableRefresh.stop();
//                    gifDrawableRefresh.seekTo(0);
//                }else{
//                    gifDrawableRefresh.start();
//                }
                gifDrawableRefresh.start();
                refresh();
            }
        });
//
//        gifDrawableRefresh.stop();
//        gifDrawableRefresh.seekTo(0);
    }

    private void initController(){
        VolumeControlView controllerView = (VolumeControlView) findViewById(R.id.controllerView);


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
                    logView = (TextView)findViewById(R.id.logTextControl);
                    logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);

//                String x = ;
//                byte[] bytes = x.getBytes(Charset.defaultCharset());
                    percent/=2;

                    byte[] bytes = toByteArray(percent);

                    if(percent!=0) {
                        mBluetoothConnection.write(bytes);
                    }
                    logView.append("Sending Message: "+ percent +"\n");
                    scrollToBottom();
                } else {
                    Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();
                    buttonLoading.setVisibility(View.VISIBLE);
                }
            }
        });

    }
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");


            logView.append("Incoming Message: " + text);
//            scrollToBottom();

            float incoming = Float.parseFloat(text);
            logView.append("\n");
            scrollToBottom();

            TextView frequencyView = (TextView)findViewById(R.id.frequencyView);
            TextView volumeView = (TextView) findViewById(R.id.volumeView);

            if(incoming<=(float)50){
                logView.append("Volume "+incoming+"\n");
                scrollToBottom();

                gifDrawableRefresh.stop();
                volumeView.setText(String.format(Locale.US, "%.0f", incoming));
            }else if(incoming==(float)424.6){

                Connected = true;
                buttonLoading.setVisibility(View.GONE);
                logView.append("Connected.\n");
                scrollToBottom();
            }else{
                logView.append("Frequency: "+incoming+"\n");
                scrollToBottom();

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

    BroadcastReceiver connectionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("connection");

            logView.append("Intent Received.\n");
            scrollToBottom();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testConnection();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            refresh();
            finishLoading();
//            refresh();
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
            //

                Toast.makeText(getApplicationContext(),"Device Successfully Connected", Toast.LENGTH_SHORT).show();
//                Intent i = new Intent("finish");
//                sendBroadcast(i);

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Toast.makeText(getApplicationContext(), "Device Not Found!", Toast.LENGTH_LONG).show();
//
//                Intent i = new Intent(ControlActivity.this, MainActivity.class);
//                startActivity(i);
//                finish();
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

//        unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver1);
//        unregisterReceiver(connectionBroadcastReceiver);

        super.onDestroy();
    }


    public void leftClick(View view){
        if (Connected) {
            logView = (TextView)findViewById(R.id.logTextControl);
            logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);

            savedStations = getStations(getApplicationContext());
            Float current = Float.parseFloat(tvAngle.getText().toString());

            for(int i=(savedStations.size()-1);i>=0;i--){
                if(savedStations.get(i)<current){
                    StationChange(savedStations.get(i));
                    return;
                }
            }
            logView.append("Station Changed.\n");
            scrollToBottom();
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();
            buttonLoading.setVisibility(View.VISIBLE);
        }
        return;

    }

    public void rightClick(View view){

        if (Connected) {
            logView = (TextView)findViewById(R.id.logTextControl);
            logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);

            savedStations = getStations(getApplicationContext());
            Float current = Float.parseFloat(tvAngle.getText().toString());

            for(int i=0;i<savedStations.size();i++){
                if(savedStations.get(i)>current){
                    StationChange(savedStations.get(i));
                    return;
                }
            }
            logView.append("Station Changed.\n");
            scrollToBottom();
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();
            buttonLoading.setVisibility(View.VISIBLE);
        }
        return;

    }

    public void rightChange(){
        if (Connected) {
            logView = (TextView)findViewById(R.id.logTextControl);
            logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);

            savedStations = getStations(getApplicationContext());
            Float current = Float.parseFloat(tvAngle.getText().toString());

            for(int i=0;i<savedStations.size();i++){
                if(savedStations.get(i)>current){
                    StationChange(savedStations.get(i));
                    return;
                }
            }
            logView.append("Station Changed.\n");
            scrollToBottom();
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();
            buttonLoading.setVisibility(View.VISIBLE);
        }
        return;
    }

    public void savedStationsClick(View view){
        logView = (TextView)findViewById(R.id.logTextControl);
        logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);

        FragmentManager fm = getFragmentManager();
        StationsDialogFragment dialogFragment = new StationsDialogFragment();
        dialogFragment.show(fm, "Sample Fragment");

    }

    public void refresh(){

        testConnection();
        if (true) {
            logView = (TextView)findViewById(R.id.logTextControl);
            logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);
            int x = 105;
            byte[] bytes = toByteArray(x);

            mBluetoothConnection.write(bytes);

            logView.append("Sending Message: "+ x +"\n");
            scrollToBottom();

//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

//            gifDrawableRefresh.stop();
//            gifDrawableRefresh.seekTo(0);
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();

            buttonLoading.setVisibility(View.VISIBLE);
//            gifDrawableRefresh.stop();
//            gifDrawableRefresh.seekTo(0);
        }
    }
    public void refreshClick(View view){
        refresh();
    }

    public void saveClick(View view){
        if (Connected) {
            logView = (TextView)findViewById(R.id.logTextControl);
            logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);

            final Context context = getApplicationContext();
            String val = tvAngle.getText().toString();
            Float frequency = Float.parseFloat(val);

            addStation(context, frequency);

            Toast.makeText(getApplicationContext(), String.format(Locale.US, "%.01f Saved", frequency), Toast.LENGTH_SHORT).show();

            logView.append("Saved Station: "+ frequency +"\n");
            scrollToBottom();
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to Device.", Toast.LENGTH_LONG).show();
            buttonLoading.setVisibility(View.VISIBLE);
        }
    }

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

        logView = (TextView)findViewById(R.id.logTextControl);
        logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);
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

    private void updateUi() {
        updateText();
    }

    private void writedata(){


        logView = (TextView)findViewById(R.id.logTextControl);
        logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);

        float frequency = (float)horizontalWheelView.getCompleteTurnFraction();
        frequency *= (float)20.0;
        frequency += (float) 88.0;

        frequency*=10;

        int x = (int)frequency;

        byte[] bytes = toByteArray(x);

        mBluetoothConnection.write(bytes);

        logView.append("Sending Message: "+ x +"\n");
        scrollToBottom();

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

    public byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value};
    }

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

    public void addStation(Context context, Float value) {
        List<Float> stations = getStations(context);
        if (stations == null)
            stations = new ArrayList<Float>();
        stations.add(value);
        Collections.sort(stations);
        saveStations(context, stations);
    }

    public void removeStation(Context context, Float value) {
        ArrayList<Float> stations = getStations(context);
        if (stations != null) {
            stations.remove(value);
            Collections.sort(stations);
            saveStations(context, stations);
        }
    }

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

    @Override
    public void onStationClick(Float frequency) {

//        Toast.makeText(getApplicationContext(),"Activity received: "+frequency,Toast.LENGTH_LONG).show();

        StationChange(frequency);
    }

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

    public void testConnection(){
        logView = (TextView)findViewById(R.id.logTextControl);
        logScroll = (ScrollView) findViewById(R.id.ScrollPaneControl);

        Connected = false;
        int x = 106;
        byte[] bytes = toByteArray(x);

        mBluetoothConnection.write(bytes);

        logView.append("Sending Message: "+ x +"\n");
        scrollToBottom();
    }
    void finishLoading() {
        //call setProgress(false) after 5 second
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                buttonLoading.setProgress(false);
            }
        }, 1000);
    }
}
