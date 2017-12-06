package com.example.uttam.driver_behaviour;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.android.volley.toolbox.Volley.newRequestQueue;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        SensorEventListener {

    private Button btnSearch, btnDirections, btnStart, btnBack;
    private EditText searchField;
    private TextView speedLimitText, currentSpeedText;
    private int turns, suddenAcceleration = 0;
    private float totalScore = 10;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker, mUserLocationMarker;
    LocationRequest mLocationRequest;
    int PROXIMITY_RADIUS = 10000;
    double latitude, longitude;
    double end_latitude, end_longitude;
    int i = 0;
    long tEnd, tStart;
    String timeString;
    private boolean running;
    private boolean paused;
    private long start = 0;
    private long pausedStart = 0;
    private long end = 0;
    private List<String> details = new ArrayList<>(4);
    int limitExceedCount = 0;
    String slimitExceedCount;
    String limitExceedTime;
    int maxSpeed = 0;
    String sMaxSpeed;
    String Name;
    private float currentSpeed = 0.0f;
    String speedlimit;
    int flag = 0;
    private boolean RainAndSnow;
    private int suddenBreaksCount = 0;
    long tBreakStart, tBreakEnd;
    float tempSpeed = 0;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mRootReference;
    private DatabaseReference mLocationReference;
    private DatabaseReference mUsersLocation;

    // sensor variables
    // for gryo
    public static final float EPSILON = 0.000000001f;
    public static final int TIME_CONSTANT = 10;
    private static final float NS2S = 1.0f / 1000000000.0f;
    int count = 1;
    float pitchOut, rollOut, yawOut;
    // counter for sensor fusion
    int overYaw = 0;
    int overPitch = 0;
    //counter for quaternion
    int overYawQ = 0;
    int overPitchQ = 0;

    // final pitch and yaw values
    int finalOverYaw = 0;
    int finalOverPitch = 0;

    //counter for accelerometer reading
    int overX = 0;
    int overY = 0;
    float[] mMagneticField;
    float[] mGravity;
    DecimalFormat d = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
    Float getPitch = 0f;
    Float getRoll = 0f;
    Float getYaw = 0f;
    Float getPitchQ = 0f;
    Float getRollQ = 0f;
    Float getYawQ = 0f;
    // normal - sensor fusion, Q - denotes quaternion
    Float newPitchOut = 0f;
    Float newRollOut = 0f;
    Float newYawOut = 0f;
    //    int underX = 0;
//    int underY = 0;
    Float newPitchOutQ = 0f;
    Float newRollOutQ = 0f;
    Float newYawOutQ = 0f;
    float mPitch, mRoll, mYaw;
    // for accelerometer
    float xAccelerometer;
    float yAccelerometer;
    float zAccelerometer;
    float xPreviousAcc;
    float yPreviousAcc;
    float zPreviousAcc;
    float xAccCalibrated = 0f;
    float yAccCalibrated = 0f;
    float zAccCalibrated = 0f;
    boolean writeCheck = false;
    TextView textOverYaw, textOverPitch, textOverYawQ, textOverPitchQ, textOverX, textOverY;
    private SensorManager mSensorManager = null;
    // angular speeds from gyro
    private float[] gyro = new float[3];
    // rotation matrix from gyro data
    private float[] gyroMatrix = new float[9];
    // orientation angles from gyro matrix
    private float[] gyroOrientation = new float[3];
    // magnetic field vector
    private float[] magnet = new float[3];
    // accelerometer vector
    private float[] accel = new float[3];
    // orientation angles from accel and magnet
    private float[] accMagOrientation = new float[3];
    // final orientation angles from sensor fusion
    private float[] fusedOrientation = new float[3];
    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];
    private float timestamp;
    private boolean initState = true;
    private Timer fuseTimer = new Timer();
    private String SHARED_PREF_NAME = "driverbehaviorapp";
    private boolean mInitialized = false;
    Boolean yAccChange = false;
    Boolean xAccChange = false;

    // for 30 sec sensor values reset
    int getFinalOverYaw = 0;
    int getFinalOverPitch = 0;
    int getFinalOverX = 0;
    int getFinalOverY = 0;
    Boolean isBrakesApplied = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btnDirections = findViewById(R.id.B_to);
        btnSearch = findViewById(R.id.B_search);
        btnStart = findViewById(R.id.B_start);
        btnBack = findViewById(R.id.B_back);
        searchField = findViewById(R.id.TF_location);
        speedLimitText = findViewById(R.id.speedLimit);
        currentSpeedText = findViewById(R.id.currentSpeed);

        Intent LoginIntent = getIntent();
        Name = LoginIntent.getStringExtra("userid");
        firebaseDatabase = FirebaseDatabase.getInstance();
        mRootReference = firebaseDatabase.getReference("Users").child(Name).child("Sessions");
        mLocationReference = firebaseDatabase.getReference("Users").child(Name).child("Location");
        mUsersLocation = firebaseDatabase.getReference("Users");

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                started(view);
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available.");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // computing sensor values
        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f;
        gyroMatrix[1] = 0.0f;
        gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f;
        gyroMatrix[4] = 1.0f;
        gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f;
        gyroMatrix[7] = 0.0f;
        gyroMatrix[8] = 1.0f;

        // get sensorManager and initialise sensor listeners
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        initListeners();
        // wait for one second until gyroscope and magnetometer/accelerometer
        // data is initialised then scedule the complementary filter task
        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                2000, TIME_CONSTANT);
        // analysing behavior every 2 sec
        fuseTimer.scheduleAtFixedRate(new BehaviorAnalysis(), 1000, 2000);
        // getting the time in the interval of 30 sec
