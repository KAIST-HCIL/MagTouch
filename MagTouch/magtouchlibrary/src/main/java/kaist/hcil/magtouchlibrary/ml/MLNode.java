package kaist.hcil.magtouchlibrary.ml;

import java.util.ArrayList;

import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.datamodel.Matrix2D;
import kaist.hcil.magtouchlibrary.datamodel.TapData;

public class MLNode {
    public static int DIM = 5;

    public double x;
    public double y;
    public double mx;
    public double my;
    public double mz;
    public int finger;

    public static int fingerToNumber(String finger)
    {
        int number = 0;
        switch (finger)
        {
            case TapData.Finger.INDEX:
                number = 1;
                break;
            case TapData.Finger.MIDDLE:
                number = 2;
                break;
            case TapData.Finger.RING:
                number =3;
                break;
            default:
                break;
        }
        return number;
    }

    public static String numberToFinger(int number)
    {
        String finger = "";
        switch (number)
        {
            case 1:
                finger = TapData.Finger.INDEX;
                break;
            case 2:
                finger = TapData.Finger.MIDDLE;
                break;
            case 3:
                finger = TapData.Finger.RING;
                break;
            default:
                finger = TapData.Finger.DONT_KNOW;
                break;
        }
        return finger;
    }

    public static Matrix2D toX(ArrayList<MLNode> nodes)
    {
        int nRow = nodes.size();
        int nCol = DIM;

        Matrix2D mat = new Matrix2D(nRow, nCol);
        for(int i=0; i<nRow; i++)
        {
            double[] rowData = mat.array[i];
            MLNode node = nodes.get(i);
            rowData[0] = node.x;
            rowData[1] = node.y;
            rowData[2] = node.mx;
            rowData[3] = node.my;
            rowData[4] = node.mz;
        }

        return mat;
    }

    public Matrix2D toX()
    {
        Matrix2D mat = new Matrix2D(1, DIM);
        mat.array[0][0] = x;
        mat.array[0][1] = y;
        mat.array[0][2] = mx;
        mat.array[0][3] = my;
        mat.array[0][4] = mz;

        return mat;
    }

    public static double[] toY(ArrayList<MLNode> nodes)
    {
        double[] y = new double[nodes.size()];
        for(int i=0; i<nodes.size(); i++)
        {
            y[i] = nodes.get(i).finger;
        }
        return y;
    }

    public static String toLog(MagTouchRequestPacket packet, CameData cameData)
    {
        String isAnomalyStr = Boolean.toString(cameData.isAnomaly);
        String tapStr = packet.toLog();
        String magStr = cameData.imuData.mag.toString();
        String fingerMagStr = cameData.fingerMag.toString();
        String tag = "tap";
        String log = tag + "," + isAnomalyStr + "," + tapStr + "," + fingerMagStr + "," + magStr;
        return log;
    }

    public static MLNode parseLog(String str)
    {
        String[] split =  str.split(",");
        double x = Double.parseDouble(split[2]);
        double y = Double.parseDouble(split[3]);
        String finger = split[4];
        double mx = Double.parseDouble(split[6]);
        double my = Double.parseDouble(split[7]);
        double mz = Double.parseDouble(split[8]);

        return new MLNode(x, y, mx, my, mz, finger);
    }

    public MLNode(double x, double y, double mx, double my, double mz, String finger)
    {
        this.x = x;
        this.y = y;
        this.mx = mx;
        this.my = my;
        this.mz = mz;
        this.finger = fingerToNumber(finger);
    }

    @Override
    public String toString()
    {
        return String.format("%.8f,%.8f,%.8f,%.8f,%.8f,%s", x, y, mx, my, mz, numberToFinger(finger));
    }
}
