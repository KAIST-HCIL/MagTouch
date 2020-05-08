package kaist.hcil.magtouchlibrary.datamodel;

public class Quaternion {
    public double w;
    public double x;
    public double y;
    public double z;

    static private final double epsilon = 1e-9;

    public static Quaternion setPrecision(Quaternion q, int numFloating)
    {
        double multVal = Math.pow(10 ,(double) numFloating);
        double w = cutPrecision(q.w, multVal);
        double x = cutPrecision(q.x, multVal);
        double y = cutPrecision(q.y, multVal);
        double z = cutPrecision(q.z, multVal);
        return new Quaternion(w, x, y, z);
    }

    private static double cutPrecision(double val, double multiVal)
    {
        double cutVal = Math.round(val * multiVal);
        return cutVal / multiVal;
    }

    public static Quaternion normalize(Quaternion q)
    {
        double norm = q.getNorm();
        if (-epsilon <= norm && norm <= epsilon)
        {
            return new Quaternion(0,0,0,0);
        }
        double w = q.w/norm;
        double x = q.x/norm;
        double y = q.y/norm;
        double z = q.z/norm;

        return new Quaternion(w, x, y, z);
    }


    public static Quaternion mult(Quaternion q, Quaternion r)
    {
        double w = r.w * q.w - r.x * q.x - r.y * q.y - r.z * q.z;
        double x = r.w * q.x + r.x * q.w - r.y * q.z + r.z * q.y;
        double y = r.w * q.y + r.x * q.z + r.y * q.w - r.z * q.x;
        double z = r.w * q.z - r.x * q.y + r.y * q.x + r.z * q.w;

        return new Quaternion(w, x, y, z);
    }

    public static Quaternion mult(Quaternion q, double val)
    {
        double w = q.w * val;
        double x = q.x * val;
        double y = q.y * val;
        double z = q.z * val;

        return new Quaternion(w, x, y, z);
    }

    public static double dot(Quaternion q1, Quaternion q2)
    {
        return q1.w * q2.w + q1.x * q2.x + q1.y * q2.y + q1.z * q2.z;
    }

    public static Quaternion add(Quaternion q1, Quaternion q2)
    {
        return new Quaternion(q1.w + q2.w, q1.x + q2.x, q1.y + q2.y, q1.z + q2.z);
    }

    public static Quaternion subtract(Quaternion q1, Quaternion q2)
    {
        return new Quaternion(q1.w - q2.w, q1.x - q2.x, q1.y- q2.y, q1.z - q2.z);
    }

    public static Quaternion eulerToQuaternion(double pitch, double roll, double yaw)
    {
        double cy = Math.cos(yaw * 0.5);
        double sy = Math.sin(yaw * 0.5);
        double cr = Math.cos(roll * 0.5);
        double sr = Math.sin(roll * 0.5);
        double cp = Math.cos(pitch * 0.5);
        double sp = Math.sin(pitch * 0.5);

        double qw = cy * cr * cp + sy * sr * sp;
        double qx = cy * sr * cp - sy * cr * sp;
        double qy = cy * cr * sp + sy * sr * cp;
        double qz = sy * cr * cp - cy * sr * sp;

        return new Quaternion(qw, qx, qy, qz);
    }

    public static Quaternion conjugate(Quaternion q)
    {
        return new Quaternion(q.w, -q.x, -q.y, -q.z);
    }

    public static Quaternion rotate(Quaternion p, Quaternion q)
    {
        // rotate quaternion p(point) by quaternion q
        Quaternion conjQ = conjugate(q);
        Quaternion rotated = mult(q, p);
        return mult(rotated, conjQ);

    }

    public Quaternion(double w, double x, double y, double z)
    {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Quaternion(float[] xyz)
    {
        this.w = 0;
        this.x = xyz[0];
        this.y = xyz[1];
        this.z = xyz[2];
    }

    public double getNorm()
    {
        return Math.sqrt(this.w * this.w + this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public void clip()
    {
        if(Math.abs(w) < epsilon)
        {
            w = 0;
        }
        if(Math.abs(x) < epsilon)
        {
            x = 0;
        }
        if(Math.abs(y) < epsilon)
        {
            y = 0;
        }
        if(Math.abs(z) < epsilon)
        {
            z = 0;
        }
    }

    public Quaternion copy()
    {
        return new Quaternion(w, x, y, z);
    }

    @Override
    public String toString()
    {
        return String.format("%.12f,%.12f,%.12f,%.12f", w, x, y, z);
    }

    public boolean equals(Quaternion q)
    {
        return ((this.w == q.w) &&(this.x == q.x) && (this.y == q.y) && (this.z == q.z));
    }
}
