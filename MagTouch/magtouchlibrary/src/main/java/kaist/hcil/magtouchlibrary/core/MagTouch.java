package kaist.hcil.magtouchlibrary.core;

import android.os.Handler;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kaist.hcil.magtouchlibrary.Settings;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.IMUData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.datamodel.Matrix2D;
import kaist.hcil.magtouchlibrary.datamodel.Quaternion;
import kaist.hcil.magtouchlibrary.ml.MLNode;
import kaist.hcil.magtouchlibrary.ml.MinMaxScaler;
import kaist.hcil.magtouchlibrary.ml.SVM;
import kaist.hcil.magtouchlibrary.util.CameDataMaxFilter;
import libsvm.svm_model;
import libsvm.svm_node;

/*
    MagTouch is the core part of the system.
    1) Run Came module
    2) Run the finger classifier
 */

public class MagTouch {
    private ExecutorService everyStepPool;
    private ExecutorService intermittentPool;
    private Came came;
    private svm_model svm;
    private MinMaxScaler scaler;
    private MagTouchCallback callback;
    private double madgwickBeta = Settings.madgwickBeta;

    private double requestHandleDelayFrom = Settings.magTouchRequestHandleDelayFrom;//sec
    private double requestHandleDelayTo = Settings.magTouchRequestHandleDelayTo;//sec

    // stabilization
    private double stabilizeTime = 5;
    private double startTime;
    private boolean wasStabilizing = true;
    private double stabilizationBeta = Settings.madgwickBetaForStabilization;
    Handler anomalyHandler;

    // async requests
    private CameCallbackWrapper callbackWrapper;
    private ConcurrentLinkedQueue<MagTouchRequestPacket> requestPackets;

    // filters
    private CameDataMaxFilter<CameData> maxFilter;


    public MagTouch(MagTouchCallback callback)
    {
        callbackWrapper = new CameCallbackWrapper();
        came = new Came(callbackWrapper);
        this.callback = callback;
        svm = null;
        scaler = null;

        requestPackets = new ConcurrentLinkedQueue<>();
        everyStepPool = Executors.newSingleThreadExecutor();
        intermittentPool = Executors.newSingleThreadExecutor();
        maxFilter = new CameDataMaxFilter();
        anomalyHandler = new Handler();
    }

    public void loadModels(File externalStorage)
    {
        svm = SVM.loadModel(externalStorage);
        scaler = MinMaxScaler.loadModel(externalStorage);
    }

    public ExecutorService getEveryStepPool()
    {
        return everyStepPool;
    }

    public void start()
    {
        came.setBeta(stabilizationBeta);
        came.setStabilizeMode(true);
        came.forceState(DistortionDetector.IDLE);
        came.start();
    }

    public void stop()
    {
        came.terminate();
        everyStepPool.shutdown();
        intermittentPool.shutdown();
    }

    public void put(IMUData imuData)
    {
        boolean stabilizing = isStabilizing(imuData.timestamp);
        setAfterStabilization(wasStabilizing, stabilizing);
        wasStabilizing = stabilizing;

        came.put(imuData);
    }

    public void requestRun(MagTouchRequestPacket packet)
    {
        requestPackets.add(packet);
    }

    private void checkRequests(CameData doneData)
    {
        if(requestPackets.isEmpty())
        {
            return;
        }

        MagTouchRequestPacket packet = requestPackets.peek();
        if(!doneData.isAnomaly)
        {
            forceAnomalyFor(1.0);
            return;
        }

        if(doneData.imuData.timestamp < packet.timestamp)
        {
            return; // ignore imu data before tap
        }

        if(doneData.imuData.timestamp < (packet.timestamp + requestHandleDelayFrom))
        {
            return; // wait at least some time.
        }

        maxFilter.push(doneData);
        if(doneData.imuData.timestamp <= (packet.timestamp + requestHandleDelayTo))
        {
            return;
        }
        else
        {
            requestPackets.poll();
            CameData selectedData = maxFilter.getMaxValData();
            maxFilter.reset();
            if(packet.needClassification)
            {
                classify(packet, selectedData);
            }
            else
            {
                callback.respondAfter(packet, selectedData);
            }

        }
    }

    public void forceState(int forcedState)
    {
        came.forceState(forcedState);
    }

    public void forceAnomalyFor(double timeInSec)
    {
        forceState(DistortionDetector.DISTORTED);
        anomalyHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                forceState(DistortionDetector.NOTHING);
            }
        }, (int)(timeInSec * 1000));
    }

    private boolean isStabilizing(double timestamp)
    {
        if(startTime == 0)
        {
            startTime = timestamp;
            return true;
        }
        double dt = timestamp - startTime;
        return dt < stabilizeTime;
    }

    private void setAfterStabilization(boolean wasStabilizing, boolean isStabilizing)
    {
        if(wasStabilizing && !isStabilizing)
        {
            came.setBeta(madgwickBeta);
            came.setStabilizeMode(false);
            came.forceState(DistortionDetector.NOTHING);
            callback.stabilized();
        }
    }

    private String classify(MagTouchRequestPacket packet, CameData data)
    {

        Quaternion fingerMag = data.fingerMag;
        double mx = fingerMag.x;
        double my = fingerMag.y;
        double mz = fingerMag.z;

        MLNode mlNode = new MLNode((int)packet.tapX, (int)packet.tapY, mx, my, mz, packet.finger);

        Matrix2D X = mlNode.toX();
        X = scaler.transform(X);
        svm_node[][] nodesX = SVM.buildNodes(X);
        double predicted = libsvm.svm.svm_predict(svm, nodesX[0]); // nodesX has only one row
        String predictedFinger = MLNode.numberToFinger((int)Math.round(predicted));

        callback.classified(packet, data, predictedFinger);

        return predictedFinger;
    }

    private class CameCallbackWrapper extends CameCallback
    {

        public CameCallbackWrapper()
        {
        }

        @Override
        public void doneStep(final CameData cameData) {

            Runnable frequentRunnable = new Runnable() {
                @Override
                public void run() {

                    callback.doneStep(cameData);
                }
            };

            Runnable heavyRunnable = new Runnable() {
                @Override
                public void run() {
                    checkRequests(cameData);
                }
            };

            synchronized (this)
            {
                if(!everyStepPool.isShutdown())
                {
                    everyStepPool.submit(frequentRunnable);
                }
            }

            synchronized (this)
            {
                if(!intermittentPool.isShutdown())
                {
                    intermittentPool.submit(heavyRunnable);
                }
            }


        }

        @Override
        public void stateChangedTo(int state) {
            callback.stateChangedTo(state);
        }
    }
}
