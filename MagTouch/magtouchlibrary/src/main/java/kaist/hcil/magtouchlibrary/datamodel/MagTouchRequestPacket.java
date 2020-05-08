package kaist.hcil.magtouchlibrary.datamodel;

public class MagTouchRequestPacket {
    public double timestamp;
    public double tapX;
    public double tapY;
    public String finger;
    public boolean needClassification;

    public MagTouchRequestPacket(double timestamp, double tapX, double tapY, String finger, boolean needClassification)
    {
        this.finger = finger;
        this.timestamp = timestamp;
        this.tapX = tapX;
        this.tapY = tapY;
        this.needClassification = needClassification;
    }

    public String toLog()
    {
        return String.format("%.4f,%.4f,%s", tapX, tapY, finger);
    }
}
