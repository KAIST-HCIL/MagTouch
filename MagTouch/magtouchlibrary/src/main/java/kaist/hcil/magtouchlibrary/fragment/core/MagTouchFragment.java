package kaist.hcil.magtouchlibrary.fragment.core;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import kaist.hcil.magtouchlibrary.Settings;
import kaist.hcil.magtouchlibrary.core.MagTouch;
import kaist.hcil.magtouchlibrary.core.MagTouchCallback;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.IMUData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.datamodel.Quaternion;
import kaist.hcil.magtouchlibrary.util.QuaternionMeanFilter;

/*
    The core fragment.
    MagTouchFragment provides convenient interfaces for using Magtouch and
    getting finger classification results asynchronous.

    If you want to use MagTouch module in your fragment,
    please inherit this class.
 */

public abstract class MagTouchFragment extends SensorFragment {
    private OnMagTouchDataCallback magTouchDataCallback;
    protected MagTouch magTouch;
    private QuaternionMeanFilter magFilter;
    double delaySec = Settings.accGyroDelay;

    private ArrayList<IMUData> accBuffer;
    private ArrayList<IMUData> gyroBuffer;

    public MagTouchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        magTouchDataCallback = new OnMagTouchDataCallback();
        magTouch = new MagTouch(magTouchDataCallback);
        magTouch.start();
        magFilter = new QuaternionMeanFilter(40);
        accBuffer = new ArrayList<>();
        gyroBuffer = new ArrayList<>();
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        super.onSensorChanged(event);

        if(magTouch == null)
        {
            return;
        }

        int sensorType = event.sensor.getType();
        if(sensorType == Sensor.TYPE_GYROSCOPE)
        {
            Quaternion acc = new Quaternion(this.accReading);
            Quaternion gyro = new Quaternion(this.gyroReading);
            Quaternion mag = new Quaternion(this.magReading);

            //magFilter.push(mag);


            IMUData accData = new IMUData(acc, null, null);
            IMUData gyroData = new IMUData(null, gyro, null);
            putInTimedBuffer(accBuffer, accData, delaySec);
            putInTimedBuffer(gyroBuffer, gyroData, delaySec);
            acc = accBuffer.get(0).acc.copy();
            gyro = gyroBuffer.get(0).gyro.copy();

            IMUData imuData = new IMUData(acc, gyro, mag);

            magTouch.put(imuData);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
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
    public void onPause()
    {
        super.onPause();
        if(magTouch == null)
        {
            return;
        }
        magTouch.stop();
    }

    protected void runMagTouch(MagTouchRequestPacket packet)
    {
        magTouch.requestRun(packet);
    }

    protected abstract void handleCameData(CameData cameData);
    protected abstract void handleClassified(MagTouchRequestPacket packet, CameData cameData, String finger);
    protected abstract void handleStabilized();
    protected abstract void handleStateChangedTo(int state);
    protected abstract void respondCameDataAfter(MagTouchRequestPacket packet, CameData cameData);

    public class OnMagTouchDataCallback implements MagTouchCallback
    {

        @Override
        public void doneStep(CameData cameData) {

            handleCameData(cameData);
        }

        @Override
        public void classified(MagTouchRequestPacket packet, CameData cameData, String finger) {
            handleClassified(packet, cameData, finger);
        }

        @Override
        public void respondAfter(MagTouchRequestPacket packet, CameData cameData) {
            respondCameDataAfter(packet, cameData);
        }

        @Override
        public void stabilized() {
            handleStabilized();
        }

        @Override
        public void stateChangedTo(int state) {
            handleStateChangedTo(state);
        }
    }

    private void putInTimedBuffer(ArrayList<IMUData> buffer, IMUData nowData, double windowTime)
    {
        buffer.add(nowData);
        IMUData first = buffer.get(0);
        if(first == null)
        {
            return;
        }
        double now = nowData.timestamp;
        while((now - first.timestamp) > windowTime)
        {
            buffer.remove(0);
            first = buffer.get(0);
            if(first == null) break;
        }
    }
}
