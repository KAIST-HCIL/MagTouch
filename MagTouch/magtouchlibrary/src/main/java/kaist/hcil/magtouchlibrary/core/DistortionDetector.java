package kaist.hcil.magtouchlibrary.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaist.hcil.magtouchlibrary.Settings;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.IMUData;
import kaist.hcil.magtouchlibrary.datamodel.Quaternion;
import kaist.hcil.magtouchlibrary.util.MeanFilter;

public class DistortionDetector {
    public static final int IDLE = 3;
    public static final int DISTORTED = 5;
    public static final int READY_DISTORTED = 9;
    public static final int READY_IDLE = 11;
    public static final int NOTHING = 7;

    List<CameData> window;

    private int internalState = IDLE;
    double anomalyIndex;
    boolean isReady = false;

    double toDistortedThrs = Settings.toDistortedThreshold;
    double toIdleThrs = Settings.toIdleThreshold;

    double noInteractionDegreeX = Settings.noInteractionDegrees;
    double noInteractionDegreeY = Settings.noInteractionDegrees;

    double windowSize = Settings.detectorWindowSize;
    double readyIdleWaitTime = Settings.detectorIdleWaitTime;

    Quaternion prevMag;
    Quaternion toDistortedRef = null;
    double readyAnomalyTimestamp = Double.POSITIVE_INFINITY;
    double readyIdleTimestamp = Double.POSITIVE_INFINITY;

    MeanFilter anomalyIndexFilter;
    MeanFilter xAngleFilter;
    MeanFilter yAngleFilter;

    public DistortionDetector()
    {
        window = new ArrayList<>(200);
        window = Collections.synchronizedList(window);

        anomalyIndexFilter = new MeanFilter(100);
        xAngleFilter = new MeanFilter(600);
        yAngleFilter = new MeanFilter(600);
    }

    public void putInBuffer(CameData cameData)
    {
        if(cameData == null)
        {
            return;
        }

        if(!checkAndUpdateIfMagChanged(cameData.imuData))
        {
            return;
        }

        window.add(cameData);
        removeOldData(cameData);
    }

    public void removeOldData(CameData nowData)
    {
        CameData firstData = window.get(0);

        double firstTimestamp = firstData.imuData.timestamp;
        double nowTimestamp = nowData.imuData.timestamp;

        while((nowTimestamp - firstTimestamp) > windowSize)
        {
            window.remove(0);
            if(!isReady)
            {
                isReady = true;
            }
            firstTimestamp= window.get(0).imuData.timestamp;
        }
    }

    public void clearWindow()
    {
        window.clear();
        isReady = false;
    }

    public double getAnomalyIndex()
    {
        return anomalyIndex;
    }

    public boolean getIsAnomaly()
    {
        return (internalState == DISTORTED) || (internalState == READY_IDLE);
    }

    public void updateStateAndIndex()
    {
        if(!isReady || window.size() < 2)
        {
            return;
        }

        CameData firstData = window.get(0);
        CameData lastData = window.get(window.size()-1);
        updateInternalState(lastData, firstData);
    }

    private void updateInternalState(CameData lastData, CameData firstData)
    {
        double nowTimestamp = lastData.imuData.timestamp;

        StateData stateData = null;

        switch (internalState)
        {
            case IDLE:
                stateData = handleIdleState(lastData, firstData);
                break;
            case READY_DISTORTED:
                break;
            case DISTORTED:
                stateData = handleDistortedState(nowTimestamp, lastData);
                break;
            case READY_IDLE:
                stateData = handleReadyIdleState(nowTimestamp, lastData);
                break;
            default:
                // shouldn't be hear
                break;

        }

        if(stateData.updateAnomalyIndex)
        {
            anomalyIndex = stateData.anomalyIndex;
        }
    }

    private StateData handleIdleState(CameData lastData, CameData firstData)
    {
        if(checkIsWatchNotInteracting(lastData))
        {
            resetStateToIdle();
            anomalyIndexFilter.push(0);
            return new StateData(false, 0);
        }

        double currentIndex = calAnomalyIndex(lastData.earthNorth, firstData.earthNorth);
        anomalyIndexFilter.push(currentIndex);
        currentIndex =  anomalyIndexFilter.getMean();

        if(currentIndex > toDistortedThrs)
        {
            toDistortedRef = firstData.earthNorth.copy();
            toAnomaly();
        }

        return new StateData(true, currentIndex);
    }

