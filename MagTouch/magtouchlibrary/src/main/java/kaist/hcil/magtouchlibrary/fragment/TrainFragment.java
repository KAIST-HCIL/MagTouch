package kaist.hcil.magtouchlibrary.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;

import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.datamodel.TapData;
import kaist.hcil.magtouchlibrary.fragment.core.TargetFragment;
import kaist.hcil.magtouchlibrary.log.LogWriter;
import kaist.hcil.magtouchlibrary.log.LogWriterCallback;
import kaist.hcil.magtouchlibrary.ml.MLNode;
import kaist.hcil.magtouchlibrary.util.NowTimestamp;

public class TrainFragment extends TargetFragment {
    ArrayList<String> logBuffer;
    LogWriter logWriter;

    public TrainFragment() {
        logBuffer = new ArrayList<>();
    }

    public static TrainFragment newInstance(int shape, int targetSize) {
        TrainFragment fragment = new TrainFragment();
        Bundle args = new Bundle();
        args.putInt(SHAPE, shape);
        args.putInt(TARGET_SIZE, targetSize);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                        MagTouchRequestPacket packet = new MagTouchRequestPacket(NowTimestamp.inSec(), x, y, curTap.getFinger(), false);
                        runMagTouch(packet);
                    }
                }
                return false;
            }
        });

        return view;
    }


    protected void toggleBackground(final int color)
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

    protected void dumpLogs()
    {

        File storageDir = Environment.getExternalStorageDirectory();
        logWriter = new LogWriter(storageDir, LogWriter.TYPE_ML);
        LogWriterCallback callback = new TrainLogCallback(logBuffer.size());
        logWriter.init(callback);
        logWriter.writeLogChunk(logBuffer);
    }

    public class TrainLogCallback implements LogWriterCallback
    {

        private int numChunk;
        private int doneChunkCount;
        public TrainLogCallback(int numChunk)
        {
            this.numChunk = numChunk;
            doneChunkCount = 0;
        }

        @Override
        public void doneWriting() {
            doneChunkCount += 1;

            if(doneChunkCount >= numChunk)
            {
                logWriter.terminate();
                finish();
            }
        }
    }

    @Override
    public void respondCameDataAfter(MagTouchRequestPacket packet, CameData cameData) {
        String log = MLNode.toLog(packet, cameData);
        logBuffer.add(log);
        toNextTap();

        if(isFinished())
        {
            dumpLogs();
        }
    }

    private void finish()
    {
        getActivity().finish();
    }
}

