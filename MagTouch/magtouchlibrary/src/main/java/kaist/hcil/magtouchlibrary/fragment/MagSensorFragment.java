package kaist.hcil.magtouchlibrary.fragment;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kaist.hcil.magtouchlibrary.R;
import kaist.hcil.magtouchlibrary.fragment.core.SensorFragment;

/*
    Show basic magnetometer values
 */
public class MagSensorFragment extends SensorFragment {


    private TextView mxTextView;
    private TextView myTextView;
    private TextView mzTextView;
    private TextView accuracyTextView;
    private TextView mTotalTextView;

    public MagSensorFragment() {
        // Required empty public constructor
    }

    public static MagSensorFragment newInstance() {
        MagSensorFragment fragment = new MagSensorFragment();
        return fragment;
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        super.onSensorChanged(event);
        if(mxTextView == null || myTextView == null || mzTextView == null)
        {
            return;
        }
        float[] targetArray = magReading;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED)
        {
            mxTextView.setText(String.format("%.2f", targetArray[0]));
            myTextView.setText(String.format("%.2f", targetArray[1]));
            mzTextView.setText(String.format("%.2f", targetArray[2]));
            float magSquare = targetArray[0] *  targetArray[0] +  targetArray[1] *  targetArray[1] +  targetArray[2] *  targetArray[2];
            double magnitude = Math.sqrt((double) magSquare);
            mTotalTextView.setText(String.format("%.2f", magnitude));

            String accuracyText = accuracyToText(event.accuracy);
            accuracyTextView.setText(accuracyText);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mag_sensor, container, false);

        mxTextView = view.findViewById(R.id.mx_text);
        myTextView = view.findViewById(R.id.my_text);
        mzTextView = view.findViewById(R.id.mz_text);
        accuracyTextView = view.findViewById(R.id.accuracy_text);
        mTotalTextView = view.findViewById(R.id.mTotal_text);
        return view;
    }

    private String accuracyToText(int accuracy)
    {
        String accText = "";
        switch (accuracy) {
            case 0:
                accText = "unreliable";
                break;
            case 1:
                accText = "low accuracy";
                break;
            case 2:
                accText = "medium accuracy";
                break;
            case 3:
                accText = "high accuracy";
                break;
        }
        return accText;
    }

}
