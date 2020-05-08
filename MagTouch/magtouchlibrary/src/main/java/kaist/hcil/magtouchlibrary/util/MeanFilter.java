package kaist.hcil.magtouchlibrary.util;

import java.util.ArrayList;

public class MeanFilter {
    private int size;
    private double sum;
    private ArrayList<Double> buffer;

    public MeanFilter(int size)
    {
        this.size = size;
        buffer = new ArrayList<>();
    }

    public void reset()
    {
        sum = 0;
        buffer.clear();
    }

    public void push(double val)
    {
        buffer.add(val);
        sum += val;
        while(buffer.size() > size)
        {
            double rVal = buffer.remove(0);
            sum -= rVal;
        }
    }

    public double getMean()
    {
        if(buffer.isEmpty())
        {
            return 0;
        }
        return sum / buffer.size();
    }
}
