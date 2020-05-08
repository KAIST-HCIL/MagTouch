package kaist.hcil.magtouchlibrary.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import kaist.hcil.magtouchlibrary.R;

/*
    Show short text that dismisses automatically.
 */
public class MessageFragment extends Fragment {


    private static final String MSG_TAG = "msg";

    private String msgParam;
    int dismissTime = 3000; //milli sec

    Handler dismissHandler;
    TextView msgTextView;

    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment newInstance(String msg) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString(MSG_TAG, msg);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            msgParam = getArguments().getString(MSG_TAG);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        dismissHandler = new Handler();
        dismissHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getFragmentManager().popBackStackImmediate();
            }
        }, dismissTime);

        msgTextView = (TextView) view.findViewById(R.id.msg_textview);
        msgTextView.setText(msgParam);

        return view;
    }

}
