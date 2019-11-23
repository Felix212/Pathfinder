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
    private static final int GOTOCIRCLE = 1;
    private static final int DESTINATIONREACHED = 2;
    private static final int TESTMODE = 3;
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
    public Bitmap stratChooserRedline(Bitmap bm) {
        switch(STRAT) {
            case GOTOCIRCLE: return redlineDetection.processImage(bm);
            case DESTINATIONREACHED: return null;
        }
        return null;
    }
    public void decideStrategy(RectF pos) {
        if((pos.height() * pos.width()) < 18000) {
            if(pos.centerX() < 100) {
                navigator.left();
            }
            if(pos.centerX() > 200) {
                navigator.right();
            }
            if(pos.centerX() > 100 && pos.centerX() < 200) {
                navigator.forward();
            }
        }
        if((pos.width() * pos.height() > 18000)) {
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
            case R.id.bntReset: STRAT = 1; navigator.forward();
            LOGGER.i("RESET!!!");
            break;
        }

    }

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
                LOGGER.i("Time elapsed after 5 Results" + elapsedSeconds);
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

