package kaist.hcil.magtouchlibrary.fragment;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import kaist.hcil.magtouchlibrary.R;
import kaist.hcil.magtouchlibrary.core.DistortionDetector;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.datamodel.Quaternion;
import kaist.hcil.magtouchlibrary.fragment.core.MagTouchFragment;
import kaist.hcil.magtouchlibrary.util.NowTimestamp;
import kaist.hcil.magtouchlibrary.view.ChartView;

/*
    Visualizes magnetic field of the magnet ring.
    You can change 'extractVizVec' to visualize other values.
 */
public class VisualizeFragment extends MagTouchFragment {


    ChartView chartView;
    TextView statusTextView;
    TextView anomalyIndexTextView;
    Runnable repeater;
    Handler repeatHandler;
    Handler UIHandler;
    double prevTime;
    double vizSamplingTime = 0.01;
    Quaternion prevMag = new Quaternion(0,0,0,0);

    public VisualizeFragment() {
        // Required empty public constructor
    }

    public static VisualizeFragment newInstance() {
        VisualizeFragment fragment = new VisualizeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    protected Quaternion extractVizVec(CameData data)
    {
        return data.fingerMag;
        /*
            Other possible examples:
            - return data.sensorNorth;
            - return Quaternion.mult(data.orientation, 50);
            - return Quaternion.mult(data.imuData.acc, 5);
            - return data.imuData.mag;
            - return data.earthNorth;
         */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View view = inflater.inflate(R.layout.fragment_visualize, container, false);

        chartView = view.findViewById(R.id.chart_view);
        statusTextView = view.findViewById(R.id.vis_status_text_view);
        anomalyIndexTextView = view.findViewById(R.id.anomaly_index_text_view);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.post(new Runnable() {
                    public void run() {
                        view.getHeight(); //height is ready
                        int width = view.getMeasuredWidth();
                        int height = view.getMeasuredHeight();
                        chartView.setSize(width, height);
                        chartView.init();
                    }
                });
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //int width = displayMetrics.widthPixels;
        //int height = displayMetrics.heightPixels;

        repeatHandler = new Handler();
        UIHandler = new Handler();

        statusTextView.setText("stabilizing...");
        prevTime = 0;
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

        if(chartView == null || repeatHandler == null || UIHandler == null)
        {
            return;
        }

        if(prevTime == 0)
        {
            prevTime = NowTimestamp.inSec();
        }

        if((NowTimestamp.inSec() - prevTime) > vizSamplingTime)
        {
            Quaternion visVec = extractVizVec(cameData);
            chartView.setData((float)visVec.x, (float)visVec.y, (float)visVec.z);
            prevTime = NowTimestamp.inSec();

            double val = cameData.earthNorth.getNorm();
            //final String indexStr = String.format("%.2f", cameData.anomalyIndex);
            final String indexStr = "";
            //final String indexStr = String.format("%.2f", val);
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    anomalyIndexTextView.setText(indexStr);
                }
            });
        }
    }

    @Override
    protected void handleClassified(MagTouchRequestPacket packet, CameData cameData, String finger) {

    }

    @Override
    protected void handleStabilized() {
        //statusTextView.setText("stabilized");
        statusTextView.setText("");
        //fingMag.forceState(AnomalyDetector.IDLE);
    }

    @Override
    protected void handleStateChangedTo(int state) {
        if(state == DistortionDetector.DISTORTED)
        {
            changeBackground(Color.WHITE);
        }
        else
        {
            changeBackground(Color.BLACK);
        }
    }

    @Override
    protected void respondCameDataAfter(MagTouchRequestPacket packet, CameData cameData) {

    }

    private void changeBackground(final int color)
    {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                chartView.setBackgroundColor(color);
            }
        });

    }

}
