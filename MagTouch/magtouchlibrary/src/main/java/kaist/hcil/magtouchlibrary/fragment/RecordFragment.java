package kaist.hcil.magtouchlibrary.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.fragment.core.TargetFragment;
import kaist.hcil.magtouchlibrary.log.LogWriter;
import kaist.hcil.magtouchlibrary.log.LogWriterCallback;
import kaist.hcil.magtouchlibrary.util.NowTimestamp;

/*
    Record IMU data.
 */

public class RecordFragment extends TargetFragment {

    LogWriter logWriter;
    RecordLogCallback logCallback;
    double starttime = 0.0;
    double recordTimestamp = 0.0;
    double recordPeriod = 0.005;

    public RecordFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //File storageDir = Environment.getExternalStorageDirectory();
        //fingMag.loadModels(storageDir);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        targetView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    if(isValidTap(x, y))
                    {
                        toggleBackground(Color.YELLOW);
                        targetView.setCurTap(null);
                        MagTouchRequestPacket packet = new MagTouchRequestPacket(NowTimestamp.inSec(), x, y, getCurrentTap().getFinger(), false);
                        runMagTouch(packet);
                    }
                }
                return false;
            }
        });

        File storageDir = Environment.getExternalStorageDirectory();

        logWriter = new LogWriter(storageDir, LogWriter.TYPE_RAW);
        logCallback = new RecordLogCallback();
        logWriter.init(logCallback);

        numRepeat = 1;
        startBlock();

        return view;
    }

    public static RecordFragment newInstance(int shape, int targetSize) {
        RecordFragment fragment = new RecordFragment();
        Bundle args = new Bundle();
        args.putInt(SHAPE, shape);
        args.putInt(TARGET_SIZE, targetSize);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void handleCameData(CameData cameData)
    {

        if(starttime == 0)
        {
            starttime = cameData.imuData.timestamp;
            recordTimestamp = starttime;
        }
        if((cameData.imuData.timestamp - recordTimestamp) < recordPeriod)
        {
            return;
        }

        recordTimestamp = cameData.imuData.timestamp;

        String tag = "no_tap";
        String isAnomalyStr = Boolean.toString(cameData.isAnomaly);
        String imuDataStr = cameData.imuData.toString();
        String northStr = cameData.earthNorth.toString();
        String magStr = cameData.imuData.mag.toString();
        String orientationStr = cameData.orientation.toString();
        String fingerMagStr = cameData.fingerMag.toString();
        //String magStr = Double.toString(cameData.earthNorth.getNorm());

        String log = tag + "," + isAnomalyStr + "," + fingerMagStr + "," + orientationStr + "," + northStr +"," + magStr +"," + Double.toString(cameData.imuData.timestamp);
        //Log.d("FingMagLog", log);
        logWriter.writeLog(log);
    }

    @Override
    public void handleStabilized()
    {
        super.handleStabilized();
        //fingMag.forceState(AnomalyDetector.IDLE);
    }

    private void endRecording()
    {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        magTouch.stop();
        while(true)
        {
            if(logCallback.isAllDone()) break;
            // busy wait.
        }
        finish();
    }


    @Override
    public void handleClassified(MagTouchRequestPacket packet, CameData cameData, String predictedFinger)
    {
        String isAnomalyStr = Boolean.toString(cameData.isAnomaly);
        String tapStr = packet.toLog();
        String fingerMagStr = cameData.fingerMag.toString();
        String northMagStr = cameData.earthNorth.toString();
        String timeStr = String.format("%.6f, %.6f", packet.timestamp, cameData.imuData.timestamp);
        String tag = "tap";
        String log = tag + "," + isAnomalyStr + "," + tapStr + "," + fingerMagStr + ","  + northMagStr + "," + timeStr;
        logWriter.writeLog(log);
        toNextTap();

        if(isFinished())
        {
            endRecording();
        }

    }

    @Override
    public void respondCameDataAfter(MagTouchRequestPacket packet, CameData cameData)
    {
        String isAnomalyStr = Boolean.toString(cameData.isAnomaly);
        String tapStr = packet.toLog();
        String fingerMagStr = cameData.fingerMag.toString();
        String timeStr = String.format("%.6f, %.6f", packet.timestamp, cameData.imuData.timestamp);
        String tag = "tap";
        String log = tag + "," + isAnomalyStr + "," + tapStr + "," + fingerMagStr + "," + timeStr;
        logWriter.writeLog(log);
        toNextTap();

        if(isFinished())
        {
            endRecording();
        }
    }

    private void toggleBackground(final int color)
    {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                targetView.setBackgroundColor(color);
            }
        });
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                targetView.setBackgroundColor(Color.BLACK);
            }
        }, 100);
    }

    class RecordLogCallback implements LogWriterCallback
    {

        private int doneLogCount;
        public RecordLogCallback()
        {
            doneLogCount = 0;
        }

        public boolean isAllDone()
        {
            return !logWriter.isLogRemaining();
        }

        @Override
        public void doneWriting() {
            doneLogCount++;
        }
    }

    public void finish()
    {
        getActivity().finish();
    }
}
