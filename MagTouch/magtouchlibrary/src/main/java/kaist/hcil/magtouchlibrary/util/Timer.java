package kaist.hcil.magtouchlibrary.util;

public class Timer {
    private long startTime = 0;
    private long interval = 0;
    private boolean isRunning;

    public Timer()
    {
        isRunning = false;
    }

    public Timer(long intervalInMillis)
    {
        this.interval = intervalInMillis;
        isRunning = false;
    }

    public boolean getIsRunning()
    {
        return isRunning;
    }

    public void start()
    {
        startTime = System.currentTimeMillis();
        isRunning = true;
    }

    public boolean isTimeout()
    {
        if(startTime == 0)
        {
            return true;
        }
        long now = System.currentTimeMillis();
        return (now - startTime) > interval;
    }

    public void reset()
    {
        startTime = 0;
        isRunning = false;
    }
}
