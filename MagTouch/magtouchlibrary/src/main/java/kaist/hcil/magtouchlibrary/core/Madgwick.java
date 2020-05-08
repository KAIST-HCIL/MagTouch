package kaist.hcil.magtouchlibrary.core;

import kaist.hcil.magtouchlibrary.Settings;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.IMUData;
import kaist.hcil.magtouchlibrary.datamodel.Matrix2D;
import kaist.hcil.magtouchlibrary.datamodel.Quaternion;

public class Madgwick {
    public static int TYPE_GYRO = 200;
    public static int TYPE_GYRO_ACC = 400;
    public static int TYPE_GYRO_ACC_MAG = 600;

    private double accAnomalyThrs = Settings.accDistortedThreshold;
    private Quaternion q;
    private Quaternion north;
    private Quaternion prevMag;
    private double beta;
    private double prevTimestamp;

    public static Quaternion calNorth(Quaternion q, Quaternion m)
    {
        Quaternion h = Quaternion.rotate(m, q);

        return h;
    }

    public Madgwick(Quaternion q, double sigma)
    {
        this.q = q;
        beta = Math.sqrt(3.0/4.0) * sigma;
        north = new Quaternion(0,1,0,0);
    }

    public CameData step(IMUData imuData, int sensorType)
    {
        if(prevTimestamp == 0)
        {
            prevTimestamp = imuData.timestamp;
            prevMag = imuData.mag.copy();
            return new CameData(imuData, q.copy(), 0, false);
        }

        double dt = imuData.timestamp - prevTimestamp;
        prevTimestamp = imuData.timestamp;

        int newSensorType = reconfigureSensorType(prevMag, imuData, sensorType);

        if(newSensorType > 0)
        {
            sensorType = newSensorType;
        }

        boolean isMagUpdated = !prevMag.equals(imuData.mag);

        boolean needNorthUpdate = false;
        if(sensorType == TYPE_GYRO_ACC_MAG)
        {
            needNorthUpdate = true;
        }

        prevMag = imuData.mag.copy();

        q = gradientDescent(sensorType, q, imuData, dt);

        if(needNorthUpdate)
        {
            north = calNorth(q, imuData.mag);
        }

        return new CameData(imuData.copy(), q.copy(), north.copy(), 0, isMagUpdated);
    }

    public Quaternion getQ()
    {
        return q;
    }

    public void rewindTo(Quaternion orientation, Quaternion north, double prevTime)
    {
        prevTimestamp = prevTime;
        this.q = orientation;
        this.north = north;
    }

    public void setBeta(double beta)
    {
        this.beta = beta;
    }

    private Quaternion calGyroGrad(Quaternion q, Quaternion w)
    {
        return Quaternion.mult(Quaternion.mult(q, w), 0.5);
    }

    private Quaternion calAccGrad(Quaternion q, Quaternion a)
    {
        Matrix2D fg = calAccCost(q, a);
        Matrix2D J = calAccJacob(q);

        Matrix2D grad = Matrix2D.mult(Matrix2D.transpose(J), fg);

        Quaternion dq = new Quaternion(grad.array[0][0], grad.array[1][0], grad.array[2][0], grad.array[3][0]);

        return dq;
    }

    private Matrix2D calAccCost(Quaternion q, Quaternion a)
    {
        a = Quaternion.normalize(a);
        Matrix2D fg = new Matrix2D(3, 1);

        fg.array[0][0] = 2.0 * (q.x * q.z - q.w * q.y) - a.x;
        fg.array[1][0] = 2.0 * (q.w * q.x + q.y * q.z) - a.y;
        fg.array[2][0] = 2.0 * (0.5 - q.x * q.x - q.y * q.y) - a.z;

        return fg;
    }

    private Matrix2D calAccJacob(Quaternion q)
    {
        Matrix2D J = new Matrix2D(3, 4);

        J.array[0][0] = -2.0 * q.y;
        J.array[0][1] = 2.0 * q.z;
        J.array[0][2] = -2.0 * q.w;
        J.array[0][3] = 2.0 * q.x;

        J.array[1][0] = 2.0 * q.x;
        J.array[1][1] = 2.0 * q.w;
        J.array[1][2] = 2.0 * q.z;
        J.array[1][3] = 2.0 * q.y;

        J.array[2][0] = 0;
        J.array[2][1] = -4.0 * q.x;
        J.array[2][2] = -4.0 * q.y;
        J.array[2][3] = 0;

        return J;
    }

