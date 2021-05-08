package cn.edu.xjtlu.testapp.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.lang.ref.WeakReference;

public class SensorUtil implements SensorEventListener{
    public static String TAG = SensorUtil.class.getSimpleName();
    private final SensorManager sensorManager;
    private final MySensorEventListener mListener;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationvectorReading = new float[5];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    public SensorUtil(Context context, MySensorEventListener listener) {
        mListener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void registerListener() {
//        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        if (accelerometer != null) {
////            sensorManager.registerListener(this, accelerometer,
////                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
//            sensorManager.registerListener(this, accelerometer,
//                    SensorManager.SENSOR_DELAY_NORMAL);
//        }
//        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        if (magneticField != null) {
//            sensorManager.registerListener(this, magneticField,
//                    SensorManager.SENSOR_DELAY_NORMAL);
//        }
        Sensor rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVector != null) {
            sensorManager.registerListener(this, rotationVector,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregisterListener() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
//                System.arraycopy(event.values, 0, accelerometerReading,
//                        0, accelerometerReading.length);
//                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, magnetometerReading,
                        0, magnetometerReading.length);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                System.arraycopy(event.values, 0, rotationvectorReading,
                        0, rotationvectorReading.length);
                break;
        }
        updateOrientationAngles(event.sensor.getType());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void updateOrientationAngles(int type) {
        // Update rotation matrix, which is needed to update orientation angles.
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_MAGNETIC_FIELD:
                SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationvectorReading);
                break;
        }

        // "rotationMatrix" now has up-to-date information.
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        // "orientationAngles" now has up-to-date information.
        float direction = (float) Math.toDegrees(orientationAngles[0]);

        mListener.onSensorUpdate(Math.round(direction));
    }

    public interface MySensorEventListener {
        void onSensorUpdate(int direction);
    }
}
