package kaist.hcil.magtouchlibrary.ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import kaist.hcil.magtouchlibrary.datamodel.Matrix2D;
import kaist.hcil.magtouchlibrary.log.LogWriter;

public class MinMaxScaler {
    public static String modelFileName = "/scaler.txt";

    private double min;
    private double max;
    private Matrix2D minMaxHolder;

    public static void saveModel(File externalStorage, MinMaxScaler model)
    {
        String filePath = externalStorage.getAbsolutePath() + LogWriter.dataDir + modelFileName;
        try
        {
            FileOutputStream outputStream = new FileOutputStream(filePath, false);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String modelStr = model.toString();
            outputStreamWriter.append(modelStr);
            outputStream.flush();
            outputStreamWriter.flush();

            outputStreamWriter.close();
            outputStream.close();
        }
        catch (Exception e)
        {

        }
    }

    public static MinMaxScaler loadModel(File externalStorage)
    {
        String filePath = externalStorage.getAbsolutePath() + LogWriter.dataDir + modelFileName;
        ArrayList<String> lines = new ArrayList<>(3);
        try
        {
            FileInputStream inputStream = new FileInputStream(filePath);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while((line = bufferedReader.readLine()) != null)
            {
                lines.add(line);
            }
        }
        catch (Exception e)
        {

        }

        String[] minMaxLine = lines.get(0).split(",");
        double min = Double.parseDouble(minMaxLine[0]);
        double max = Double.parseDouble(minMaxLine[1]);

        String[] minLine = lines.get(1).split(",");
        int nDim = minLine.length;

        Matrix2D minMaxHolder = new Matrix2D(2, nDim);
        for(int i=0; i<nDim; i++)
        {
            minMaxHolder.array[0][i] =Double.parseDouble(minLine[i]);
        }

        String[] maxLine = lines.get(2).split(",");
        for(int i=0; i<nDim; i++)
        {
            minMaxHolder.array[1][i] =Double.parseDouble(maxLine[i]);
        }

        MinMaxScaler model = new MinMaxScaler(min, max, nDim);
        model.setMinMaxHolder(minMaxHolder);

        return model;
    }

    public MinMaxScaler(double min, double max, int dim)
    {
        this.min = min;
        this.max = max;
        minMaxHolder = new Matrix2D(2, dim); // 0: min, 1: max
        for(int j=0; j<dim; j++)
        {
            minMaxHolder.array[0][j] = Double.POSITIVE_INFINITY;
            minMaxHolder.array[1][j] = Double.NEGATIVE_INFINITY;
        }
    }

    public void setMinMaxHolder(Matrix2D minMaxHolder)
    {
        this.minMaxHolder = minMaxHolder;
    }

    public void fit(Matrix2D data)
    {
        assert data != null;
        assert data.getCol() == minMaxHolder.getCol();

        int dim = data.getCol();
        int nRow = data.getRow();

        for(int i=0; i<nRow; i++)
        {
            for(int j=0; j<dim; j++)
            {
                minMaxHolder.array[0][j] = Math.min(data.array[i][j], minMaxHolder.array[0][j]);
                minMaxHolder.array[1][j] = Math.max(data.array[i][j], minMaxHolder.array[1][j]);
            }
        }
    }

    public Matrix2D transform(Matrix2D data)
    {

        assert data != null;
        assert data.getCol() == minMaxHolder.getCol();
        Matrix2D transformed = new Matrix2D(data.getRow(), data.getCol());
        int dim = data.getCol();
        int nRow = data.getRow();

        for(int i=0; i<nRow; i++)
        {
            for(int j=0; j<dim; j++)
            {
                double minVal = minMaxHolder.array[0][j];
                double maxVal = minMaxHolder.array[1][j];
                double val = data.array[i][j];
                double tVal =  (val - minVal) / (maxVal - minVal);
                transformed.array[i][j] = tVal;
            }
        }
        return transformed;
    }

    @Override
    public String toString()
    {
        String str = String.format("%.6f,%.6f\n", min, max);
        str += minMaxHolder.toString();
        return str;
    }
}
