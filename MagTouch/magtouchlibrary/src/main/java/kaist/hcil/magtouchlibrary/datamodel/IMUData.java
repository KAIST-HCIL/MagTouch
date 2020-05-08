package kaist.hcil.magtouchlibrary.datamodel;

import kaist.hcil.magtouchlibrary.util.NowTimestamp;

public class IMUData {
    public Quaternion acc;
    public Quaternion gyro;
    public Quaternion mag;
    public double timestamp;
    public IMUData(Quaternion acc, Quaternion gyro, Quaternion mag, double timestamp)
    {
        assert acc != null;
        assert gyro != null;
        assert mag != null;

        this.acc = acc;
        this.gyro = gyro;
        this.mag = mag;
        this.timestamp = timestamp;
    }

    public IMUData(Quaternion acc, Quaternion gyro, Quaternion mag)
    {
        assert acc != null;
        assert gyro != null;
        assert mag != null;

        this.acc = acc;
        this.gyro = gyro;
        this.mag = mag;
        this.timestamp = NowTimestamp.inSec();
    }

    public double getDipAngle()
    {
        double accNorm = acc.getNorm();
        double magNorm = mag.getNorm();
        double cos = (acc.x * mag.x + acc.y * mag.y + acc.z * mag.z) / (accNorm * magNorm);

        double dip = Math.acos(cos);
        return Math.toDegrees(dip);
    }

    public IMUData copy()
    {
        return new IMUData(acc.copy(), gyro.copy(), mag.copy(), timestamp);
    }

    @Override
    public String toString()
    {
        return String.format("%s,%s,%s,%.3f", acc.toString(), gyro.toString(), mag.toString(), timestamp);
    }
}
