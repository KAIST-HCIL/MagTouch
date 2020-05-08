package kaist.hcil.magtouchlibrary.datamodel;

public class CameData {
    public IMUData imuData;
    public Quaternion orientation;
    public Quaternion earthNorth;
    public Quaternion sensorNorth;
    public Quaternion fingerMag;
    public boolean isAnomaly;
    public double anomalyIndex;
    public boolean isMagUpdated;

    public CameData()
    {

    }

    public CameData(IMUData data, Quaternion orientation, double anomalyIndex, boolean isMagUpdated)
    {
        this.imuData = data;
        this.orientation = orientation;
        this.anomalyIndex = anomalyIndex;
        this.isMagUpdated = isMagUpdated;
    }

    public CameData(IMUData data, Quaternion orientation, Quaternion earthNorth, double anomalyIndex, boolean isMagUpdated)
    {
        this.imuData = data;
        this.orientation = orientation;
        this.anomalyIndex = anomalyIndex;
        this.isMagUpdated = isMagUpdated;
        setEarthNorth(earthNorth);
    }

    public void setEarthNorth(Quaternion north)
    {
        this.earthNorth = north;
        //Log.d("FingMagLog", north.toString());
        this.sensorNorth = Quaternion.rotate(earthNorth, Quaternion.conjugate(orientation));
        this.fingerMag = Quaternion.subtract(imuData.mag, sensorNorth);
    }

    public CameData copy()
    {
        CameData copied = new CameData();
        copied.imuData = this.imuData.copy();
        copied.orientation = this.orientation.copy();
        copied.anomalyIndex = this.anomalyIndex;
        copied.isAnomaly = this.isAnomaly;
        copied.isMagUpdated = this.isMagUpdated;

        if(earthNorth != null)
        {
            copied.earthNorth = this.earthNorth.copy();
        }

        if(sensorNorth != null)
        {
            copied.sensorNorth = this.sensorNorth.copy();
        }

        if(fingerMag != null)
        {
            copied.fingerMag = this.fingerMag.copy();
        }

        return copied;
    }
}
