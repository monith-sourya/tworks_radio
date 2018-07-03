package com.example.monith.test;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

public class StationsDialogFragment extends DialogFragment {


    public static final String PREFS_NAME = "STATIONS_APP";
    public static final String STATIONS = "saved_stations";

    ListView list ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog, container, false);
        getDialog().setTitle("Simple Dialog");

        final Context context = getActivity();

        addStation(context, (float)100.00);

        final ArrayList<Float> values = getStations(context);

        final CustomList listAdapter = new
                CustomList(getActivity(), values);
        list=(ListView)rootView.findViewById(R.id.list);
        list.setAdapter(listAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getActivity(), String.format("You Clicked at %s", values.get(position)), Toast.LENGTH_SHORT).show();

            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                removeStation(context,values.get(position));

                Toast.makeText(getActivity(), String.format("%s Deleted", values.get(position)), Toast.LENGTH_SHORT).show();

                dismiss();
                return false;
            }
        });


        Button dismiss = (Button) rootView.findViewById(R.id.dismiss);
        dismiss.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return rootView;
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
        saveStations(context, stations);
    }

    public void removeStation(Context context, Float value) {
        ArrayList<Float> stations = getStations(context);
        if (stations != null) {
            stations.remove(value);
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
}
