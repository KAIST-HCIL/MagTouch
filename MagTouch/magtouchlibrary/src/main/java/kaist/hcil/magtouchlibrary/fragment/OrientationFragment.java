package kaist.hcil.magtouchlibrary.fragment;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import kaist.hcil.magtouchlibrary.R;
import kaist.hcil.magtouchlibrary.core.DistortionDetector;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.datamodel.Quaternion;
import kaist.hcil.magtouchlibrary.fragment.core.MagTouchFragment;
import kaist.hcil.magtouchlibrary.network.BluetoothSerial;

/*
    Visualized the true north.
 */
public class OrientationFragment extends MagTouchFragment {


    protected BluetoothSerial bSerial;
    protected final int REQUEST_ENABLE_BT = 1;
    long sendInterval = 10;
    long timestamp;
    boolean isForceingAnomaly;
    Handler UIHandler;
    View wrapper;

    public OrientationFragment() {
        // Required empty public constructor
    }

    public static OrientationFragment newInstance() {
        OrientationFragment fragment = new OrientationFragment();
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

        View view = inflater.inflate(R.layout.fragment_orientation, container, false);
        Button changeStateButton = view.findViewById(R.id.change_state_button);
        changeStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickChangeState();
            }
        });
        wrapper = view.findViewById(R.id.orientation_view_wrapper);
        UIHandler = new Handler();

        startBluetoothSerial();

        isForceingAnomaly = true;

        return view;
    }

    private void startBluetoothSerial()
    {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
        {
            // Device doesn't support bluetooth
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        bSerial = new BluetoothSerial(mBluetoothAdapter, null);
        bSerial.start();
    }

    @Override
    protected void handleCameData(CameData cameData) {

        if(bSerial == null || !bSerial.isRunning())
        {
            return;
        }

        long now = System.currentTimeMillis();
        if (timestamp == 0)
        {
            timestamp = now;
            return;
        }

        if ((now - timestamp) > sendInterval)
        {
            //Quaternion sendQ = Quaternion.normalize(cameData.earthNorth);
            Quaternion sendQ = cameData.earthNorth;
            bSerial.writeAsync(sendQ.toString() + "$");
            //bSerial.writeAsync(cameData.orientation.toString() + "$");
            timestamp = now;
        }


    }

    @Override
    protected void handleClassified(MagTouchRequestPacket packet, CameData cameData, String finger) {

    }

    @Override
    protected void handleStabilized() {
        if(isForceingAnomaly)
        {
            magTouch.forceState(DistortionDetector.IDLE);
        }
        changeBackground(Color.WHITE);
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeBackground(Color.BLACK);
            }
        }, 3000);
    }

    @Override
    protected void handleStateChangedTo(int state) {
        if(state == DistortionDetector.IDLE)
        {
            changeBackground(Color.BLACK);
        }
        else
        {
            changeBackground(Color.YELLOW);
        }
    }

    @Override
    protected void respondCameDataAfter(MagTouchRequestPacket packet, CameData cameData) {

    }

    public void clickChangeState()
    {
        if(!isForceingAnomaly)
        {
            isForceingAnomaly = true;
            magTouch.forceState(DistortionDetector.DISTORTED);
        }
        else
        {
            isForceingAnomaly = false;
            magTouch.forceState(DistortionDetector.IDLE);
        }

    }

    private void changeBackground(final int color)
    {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                wrapper.setBackgroundColor(color);
            }
        });

    }


}
