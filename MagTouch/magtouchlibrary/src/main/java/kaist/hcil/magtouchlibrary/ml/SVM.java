package kaist.hcil.magtouchlibrary.ml;


import android.util.Log;

import java.io.File;
import java.io.IOException;

import kaist.hcil.magtouchlibrary.Settings;
import kaist.hcil.magtouchlibrary.datamodel.Matrix2D;
import kaist.hcil.magtouchlibrary.log.LogWriter;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class SVM {
    public static String modelFileName = "/model.txt";

    public static svm_model loadModel(File externalStorage)
    {
        String filePath = externalStorage.getAbsolutePath() + LogWriter.dataDir + modelFileName;
        try
        {
            return svm.svm_load_model(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveModel(File externalStorage, svm_model model)
    {
        String filePath = externalStorage.getAbsolutePath() + LogWriter.dataDir + modelFileName;
        try
        {
            svm.svm_save_model(filePath, model);
        }
        catch (Exception e)
        {
            Log.e("FingMag", e.toString());
        }

    }

    public static svm_model buildModel(svm_node[][] X, double[] y)
    {

        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.C = Settings.SVM_C;
        param.kernel_type = svm_parameter.RBF;
        param.gamma = Settings.SVM_gamma;
        param.cache_size = 200;
        param.eps = Settings.SVM_eps;

        svm_problem problem = new svm_problem();
        problem.x = X;
        problem.l = X.length;
        problem.y = y;

        return svm.svm_train(problem, param);
    }

    public static svm_node[][] buildNodes(Matrix2D mat)
    {
        int nRow = mat.getRow();
        int nCol = mat.getCol();
        svm_node[][] nodes = new svm_node[nRow][nCol];
        for(int i=0; i<nRow; i++)
        {
            nodes[i] = buildNode(mat.array[i]);
        }

        return nodes;
    }

    public static svm_node[] buildNode(double[] data)
    {
        assert data != null;
        svm_node[] node = new svm_node[data.length];
        for(int i=0; i<data.length; i++)
        {
            node[i] = new svm_node();
            node[i].index = i;
            node[i].value = data[i];
        }
        return node;
    }
}
