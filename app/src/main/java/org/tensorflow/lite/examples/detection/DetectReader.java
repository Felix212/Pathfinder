package org.tensorflow.lite.examples.detection;
import android.content.Context;
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
    private Checkresult cr;
    public RedlineDetection redlineDetection;
    public int sizeRect = 0;
    private Logger LOGGER = new Logger();
    public int Strategy = 3;
    public final int SCAN = -1;
    public final int AVOID = 0;
    public final int GOTOCIRCLE = 1;
    public final int DESTINATIONREACHED = 2;
    public final int TESTMODE = 3;
    private boolean ObjectFound = false;
    public long startTime = 0;
    public Soundcontrol soundcontrol;

    private final int MINSIZEDESTINATION = 14000;
    private final int MAXSIZEDESTINATION = 30000;
    private final int TIME_TO_WAIT = 15000;
    private long timeremaining = TIME_TO_WAIT;
    private Bitmap fakeBitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
    public DetectReader(RobotNavigator nav) {
        this.navigator = nav;
        this.redlineDetection = new RedlineDetection(this.navigator);
    }

    // Tensor detection for Circle
    public void stratChooserTensor(Classifier.Recognition o) {
        switch(Strategy) {
            case SCAN:
                if(tensorStrategy(o.getLocation())) {
                    LOGGER.i("Strat changed from scan to gotocircle");
                    this.Strategy = GOTOCIRCLE;
                }
                break;
            case GOTOCIRCLE: this.ObjectFound = this.tensorStrategy(o.getLocation());
            break;
            case DESTINATIONREACHED:
            break;
            case TESTMODE: //Do nothing
        }
        if(Strategy == GOTOCIRCLE) {
            sizeRect = ((int)(o.getLocation().width() * o.getLocation().height()));
        }
    }
    // redline detection
    public Bitmap stratChooserRedline(Bitmap bm) {
        switch(Strategy) {
            case SCAN:
                if(!navigator.scanThread.isAlive()) {
                    this.Strategy = GOTOCIRCLE;
                }
                break;
            case AVOID:
                if(!redlineDetection.checkRedDuringAvoid(bm)) {
                    this.Strategy = this.GOTOCIRCLE;
                    this.navigator.resume();
                    this.startScanRoutine(this.timeremaining);
                }
                return this.redlineDetection.bitmapResult;
            case GOTOCIRCLE:
                this.redlineDetection.processImage(bm);
                // >= 0 redline left or right
                if(this.redlineDetection.Avoid >= 0) {
                    this.Strategy = AVOID;
                    this.stopScanRoutine();
                }
                return this.redlineDetection.bitmapResult;
            case DESTINATIONREACHED: return fakeBitmap;
        }
        return fakeBitmap;
    }
    // TensorFlow Strategy
    public boolean tensorStrategy(RectF pos) {
        boolean goodObject = false;
        // navigate to circle
        float rectSize = pos.width() * pos.height();
        //when destination is not reached yet
        // filter out faulty wide objects
        if(pos.width() < 260 ) {
            if(rectSize < MINSIZEDESTINATION) {
                // go left when circle is on the left side of the frame
                if(pos.centerX() < 105) {
                    this.navigator.left();
                    goodObject = true;
                }
                // go right..
                if(pos.centerX() > 195) {
                    this.navigator.right();
                    goodObject = true;
                }
                // stay mid
                if(pos.centerX() >= 105 && pos.centerX() <= 195) {
                    this.navigator.forward();
                    goodObject = true;
                }
            }
            // filter out big faulty detected Objects
            // destination is reached when 3 objects of the size between MINSIZEDESTINATION and MAXSIZEDESTINATION pixels are detected within 1.5 seconds (see Checkresult class)
            if(rectSize > MINSIZEDESTINATION && rectSize < MAXSIZEDESTINATION) {
                if(this.cr == null) {
                    this.cr = new Checkresult();
                    this.cr.check();
                } else {
                    int res = this.cr.check();
                    if(res == 0) {
                        cr = new Checkresult();
                    }
                    if(res == 1) {
                        this.LOGGER.i("We reached our destination.");
                        this.Strategy = DESTINATIONREACHED;
                        this.stopScanRoutine();
                        this.navigator.lastForwardToDestination();
                        soundcontrol.play_reached_destiation();
                    }
                    if(res == 2) {
                        this.LOGGER.i("Not enough results yet.");
                    }
                }
            }
        }
        return goodObject;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.bntReset: this.Strategy = GOTOCIRCLE; navigator.forward(); cr = null;
            LOGGER.i("RESET!!!");
            break;
        }

    }
    // switch between test and find mode
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b) {
            this.Strategy = GOTOCIRCLE;
            this.navigator.scan();
            this.startScanRoutine(TIME_TO_WAIT);
            this.cr = null;
            this.LOGGER.i("MODE: " + this.Strategy);
        } else {
            this.Strategy = TESTMODE;
            this.stopScanRoutine();
            this.navigator.stop();
            this.LOGGER.i("MODE: " + this.Strategy);
        }
    }

    Runnable scanTimer = new Runnable() {
        @Override
        public void run() {
            LOGGER.i("Executing scan routine.");
            timeremaining = TIME_TO_WAIT;
            Strategy = SCAN;
            navigator.scan();
            restartScanRoutine(TIME_TO_WAIT);
        }
    };

    public static Handler myHandler = new Handler();


    public long startScanRoutine(long waitTime) {
        this.myHandler.postDelayed(scanTimer, waitTime);
        this.LOGGER.i("Starting scan routine.");
        this.LOGGER.i("Waittime: " + waitTime);
        this.startTime = System.currentTimeMillis();
        return System.currentTimeMillis();
    }

    public long stopScanRoutine() {
        this.myHandler.removeCallbacks(scanTimer);
        this.LOGGER.i("Stopping scan routine.");
        this.timeremaining = this.timeremaining-(System.currentTimeMillis()-startTime);
        return timeremaining;
    }

    public void restartScanRoutine(int time) {
        this.myHandler.removeCallbacks(scanTimer);
        this.myHandler.postDelayed(scanTimer, time);
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

    public void createSoundcontrol(Context cn){
        soundcontrol = new Soundcontrol(cn);
    }
}

