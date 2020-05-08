package kaist.hcil.magtouchlibrary.fragment;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import kaist.hcil.magtouchlibrary.R;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.fragment.core.MagTouchFragment;

/*
    List all the sensors and its specs in the device.
 */
public class SystemStatFragment extends MagTouchFragment {


    long accTime;
    long gyroTime;
    long magTime;
    long updateTime;

    int accCount = 0;
    int gyroCount = 0;
    int magCount = 0;
    int fingMagCount = 0;

    long updatePeriod = 1000;//ms

    double accSamplingRate;
    double gyroSamplingRate;
    double magSamplingRate;
    double fingMagSamplingRate;

    TextView accTextView;
    TextView magTextView;
    TextView gyroTextView;
    TextView fingMagTextView;
    TextView sensorsTextView;
    List<Sensor> sensors;

    private SensorManager sensorManager;

    public SystemStatFragment() {
        // Required empty public constructor
    }

    public static SystemStatFragment newInstance() {
        SystemStatFragment fragment = new SystemStatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_system_stat, container, false);

        accTime = System.currentTimeMillis();
        gyroTime = System.currentTimeMillis();
        magTime = System.currentTimeMillis();

        accTextView = view.findViewById(R.id.acc_sr_text_view);
        gyroTextView = view.findViewById(R.id.gyro_sr_text_view);
        magTextView = view.findViewById(R.id.mag_sr_text_view);
        fingMagTextView = view.findViewById(R.id.fingmag_sr_text_view);
        sensorsTextView = view.findViewById(R.id.sensor_info_text_view);
        sensorsTextView.setMovementMethod(new ScrollingMovementMethod());

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        String sensorsText = "";
        for(Sensor s:sensors)
        {
            sensorsText += (s.toString() + "\n");
        }
        sensorsTextView.setText(sensorsText);

        return view;
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        super.onSensorChanged(event);

        int sensorType = event.sensor.getType();
        switch (sensorType)
        {
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                magCount++;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accCount++;
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroCount++;
                break;
            default:
                //do nothing
                break;
        }

        long now = System.currentTimeMillis();
        if (now - updateTime > updatePeriod)
        {
            double dt = (now - updateTime) / 1000.0;
            updateSamplingRate(dt);
            updateTime = System.currentTimeMillis();
        }

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
    protected void handleCameData(CameData cameData) {
        fingMagCount++;
    }

    @Override
    protected void handleClassified(MagTouchRequestPacket packet, CameData cameData, String finger) {

    }

    @Override
    protected void handleStabilized() {

    }

    @Override
    protected void handleStateChangedTo(int state) {

    }

    @Override
    protected void respondCameDataAfter(MagTouchRequestPacket packet, CameData cameData) {

    }

    private void updateSamplingRate(double dt)
    {
        accSamplingRate = accCount / dt;
        gyroSamplingRate = gyroCount / dt;
        magSamplingRate = magCount / dt;
        fingMagSamplingRate = fingMagCount / dt;

        accTextView.setText(toFixedDecimalString(accSamplingRate));
        gyroTextView.setText(toFixedDecimalString(gyroSamplingRate));
        magTextView.setText(toFixedDecimalString(magSamplingRate));
        fingMagTextView.setText(toFixedDecimalString(fingMagSamplingRate));

        accCount = 0;
        gyroCount = 0;
        magCount = 0;
        fingMagCount = 0;
    }

    private String toFixedDecimalString(double val)
    {
        return String.format("%3.2f", val);
    }

}
