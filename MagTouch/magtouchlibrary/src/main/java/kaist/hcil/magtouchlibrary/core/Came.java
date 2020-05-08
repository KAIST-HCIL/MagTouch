package kaist.hcil.magtouchlibrary.core;

import java.util.ArrayList;

import kaist.hcil.magtouchlibrary.Settings;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.IMUData;
import kaist.hcil.magtouchlibrary.datamodel.Quaternion;

public class Came {
        private DistortionDetector detector;
    private Madgwick madgwick;
    private CameCallback cameCallback;

    private double rewindTime = Settings.rewindTime;
    private CameData latestCameData;
    private boolean wasAnomaly;
    private boolean isAnomaly;
    private int forcedState = DistortionDetector.NOTHING;
    private double notAnomalyTimeStamp;
    private boolean isTerminated;
    private boolean stabilizeMode;

    ArrayList<IMUData> threadBuffer;
    ArrayList<CameData> rewindBuffer;
    CameRunnable runningThread;

    public Came(CameCallback cameCallback)
    {
        detector = new DistortionDetector();

        Quaternion initialOrientation = new Quaternion(1,0,0,0);

        madgwick = new Madgwick(initialOrientation, toRadian(20));

        threadBuffer = new ArrayList<>();
        isAnomaly = false;
        wasAnomaly = false;
        isTerminated = false;
        notAnomalyTimeStamp = Double.NEGATIVE_INFINITY;

        this.cameCallback = cameCallback;

    }

    public void start()
    {
        runningThread = new CameRunnable();
        new Thread(runningThread).start();
    }

    public void terminate()
    {
        runningThread.stop();
    }

    public void setBeta(double beta)
    {
        madgwick.setBeta(beta);
    }

    public void put(IMUData data)
    {
        if(data == null)
        {
            return;
        }
        if(isTerminated)
        {
            return;
        }
        threadBuffer.add(data.copy());
    }

    public boolean getIsAnomaly() { return isAnomaly; }

    public void setStabilizeMode(boolean mode)
    {
        stabilizeMode = mode;
    }

    public void forceState(int forcedState)
    {
        this.forcedState = forcedState;
    }

    private class CameRunnable implements Runnable
    {
        /*
            Continuously run the CAME algorithm,
            by popping the IMU data from the buffer.
         */
        private boolean isRunning = false;

        @Override
        public void run() {
            isRunning = true;
            rewindBuffer = new ArrayList<>();
            Quaternion latestNorth = new Quaternion(1,0,0,0);
            while (isRunning)
            {
                if(threadBuffer.isEmpty())
                {
                    continue;
                }
                IMUData nowData = threadBuffer.remove(0);
                if(!isValidData(nowData))
                {
                    continue;
                }

                wasAnomaly = isAnomaly;

                if(!stabilizeMode)
                {
                    detector.updateStateAndIndex();
                    isAnomaly = detector.getIsAnomaly();
                }

                if(forcedState == DistortionDetector.DISTORTED)
                {
                    isAnomaly = true;
                }
                else if(forcedState == DistortionDetector.IDLE)
                {
                    isAnomaly = false;
                }

                latestCameData = runModule(madgwick, nowData, isAnomaly, false);


                if(needRewind())
                {
                    rewindBuffer = rewindMadgwick(madgwick, rewindBuffer);
                    cameCallback.stateChangedTo(DistortionDetector.DISTORTED);
                    latestNorth = rewindBuffer.get(0).earthNorth.copy();
                }

                if(isAnomalyEnded())
                {
                    notAnomalyTimeStamp = getNowInSec();
                    cameCallback.stateChangedTo(DistortionDetector.IDLE);
                }

                putInRewindBuffer(latestCameData.copy());
                detector.putInBuffer(latestCameData.copy());
                latestCameData.anomalyIndex = detector.getAnomalyIndex();
                latestCameData.isAnomaly = isAnomaly;

                cameCallback.doneStep(latestCameData);
            }
        }

        public void stop()
        {
            isRunning = false;
        }
    }

    private boolean needRewind()
    {
        return isAnomaly && !wasAnomaly;
    }

    private boolean isAnomalyEnded()
    {
        return !isAnomaly && wasAnomaly;
    }

    private boolean isValidData(IMUData data)
    {
        if(data == null)
        {
            return false;
        }

        if(data.acc == null || data.gyro == null || data.mag == null)
        {
            return false;
        }

        return true;
    }

    private void putInRewindBuffer(CameData nowData)
    {
        putInTimedBuffer(rewindBuffer, nowData, rewindTime);
    }


    public void putInTimedBuffer(ArrayList<CameData> buffer, CameData nowData, double windowTime)
    {
        buffer.add(nowData);
        CameData first = buffer.get(0);
        if(first == null)
        {
            return;
        }
        double now = nowData.imuData.timestamp;
        while((now - first.imuData.timestamp) > windowTime)
        {
            buffer.remove(0);
            first = buffer.get(0);
            if(first == null) break;
        }
    }

    private CameData runModule(Madgwick module, IMUData data, boolean isAnomaly, boolean accOff)
    {
        if(accOff)
        {
            return module.step(data, Madgwick.TYPE_GYRO);
        }
        else
        {
            if(isAnomaly)
            {
                return module.step(data, Madgwick.TYPE_GYRO_ACC);
            }
            else
            {
                return module.step(data, Madgwick.TYPE_GYRO_ACC_MAG);
            }
        }
    }

    private ArrayList<CameData> rewindMadgwick(Madgwick module, ArrayList<CameData> pastBuffer)
    {

        if(pastBuffer.size() < 2)
        {
            return new ArrayList<>();
        }
        CameData firstState = pastBuffer.remove(0);
        CameData secondState = pastBuffer.remove(0);
        module.rewindTo(secondState.orientation, secondState.earthNorth, firstState.imuData.timestamp);

        ArrayList<CameData> newPastBuffer = new ArrayList<>(pastBuffer.size());
        if(pastBuffer.isEmpty())
        {
            return newPastBuffer;
        }
        CameData state = null;
        CameData recalculatedState = null;
        while(!pastBuffer.isEmpty())
        {
            state = pastBuffer.remove(0);
            if(state == null) break;
            recalculatedState = runModule(module, state.imuData, true, false);
            newPastBuffer.add(recalculatedState);
        }

        latestCameData = newPastBuffer.remove(newPastBuffer.size() - 1);
        return newPastBuffer;
    }

    private double toRadian(double val)
    {
        return (Math.PI / 180) * val;
    }

    private double getNowInSec()
    {
        return System.currentTimeMillis() / 1000.0;
    }
}
