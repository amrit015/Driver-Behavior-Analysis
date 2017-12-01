package com.example.uttam.driver_behaviour;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Amrit on 12/1/2017.
 */

public class SensorFusionActivity extends AppCompatActivity implements SensorEventListener {

    // for gryo
    public static final float EPSILON = 0.000000001f;
    public static final int TIME_CONSTANT = 10;
    private static final float NS2S = 1.0f / 1000000000.0f;
    TextView sPitch, sRoll, sYaw;
    String displayPitch = "", displayRoll = "", displayYaw = "";
    int count = 0;
    float pitchOut, rollOut, yawOut;
    // counter for sensor fusion
    int overYaw = 0;
    int overPitch = 0;
    //    int underYaw = 0;
//    int underPitch = 0;
    //counter for quaternion
    int overYawQ = 0;
    int overPitchQ = 0;
    //    int underYawQ = 0;
//    int underPitchQ = 0;
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
    Float newPitchOut;
    Float newRollOut;
    Float newYawOut;
    //    int underX = 0;
//    int underY = 0;
    Float newPitchOutQ;
    Float newRollOutQ;
    Float newYawOutQ;
    float mPitch, mRoll, mYaw;
    // for accelerometer
    float xAccelerometer;
    float yAccelerometer;
    float zAccelerometer;
    float xPreviousAcc;
    float yPreviousAcc;
    float zPreviousAcc;
    float xAccCalibrated;
    float yAccCalibrated;
    float zAccCalibrated;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fusion_sensor_activity);
        textOverX = (TextView) findViewById(R.id.x_over_counter);
        textOverY = (TextView) findViewById(R.id.y_over_counter);
        textOverYaw = (TextView) findViewById(R.id.yaw_over_counter);
        textOverPitch = (TextView) findViewById(R.id.pitch_over_counter);
        textOverYawQ = (TextView) findViewById(R.id.yaw_over_qua_counter);
        textOverPitchQ = (TextView) findViewById(R.id.pitch_over_qua_counter);

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

        sPitch = (TextView) findViewById(R.id.pitch);
        sRoll = (TextView) findViewById(R.id.roll);
        sYaw = (TextView) findViewById(R.id.yaw);

        // get sensorManager and initialise sensor listeners
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        initListeners();
        // wait for one second until gyroscope and magnetometer/accelerometer
        // data is initialised then scedule the complementary filter task
        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                3000, TIME_CONSTANT);

    }

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
        displayQuaternion();
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

    private void displayQuaternion() {
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
        if (!displayPitch.equals("") && !displayRoll.equals("") && !displayYaw.equals("")) {
            writeCheck = false;
            sPitch.setText(displayPitch);
            sRoll.setText(displayRoll);
            sYaw.setText(displayYaw);
            count++;

            if (newYawOut > .30 || newYawOut < -.30) {
                overYaw = overYaw + 1;
                textOverYaw.setText("Yaw Counter SensorFusion: " + d.format(overYaw));
                writeCheck = true;
            } else {
//                underYaw = underYaw + 1;
            }

            if (newPitchOut > .12 || newPitchOut < -.12) {
                overPitch = overPitch + 1;
                textOverPitch.setText("Pitch Counter SensorFusion: " + d.format(overPitch));
                writeCheck = true;
            } else {
//                underPitch = underPitch + 1;
            }

            if (newYawOutQ > .30 || newYawOutQ < -.30) {
                overYawQ = overYawQ + 1;
                textOverYawQ.setText("Yaw Counter Quaternion :" + d.format(overYawQ));
                writeCheck = true;
            } else {

            }

            if (newPitchOutQ > .12 || newPitchOutQ < -.12) {
                overPitchQ = overPitchQ + 1;
                textOverPitchQ.setText("Pitch Counter Quaternion :" + d.format(overPitchQ));
                writeCheck = true;
            } else {
//                underPitchQ = underPitchQ + 1;
            }

            if (xAccCalibrated > 3 || xAccCalibrated < -.3) {
                overX = overX + 1;
                textOverX.setText("X counter Accelerometer :" + d.format(overX));
                writeCheck = true;
            } else {
//                 underX = underX + 1;
            }

            if (yAccCalibrated > 2.5 || yAccCalibrated < -2.5) {
                overY = overY + 1;
                textOverY.setText("Y counter Accelerometer :" + d.format(overY));
                writeCheck = true;
            } else {
//                 underY = underY+1;
            }

            // only saving if there is change in the counters
            if (writeCheck) {

                //Creating a shared preference
                SharedPreferences sharedPreferences = SensorFusionActivity.this.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

                //Creating editor to store values to shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();

                //Adding values to editor
                editor.putInt("overPitch", overPitch);
                editor.putInt("overYaw", overYaw);
                editor.putInt("overX", overX);
                editor.putInt("overY", overY);

                //Saving values to editor
                editor.commit();
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

            newPitchOut = getPitch - pitchOut;
            newRollOut = getRoll - rollOut;
            newYawOut = getYaw - yawOut;

            displayPitch = d.format(newPitchOut * 180 / Math.PI) + "degrees / " + d.format(newPitchOut) + " rad/s";
            displayRoll = d.format(newRollOut * 180 / Math.PI) + "degrees / " + d.format(newRollOut) + " rad/s";
            displayYaw = d.format(newYawOut * 180 / Math.PI) + "degrees / " + d.format(newYawOut) + " rad/s";

            getPitch = pitchOut;
            getRoll = rollOut;
            getYaw = yawOut;
        }
    }
}