//        timeElapsed = (System.nanoTime() - start) % 30;
        //resetting the sensor values every 30 sec
        fuseTimer.scheduleAtFixedRate(new ResetSensorValues(), 1000, 30000);
    }

    // initializing the sensors
    public void initListeners() {
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void started(View view) {
        String input = searchField.getText().toString().trim();
        if (input.isEmpty()) {
            searchField.setError("Cannot be blank");
        } else {
            if (i == 0) {
                btnStart.setText("END");
                tStart = System.currentTimeMillis();
                tBreakStart = System.currentTimeMillis();
                suddenBreaksCount = 0;
                suddenAcceleration = 0;
                i = 1;
                LatLng latLng = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));
                //  btnStart.setVisibility(view.GONE);
                btnSearch.setVisibility(View.GONE);
                btnDirections.setVisibility(View.GONE);
                searchField.setVisibility(View.GONE);
                //   LinearLayout one = (LinearLayout) findViewById(R.id.linearLayout);
                //   one.setVisibility(View.INVISIBLE);
                btnBack.setVisibility(View.VISIBLE);
                currentSpeedText.setVisibility(View.VISIBLE);
                speedLimitText.setVisibility(View.VISIBLE);
            } else {

                btnStart.setText("START");
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
                //Toast.makeText(getApplicationContext(),Double.toStringText(elapsedSeconds),Toast.LENGTH_SHORT).show();
                i = 0;
                onadd();
                details.clear();

                // locationManager.removeUpdates(this);
            }

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

    public String toStringText() {
        long enlapsed = elapsed();
        return ((double) enlapsed / 1000000000.0) + " Seconds";
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

    public void onadd() {
        details.add("Total Time: " + timeString);
        details.add("Max Speed: " + sMaxSpeed);
        details.add("LimitExceedTime: " + limitExceedTime);
        details.add("LimitExceedCount: " + slimitExceedCount);
        details.add("suddenBreaksCount: " + suddenBreaksCount);
        details.add("suddenAcceleration: " + suddenAcceleration);
        details.add("RainOrSnow: " + RainAndSnow);
        String id = mRootReference.push().getKey();
        mRootReference.child(id).setValue(details);

    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void onClick(View v) {
        Object dataTransfer[] = new Object[2];
        //GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();


        switch (v.getId()) {
            case R.id.B_search: {
                mMap.clear();
                EditText tf_location = findViewById(R.id.TF_location);
                String location = tf_location.getText().toString();
                List<Address> addressList = null;
                MarkerOptions markerOptions = new MarkerOptions();
                Log.d("location = ", location);

                if (!location.equals("")) {
                    Geocoder geocoder = new Geocoder(this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 5);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (addressList != null) {
                        //  for (int i = 0; i < addressList.size(); i++) {
                        Address myAddress = addressList.get(0);
                        LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                        markerOptions.position(latLng);
                        mMap.addMarker(markerOptions);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        end_latitude = myAddress.getLatitude();
                        end_longitude = myAddress.getLongitude();
                        // }
                    }

                }
            }
            break;

            case R.id.B_to:
                dataTransfer = new Object[3];
                String url = getDirectionsUrl();
                GetDirectionsData getDirectionsData = new GetDirectionsData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = new LatLng(end_latitude, end_longitude);
                getDirectionsData.execute(dataTransfer);
                break;
        }
    }

    private String getDirectionsUrl() {
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin=" + latitude + "," + longitude);
        googleDirectionsUrl.append("&destination=" + end_latitude + "," + end_longitude);
        googleDirectionsUrl.append("&key=" + "AIzaSyCAcfy-02UHSu2F6WeQ1rhQhkCr51eBL9g");

        return googleDirectionsUrl.toString();
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyCAcfy-02UHSu2F6WeQ1rhQhkCr51eBL9g");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }


    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    private double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.####");
        return Double.valueOf(twoDForm.format(d));
    }

    @Override
    public void onLocationChanged(final Location location) {
        Log.d("onLocationChanged", "entered");

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        if (mUserLocationMarker != null) {
            mUserLocationMarker.remove();
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();


        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        mUsersLocation.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.hasChild("Location") && !(dataSnapshot.child("UserName").getValue().equals(Name))) {
                    String loc = dataSnapshot.child("Location").getValue().toString();
                    if (!loc.isEmpty()) {
                        String[] strSplit = loc.split("\\s*,\\s*");
                        String latitudeString = strSplit[0].substring(10, 17);
                        String longitudeString = strSplit[1].substring(10, 18);
                        //  Toast.makeText(getApplicationContext(),latitudeString,Toast.LENGTH_SHORT).show();
                        //  Toast.makeText(getApplicationContext(),dataSnapshot.child("UserName").getValue().toString(),Toast.LENGTH_SHORT).show();
                        //   Toast.makeText(getApplicationContext(),longitudeString,Toast.LENGTH_SHORT).show();
                        float lat = Float.parseFloat(latitudeString);
                        float lng = Float.parseFloat(longitudeString);
                        LatLng latLng = new LatLng(lat, lng);
                        MarkerOptions markerOptionsUser = new MarkerOptions();
                        markerOptionsUser.position(latLng);
                        markerOptionsUser.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        mUserLocationMarker = mMap.addMarker(markerOptionsUser);

                    }
                    //Toast.makeText(getApplicationContext(),loc,Toast.LENGTH_SHORT).show();

                }
                // Toast.makeText(getApplicationContext(),dataSnapshot.child("Email").getValue().toString(),Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //move map camera
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(17));


        Toast.makeText(MapsActivity.this, "Your Current Location", Toast.LENGTH_LONG).show();


        //stop location updates
      /*  if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }*/
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (com.android.volley.Request.Method.GET, "https://api.darksky.net/forecast/662d2d0ff78f38a9e1405ebdd26049ac/" + location.getLatitude() + "," + location.getLongitude(), null, new com.android.volley.Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject sys = response.getJSONObject("currently");
                            String icon = sys.getString("icon");
                            RainAndSnow = icon == "rain" || icon == "snow";
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // mTxtDisplay.setText("Response: " + response.toString());
                    }
                }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
        //Speed
        RequestQueue requestQueue = newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.GET, "https://roads.googleapis.com/v1/speedLimits?path=" + location.getLatitude() + "," + location.getLongitude() + "&key=AIzaSyAcXgGZ0d9ujapO3SMXvq5EeVG1Utb4wVI", null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = response.getJSONArray("speedLimits").getJSONObject(0);
                    speedlimit = obj.getString("speedLimit");
                    //Toast.makeText(getApplicationContext(),speedlimit,Toast.LENGTH_SHORT).show();
                    double kph = (Double.parseDouble(speedlimit)) * 0.621;
                    int mph = (int) Math.round(kph);
                    speedLimitText.setText("Limit:" + "" + Integer.toString(mph));
                    pause();
                    currentSpeed = location.getSpeed() * 2.23f;
                    CharSequence text = "Speed Limit Exceeded!";
                    tBreakEnd = System.currentTimeMillis();
                    long breakElapsed = tBreakStart - tBreakEnd;
                    double breakElapsedSeconds = breakElapsed / 1000.0;
                    int breakSeconds = (int) (breakElapsedSeconds % 60);
                    if (breakSeconds % 5 == 0) {
                        tempSpeed = currentSpeed;
                    }
                    if (breakSeconds % 2 == 0 && tempSpeed >= 35 && (tempSpeed - currentSpeed >= 20)) {

                        suddenBreaksCount++;
                        isBrakesApplied = true;
                    } else {
                        isBrakesApplied = false;
                    }
                    if (breakSeconds % 2 == 0 && currentSpeed - tempSpeed >= 20) {
                        suddenAcceleration++;
                    }
                    if (currentSpeed > mph) {
                        if (!isRunning()) {
                            start();
                        } else {
                            resume();
                        }
                        if (flag == 0) {
                            limitExceedCount++;
                            flag = 1;
                        }
                    }
                    if (currentSpeed < mph) {
                        flag = 0;
                    }
                    if (maxSpeed < currentSpeed) {
                        maxSpeed = (int) currentSpeed;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(request);
        currentSpeedText.setText("Speed: " + new DecimalFormat("##").format(currentSpeed));

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void back(View view) {
        // btnStart.setVisibility(view.VISIBLE);
        btnSearch.setVisibility(View.VISIBLE);
        btnDirections.setVisibility(View.VISIBLE);
        searchField.setVisibility(View.VISIBLE);
        LinearLayout one = findViewById(R.id.linearLayout);
        one.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.GONE);
        currentSpeedText.setVisibility(View.INVISIBLE);
        speedLimitText.setVisibility(View.INVISIBLE);
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setDraggable(true);
        end_latitude = marker.getPosition().latitude;
        end_longitude = marker.getPosition().longitude;

        Log.d("end_lat", "" + end_latitude);
        Log.d("end_lng", "" + end_longitude);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        end_latitude = marker.getPosition().latitude;
        end_longitude = marker.getPosition().longitude;

        Log.d("end_lat", "" + end_latitude);
        Log.d("end_lng", "" + end_longitude);
    }

    // for sensor
    // Sensor Fusion involving Accelerometer, Gyroscope, and Magnetometer
    // Quaternion
    // Accelerometer
    @Override
    public void onSensorChanged(SensorEvent event) {
        updateValues();
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values;
                xAccelerometer = event.values[0];
                yAccelerometer = event.values[1];
                zAccelerometer = event.values[2];
                calibrateAccelerometer();
                // copy new accelerometer data into accel array
                // then calculate new orientation
                System.arraycopy(event.values, 0, accel, 0, 3);
                calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                // process gyro data
                gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                // copy new magnetometer data into magnet array
                mMagneticField = event.values;
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;
        }
        computeQuaternion();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void calibrateAccelerometer() {
        if (!mInitialized) {
            xPreviousAcc = xAccelerometer;
            yPreviousAcc = yAccelerometer;
            zPreviousAcc = zAccelerometer;
            mInitialized = true;
        } else {
            xAccCalibrated = (xPreviousAcc - xAccelerometer);
            yAccCalibrated = (yPreviousAcc - yAccelerometer);
            zAccCalibrated = (zPreviousAcc - zAccelerometer);
            xPreviousAcc = xAccelerometer;
            yPreviousAcc = yAccelerometer;
            zPreviousAcc = zAccelerometer;
        }
    }

    private void computeQuaternion() {
        float R[] = new float[9];
        float I[] = new float[9];
        if (mMagneticField != null && mGravity != null) {
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mMagneticField);
            if (success) {
                float[] mOrientation = new float[3];
                float[] mQuaternion = new float[4];
                SensorManager.getOrientation(R, mOrientation);

                SensorManager.getQuaternionFromVector(mQuaternion, mOrientation);

                mYaw = mQuaternion[1]; // orientation contains: azimuth(yaw), pitch and Roll
                mPitch = mQuaternion[2];
                mRoll = mQuaternion[3];

                newPitchOutQ = getPitchQ - mPitch;
                newRollOutQ = getRollQ - mRoll;
                newYawOutQ = getYawQ - mYaw;

                getPitchQ = mPitch;
                getRollQ = mRoll;
                getYawQ = mYaw;
            }
        }
    }

    private void updateValues() {
        if (newPitchOut != 0 && newPitchOutQ != 0 && newYawOut != 0 && newYawOutQ != 0 && xAccCalibrated != 0 && yAccCalibrated != 0) {
            writeCheck = false;
            xAccChange = false;
            yAccChange = false;
            count = count+1;
            if (count == 2250){
                count = 1;
            }

            if (newYawOut > .30 || newYawOut < -.30) {
                overYaw = overYaw + 1;
                writeCheck = true;
            }

            if (newPitchOut > .12 || newPitchOut < -.12) {
                overPitch = overPitch + 1;
                writeCheck = true;
            }

            if (newYawOutQ > .30 || newYawOutQ < -.30) {
                overYawQ = overYawQ + 1;
                writeCheck = true;
            }

            if (newPitchOutQ > .12 || newPitchOutQ < -.12) {
                overPitchQ = overPitchQ + 1;
                writeCheck = true;
            }

            if (xAccCalibrated > 3 || xAccCalibrated < -3) {
                overX = overX + 1;
                writeCheck = true;
                xAccChange = true;
            }

            if (yAccCalibrated > 2.5 || yAccCalibrated < -2.5) {
                overY = overY + 1;
                writeCheck = true;
                yAccChange = true;
            }

            // computing final values for pitch and yaw counters
            if (overPitch != 0 || overPitchQ != 0) {
                finalOverPitch = (int) (overPitch + 0.3 * overPitchQ);
            }

            if (overYaw != 0 || overYawQ != 0) {
                finalOverYaw = (int) (overYaw + 0.4 * overYawQ);
            }

            /*

            Here, one counter on any sensor doesn't reflect the crossing of threshold for 1 time,
            it just gives the total number of times the data was recorded during "1 crossing"
            For one time the user makes a rash turn, counter was reach upto 10 for that one single incident

            */

            // only saving if there is change in the counters
            if (writeCheck) {

                //Creating a shared preference
                SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

                //Creating editor to store values to shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();

                //Adding values to editor
                editor.putInt("overPitch", finalOverPitch);
                editor.putInt("overYaw", finalOverYaw);
                editor.putInt("overX", overX);
                editor.putInt("overY", overY);

                //Saving values to editor
                editor.commit();
                Log.i("MapsActivity", "finalOverPitch : " + finalOverPitch);
            }
        }
    }

    public void calculateAccMagOrientation() {
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    public void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if (initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    private void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor) {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float) Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if (omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float) Math.sin(o[1]);
        float cosX = (float) Math.cos(o[1]);
        float sinY = (float) Math.sin(o[2]);
        float cosY = (float) Math.cos(o[2]);
        float sinZ = (float) Math.sin(o[0]);
        float cosZ = (float) Math.cos(o[0]);

        // rotation about x-axis (displayPitch)
        xM[0] = 1.0f;
        xM[1] = 0.0f;
        xM[2] = 0.0f;
        xM[3] = 0.0f;
        xM[4] = cosX;
        xM[5] = sinX;
        xM[6] = 0.0f;
        xM[7] = -sinX;
        xM[8] = cosX;

        // rotation about y-axis (displayRoll)
        yM[0] = cosY;
        yM[1] = 0.0f;
        yM[2] = sinY;
        yM[3] = 0.0f;
        yM[4] = 1.0f;
        yM[5] = 0.0f;
        yM[6] = -sinY;
        yM[7] = 0.0f;
        yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ;
        zM[1] = sinZ;
        zM[2] = 0.0f;
        zM[3] = -sinZ;
        zM[4] = cosZ;
        zM[5] = 0.0f;
        zM[6] = 0.0f;
        zM[7] = 0.0f;
        zM[8] = 1.0f;

        // rotation order is y, x, z (displayRoll, displayPitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private class calculateFusedOrientationTask extends TimerTask {
        float filter_coefficient = 0.85f;
        float oneMinusCoeff = 1.0f - filter_coefficient;

        public void run() {
            // Azimuth
            if (gyroOrientation[0] < -0.5 * Math.PI && accMagOrientation[0] > 0.0) {
                fusedOrientation[0] = (float) (filter_coefficient * (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[0]);
                fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
            } else if (accMagOrientation[0] < -0.5 * Math.PI && gyroOrientation[0] > 0.0) {
                fusedOrientation[0] = (float) (filter_coefficient * gyroOrientation[0] + oneMinusCoeff * (accMagOrientation[0] + 2.0 * Math.PI));
                fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
            } else
                fusedOrientation[0] = filter_coefficient * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];

            // Pitch
            if (gyroOrientation[1] < -0.5 * Math.PI && accMagOrientation[1] > 0.0) {
                fusedOrientation[1] = (float) (filter_coefficient * (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[1]);
                fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
            } else if (accMagOrientation[1] < -0.5 * Math.PI && gyroOrientation[1] > 0.0) {
                fusedOrientation[1] = (float) (filter_coefficient * gyroOrientation[1] + oneMinusCoeff * (accMagOrientation[1] + 2.0 * Math.PI));
                fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
            } else
                fusedOrientation[1] = filter_coefficient * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];

            // Roll
            if (gyroOrientation[2] < -0.5 * Math.PI && accMagOrientation[2] > 0.0) {
                fusedOrientation[2] = (float) (filter_coefficient * (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[2]);
                fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
            } else if (accMagOrientation[2] < -0.5 * Math.PI && gyroOrientation[2] > 0.0) {
                fusedOrientation[2] = (float) (filter_coefficient * gyroOrientation[2] + oneMinusCoeff * (accMagOrientation[2] + 2.0 * Math.PI));
                fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
            } else
                fusedOrientation[2] = filter_coefficient * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];

            // Overwrite gyro matrix and orientation with fused orientation to comensate gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);

            pitchOut = fusedOrientation[1];
            rollOut = fusedOrientation[2];
            yawOut = fusedOrientation[0];

            // present instance values
            newPitchOut = getPitch - pitchOut;
            newRollOut = getRoll - rollOut;
            newYawOut = getYaw - yawOut;

            // saving values for calibration
            getPitch = pitchOut;
            getRoll = rollOut;
            getYaw = yawOut;
        }

    }

    private class BehaviorAnalysis extends TimerTask {
        float speedLimit = 0f;
        int factorSpeed = 0;
        int factorBrakes = 0;
        int factorWeather = 0;
        int factorAcceleration = 0;
        int factorTurn = 0;
        //calculate rateOverYaw and rateOverPitch by taking the division of pitch/yaw over 30 sec interval
        double rateOverPitch = finalOverPitch /count;
        double rateOverYaw = finalOverYaw/count;

        @Override
        public void run() {
            if (currentSpeed != 0) {
                if (currentSpeed > speedLimit) {
                    factorSpeed = 10;
                } else {
                    factorSpeed = 5;
                }

                if (isBrakesApplied == true) {
                    factorBrakes = 10;
                } else {
                    factorBrakes = 0;
                }

                if (RainAndSnow == true) {
                    factorWeather = 10;
                } else {
                    factorWeather = 0;
                }

                // writeCheck is the boolean used above to indicate the change in counters in turn and acc
                if (writeCheck == true) {

                    if (rateOverPitch < 0.05) {
                        if (xAccChange == true) {
                            // likely unsafe
                            factorAcceleration = 8;
                        } else {
                            // likely safe
                            factorAcceleration = 2;
                        }
                    } else {
                        if (xAccChange == true) {
                            // definitely unsafe
                            factorAcceleration = 10;
                        } else {
                            // probably unsafe
                            factorAcceleration = 6;
                        }
                    }

                    if (rateOverYaw < 0.02) {
                        if (yAccChange == true) {
                            // likely unsafe
                            factorTurn = 8;
                        } else {
                            // likely safe
                            factorTurn = 2;
                        }
                    } else {
                        if (yAccChange == true) {
                            // definitely unsafe
                            factorTurn = 10;
                        } else {
                            // probably unsafe
                            factorTurn = 6;
                        }
                    }
                } else {
                    factorAcceleration = 0;
                    factorTurn = 0;
                }
            }
            double safeScore = 0;
            double unsafeScore = 0.3 * factorSpeed + 0.2 * factorBrakes + 0.2 * factorWeather + 0.2 * factorAcceleration + 0.2 * factorTurn;
            if (unsafeScore < 10) {
                safeScore = 10 - unsafeScore;
            }

            if (unsafeScore > 10) {
                safeScore = 0;
            }
            final double finalSafeScore = safeScore;
            if (currentSpeed != 0) {
                runOnUiThread(new Runnable() {
                    public void run() {
//                        Toast.makeText(getApplicationContext(), "Score: " + finalSafeScore, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Log.i("MapsActivity", "count : " + count);
            Log.i("MapsActivity", "score : " + finalSafeScore);
            Log.i("MapsActivity", "final Pitch rate : " + rateOverPitch);
            Log.i("MapsActivity", "final Yaw rate : " + rateOverYaw);
        }
    }

    private class ResetSensorValues extends TimerTask {

        @Override
        public void run() {
            finalOverYaw = finalOverYaw - getFinalOverYaw;
            finalOverPitch = finalOverPitch - getFinalOverPitch;
            overX = overX - getFinalOverX;
            overY = overY - getFinalOverY;

            getFinalOverPitch = finalOverPitch;
            getFinalOverYaw = finalOverYaw;
            getFinalOverX = overX;
            getFinalOverY = overY;
            Log.i("MapsActivity", "final Pitch : " + finalOverPitch);
            Log.i("MapsActivity", "final Yaw : " + finalOverYaw);
            Log.i("MapsActivity", "final overX : " + overX);
            Log.i("MapsActivity", "final overY : " + overY);
        }
    }
}

