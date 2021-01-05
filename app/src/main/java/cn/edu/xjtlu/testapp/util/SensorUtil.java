package cn.edu.xjtlu.testapp.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class SensorUtil implements SensorEventListener{
    private static SensorUtil instance;
    private Context mContext;
    private SensorManager sensorManager;
    private static MySensorEventListener mySensorEventListener;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private SensorUtil() {}

    private SensorUtil(Context context) {
        mContext = context.getApplicationContext();
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
    }

    public static SensorUtil getInstance(Context context, MySensorEventListener listener) {
        mySensorEventListener = listener;
        if (instance == null) {
            instance = new SensorUtil(context);
        }
        return instance;
    }

    public void registerListener() {
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
//            sensorManager.registerListener(this, accelerometer,
//                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregisterListener() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
        float direction = (float) Math.toDegrees(orientationAngles[0]);
        mySensorEventListener.onSensorUpdate(direction);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface MySensorEventListener {
        void onSensorUpdate(double direction);
    }
}
