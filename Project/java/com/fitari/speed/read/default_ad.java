package com.fitari.speed.read;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * by GerH, 11.06.2019
 */
public class default_ad extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_ad);
        hideStuff();

        //make entire view clickable
        Button buttonInvisibleDefaultAd = findViewById(R.id.buttonInvisibleDefaultAd);
        buttonInvisibleDefaultAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uriUrl = Uri.parse(getString(R.string.ad_link_fitClock));
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
                finish();
            }
        });
    }
}

