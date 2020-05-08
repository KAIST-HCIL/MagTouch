package kaist.hcil.magtouchlibrary.fragment;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;

import kaist.hcil.magtouchlibrary.R;
import kaist.hcil.magtouchlibrary.datamodel.Matrix2D;
import kaist.hcil.magtouchlibrary.log.LogReader;
import kaist.hcil.magtouchlibrary.log.LogWriter;
import kaist.hcil.magtouchlibrary.ml.MLNode;
import kaist.hcil.magtouchlibrary.ml.MinMaxScaler;
import kaist.hcil.magtouchlibrary.ml.SVM;
import libsvm.svm_model;
import libsvm.svm_node;

/*
    Create SVM model from the recorded data.
 */
public class MakeModelFragment extends Fragment {

    LogReader logReader;
    MinMaxScaler scaler;
    public MakeModelFragment() {
        // Required empty public constructor
    }

    public static MakeModelFragment newInstance() {
        MakeModelFragment fragment = new MakeModelFragment();
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                makeModel();
            }
        }, 500);
        return inflater.inflate(R.layout.fragment_make_model, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void makeModel()
    {
        File storageDir = Environment.getExternalStorageDirectory();
        logReader = new LogReader(storageDir, LogWriter.TYPE_ML);
        ArrayList<MLNode> mlNodes =  logReader.readMLNodes();
        Matrix2D matNodes = MLNode.toX(mlNodes);
        double[] y = MLNode.toY(mlNodes);
        scaler = new MinMaxScaler(0, 1, MLNode.DIM);
        scaler.fit(matNodes);
        Matrix2D scaled = scaler.transform(matNodes);
        svm_node[][] X = SVM.buildNodes(scaled);

        svm_model model = SVM.buildModel(X, y);

        SVM.saveModel(storageDir, model);
        MinMaxScaler.saveModel(storageDir, scaler);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void finish()
    {
        getActivity().finish();
    }

}
