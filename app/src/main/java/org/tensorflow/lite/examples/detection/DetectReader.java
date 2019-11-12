package org.tensorflow.lite.examples.detection;

import android.graphics.RectF;

import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.env.TrackingResult;
import org.tensorflow.lite.examples.detection.tflite.Classifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class DetectReader {
    private RobotNavigator navigator;
    public DetectReader(RobotNavigator nav) {
        this.navigator = nav;
    }
    private static final Logger LOGGER = new Logger();
    private static int STRAT = 0;
    private static final int FINDCIRCLE = 0;
    private static final int GOTOCIRCLE = 1;
    private List<Classifier.Recognition> Objects = new LinkedList<Classifier.Recognition>();
    private Classifier.Recognition TrackedObject;
    private static RectF oldLocation;
    public void stratChooser(Classifier.Recognition o) {
        switch(STRAT) {
            case FINDCIRCLE: addObject(o);
            break;
            case GOTOCIRCLE: decideStrategy(o.getLocation());
            break;
        }
    }
    public void addObject(Classifier.Recognition o) {
        this.Objects.add(o);
        if(this.Objects.size() > 20) {
            TrackingResult res = getBestObject();
            LOGGER.i(String.valueOf(res.getResults()));
            this.Objects = new LinkedList<Classifier.Recognition>();
            if(res.getResults() > 19) {
                this.STRAT = GOTOCIRCLE;
                this.oldLocation = o.getLocation();
                LOGGER.i("FOUND OBJECT. CHANGING STRATEGY.");
            }

        }
    }
    // Get best Result from 4 object
    private TrackingResult getBestObject() {
        List<TrackingResult> res = new ArrayList<>();
        TrackingResult FINALRESULT = null;
        for (final Classifier.Recognition tmp : this.Objects) {
            res.add(new TrackingResult(tmp.getConfidence(), tmp.getLocation()));
        }

        for(TrackingResult result: res) {
            int i = 0;
            for (final Classifier.Recognition tmp: this.Objects) {
                if (result.checkCollide(this.Objects.get(i).getLocation())) {
                    result.addResult();
                }
                i++;
            }


        }
        FINALRESULT = res.get(0);
        for(TrackingResult result: res) {
            LOGGER.i(String.valueOf(result.BOX.centerX()));
            if(FINALRESULT.getResults() <= result.getResults()) {
                FINALRESULT = result;
            }

        }
        return FINALRESULT;
    }
    public String decideStrategy(RectF pos) {
        LOGGER.i(String.valueOf(pos.centerX()));
        LOGGER.i(String.valueOf(oldLocation.centerX()));

        oldLocation = pos;

        if(pos.centerX() < 100) {
            navigator.left();
        }
        if(pos.centerX() > 200) {
            navigator.right();
        }
        if(pos.centerX() > 100 && pos.centerX() < 200) {
            navigator.forward();
        }

        return null;
    }
}
