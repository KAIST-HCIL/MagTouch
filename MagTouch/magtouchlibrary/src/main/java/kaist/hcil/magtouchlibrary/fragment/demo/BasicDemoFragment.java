package kaist.hcil.magtouchlibrary.fragment.demo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.io.File;

import kaist.hcil.magtouchlibrary.R;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.datamodel.TapData;
import kaist.hcil.magtouchlibrary.fragment.core.MagTouchFragment;
import kaist.hcil.magtouchlibrary.util.NowTimestamp;
import kaist.hcil.magtouchlibrary.view.TargetView;

public class BasicDemoFragment extends MagTouchFragment {
    protected TargetView targetView;
    protected ProgressBar progressBar;
    TapData currentTap;
    Paint textPaint = new Paint();

    protected static final String TARGET_SIZE = "target_size";

    private int targetRadius;

    public BasicDemoFragment() {
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);

        // Required empty public constructor
    }

    public static BasicDemoFragment newInstance(int targetSize) {
        BasicDemoFragment fragment = new BasicDemoFragment();
        Bundle args = new Bundle();
        args.putInt(TARGET_SIZE, targetSize);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetRadius = getArguments().getInt(TARGET_SIZE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_target, container, false);
        targetView = view.findViewById(R.id.target_view);
        targetView.setIsDemo(true);

        targetView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    currentTap = new TapData(x, y, TapData.Finger.DONT_KNOW, targetRadius);
                    targetView.setCurTap(currentTap);
                    MagTouchRequestPacket packet = new MagTouchRequestPacket(NowTimestamp.inSec(), x, y, TapData.Finger.DONT_KNOW, true);
                    runMagTouch(packet);

                }
                else if(event.getAction() == MotionEvent.ACTION_MOVE)
                {

                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    TapData curViewTap = targetView.getCurTap();
                    curViewTap.setTargetX(x);
                    curViewTap.setTargetY(y);
                    targetView.invalidate();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    currentTap = null;
                    targetView.setCurTap(null);
                }
                /*
                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    targetView.setCurTap(null);
                }
                */
                return true;
            }
        });
        progressBar = view.findViewById(R.id.target_progress_bar);

        // Load SVM Module
        File storageDir = Environment.getExternalStorageDirectory();
        magTouch.loadModels(storageDir);

        return view;
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

    }

    @Override
    protected void handleClassified(MagTouchRequestPacket packet, CameData cameData, String predictedFinger) {
        //TapData tapData = new TapData((int)packet.tapX, (int)packet.tapY, predictedFinger, targetRadius);
        //targetView.setCurTap(tapData);
        TapData curViewTap = targetView.getCurTap();
        if(curViewTap != null)
        {
            curViewTap.setFinger(predictedFinger);
        }
    }

    @Override
    protected void handleStabilized() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void handleStateChangedTo(int state) {

    }

    @Override
    protected void respondCameDataAfter(MagTouchRequestPacket packet, CameData cameData) {

    }
}
