package com.mdb_software.helena.travelmemories3;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by Helena on 4/23/2015.
 */
public class Spinners {


    public static void setSpinner(Context context, Spinner spinner, String arrayName){
        int id = context.getResources().getIdentifier(arrayName, "array", context.getPackageName());
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                id, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }
}
