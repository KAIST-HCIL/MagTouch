package kaist.hcil.magtouchlibrary.fragment.demo;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.io.File;

import kaist.hcil.magtouchlibrary.R;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.datamodel.TapData;
import kaist.hcil.magtouchlibrary.fragment.MessageFragment;
import kaist.hcil.magtouchlibrary.fragment.core.MagTouchFragment;
import kaist.hcil.magtouchlibrary.util.NowTimestamp;

/**
 * A simple {@link Fragment} subclass.
 */
public class MailAppFragment extends MagTouchFragment {


    protected ProgressBar progressBar;
    protected ImageButton multiFunctionButton;

    public MailAppFragment() {

        // Required empty public constructor
    }

    public static MailAppFragment newInstance() {
        MailAppFragment fragment = new MailAppFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_mail_app, container, false);
        progressBar = view.findViewById(R.id.progress_bar);
        multiFunctionButton = view.findViewById(R.id.multi_func_button);
        multiFunctionButton.setOnTouchListener(btnOnClickListener);

        loadSvmModule();

        return view;
    }

    private void loadSvmModule()
    {
        // Load SVM Module
        File storageDir = Environment.getExternalStorageDirectory();
        magTouch.loadModels(storageDir);
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
    protected void handleClassified(MagTouchRequestPacket packet, CameData cameData, String finger) {
        if (TapData.Finger.INDEX.equals(finger))
        {
            openMessageFragment("Reply");
        }
        else if(TapData.Finger.MIDDLE.equals(finger))
        {
            openMessageFragment("Archive");
        }
        else if (TapData.Finger.RING.equals(finger))
        {
            openMessageFragment("Delete");
        }
        else
        {
            openMessageFragment("?");
        }
    }

    public void openMessageFragment(String msg)
    {
        MessageFragment msgFragment = MessageFragment.newInstance(msg);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_container, msgFragment)
                .addToBackStack(null)
                .commit();
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

    private View.OnTouchListener btnOnClickListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            {
                int x = (int) motionEvent.getRawX();
                int y = (int) motionEvent.getRawY();
                MagTouchRequestPacket packet = new MagTouchRequestPacket(NowTimestamp.inSec(), x, y, TapData.Finger.DONT_KNOW, true);
                runMagTouch(packet);

            }
            //openMessageFragment("test");
            return false;
        }
    };

}
