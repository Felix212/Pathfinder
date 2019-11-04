package org.tensorflow.lite.examples.detection.env;

import android.graphics.RectF;

public class TrackingResult {
    private float CONFIDENTLEVEL;
    private int RESULTS;
    public RectF BOX;
    public TrackingResult(float confident, RectF box) {
        this.CONFIDENTLEVEL = confident;
        this.BOX = box;
        this.RESULTS = 1;
    }
    public boolean checkCollide(RectF otherBox) {
        return this.BOX.intersect(otherBox);
    }
    public void addResult() {
        this.RESULTS++;
    }
    public int getResults() {
        return this.RESULTS;
    }
}
