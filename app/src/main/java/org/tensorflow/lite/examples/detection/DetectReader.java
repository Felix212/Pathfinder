package org.tensorflow.lite.examples.detection;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.tflite.Classifier;


public class DetectReader implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private RobotNavigator navigator;
    private static Checkresult cr;
    public RedlineDetection redlineDetection;
    public int sizeRect = 0;
    private Logger LOGGER = new Logger();
    public static int STRAT = 3;
    public static final int SCAN = -1;
    public static final int AVOID = 0;
    public static final int GOTOCIRCLE = 1;
    public static final int DESTINATIONREACHED = 2;
    public static final int TESTMODE = 3;
    public long startTime = 0;
    public long endTime = 0;
    public long elapsedTime = 0;
    public long timeremaining = TIME_TO_WAIT;
    public static final int MINSIZEDESTINATION = 14000;
    public static final int MAXSIZEDESTINATION = 30000;
    public static final int TIME_TO_WAIT = 25000;
    private Bitmap fakeBitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
    public DetectReader(RobotNavigator nav) {
        this.navigator = nav;
        this.redlineDetection = new RedlineDetection(this.navigator, this);
    }

    Runnable scanTimer = new Runnable() {
        @Override
        public void run() {
            LOGGER.i("Executing scan routine.");
            timeremaining = TIME_TO_WAIT;
            navigator.scan();
            STRAT = SCAN;
            restartScanRoutine(TIME_TO_WAIT);
        }
    };

    public static Handler myHandler = new Handler();


    public long startScanRoutine(long waitTime) {
        myHandler.postDelayed(scanTimer, waitTime);
        LOGGER.i("Starting scan routine.");
        LOGGER.i("Waittime: " + waitTime);
        this.startTime = System.currentTimeMillis();
        return System.currentTimeMillis();
    }

    public long stopScanRoutine() {
        myHandler.removeCallbacks(scanTimer);
        LOGGER.i("Stopping scan routine.");
        this.timeremaining = this.timeremaining-(System.currentTimeMillis()-startTime);
        return timeremaining;
    }

    public void restartScanRoutine(int time) {
        myHandler.removeCallbacks(scanTimer);
        myHandler.postDelayed(scanTimer, time);

    }
    // circle detection
    public void stratChooserTensor(Classifier.Recognition o) {
        switch(STRAT) {
            case SCAN:
                if(decideStrategy(o.getLocation())) {
                    STRAT = GOTOCIRCLE;
                }
                break;
            case GOTOCIRCLE: decideStrategy(o.getLocation());
            break;
            case DESTINATIONREACHED:
            break;
            case TESTMODE: //Do nothing
        }
        if(STRAT == GOTOCIRCLE) {
            sizeRect = ((int)(o.getLocation().width() * o.getLocation().height()));
        }
    }
    // redline detection
    public Bitmap stratChooserRedline(Bitmap bm) {
        switch(STRAT) {
            case SCAN:
                LOGGER.i("IS alive: " + navigator.scanThread.isAlive());
                if(!navigator.scanThread.isAlive()) {
                    STRAT = GOTOCIRCLE;
                }
                break;
            case AVOID:
                if(!redlineDetection.checkRed(bm)) {
                    this.STRAT = this.GOTOCIRCLE;
                    this.navigator.resume();
                    startScanRoutine(this.timeremaining);
                }
            break;
            case GOTOCIRCLE: return this.redlineDetection.processImage(bm);
            case DESTINATIONREACHED: return fakeBitmap;
        }
        return fakeBitmap;
    }
    // TensorFlow Strategy
    public boolean decideStrategy(RectF pos) {
        boolean goodObject = false;
        // navigate to circle
        float rectSize = pos.width() * pos.height();
        //when destination is not reached yet
        // filter out faulty wide objects
        if(pos.width() < 260 ) {
            if(rectSize < MINSIZEDESTINATION) {
                // go left when circle is on the left side of the frame
                if(pos.centerX() < 105) {
                    navigator.left();
                    goodObject = true;
                }
                // go right..
                if(pos.centerX() > 195) {
                    navigator.right();
                    goodObject = true;
                }
                // stay mid
                if(pos.centerX() >= 105 && pos.centerX() <= 195) {
                    navigator.forward();
                    goodObject = true;
                }
            }
            // filter out big faulty detected Objects
            // destination is reached when 3 objects of the size between MINSIZEDESTINATION and MAXSIZEDESTINATION pixels are detected within 1.5 seconds (see Checkresult class)
            if(rectSize > MINSIZEDESTINATION && rectSize < MAXSIZEDESTINATION) {
                if(cr == null) {
                    cr = new Checkresult();
                    cr.check();
                } else {
                    int res = cr.check();
                    if(res == 0) {
                        cr = new Checkresult();
                    }
                    if(res == 1) {
                        LOGGER.i("We reached our destination.");
                        STRAT = DESTINATIONREACHED;
                        stopScanRoutine();
                        this.navigator.lastForwardToDestination();
                    }
                    if(res == 2) {
                        LOGGER.i("Not enough results yet.");
                    }
                }
            }
        }
        return goodObject;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.bntReset: STRAT = GOTOCIRCLE; navigator.forward(); cr = null;
            LOGGER.i("RESET!!!");
            break;
        }

    }
    // switch between test and find mode
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b) {
            this.STRAT = GOTOCIRCLE;
            this.navigator.scan();
            this.startScanRoutine(TIME_TO_WAIT);
            cr = null;
            LOGGER.i("MODE: " + this.STRAT);
        } else {
            this.STRAT = TESTMODE;
            this.stopScanRoutine();
            this.navigator.stop();
            LOGGER.i("MODE: " + this.STRAT);
        }
    }
    private class Checkresult {
        final long tStart = System.currentTimeMillis();
        private int counter;
        private int check() {
            counter++;
            LOGGER.i("ResultCounter: " + counter);
            if(counter == 3) {
                long tEnd = System.currentTimeMillis();
                long tDelta = tEnd - tStart;
                double elapsedSeconds = tDelta / 1000.0;
                LOGGER.i("Time elapsed after 5 detections" + elapsedSeconds);
                if(elapsedSeconds < 1.5) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return 2;
        }

    }
}

