package kaist.hcil.magtouchlibrary;

public class Settings {


    /* CAME */

    // Beta values are larger than original Madgwick's paper.
    // In our test, the beta value doesn't seem to have a significant effect on the accuracy.
    // However, if the ambient magnetic field changes a lot, higher beta might help
    // the algorithm to converge fast.
    public static final double madgwickBeta = Math.toRadians(15);
    public static final double madgwickBetaForStabilization = Math.toRadians(20);
    public static final double rewindTime = 1.5;

    /* Distortion Detector */
    public static final double toDistortedThreshold = 35;
    public static final double toIdleThreshold = 15;
    public static final double noInteractionDegrees = 30;
    public static final double detectorWindowSize = 1.5; // sec
    public static final double detectorDistortedWaitTime= 0.1; // sec
    public static final double detectorIdleWaitTime = detectorWindowSize + 1.0; // this should be longer than window size. window should be filled up with fresh data.

    public static final double accDistortedThreshold = 20;

    /* SVM */
    public static final double SVM_C = 2.976351441631316;
    public static final double SVM_gamma = 0.6951927961775606;
    public static final double SVM_eps = 1e-8;

    /* Train and Test */
    public static final int defaultNumTargetRepeat = 2;

    public static final boolean recordRawDataWhileTest = false;
    // only valid when 'recordRawDataWhileTest' is true.
    public static final double recordPeriodWhileTest = 0.005;//sec
    public static final int inSituTestNumChunk = 10;
    public static final double inSituTestMinInterval = 30;//sec
    public static final double inSituTestMaxInterval = 60;//sec
    public static final double inSituTestFirstInterval = 120;//sec
    public static final int inSituTestNumTargetRepeat = 3;

    /* Misc */
    public static final String BluetoothTargetAddress = "Address of bluetooth server";
    public static final double accGyroDelay = 3.0 / 60.0; // sec.
    // These delays are for waiting a short time after a tap.
    // It is to make sure that MagTouch uses IMU data after the tap.
    public static final double magTouchRequestHandleDelayFrom = 0.01;
    public static final double magTouchRequestHandleDelayTo = 0.15;

}
