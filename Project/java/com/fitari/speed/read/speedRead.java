package com.fitari.speed.read;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tappx.sdk.android.TappxAdError;
import com.tappx.sdk.android.TappxInterstitial;
import com.tappx.sdk.android.TappxInterstitialListener;

/**
 *  Created by GerH on 16.03.2016.
 *
 // 0  200   slow (Normal reading for comprehension (around 200–230 wpm))
 // 1  300   unhurried*
 // 2  375   normal*
 // 3  450   fast (Auditory readers read at approximately 450 words per minute.)
 // 4  525   ambitious*
 // 5  600   talented*
 // 6  700   skimming (700 words per minute and above)
 // 7  1000  Top level*
 // 8  1500  Top contestant (s typically read around 1,000 to 2,000 words per minute)
 // 9  4700  World Champion (The world champion is Anne Jones with 4,700 words per minute)
 // 10 10000 Impossible (The 10,000 word/min claimants have yet to reach this level.)
 // X*w/m  w=m/X  w=60000/X
 */
public class speedRead extends AppCompatActivity {

    boolean speedreadingstopped =false;
    boolean speedChanged = false;

    TappxInterstitial tappxInterstitial;
    boolean hasAd = false;
    String readText = "";
    int textDynamicPos = 0; //for the bookmark

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.appcompat.app.ActionBar AB = getSupportActionBar();
        assert AB != null;
        AB.hide();
        setContentView(R.layout.speedread);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        RelativeLayout RL_fr = findViewById(R.id.RL_fr);
        RL_fr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedreadingstopped = !speedreadingstopped;
            }
        });

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                readText = null;
            } else {
                readText = extras.getString("TEXT_TO_READ");
            }
        } else {
            readText = (String) savedInstanceState.getSerializable("TEXT_TO_READ");
        }

        //load ad
        hasAd = (int)((Math.random()*2)+1) > 1; //1 in 2, 50:50
        if (hasAd){
            tappxInterstitial = new TappxInterstitial(speedRead.this, "pub-52851-android-9298");
            tappxInterstitial.loadAd();
        }

        //white mode
        final SharedPreferences settings = getSharedPreferences("enter", MODE_PRIVATE);
        boolean whiteMode = settings.getBoolean("white_mode", false);
        if(whiteMode) {
            TextView textFR = findViewById(R.id.textViewFR);
            ProgressBar progressBarFR = findViewById(R.id.progressBarFR);
            textFR.setTextColor(getResources().getColor(R.color.black));
            progressBarFR.setBackgroundColor(getResources().getColor(R.color.black));
            RL_fr.setBackgroundColor(getResources().getColor(R.color.white));
        }

        //start reading
        readText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
    }

    @Override
    protected void onDestroy() {

        //show ad if random val is true
        if (hasAd) {
            showAd();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //store bookmark if "always store bookmark" option is true
        final SharedPreferences settings = getSharedPreferences("enter", MODE_PRIVATE);
        boolean alwaysBookmark = settings.getBoolean("always_bookmark", false);
        if(alwaysBookmark)
            storeRemainingText();

        super.onBackPressed();
    }

    //Bookmark: get remaining text (which wasn't read yet)
    public void storeRemainingText(){

        final SharedPreferences settings = getSharedPreferences("enter", MODE_PRIVATE);

        String[] arrayArt = getTextArray();
        StringBuilder remainingTest= new StringBuilder();

        textDynamicPos=textDynamicPos-10;
        if(textDynamicPos<0)
            textDynamicPos=0;

        for (int i=textDynamicPos; i<arrayArt.length; i++) {
            remainingTest.append(arrayArt[i]).append(" ");
        }

        settings.edit().putString("remaining_text", remainingTest.toString()).apply();
        Toast.makeText(speedRead.this, R.string.text_bookmarked, Toast.LENGTH_LONG).show();
    }

    public String[] getTextArray(){
        return readText.replace("\n", " ").replace("\\", " \\ ").replace("-", " - ").replace(".", ". ").replace("/", " / ").split(" ");
    }

    public void readText(){

        final SharedPreferences settings = getSharedPreferences("enter", MODE_PRIVATE);
        final int WpM = settings.getInt("rs", 375);
        final int[] wordPause = {60000 / WpM};

        String[] arrayArt = {"An","error","has","occurred!"};
        if (readText != null) {
            arrayArt = getTextArray();
        }

        for (int textPosition=0; textPosition <arrayArt.length; textPosition++) {
            for(int index = 0; index < arrayArt[textPosition].length(); index++){
                char vowel = arrayArt[textPosition].charAt(index);
                if( (vowel == 'a') || (vowel == 'e') || (vowel == 'i') || (vowel == 'o') || (vowel == 'u')){
                    StringBuilder startEmpty= new StringBuilder();
                    StringBuilder endEmpty= new StringBuilder();
                    int req = arrayArt[textPosition].length() - 2*index;
                    if(req>0)
                        for(int a=0; a<req;a++)
                            startEmpty.append(" ");
                    if(req<0)
                        for(int a=0; a>req;a--)
                            endEmpty.append(" ");
                    arrayArt[textPosition] = startEmpty+arrayArt[textPosition]+endEmpty;
                    break;
                }
            }
        }
        final String[] finArrayArt=arrayArt;

        int totalSize=finArrayArt.length;
        final double per1percent= 100/(double)totalSize;

        Runnable runnableFR = new Runnable() {
            public void run() {

                editFRtext(getString(R.string.conc_on_dot), 0);
                try {Thread.sleep(5* wordPause[0]);} catch (InterruptedException e) {e.printStackTrace();}
                editFRtext(".", 0);
                try {Thread.sleep(6* wordPause[0]);} catch (InterruptedException e) {e.printStackTrace();}

                int i=0;
                for (String anArrayArt : finArrayArt) {
                    while(speedreadingstopped)
                        try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
                    try {Thread.sleep(wordPause[0]);} catch (InterruptedException e) {e.printStackTrace();}
                    editFRtext(anArrayArt, (int)(per1percent*i));
                    wordPause[0] = checkForChange(settings, wordPause[0]);
                    i++;
                    textDynamicPos=i;
                }
                try {Thread.sleep(500+ wordPause[0]);} catch (InterruptedException e) {e.printStackTrace();}

                //At the end: delete bookmark
                settings.edit().putString("remaining_text", "").apply();
                finish();
            }
        };
        Thread readstopthread = new Thread(runnableFR);
        readstopthread.start();
    }

    //Check live if reading speed changed
    public int checkForChange(SharedPreferences settings, int wordPause){
        if(speedChanged) {
            int WpM = settings.getInt("rs", 375);
            wordPause = 60000 / WpM;
        }
        return wordPause;
    }

    public void editFRtext(final String anArrayArt, final int progress) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView textFR = findViewById(R.id.textViewFR);
                ProgressBar progressBarFR = findViewById(R.id.progressBarFR);
                textFR.setText(anArrayArt);
                progressBarFR.setProgress(progress);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_speedread, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        final SharedPreferences settings = getSharedPreferences("enter", MODE_PRIVATE);
        int storeInt = settings.getInt("rs", 375);

        if (id == R.id.bookmark) {
            storeRemainingText();
        }
        if (id == R.id.speed_200)
            storeInt=200;
        if (id == R.id.speed_300)
            storeInt=300;
        if (id == R.id.speed_375)
            storeInt=375;
        if (id == R.id.speed_450)
            storeInt=450;
        if (id == R.id.speed_525)
            storeInt=525;
        if (id == R.id.speed_600)
            storeInt=600;
        if (id == R.id.speed_700)
            storeInt=700;
        if (id == R.id.speed_1000)
            storeInt=1000;
        if (id == R.id.speed_1500)
            storeInt=1500;
        if (id == R.id.speed_4700)
            storeInt=4700;
        if (id == R.id.speed_10000)
            storeInt=10000;

        speedChanged = true;
        settings.edit().putInt("rs", storeInt).apply();
        return true;
    }

    public void showAd() {
        if (tappxInterstitial != null) {
            tappxInterstitial.setListener(new TappxInterstitialListener() {
                @Override
                public void onInterstitialLoaded(TappxInterstitial tappxInterstitial) {
                }

                @Override
                public void onInterstitialLoadFailed(TappxInterstitial tappxInterstitial, TappxAdError tappxAdError) {
                    showOfflineAd();
                }

                @Override
                public void onInterstitialShown(TappxInterstitial tappxInterstitial) {
                }

                @Override
                public void onInterstitialClicked(TappxInterstitial tappxInterstitial) {
                }

                @Override
                public void onInterstitialDismissed(TappxInterstitial tappxInterstitial) {
                }
            });
            if (tappxInterstitial.isReady())
                tappxInterstitial.show();
            else
                showOfflineAd();
        } else
            showOfflineAd();
    }

    public void showOfflineAd() {
        Intent i = new Intent(getApplicationContext(), default_ad.class);
        startActivity(i);
    }
}