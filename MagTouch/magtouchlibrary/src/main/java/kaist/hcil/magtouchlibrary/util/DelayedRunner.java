package kaist.hcil.magtouchlibrary.util;
import android.os.Handler;

public class DelayedRunner {
    private long delay;
    private Handler handler;
    private boolean isJobPosted = false;
    private DelayedJob job;

    public DelayedRunner(long delayInMillis, DelayedJob job)
    {
        delay = delayInMillis;
        isJobPosted = false;
        this.job = job;
        handler = new Handler();
    }

    public void postJob()
    {
        if(isJobPosted)
        {
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                job.doJob();
                isJobPosted = false;
            }
        }, delay);
        isJobPosted = true;
    }

    public void cancelJob()
    {
        handler.removeCallbacksAndMessages(null);
        isJobPosted = false;
    }

    public interface DelayedJob
    {
        void doJob();
    }
}
