package kaist.hcil.magtouchlibrary.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kaist.hcil.magtouchlibrary.Settings;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.datamodel.TapData;
import kaist.hcil.magtouchlibrary.log.LogWriter;
import kaist.hcil.magtouchlibrary.log.LogWriterCallback;
import kaist.hcil.magtouchlibrary.ml.MLNode;
import kaist.hcil.magtouchlibrary.util.NowTimestamp;

/*
    A program for the in-situation test in MagTouch
 */

public class InSituExpFragment extends TrainFragment {
    private boolean isRecording = Settings.recordRawDataWhileTest;
    double starttime = 0.0;
    double recordTimestamp = 0.0;
    double recordPeriod = Settings.recordPeriodWhileTest;

    private int secInMillis = 1000;
    private int numChunks = Settings.inSituTestNumChunk;
    private double intervalMin = Settings.inSituTestMinInterval;
    private double intervalMax = Settings.inSituTestMaxInterval;
    private double firstInterval = Settings.inSituTestFirstInterval;
    protected ArrayList<Double> intervals; // in sec;
    protected ArrayList<ArrayList> tapChunks;

    protected List<TapData> currentChunk;
    TapData currentTap = null;

    ArrayList<String> logBuffer;
    Handler handler;

    boolean isWaitingForNextChunk = false;
    private RecordLogCallback logCallback;

    Vibrator vibrator;

    public InSituExpFragment() {

        // Required empty public constructor
    }
    public static InSituExpFragment newInstance(int shape, int targetSize) {
        InSituExpFragment fragment = new InSituExpFragment();
        Bundle args = new Bundle();
        args.putInt(SHAPE, shape);
        args.putInt(TARGET_SIZE, targetSize);
        fragment.setArguments(args);
        return fragment;
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
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    if(currentTap == null)
                    {
                        return false;
                    }
                    if(currentTap.isValidTap(x, y))
                    {
                        //toggleBackground(Color.YELLOW);
                        targetView.setCurTap(null);
                        MagTouchRequestPacket packet = new MagTouchRequestPacket(NowTimestamp.inSec(), x, y, currentTap.getFinger(), true);
                        runMagTouch(packet);


                    }
                }
                return false;
            }
        });

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        handler = new Handler();
        File storageDir = Environment.getExternalStorageDirectory();
        magTouch.loadModels(storageDir);
        logBuffer = new ArrayList<>();

        if(isRecording)
        {
            logWriter = new LogWriter(storageDir, LogWriter.TYPE_SITU);
            logCallback = new RecordLogCallback();
            logWriter.init(logCallback);
        }


        numRepeat = Settings.inSituTestNumTargetRepeat;
        progressBar.setVisibility(View.VISIBLE);
        return view;
    }

    public static InSituExpFragment newInstance() {
        InSituExpFragment fragment = new InSituExpFragment();
        return fragment;
    }

    @Override
    public void handleStabilized() {
        generateIntervals();
        generateTapChunks();
        startBlock();
        progressBar.setVisibility(View.GONE);
    }

    protected void generateIntervals()
    {
        Random random = new Random();
        intervals = new ArrayList<>();
        intervals.add(firstInterval);
        for(int i=0; i<numChunks-1; i++)
        {
            double r = random.nextDouble();
            double interval = (intervalMax - intervalMin) * r + intervalMin;
            intervals.add(interval);
        }
    }

    protected void generateTapChunks()
    {
        tapChunks = new ArrayList<>();
        if(shape == CIRCLE)
        {
            generateTasksCircle();
        }
        else
        {
            generateTasksRect();
        }

        int numTasks = tasks.size();
        int numTasksPerChunk = numTasks / numChunks;

        int i=0;
        for(; i<(numChunks - 1); i++)
        {
            tapChunks.add(new ArrayList(tasks.subList(i * numTasksPerChunk, (i+1) * numTasksPerChunk)));
        }
        tapChunks.add(new ArrayList(tasks.subList(i * numTasksPerChunk,tasks.size())));
    }

    @Override
    protected void toNextTap()
    {
        if(currentChunk.isEmpty())
        {
            targetView.setCurTap(null);
            currentTap = null;
            waitForNextChunk();
        }
        else
        {
            currentTap = currentChunk.remove(0);
            targetView.setCurTap(currentTap);
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
        //String magStr = Double.toString(cameData.earthNorth.getNorm());

        String log = tag + "," + isAnomalyStr + "," + fingerMagStr + "," + orientationStr + "," + northStr +"," + magStr +"," + Double.toString(cameData.imuData.timestamp);
        //Log.d("FingMagLog", log);
        logWriter.writeLog(log);
    }

    protected void waitForNextChunk()
    {
        if(isWaitingForNextChunk)
        {
            return;
        }

        isWaitingForNextChunk = true;

        if(intervals.isEmpty())
        {
            // finish the test
            if(isRecording)
            {
                endRecording();
            }
            else
            {
                dumpLogs();
            }

            return;
        }
        int delayTime = (int) (intervals.remove(0) * secInMillis);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toNextChunk();
                toNextTap();
                isWaitingForNextChunk = false;
            }
        }, delayTime);
    }

    protected void toNextChunk()
    {
        if(tapChunks.isEmpty())
        {
            // test is finished
            currentChunk = null;
        }
        else
        {
            currentChunk = tapChunks.remove(0);
            vibrator.vibrate(500);
        }
    }

    @Override
    protected void startBlock()
    {
        waitForNextChunk();
    }

    @Override
    protected void dumpLogs()
    {
        File storageDir = Environment.getExternalStorageDirectory();
        logWriter = new LogWriter(storageDir, LogWriter.TYPE_SITU);
        LogWriterCallback callback = new TrainLogCallback(logBuffer.size());
        logWriter.init(callback);
        logWriter.writeLogChunk(logBuffer);
    }

    @Override
    public void handleClassified(MagTouchRequestPacket packet, CameData cameData, String predictedFinger)
    {
        String log = MLNode.toLog(packet, cameData);
        String timeStr = String.format("%.6f, %.6f", packet.timestamp, cameData.imuData.timestamp);
        log = log + "," + predictedFinger + ',' + timeStr;

        if(isRecording)
        {
            logWriter.writeLog(log);
        }
        else
        {
            logBuffer.add(log);
        }

        toNextTap();
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
