package kaist.hcil.magtouchlibrary.util;

import java.util.ArrayList;

import kaist.hcil.magtouchlibrary.datamodel.Quaternion;

public class QuaternionMeanFilter {
    private int size;
    private int count;
    private Quaternion sum;
    private ArrayList<Quaternion> buffer;
    public QuaternionMeanFilter(int size)
    {
        this.size = size;
        count = 0;
        sum = new Quaternion(0,0,0,0);
        buffer = new ArrayList<>();
    }

    public void push(Quaternion q)
    {
        buffer.add(q);
        sum = Quaternion.add(sum, q);
        while(buffer.size() > size)
        {
            Quaternion rq = buffer.remove(0);
            sum = Quaternion.subtract(sum, rq);
        }
    }

    public Quaternion getMean()
    {
        if(buffer.isEmpty())
        {
            return new Quaternion(0,0,0,0);
        }
        return Quaternion.mult(sum, 1.0/buffer.size());
    }
}
