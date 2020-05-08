package kaist.hcil.magtouchlibrary.datamodel;

public class TapData {
    public class Finger
    {
        public static final String DONT_KNOW = "?";
        public static final String INDEX = "i";
        public static final String MIDDLE = "m";
        public static final String RING = "r";

    }

    private int targetX;
    private int targetY;
    private double tapX;
    private double tapY;

    private int r;
    private String finger;
    private String fingerDisplay;

    public TapData(int x, int y, String finger, int radius)
    {
        this.targetX = x;
        this.targetY = y;

        this.finger = finger;
        this.fingerDisplay = finger.toUpperCase();
        this.r = radius;
    }

    public void setTargetX(double x)
    {
        targetX = (int)x;
    }

    public void setTargetY(double y)
    {
        targetY = (int)y;
    }



    public int getTargetX()
    {
        return targetX;
    }

    public int getTargetY()
    {
        return targetY;
    }

    public void setTapX(double x)
    {
        tapX = x;
    }

    public void setTapY(double y)
    {
        tapY = y;
    }

    public double getTapX()
    {
        return tapX;
    }
    public double getTapY()
    {
        return tapY;
    }
    public int getRadius() { return r; }

    public String getFinger()
    {
        return finger;
    }
    public String getFingerFullName()
    {
        if(Finger.INDEX.equals(finger))
        {
            return "index";
        }
        else if (Finger.MIDDLE.equals(finger))
        {
            return "middle";
        }
        else if (Finger.RING.equals(finger))
        {
            return "ring";
        }
        else
        {
            return "?";
        }
    }
    public void setFinger(String finger)
    {
        if(Finger.INDEX.equals(finger))
        {
            this.finger = Finger.INDEX;
        }
        else if (Finger.MIDDLE.equals(finger))
        {
            this.finger = Finger.MIDDLE;
        }
        else if (Finger.RING.equals(finger))
        {
            this.finger = Finger.RING;
        }
        else
        {
            this.finger = Finger.DONT_KNOW;
        }

    }
    public String getFingerDisplay()
    {
        return fingerDisplay;
    }

    public boolean isValidTap(double tapX, double tapY)
    {
        double dx = tapX - targetX;
        double dy = tapY - targetY;

        if((dx*dx + dy*dy) < r*r )
        {
            return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return String.format("%f,%f,%s", tapX, tapY, finger);
    }
}
