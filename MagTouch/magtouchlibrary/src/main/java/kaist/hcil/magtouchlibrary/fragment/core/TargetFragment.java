package kaist.hcil.magtouchlibrary.fragment.core;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaist.hcil.magtouchlibrary.R;
import kaist.hcil.magtouchlibrary.Settings;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.datamodel.TapData;
import kaist.hcil.magtouchlibrary.view.TargetView;

/*
    A base fragment for target selection or
    fragment that uses simple targets.
 */

public class TargetFragment extends MagTouchFragment {


    protected static final String SHAPE = "shape";
    protected static final String TARGET_SIZE = "target_size";

    public static int RECT = 1;
    public static int CIRCLE = 2;
    public static int largeRadius = 80;
    public static int smallRadius = 50;
    protected int shape;

    public static int targetRadius;

    protected int height;
    protected int width;

    protected int taskIdx = 0;

    protected List<TapData> tasks;
    protected TargetView targetView;
    protected ProgressBar progressBar;
    protected Handler UIHandler;
    protected int numRepeat = Settings.defaultNumTargetRepeat;


    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    protected int numXaxis = 4;
    protected int numYaxis = 4;


    public TargetFragment() {
        // Required empty public constructor
    }

    public static TargetFragment newInstance() {
        TargetFragment fragment = new TargetFragment();
        return fragment;
    }

    private void checkPermissions()
    {
        Activity activity = getActivity();
        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shape = getArguments().getInt(SHAPE);
            targetRadius = getArguments().getInt(TARGET_SIZE);
        }
        checkPermissions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_target, container, false);


        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.post(new Runnable() {
                    public void run() {
                        view.getHeight(); //height is ready
                        width = view.getMeasuredWidth();
                        height = view.getMeasuredHeight();
                    }
                });
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        targetView = view.findViewById(R.id.target_view);
        progressBar = view.findViewById(R.id.target_progress_bar);
        UIHandler = new Handler();

        return view;
    }

    @Override
    protected void handleCameData(CameData cameData) {

    }

    @Override
    protected void handleClassified(MagTouchRequestPacket packet, CameData cameData, String finger) {

    }

    @Override
    protected void handleStabilized() {
        startBlock();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void handleStateChangedTo(int state) {

    }

    @Override
    protected void respondCameDataAfter(MagTouchRequestPacket packet, CameData cameData) {

    }

    protected void generateTasksRect()
    {
        ArrayList<String> fingers = new ArrayList<>();
        fingers.add(TapData.Finger.RING);
        fingers.add(TapData.Finger.MIDDLE);
        fingers.add(TapData.Finger.INDEX);

        ArrayList<Integer> xaxis = new ArrayList<>();
        int xRange = width - 2*targetRadius;
        int xInterval =xRange / (numXaxis-1);
        for(int i=0; i<numXaxis; i++)
        {
            xaxis.add(xInterval * i + targetRadius);
        }

        ArrayList<Integer> yaxis = new ArrayList<>();
        int yRange = height - 2*targetRadius;
        int yInterval =yRange / (numYaxis-1);
        for(int i=0; i<numYaxis; i++)
        {
            yaxis.add(yInterval * i + targetRadius);
        }

        tasks = new ArrayList<>();

        // 1. add test data
        for(String f : fingers)
        {
            for(int i = 0; i< numRepeat; i++)
            {
                for(Integer x : xaxis)
                {
                    for(Integer y : yaxis)
                    {
                        tasks.add(new TapData(x,y, f, targetRadius));
                    }
                }
            }
        }

        Collections.shuffle(tasks);
        taskIdx = 0;
    }

    protected void generateTasksCircle()
    {
        ArrayList<String> fingers = new ArrayList<>();
        fingers.add(TapData.Finger.RING);
        fingers.add(TapData.Finger.MIDDLE);
        fingers.add(TapData.Finger.INDEX);

        ArrayList<Integer> angles = new ArrayList<>();
        angles.add(0);
        angles.add(45);
        angles.add(90);
        angles.add(135);
        angles.add(180);
        angles.add(225);
        angles.add(270);
        angles.add(315);

        ArrayList<Integer> radiuses = new ArrayList<>();
        radiuses.add(width/2 - targetRadius);
        radiuses.add(width/4 - targetRadius/2);

        tasks = new ArrayList<>();

        // 1. add test data
        for(String f : fingers)
        {
            for(int i = 0; i< numRepeat; i++)
            {
                for(Integer angle : angles)
                {
                    for(Integer radius : radiuses)
                    {
                        double rad = Math.PI * angle / 180;
                        int x = (int)Math.round(width/2 + radius * Math.cos(rad));
                        int y = (int)Math.round(height/2 + radius * Math.sin(rad));
                        tasks.add(new TapData(x,y, f, targetRadius));
                    }
                }
                tasks.add(new TapData(width/2,height/2, f, targetRadius));
            }
        }

        Collections.shuffle(tasks);
        taskIdx = 0;
    }

    protected boolean isValidTap(double x, double y)
    {
        if(taskIdx >= tasks.size())
        {
            return false;
        }

        TapData currentTap = targetView.getCurTap();
        if(currentTap == null)
        {
            return false;
        }

        return currentTap.isValidTap(x, y);
    }

    protected void toNextTap()
    {

        taskIdx++;
        if(taskIdx < tasks.size())
        {
            TapData nextTap= tasks.get(taskIdx);
            targetView.setCurTap(nextTap);
        }
        else
        {
            targetView.setCurTap(null);
        }
    }

    protected TapData getCurrentTap()
    {
        TapData currentTap = tasks.get(taskIdx);
        return currentTap;
    }

    protected void startBlock()
    {
        if(shape == RECT)
        {
            generateTasksRect();
        }
        else
        {
            generateTasksCircle();
        }
        targetView.setCurTap(tasks.get(taskIdx));
    }

    protected boolean isFinished()
    {
        return taskIdx >= tasks.size();
    }

}
