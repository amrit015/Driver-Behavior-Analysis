package com.example.uttam.driver_behaviour;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AlertDialog;


import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.android.volley.toolbox.Volley.newRequestQueue;

public class MainActivity extends Activity implements SensorEventListener, LocationListener, ValueEventListener{

    private TextView speedView,speedLimit;
    private final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;
    private Sensor accSensor, gyroSensor, magnetometerSensor;
    private SensorManager SM;
    private LocationManager locationManager;
    private float currentSpeed = 0.0f;
    String speedlimit;
    int limitExceedCount =0;
    String slimitExceedCount;
    String limitExceedTime;
    int maxSpeed =0;
    String sMaxSpeed;
    long tEnd, tStart;
    String timeString;
    String Name;


    private boolean running;
    protected boolean isMph = true;
    int i = 0;
    int flag =0;

    Button button;
    private boolean paused;
    private long start = 0;
    private long pausedStart = 0;
    private long end = 0;

    private List<String> details = new ArrayList<>();


    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mRootReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        Intent LoginIntent = getIntent();
        Name = LoginIntent.getStringExtra("userid");
        firebaseDatabase = FirebaseDatabase.getInstance();
        mRootReference = firebaseDatabase.getReference("Users").child(Name).child("Sessions");
    }
    public void started(View view)
    {
        if (i==0)
        {
        button.setText("END");
        button.setBackgroundResource(R.drawable.back4);
            tStart = System.currentTimeMillis();
            i=1;
        }
        else
        {
         button.setText("START");
            button.setBackgroundResource(R.drawable.back3);
            tEnd = System.currentTimeMillis();
            long tDelta = tEnd - tStart;
            double elapsedSeconds = tDelta / 1000.0;
            int hours = (int) (elapsedSeconds / 3600);
            int minutes = (int) ((elapsedSeconds % 3600) / 60);
            int seconds = (int) (elapsedSeconds % 60);
            timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            long elapsed = stop();
            double tseconds = ((double) elapsed / 1000000000.0);
            int shours = (int) (tseconds / 3600);
            int sminutes = (int) ((tseconds % 3600) / 60);
            int sseconds = (int) (tseconds % 60);
            limitExceedTime = String.format("%02d:%02d:%02d", shours, sminutes, sseconds);
            slimitExceedCount = Integer.toString(limitExceedCount);
            sMaxSpeed = Integer.toString(maxSpeed);
            //Toast.makeText(getApplicationContext(),Double.toString(elapsedSeconds),Toast.LENGTH_SHORT).show();
            i = 0;
            onadd();
            locationManager.removeUpdates(this);

        }
        // Create our Sensor Manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer Sensor
        accSensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometerSensor = SM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        // Register sensor Listener
        SM.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        speedView = (TextView)findViewById(R.id.speedView);
        speedLimit = (TextView)findViewById(R.id.speedLimit);


        //turn on speedometer using GPS
        turnOnGps();

    }
    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public void start() {
        start = System.nanoTime();
        running = true;
        paused = false;
        pausedStart = -1;
    }

    public long stop() {
        if (!isRunning()) {
            return -1;
        } else if (isPaused()) {
            running = false;
            paused = false;

            return pausedStart - start;
        } else {
            end = System.nanoTime();
            running = false;
            return end - start;
        }
    }

    public long pause() {
        if (!isRunning()) {
            return -1;
        } else if (isPaused()) {
            return (pausedStart - start);
        } else {
            pausedStart = System.nanoTime();
            paused = true;
            return (pausedStart - start);
        }
    }

    public void resume() {
        if (isPaused() && isRunning()) {
            start = System.nanoTime() - (pausedStart - start);
            paused = false;
        }
    }

    public long elapsed() {
        if (isRunning()) {
            if (isPaused())
                return (pausedStart - start);
            return (System.nanoTime() - start);
        } else
            return (end - start);
    }

    public String toString() {
        long enlapsed = elapsed();
        return ((double) enlapsed / 1000000000.0) + " Seconds";
    }

    public void onadd() {
        details.add("Total Time: "+timeString);
        details.add("Max Speed: "+sMaxSpeed);
        details.add("LimitExceedTime: "+limitExceedTime);
        details.add("LimitExceedCount: "+slimitExceedCount);
        String id = mRootReference.push().getKey();
        mRootReference.child(id).setValue(details);

    }

    public void display(View v) {
        Intent intent = new Intent(MainActivity.this,ViewDatabase.class);
        intent.putExtra("userid",Name);
        startActivity(intent);
        /*Cursor result = db.display();
        if (result.getCount()==0)
        {
            show("Data","Empty Table");
        }
        else
        {
            StringBuffer sb = new StringBuffer();
            while (result.moveToNext())
            {
                sb.append("ID : "+result.getString(0)+"\n"+
                        "TotalTime : "+result.getString(1)+"\n"+
                        "LimitExceedCount : "+result.getString(2)+"\n"+
                        "OverspeedTime :"+result.getString(3)+"\n"+
                        "MaxSpeed :"+result.getString(4)+"\n\n");
            }
            show("Data",sb.toString());
        }*/
    }

    private void show(String s, String s1) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setCancelable(true);
        b.setTitle(s);
        b.setMessage(s1);
        b.show();
    }
 /*   public void del(View v) {
        EditText e = (EditText)findViewById(R.id.editText);
        String s = e.getText().toString();
        db.delete(s);
    }*/

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not in use
    }


    public boolean isMph() { return isMph; }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onLocationChanged(final Location location) {
        RequestQueue requestQueue = newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.GET, "https://roads.googleapis.com/v1/speedLimits?path=" + location.getLatitude() + "," + location.getLongitude() + "&key=AIzaSyAcXgGZ0d9ujapO3SMXvq5EeVG1Utb4wVI", null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = response.getJSONArray("speedLimits").getJSONObject(0);
                    speedlimit = obj.getString("speedLimit");
                    //Toast.makeText(getApplicationContext(),speedlimit,Toast.LENGTH_SHORT).show();
                    double kph = (Double.parseDouble(speedlimit))*0.621;
                    int mph = (int) Math.round(kph);
                    speedLimit.setText("Limit:"+""+Integer.toString(mph));
                    pause();
                    currentSpeed = location.getSpeed() * 2.23f;
                    CharSequence text = "Speed Limit Exceeded!";
                    if (currentSpeed > mph){
                        if(!isRunning()){
                            start();
                        }
                        else {
                            resume();
                        }
                        if(flag==0) {
                            limitExceedCount++;
                            flag = 1;
                        }
                    }
                    if(currentSpeed<mph){
                        flag =0;
                    }
                    if(maxSpeed<currentSpeed){
                        maxSpeed = (int)currentSpeed;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Error"+error.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(request);
        speedView.setText("Speed: "+new DecimalFormat("##").format(currentSpeed));
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        turnOnGps();
    }

    @Override
    public void onProviderDisabled(String provider) {
        turnOffGps();

    }

    private void turnOnGps() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, this);
        }
    }
    private void turnOffGps() {

        locationManager.removeUpdates(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}