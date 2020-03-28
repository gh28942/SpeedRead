package com.fitari.speed.read;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tappx.sdk.android.Tappx;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    boolean isFABOpen = false;
    FloatingActionButton fab1;
    FloatingActionButton fab2;
    FloatingActionButton fab3;
    private GoEditText editText1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonRead = findViewById(R.id.buttonRead);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedRead();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);
        fab3 = findViewById(R.id.fab3);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
            }
        });

        // Load file
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("*/*");
                String[] mimetypes = {/*"application/pdf",*/ "text/plain"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_text_file)), 4628);
            }
        });

        // Settings
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Settings.class);
                startActivity(i);
            }
        });

        // Share App
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_string));
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });


        //get user decision (TOS & privacy)
        final SharedPreferences settings = getSharedPreferences("enter", MODE_PRIVATE);
        boolean userAgreed = settings.getBoolean("user_agreed", false);

        //The user HAS to read & agree to the TOS and privacy agreement.
        if (!userAgreed) { //if the user didn't agree yet, show message box
            new AlertDialog.Builder(this)
                    .setTitle(R.string.tos_title)
                    .setMessage(getString(R.string.tos_text) +
                            "\n\nbit.ly/speed-tos \n\nbit.ly/speed-priv")
                    .setCancelable(false)

                    .setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // if user agrees
                            settings.edit().putBoolean("user_agreed", true).apply();
                            Toast.makeText(MainActivity.this, R.string.thanks, Toast.LENGTH_LONG).show();
                            showTutorial();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // user disagrees
                            finish();
                            System.exit(0);
                        }
                    })
                    .show();

        }

        //clear text button
        ImageView buttonClear = findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EditText)findViewById(R.id.editTextToRead)).setText("");
            }
        });

        //Add bookmark text (if available)
        String remaining_text = settings.getString("remaining_text", "");
        if(!remaining_text.equals("")) {
            ((EditText) findViewById(R.id.editTextToRead)).setText(remaining_text);
            Toast.makeText(this, R.string.bookmark_loaded,Toast.LENGTH_LONG).show();
        }

        //Check how many times the app was opened, give user URL example
        int timesOpened = settings.getInt("times_opened", 0);
        timesOpened++;
        settings.edit().putInt("times_opened", timesOpened).apply();
        if(timesOpened == 3){
            ((EditText)findViewById(R.id.editTextToRead)).setText(R.string.link_funWiki);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.url_example)
                    .setMessage(R.string.url_example_text)
                    .setPositiveButton("okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
        //show user info (pausing text while reading, bookmark)
        else if(timesOpened == 5)
            showInfo(R.string.info_pause_text);
        else if(timesOpened == 7)
            showInfo(R.string.info_bookmark_text);
        //regularly ask user to rate the app if he hasn't done so already
        boolean showRatingPlea = timesOpened>11 && ((timesOpened-12)%5)==0; //after 12 app openings, show plea every 5th time
        boolean userHasRated = settings.getBoolean("user_has_rated", false);
        if(showRatingPlea && !userHasRated)
            new AlertDialog.Builder(this)
                    .setTitle(R.string.rate_app_title)
                    .setMessage(R.string.rate_app_text)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String inURL = getString(R.string.rate_app_link);
                            Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse(inURL) );
                            startActivity(browse);
                            settings.edit().putBoolean("user_has_rated", true).apply();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();

        //Copy-Paste listener, show to the user the time it takes to read the text.
        editText1 = findViewById(R.id.editTextToRead);
        editText1.addListener(new GoEditTextListener() {
            @Override
            public void onUpdate() {
                //if it isn't an URL, show the word count.
                String content = String.valueOf(editText1.getText());
                if (!URLUtil.isValidUrl(content)) {
                    int textLength = content.split(" ").length;
                    int readSpeed = settings.getInt("rs", 375);
                    int minutes = (textLength / readSpeed);
                    Toast.makeText(MainActivity.this, getString(R.string.copied_1) + " " + textLength + " " + getString(R.string.copied_2) + " " + minutes + " " + getString(R.string.copied_3), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void showInfo(int text){
        new AlertDialog.Builder(this)
                .setTitle(String.valueOf(R.string.info_pause_title))
                .setMessage(String.valueOf(text))
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public void showTutorial(){
        showFABMenu();

        TapTargetSequence mTapTargetSequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(findViewById(R.id.editTextToRead), getString(R.string.tutorial_1))
                                .outerCircleColor(R.color.blue).outerCircleAlpha(0.96f).targetCircleColor(R.color.white).titleTextSize(20).titleTextColor(R.color.white).descriptionTextSize(10).descriptionTextColor(R.color.white).textColor(R.color.white).textTypeface(Typeface.SANS_SERIF).dimColor(R.color.black).drawShadow(true).cancelable(false).tintTarget(true).transparentTarget(true).targetRadius(60),
                        TapTarget.forView(findViewById(R.id.editTextToRead), getString(R.string.tutorial_2))
                                .outerCircleColor(R.color.blue).outerCircleAlpha(0.96f).targetCircleColor(R.color.white).titleTextSize(20).titleTextColor(R.color.white).descriptionTextSize(10).descriptionTextColor(R.color.white).textColor(R.color.white).textTypeface(Typeface.SANS_SERIF).dimColor(R.color.black).drawShadow(true).cancelable(false).tintTarget(true).transparentTarget(true).targetRadius(60),
                        TapTarget.forView(findViewById(R.id.buttonRead), getString(R.string.tutorial_3),getString(R.string.tutorial_3b))
                                .outerCircleColor(R.color.blue).outerCircleAlpha(0.96f).targetCircleColor(R.color.white).titleTextSize(20).titleTextColor(R.color.white).descriptionTextSize(10).descriptionTextColor(R.color.white).textColor(R.color.white).textTypeface(Typeface.SANS_SERIF).dimColor(R.color.black).drawShadow(true).cancelable(false).tintTarget(true).transparentTarget(true).targetRadius(60),
                        TapTarget.forView(findViewById(R.id.fab1), getString(R.string.tutorial_4), getString(R.string.tutorial_4b))
                                .outerCircleColor(R.color.blue).outerCircleAlpha(0.96f).targetCircleColor(R.color.white).titleTextSize(20).titleTextColor(R.color.white).descriptionTextSize(10).descriptionTextColor(R.color.white).textColor(R.color.white).textTypeface(Typeface.SANS_SERIF).dimColor(R.color.black).drawShadow(true).cancelable(false).tintTarget(true).transparentTarget(true).targetRadius(60),
                        TapTarget.forView(findViewById(R.id.fab2), getString(R.string.tutorial_5), getString(R.string.tutorial_5b))
                                .outerCircleColor(R.color.blue).outerCircleAlpha(0.96f).targetCircleColor(R.color.white).titleTextSize(20).titleTextColor(R.color.white).descriptionTextSize(10).descriptionTextColor(R.color.white).textColor(R.color.white).textTypeface(Typeface.SANS_SERIF).dimColor(R.color.black).drawShadow(true).cancelable(false).tintTarget(true).transparentTarget(true).targetRadius(60),
                        TapTarget.forView(findViewById(R.id.fab3), getString(R.string.tutorial_6), getString(R.string.tutorial_6b))
                                .outerCircleColor(R.color.blue).outerCircleAlpha(0.96f).targetCircleColor(R.color.white).titleTextSize(20).titleTextColor(R.color.white).descriptionTextSize(10).descriptionTextColor(R.color.white).textColor(R.color.white).textTypeface(Typeface.SANS_SERIF).dimColor(R.color.black).drawShadow(true).cancelable(false).tintTarget(true).transparentTarget(true).targetRadius(60),
                        TapTarget.forView(findViewById(R.id.buttonClear), getString(R.string.tutorial_7), getString(R.string.tutorial_7b))
                                .outerCircleColor(R.color.blue).outerCircleAlpha(0.96f).targetCircleColor(R.color.white).titleTextSize(20).titleTextColor(R.color.white).descriptionTextSize(10).descriptionTextColor(R.color.white).textColor(R.color.white).textTypeface(Typeface.SANS_SERIF).dimColor(R.color.black).drawShadow(true).cancelable(false).tintTarget(true).transparentTarget(true).targetRadius(60),
                        TapTarget.forView(findViewById(R.id.buttonClear), getString(R.string.tutorial_8),getString(R.string.tutorial_8b))
                                .outerCircleColor(R.color.blue)      // Specify a color for the outer circle
                                .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                .targetCircleColor(R.color.white)   // Specify a color for the target circle
                                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.white)      // Specify the color of the title text
                                .descriptionTextSize(10)            // Specify the size (in sp) of the description text
                                .descriptionTextColor(R.color.white)  // Specify the color of the description text
                                .textColor(R.color.white)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)                   // Whether to tint the target view's color
                                .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                                .targetRadius(60)                   // Specify the target radius (in dp),
                )
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        closeFABMenu();
                        //License and Tutorial has finsihed, now show Tappx Info
                        Tappx.getPrivacyManager(MainActivity.this).setAutoPrivacyDisclaimerEnabled(true);

                        //& now give the user the first text.
                        ((EditText)findViewById(R.id.editTextToRead)).setText(R.string.tutorial_lastText);
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    }
                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                    }
                });
        mTapTargetSequence.start();
    }

    private void showFABMenu() {
        isFABOpen = true;
        fab1.animate().translationY(+getResources().getDimension(R.dimen.standard_55));
        fab2.animate().translationY(+getResources().getDimension(R.dimen.standard_105));
        fab3.animate().translationY(+getResources().getDimension(R.dimen.standard_155));
    }

    private void closeFABMenu() {
        isFABOpen = false;
        fab1.animate().translationY(0);
        fab2.animate().translationY(0);
        fab3.animate().translationY(0);
    }

    @Override
    public void onBackPressed() {
        if (!isFABOpen) {
            super.onBackPressed();
        } else {
            closeFABMenu();
        }
    }

    public void speedRead() {
        String TextToRead = String.valueOf(((EditText) findViewById(R.id.editTextToRead)).getText());

        if (URLUtil.isValidUrl(TextToRead))
            readUrl(TextToRead);
        else {
            Intent i = new Intent(getApplicationContext(), speedRead.class);
            i.putExtra("TEXT_TO_READ", TextToRead);
            startActivity(i);
        }
    }

    //get text from pdf
    /*public String readPdf(Uri uriPath) {

        String returnString = "";
        try {

            //must be called before utilizing qPDF Toolkit
            StandardFontTF.mAssetMgr = getAssets();
            //load the PDF document you want to extract text from
            File file = new File(Environment.getExternalStorageDirectory(), uriPath.getPath());
            PDFDocument pdf = new PDFDocument(file.getAbsolutePath(), null);
            // extract the text
            String returnString = pdf.getText();
            return returnString;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
            return ("Error - could not read PDF.");
        }
    }*/

    //get text from website
    public void readUrl(String inputUrl) {

        // solve encoding issues
        final String TextToRead = inputUrl.replace("%E2%80%93", "-").replace("%27", "'");

        String[] baseDomain = { "Error" };
        try {
            baseDomain[0] = getDomainName(TextToRead);
        } catch (URISyntaxException e1) {
            baseDomain[0] = "Error";
            e1.printStackTrace();
            Toast.makeText(this,"Error! " + e1.getMessage() + e1.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        if (TextToRead.equals("") || TextToRead.isEmpty() || baseDomain[0].equals("Error")) {
            Toast.makeText(this, "Error: URL field contains no valid link. Please enter an URL or search term to load text from the internet", Toast.LENGTH_LONG).show();
        } else {
            final Runnable runnable = new Runnable() {
                public void run() {

                    StringBuilder speakText = new StringBuilder();
                    Document doc;
                    try {
                        // get first website text + links
                        doc = Jsoup.connect(TextToRead).userAgent(
                                "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                                .referrer("http://www.google.com").timeout(9000).get();

                        for (Element element : doc.select("p")) {
                            speakText.append(element.text());
                        }
                        if (speakText.toString().equals("") || (speakText.length() == 0)) {
                            for (Element element : doc.select("html")) {
                                speakText.append(element.text());
                                speakText = new StringBuilder(speakText.toString().replaceAll("<[^>]*>", ""));
                            }
                        }

                        Looper.prepare();
                        //Toast.makeText(MainActivity.this, "Process finished! The text was loaded into the text area.", Toast.LENGTH_LONG).show();

                        final String finalSpeakText = speakText.toString();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                        EditText editTextToRead = findViewById(R.id.editTextToRead);
                        editTextToRead.setText(finalSpeakText);

                                final SharedPreferences settings = getSharedPreferences("enter", MODE_PRIVATE);
                                int textLength = finalSpeakText.split(" ").length;
                                int readSpeed = settings.getInt("rs", 375);
                                int minutes = (textLength/readSpeed);
                                Toast.makeText(MainActivity.this, getString(R.string.copied_1) +" "+ textLength +" "+ getString(R.string.copied_2) +" "+ minutes +" "+ getString(R.string.copied_3), Toast.LENGTH_LONG).show();
                            }
                        });

                    } catch (IOException | IllegalArgumentException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error! Website URL not found: " + TextToRead + "\n" + e.getMessage() + "Please reformat and try again. You can try to copy-paste the URL directly from the browser URL bar.", Toast.LENGTH_LONG).show();
                    }
                }
            };
            Thread websiteLoadThread = new Thread(runnable);
            websiteLoadThread.start();
        }
    }
    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        //Read file
        if (requestCode == 4628 && resultCode == Activity.RESULT_OK) {

            Uri uri = resultData.getData();
            String text;
            /*if(uri.toString().endsWith(".pdf"))
                text = readPdf(uri);
            else*/
            text = readTextFile(uri);

            EditText editTextToRead = findViewById(R.id.editTextToRead);
            editTextToRead.setText(text);
        }
    }

    //get text from txt file
    private String readTextFile(Uri uri){
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getContentResolver().openInputStream(uri))));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }
}