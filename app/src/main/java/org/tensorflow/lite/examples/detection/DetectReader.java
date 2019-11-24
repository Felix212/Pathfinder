package org.tensorflow.lite.examples.detection;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.view.View;
import android.widget.CompoundButton;

import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.tflite.Classifier;


public class DetectReader implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private RobotNavigator navigator;
    public DetectReader(RobotNavigator nav) {
        this.navigator = nav;
        this.redlineDetection = new RedlineDetection(this.navigator);
    }
    private static Checkresult cr;
    public RedlineDetection redlineDetection;
    private static final Logger LOGGER = new Logger();
    public static int STRAT = 3;
    private static final int FINDCIRCLE = 0;
    public static final int GOTOCIRCLE = 1;
    public static final int DESTINATIONREACHED = 2;
    public static final int TESTMODE = 3;
    // circle detection
    public void stratChooserTensor(Classifier.Recognition o) {
        switch(STRAT) {
            case FINDCIRCLE:
            break;
            case GOTOCIRCLE: decideStrategy(o.getLocation());
            break;
            case DESTINATIONREACHED: navigator.stop();
            break;
            case TESTMODE: //Do nothing
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
    public void decideStrategy(RectF pos) {
        // navigate to circle
        float rectSize = pos.width() * pos.height();
        if(rectSize < 16000) {
            // go left when circle is on the left side of the frame
            if(pos.centerX() < 100) {
                navigator.left();
            }
            // go right..
            if(pos.centerX() > 200) {
                navigator.right();
            }
            // stay mid
            if(pos.centerX() > 100 && pos.centerX() < 200) {
                navigator.forward();
            }
        }
        // filter out big faulty detected Objects
        // destination is reached when 5 objects of the size between 16000 and 20000 pixels are detected within 2 seconds (see Checkresult class)
        if(rectSize > 16000 && rectSize < 20000) {
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
            LOGGER.i("MODE: " + this.STRAT);
        } else {
            this.STRAT = TESTMODE;
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
            if(counter == 5) {
                long tEnd = System.currentTimeMillis();
                long tDelta = tEnd - tStart;
                double elapsedSeconds = tDelta / 1000.0;
                LOGGER.i("Time elapsed after 5 detections" + elapsedSeconds);
                if(elapsedSeconds < 2.0) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return 2;
        }

    }
}

