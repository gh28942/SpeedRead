package com.fitari.speed.read;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        hideStuff();

        //set up spinner
        Spinner spinner = findViewById(R.id.spinnerVelocity);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.speeds, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //get initial speed
        final SharedPreferences settings = getSharedPreferences("enter", MODE_PRIVATE);
        int storeInt = settings.getInt("rs", 375);
        final int[] speeds = {200, 300, 375, 450, 525, 600, 700, 1000, 1500, 4700, 10000};
        int positionOfSavedVal = 2;
        for (int i = 0; i < speeds.length; i++)
            if (speeds[i] == storeInt)
                positionOfSavedVal = i;
        spinner.setSelection(positionOfSavedVal);

        //store speed value once the user picks one
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView adapterView, View view, int i, long l) {
                settings.edit().putInt("rs", speeds[adapterView.getSelectedItemPosition()]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView adapterView) {
            }
        });

        //rate button
        Button buttonRate = findViewById(R.id.buttonRate);
        buttonRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inURL = getString(R.string.rate_app_link);
                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
                startActivity(browse);
            }
        });

        //handle white mode
        final Switch switchTextColor = findViewById(R.id.switchTextColor);
        boolean whiteMode = settings.getBoolean("white_mode", false);
        if(whiteMode)
            switchTextColor.setChecked(true);
        switchTextColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.edit().putBoolean("white_mode", switchTextColor.isChecked()).apply();
            }
        });

        //handle always bookmarking option
        final Switch switchAlwaysBookmark = findViewById(R.id.switchAlwaysBookmark);
        boolean alwaysBookmark = settings.getBoolean("always_bookmark", false);
        if(alwaysBookmark)
            switchAlwaysBookmark.setChecked(true);
        switchAlwaysBookmark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.edit().putBoolean("always_bookmark", switchAlwaysBookmark.isChecked()).apply();
            }
        });
    }

    public void hideStuff(){
        ActionBar AB = getSupportActionBar();
        android.app.ActionBar AB11 = getActionBar();
        if (AB != null) {
            AB.hide();
        }
        if (AB11 != null) {
            AB11.hide();
        }
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
