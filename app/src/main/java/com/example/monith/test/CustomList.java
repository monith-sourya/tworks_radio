package com.example.monith.test;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomList extends ArrayAdapter<Float>{
    private final Activity context;
    private final ArrayList<Float> values;
    public CustomList(Activity context,
                      ArrayList<Float> values ) {
        super(context, R.layout.list_single, values);
        this.context = context;
        this.values = values;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        txtTitle.setText(values.get(position).toString());
        return rowView;
    }
}