    private Quaternion calMagGrad(Quaternion q, Quaternion m)
    {
        Quaternion b = calB(q, m);

        Matrix2D fb = calMagCost(q, m, b);
        Matrix2D J = calMagJacob(q, b);

        Matrix2D grad = Matrix2D.mult(Matrix2D.transpose(J), fb);

        Quaternion dq = new Quaternion(grad.array[0][0], grad.array[1][0], grad.array[2][0], grad.array[3][0]);
        return dq;
    }

    private Quaternion calB(Quaternion q, Quaternion m)
    {
        m = Quaternion.normalize(m);
        Quaternion h = Quaternion.normalize(Quaternion.rotate(m, q));


        double bx = Math.sqrt(h.x*h.x + h.y*h.y);
        double bz = h.z;

        return new Quaternion(0, bx, 0, bz);

    }

    private Matrix2D calMagCost(Quaternion q, Quaternion m, Quaternion b)
    {
        Matrix2D fb = new Matrix2D(3, 1);
        m = Quaternion.normalize(m);

        Quaternion qf = Quaternion.rotate(b, Quaternion.conjugate(q));
        qf = Quaternion.subtract(qf, m);

        fb.array[0][0] = qf.x;
        fb.array[1][0] = qf.y;
        fb.array[2][0] = qf.z;

        return fb;
    }

    private Matrix2D calMagJacob(Quaternion q, Quaternion b)
    {
        Matrix2D J = new Matrix2D(3, 4);

        double bx = b.x;
        double by = b.y;
        double bz = b.z;

        J.array[0][0] = 2.0*by*q.z - 2.0*bz*q.y;
        J.array[0][1] = 2.0*by*q.y + 2.0*bz*q.z;
        J.array[0][2] = -4.0*bx*q.y + 2.0*by*q.x - 2.0*bz*q.w;
        J.array[0][3] = -4.0*bx*q.z + 2.0*by*q.w + 2.0*bz*q.x;

        J.array[1][0] = -2.0*bx*q.z + 2.0*bz*q.x;
        J.array[1][1] = 2.0*bx*q.y - 4.0*by*q.x + 2.0*bz*q.w;
        J.array[1][2] = 2.0*bx*q.x + 2.0*bz*q.z;
        J.array[1][3] = -2.0*bx*q.w - 4.0*by*q.z + 2.0*bz*q.y;

        J.array[2][0] = 2.0*bx*q.y - 2.0*by*q.x;
        J.array[2][1] = 2.0*bx*q.z - 2.0*by*q.w - 4.0*bz*q.x;
        J.array[2][2] = 2.0*bx*q.w + 2.0*by*q.z - 4.0*bz*q.y;
        J.array[2][3] = 2.0*bx*q.x + 2.0*by*q.y;

        return J;
    }

    private Quaternion gradientDescent(int sensorType, Quaternion currentQ, IMUData imuData, double dt)
    {
        Quaternion grad = null;
        Quaternion gyroGrad = calGyroGrad(currentQ, imuData.gyro);
        Quaternion accGrad = calAccGrad(currentQ, imuData.acc);
        Quaternion magGrad = calMagGrad(currentQ, imuData.mag);

        grad = gyroGrad;
        if(sensorType == TYPE_GYRO)
        {
            grad = gyroGrad;
        }
        else if(sensorType == TYPE_GYRO_ACC)
        {
            accGrad = Quaternion.normalize(accGrad);
            grad = Quaternion.subtract(gyroGrad, Quaternion.mult(accGrad, beta));
        }
        else
        {
            Quaternion accMagGrad = Quaternion.add(accGrad, magGrad);
            accMagGrad = Quaternion.normalize(accMagGrad);
            accMagGrad = Quaternion.mult(accMagGrad, beta);

            grad = Quaternion.subtract(gyroGrad, accMagGrad);
        }
        Quaternion dq = Quaternion.mult(grad, dt);

        Quaternion newQ = Quaternion.add(currentQ, dq);
        newQ = Quaternion.normalize(newQ);

        return newQ;
    }

    private boolean checkAccAnomaly(IMUData imuData)
    {
        return imuData.acc.getNorm() > accAnomalyThrs;
    }

    private int reconfigureSensorType(Quaternion prevMag, IMUData imuData, int currentSensorType)
    {
        if(currentSensorType == TYPE_GYRO_ACC)
        {
            if(checkAccAnomaly(imuData))
            {
                return TYPE_GYRO;
            }
        }

        return -1;
    }
}
