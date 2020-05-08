package kaist.hcil.magtouchlibrary.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import kaist.hcil.magtouchlibrary.Settings;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.datamodel.TapData;
import kaist.hcil.magtouchlibrary.log.LogWriter;
import kaist.hcil.magtouchlibrary.log.LogWriterCallback;
import kaist.hcil.magtouchlibrary.ml.MLNode;
import kaist.hcil.magtouchlibrary.util.NowTimestamp;

/*
    The test program.
 */

public class TestFragment extends TrainFragment {
    private boolean isRecording = Settings.recordRawDataWhileTest;
    double starttime = 0.0;
    double recordTimestamp = 0.0;
    double recordPeriod = Settings.recordPeriodWhileTest;

    private RecordLogCallback logCallback;

    public TestFragment() {
        // Required empty public constructor
    }

    public static TestFragment newInstance(int shape, int targetSize) {
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putInt(SHAPE, shape);
        args.putInt(TARGET_SIZE, targetSize);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File storageDir = Environment.getExternalStorageDirectory();
        magTouch.loadModels(storageDir);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        targetView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    double x = (double) event.getX();
                    double y = (double) event.getY();

                    if(isValidTap(x, y))
                    {
                        //toggleBackground(Color.YELLOW);
                        targetView.setCurTap(null);
                        TapData curTap = getCurrentTap();
                        MagTouchRequestPacket packet = new MagTouchRequestPacket(NowTimestamp.inSec(), x, y, curTap.getFinger(), true);
                        runMagTouch(packet);
                    }
                }
                return false;
            }
        });

        if(isRecording)
        {
            File storageDir = Environment.getExternalStorageDirectory();
            logWriter = new LogWriter(storageDir, LogWriter.TYPE_EXP);
            logCallback = new RecordLogCallback();
            logWriter.init(logCallback);
        }

        return view;
    }

    @Override
    protected void dumpLogs()
    {
        File storageDir = Environment.getExternalStorageDirectory();
        logWriter = new LogWriter(storageDir, LogWriter.TYPE_EXP);
        LogWriterCallback callback = new TrainFragment.TrainLogCallback(logBuffer.size());
        logWriter.init(callback);
        logWriter.writeLogChunk(logBuffer);
    }

    @Override
    public void handleClassified(MagTouchRequestPacket packet, CameData cameData, String predictedFinger)
    {
        String log = MLNode.toLog(packet, cameData);
        String timeStr = String.format("%.6f, %.6f", packet.timestamp, cameData.imuData.timestamp);
        log = log + "," + predictedFinger + "," + timeStr;
        if(isRecording)
        {
            logWriter.writeLog(log);
        }
        else
        {
            logBuffer.add(log);
        }


        toNextTap();

        if(isFinished())
        {
            if(isRecording)
            {
                endRecording();
            }
            else
            {
                dumpLogs();
            }
        }
    }

    @Override
    public void handleCameData(CameData cameData)
    {
        if(!isRecording)
        {
            return;
        }
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

        String tag = "rec";
        String isAnomalyStr = Boolean.toString(cameData.isAnomaly);
        String imuDataStr = cameData.imuData.toString();
        String northStr = cameData.earthNorth.toString();
        String magStr = cameData.imuData.mag.toString();
        String orientationStr = cameData.orientation.toString();
        String fingerMagStr = cameData.fingerMag.toString();

        String log = tag + "," + isAnomalyStr + "," + fingerMagStr + "," + orientationStr + "," + northStr +"," + magStr +"," + Double.toString(cameData.imuData.timestamp);
        logWriter.writeLog(log);
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

    public void finish()
    {
        getActivity().finish();
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
}
