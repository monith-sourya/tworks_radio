package com.example.monith.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.google.gson.Gson;

public class SharedPreference {
    public static final String PREFS_NAME = "STATIONS_APP";
    public static final String STATIONS = "saved_stations";

    public SharedPreference() {
        super();
    }

    // This four methods are used for maintaining favorites.
    public void saveStations(Context context, List<Float> stations) {
        SharedPreferences settings;
        Editor editor;

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
