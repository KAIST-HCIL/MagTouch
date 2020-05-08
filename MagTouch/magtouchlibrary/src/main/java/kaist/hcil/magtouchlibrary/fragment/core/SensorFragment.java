package kaist.hcil.magtouchlibrary.fragment.core;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class SensorFragment extends Fragment implements SensorEventListener {
    private Sensor mMagSensor;
    private Sensor mAccSensor;
    private Sensor mGyroSensor;
    private SensorManager mSensorManager;

    protected final float[] magReading = new float[3];
    protected final float[] uncalibReading = new float[6];
    protected final float[] magOffest = new float[3];
    protected final float[] accReading = new float[3];
    protected final float[] gyroReading = new float[3];
    private int magAccuracy;
    private boolean isBadAccuracy = false;

    private int numMagOffsetSample = 100;
    private int magOffsetSampleCnt = 0;

    public SensorFragment() {


    }

    public static SensorFragment newInstance() {
        SensorFragment fragment = new SensorFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) this.getActivity().getSystemService(Context.SENSOR_SERVICE);
        mMagSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(this, mMagSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mAccSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();
        switch (sensorType)
        {
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                System.arraycopy(event.values, 0, uncalibReading, 0, uncalibReading.length);

                if(magOffsetSampleCnt < numMagOffsetSample)
                {
                    magOffest[0] = uncalibReading[3];
                    magOffest[1] = uncalibReading[4];
                    magOffest[2] = uncalibReading[5];
                    magOffsetSampleCnt ++;
                }
                magReading[0] = uncalibReading[0] - magOffest[0];
                magReading[1] = uncalibReading[1] - magOffest[1];
                magReading[2] = uncalibReading[2] - magOffest[2];
                magAccuracy = event.accuracy;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, accReading, 0, accReading.length);
                break;
            case Sensor.TYPE_GYROSCOPE:
                System.arraycopy(event.values, 0, gyroReading, 0, gyroReading.length);
                break;
            default:
                //do nothing
                break;
        }

        if(magAccuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM)
        {
            if(!isBadAccuracy)
            {
                isBadAccuracy = true;
            }
        }
        else
        {
            if(isBadAccuracy)
            {
                isBadAccuracy = false;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
