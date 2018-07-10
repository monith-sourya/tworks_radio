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

    //Variables to Check if device is already paired before launching Control Activity
    private boolean deviceFound;
    private static final String DEVICE_NAME = "RADIO002";

    private GifDrawable gifDrawable;


    private BluetoothDevice mBTDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //GIF Code Begins

        //Initializing GIF
        final GifImageView gifImageView = (GifImageView) findViewById(R.id.gif);

        try {
            gifDrawable = new GifDrawable(getResources(), R.drawable.artificial_intelligence_product_rokid);
            gifImageView.setImageDrawable(gifDrawable);
        } catch (Resources.NotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Launch for Control Activity
        gifImageView.setOnClickListener(new View.OnClickListener() {
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
    }

    public void btnPairActivity(View view){
        Intent i = new Intent(this, PairActivity.class);
        startActivity(i);
    }

    public void btnAboutActivity(View view){
        Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }

    // Function to check if Device is already paired
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
