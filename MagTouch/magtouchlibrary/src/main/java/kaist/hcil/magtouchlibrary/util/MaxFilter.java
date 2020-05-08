package kaist.hcil.magtouchlibrary.util;

import java.util.ArrayList;

public abstract class MaxFilter<T> {
    private ArrayList<T> buffer;
    double maxVal = Double.NEGATIVE_INFINITY;
    int maxIdx = 0;
    public MaxFilter()
    {
        buffer = new ArrayList<T>();
    }

    public void push(T data)
    {
        buffer.add(data);
        double val = calVal(data);
        if(val >= maxVal)
        {
            maxVal = val;
            maxIdx = buffer.size() - 1;
        }
    }

    public T getMaxValData()
    {
        return buffer.get(maxIdx);
    }

    public void reset()
    {
        buffer.clear();
        maxVal = Double.NEGATIVE_INFINITY;
        maxIdx = 0;
    }

    public abstract double calVal(T data);
}
