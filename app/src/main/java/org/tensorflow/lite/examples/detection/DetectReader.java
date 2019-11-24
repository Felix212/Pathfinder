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
    private static final Logger LOGGER = new Logger();
    public static int STRAT = 3;
    private static final int FINDCIRCLE = 0;
    public static final int GOTOCIRCLE = 1;
    public static final int DESTINATIONREACHED = 2;
    public static final int TESTMODE = 3;

    public static final int MINSIZEDESTINATION = 14000;
    public static final int MAXSIZEDESTINATION = 30000;

    public DetectReader(RobotNavigator nav) {
        this.navigator = nav;
        this.redlineDetection = new RedlineDetection(this.navigator);
    }

    Runnable scanTimer = new Runnable() {
        @Override
        public void run() {
            LOGGER.i("Executing scan routine.");
            navigator.scan();
            restartScanRoutine(35000);
        }
    };

    public static Handler myHandler = new Handler();
    private static final int TIME_TO_WAIT = 35000;

    public void startScanRoutine() {
        myHandler.postDelayed(scanTimer, TIME_TO_WAIT);
        LOGGER.i("Starting scan routine.");
    }

    public void stopScanRoutine() {
        myHandler.removeCallbacks(scanTimer);
        LOGGER.i("Stopping scan routine.");
    }

    public void restartScanRoutine(int time) {
        myHandler.removeCallbacks(scanTimer);
        myHandler.postDelayed(scanTimer, time);
    }
    // circle detection
    public void stratChooserTensor(Classifier.Recognition o) {
        switch(STRAT) {
            case FINDCIRCLE:
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
            case GOTOCIRCLE: return redlineDetection.processImage(bm);
            case DESTINATIONREACHED: return null;
        }
        return null;
    }
    // TensorFlow Strategy
    public void decideStrategy(RectF pos) {
        // navigate to circle
        float rectSize = pos.width() * pos.height();
        //when destination is not reached yet
        if(rectSize < MINSIZEDESTINATION) {
            // go left when circle is on the left side of the frame
            if(pos.centerX() < 105) {
                navigator.left();
            }
            // go right..
            if(pos.centerX() > 195) {
                navigator.right();
            }
            // stay mid
            if(pos.centerX() >= 105 && pos.centerX() <= 195) {
                navigator.forward();
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
            this.startScanRoutine();
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