    private StateData handleDistortedState(double nowTimestamp, CameData lastData)
    {
        Quaternion currentAmbMag = calNorth(lastData);
        double currentIndex = calAnomalyIndex(currentAmbMag, toDistortedRef);
        anomalyIndexFilter.push(currentIndex);
        currentIndex =  anomalyIndexFilter.getMean();

        if(currentIndex < toIdleThrs)
        {
            toReadyIdle(nowTimestamp);
        }

        if(checkIsWatchNotInteracting(lastData))
        {
            resetStateToIdle();
        }

        return new StateData(true, currentIndex);
    }

    private StateData handleReadyIdleState(double nowTimestamp, CameData lastData)
    {
        Quaternion currentAmbMag = calNorth(lastData);
        double currentIndex = calAnomalyIndex(currentAmbMag, toDistortedRef);
        anomalyIndexFilter.push(currentIndex);
        currentIndex =  anomalyIndexFilter.getMean();

        if((nowTimestamp - readyIdleTimestamp) > readyIdleWaitTime)
        {
            toIdle();
            readyIdleTimestamp = Double.POSITIVE_INFINITY;
        }
        else if(currentIndex > toDistortedThrs)
        {
            toAnomaly();
            readyIdleTimestamp = Double.POSITIVE_INFINITY;
        }

        return new StateData(true, currentIndex);
    }

    private void toAnomaly()
    {
        internalState = DISTORTED;
    }

    private void toReadyIdle(double timestamp)
    {
        internalState = READY_IDLE;
        readyIdleTimestamp = timestamp;
    }

    private void toIdle()
    {
        internalState = IDLE;
        toDistortedRef = null;
    }

    private void resetStateToIdle()
    {
        anomalyIndex = 0;
        readyAnomalyTimestamp = Double.POSITIVE_INFINITY;
        readyIdleTimestamp = Double.POSITIVE_INFINITY;
        toIdle();
    }

    private Quaternion calNorth(CameData data)
    {
        return Madgwick.calNorth(data.orientation, data.imuData.mag);
    }

    private boolean checkAndUpdateIfMagChanged(IMUData imuData)
    {
        if(prevMag == null)
        {
            prevMag = imuData.mag.copy();
        }
        else
        {
            if(prevMag.equals(imuData.mag))
            {
                return false;
            }
            else
            {
                prevMag = imuData.mag.copy();
            }
        }
        return true;
    }

    private double calAnomalyIndex(Quaternion input, Quaternion reference)
    {
        return Quaternion.subtract(input, reference).getNorm();
    }

    private boolean checkIsWatchNotInteracting(CameData data)
    {
        /*
            A user is not interacting with smartwatch, if
            the smartwatch faces outward (yAngle < threshold), or
            her arm is straight down (xAngle < threshold).
         */
        Quaternion minusXAxis = new Quaternion(0,-1,0,0);
        double xAngle = calAngle3d(minusXAxis, data.imuData.acc);
        xAngleFilter.push(xAngle);

        if(xAngleFilter.getMean() < noInteractionDegreeX)
        {
            return true;
        }

        Quaternion minusYAxis = new Quaternion(0,0,-1,0);
        double yAngle = calAngle3d(minusYAxis, data.imuData.acc);
        yAngleFilter.push(yAngle);

        if(yAngleFilter.getMean() < noInteractionDegreeY)
        {
            return true;
        }

        return false;
    }

    private double calAngle3d(Quaternion q1, Quaternion q2)
    {
        double dot = Quaternion.dot(q1, q2);
        double cos = dot / (q1.getNorm() * q2.getNorm());
        double angle = Math.toDegrees(Math.acos(cos));
        return angle;
    }

    private class StateData
    {
        public boolean updateAnomalyIndex;
        public double anomalyIndex;
        public StateData(boolean updateAnomalyIndex, double anomalyIndex)
        {
            this.updateAnomalyIndex = updateAnomalyIndex;
            this.anomalyIndex = anomalyIndex;
        }
    }
}
