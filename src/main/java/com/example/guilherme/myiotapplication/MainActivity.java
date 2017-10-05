package com.example.guilherme.myiotapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ToggleButton;
import android.view.View.OnClickListener;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tgio.parselivequery.BaseQuery;
import tgio.parselivequery.LiveQueryClient;
import tgio.parselivequery.LiveQueryEvent;
import tgio.parselivequery.Subscription;
import tgio.parselivequery.interfaces.OnListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Insert toolbar on app. Further description of this bar ins on menu_main_activity.xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initializing Parse Server
        Parse.initialize(new Parse.Configuration.Builder(this)
            .applicationId("4OU4UVDyP0nZj7CJOB6AlAaZNbpMYLB4rbFm2LOg") // from Core Settings, on "Features"
            .clientKey("jApSFpcbqVgXxWWNo9JAsUYx8VrM6vV8ouNGCjkD") // from Core Settings, on "Features"
            .server("https://parseapi.back4app.com/")
            .build()
        );


        /* -- Part 1: saving objects to Parse Server, which will trigger actions on IoT devices -- */


        // Defining text element for showing status of output pin, which receives commands from the app
        // The commands are sent by buttons, which are later described on this code.
        final EditText textOutPin = (EditText) findViewById(R.id.status1Text);
        textOutPin.setFocusable(false);
        textOutPin.setClickable(true);
        textOutPin.setText("Loading status of output pin...");


        // Although we could tell the status of the output pin using variables within this code,
        // it is more robust to do a query and retrieve the last object with its real status.

        // Initialization query
        ParseQuery<ParseObject> queryOut = ParseQuery.getQuery("CommandGPIO1");
        queryOut.whereEqualTo("destination", "command");
        queryOut.addDescendingOrder("createdAt");
        queryOut.setLimit(1);

        queryOut.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    textOutPin.setText("Output is " + objects.get(0).getString("content").toUpperCase());
                }
                else{
                    textOutPin.setText("Error: " + e.getMessage());
                }
            }
        });

        // Buttons for saving "on" and "off" objects when clicking
        // On
        Button buttonOn = (Button) findViewById(R.id.buttonSendOn);
        buttonOn.setOnClickListener( new OnClickListener(){
            @Override
            public void onClick(final View view) {

                // Creating new object and assigning proper attributes
                ParseObject command = new ParseObject("CommandGPIO1");

                command.put("content","on");
                command.put("destination", "command");
                command.saveInBackground(new SaveCallback(){
                    @Override
                    public void done(ParseException e){
                        Snackbar.make(view, "Sent ON to Output", Snackbar.LENGTH_LONG )
                                .setAction("Action", null).show();

                        // Performing the same query as previous to update the status text
                        ParseQuery<ParseObject> queryOut = ParseQuery.getQuery("CommandGPIO1");
                        queryOut.whereEqualTo("destination", "command");
                        queryOut.addDescendingOrder("createdAt");
                        queryOut.setLimit(1);

                        queryOut.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e == null){
                                    textOutPin.setText("Output is " + objects.get(0).getString("content").toUpperCase());
                                }
                                else{
                                    textOutPin.setText("Error: " + e.getMessage());
                                }
                            }
                        });
                    }
                });
            }
        });

        // Off
        Button buttonOff = (Button) findViewById(R.id.buttonSendOff);
        buttonOff.setOnClickListener( new OnClickListener(){
            @Override
            public void onClick(final View view) {

                // Creating new object and assigning proper attributes
                ParseObject command = new ParseObject("CommandGPIO1");

                command.put("content","off");
                command.put("destination", "command");
                command.saveInBackground(new SaveCallback(){
                    @Override
                    public void done(ParseException e){
                        Snackbar.make(view, "Sent OFF to Output", Snackbar.LENGTH_LONG )
                                .setAction("Action", null).show();

                        // Performing the same query as the previous to update the status text
                        ParseQuery<ParseObject> queryOut = ParseQuery.getQuery("CommandGPIO1");
                        queryOut.whereEqualTo("destination", "command");
                        queryOut.addDescendingOrder("createdAt");
                        queryOut.setLimit(1);

                        queryOut.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e == null){
                                    textOutPin.setText("Output is " + objects.get(0).getString("content").toUpperCase());
                                }
                                else{
                                    textOutPin.setText("Error: " + e.getMessage());
                                }
                            }
                        });
                    }
                });
            }
        });

        // Refresh button to obtain the output status when requested.
        // The same query as the first is performed here
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View view){
                ParseQuery<ParseObject> queryOut = ParseQuery.getQuery("CommandGPIO1");
                queryOut.whereEqualTo("destination", "command");
                queryOut.addDescendingOrder("createdAt");
                queryOut.setLimit(1);

                queryOut.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null){
                            textOutPin.setText("Output is " + objects.get(0).getString("content").toUpperCase());
                        }
                        else{
                            textOutPin.setText("Error: " + e.getMessage());
                        }
                        Snackbar.make(view, "Updated status of Output", Snackbar.LENGTH_LONG )
                                .setAction("Action", null).show();
                    }
                });
            }
        });


        /* -- Part 2: listening to live events, triggered by actions on IoT device -- */

        final EditText textInPin = (EditText) findViewById(R.id.status2Text);
        textInPin.setFocusable(false);
        textInPin.setClickable(true);
        textInPin.setText("Loading status of input pin...");

        // Initial (non live) query to obtain last stored status of pin
        ParseQuery<ParseObject> queryIn = ParseQuery.getQuery("InputGPIO");
        queryIn.whereEqualTo("type", "interrupt");
        queryIn.addDescendingOrder("createdAt");
        queryIn.setLimit(1);

        queryIn.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    textInPin.setText("Input is " + objects.get(0).getString("content").toUpperCase());
                }
                else{
                    textInPin.setText("Error: " + e.getMessage());
                }
            }
        });


        // Initializing Live Query
        LiveQueryClient.init("wss:hellonewworld.back4app.io", // from LiveQuery, on "Features"
                "4OU4UVDyP0nZj7CJOB6AlAaZNbpMYLB4rbFm2LOg", true );
        LiveQueryClient.connect();

        // Defining attributes of LiveQuery
        Subscription subIn = new BaseQuery.Builder("InputGPIO")
                .where("type","interrupt")
                .addField("content")
                .build()
                .subscribe();

        // Starting to listen to LiveQuery CREATE events, getting its content and writing
        subIn.on(LiveQueryEvent.CREATE, new OnListener() {
            @Override
            public void on(JSONObject object) {
                try {
                    final String subInContent = (String) ((JSONObject) object.get("object")).get("content");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            textInPin.setText("Input is " + subInContent.toUpperCase());

                            Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Input pin was changed to " + subInContent.toUpperCase(), Snackbar.LENGTH_LONG )
                                    .setAction("Action", null).show();
                        }
                    });

                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });


        // Toggle button is here just to emulate the objects the hardware would create
        ToggleButton toggleTest = (ToggleButton) findViewById(R.id.toggleTest);
        toggleTest.setOnClickListener( new OnClickListener(){
            @Override
            public void onClick(final View view) {
                ParseObject command = new ParseObject("InputGPIO");
                if(gpioStatusTest.equals("off"))
                    gpioStatusTest = "on";
                else
                    gpioStatusTest = "off";

                command.put("type","interrupt");
                command.put("content", "From Toggle: " + gpioStatusTest);
                command.saveInBackground(new SaveCallback(){
                    @Override
                    public void done(ParseException e){
                        Snackbar.make(view, "Changed input pin", Snackbar.LENGTH_LONG )
                                .setAction("Action", null).show();
                    }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    String gpioStatusTest = "off";
}








