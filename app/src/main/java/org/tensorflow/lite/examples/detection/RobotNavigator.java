package org.tensorflow.lite.examples.detection;


import android.view.View;
import android.widget.Button;

import org.tensorflow.lite.examples.detection.env.Logger;

import geist.re.mindlib.RobotControlActivity;
import geist.re.mindlib.RobotService;
import geist.re.mindlib.events.TouchStateEvent;
import geist.re.mindlib.exceptions.SensorDisconnectedException;
import geist.re.mindlib.hardware.Sensor;
import geist.re.mindlib.listeners.TouchSensorListener;

public class RobotNavigator extends RobotControlActivity implements View.OnClickListener  {

    private static final Logger LOGGER = new Logger();

    private static final String TAG = "ControlApp";
    private static final String ROBOT_NAME = "02Bolek";


    @Override
    protected void onRobotServiceConnected() {

    }

    @Override
    protected void onStartListeningForVoiceCommands() {

    }

    @Override
    protected void onStartListeningForVoiceWakeup() {

    }

    @Override
    protected void onRobotDisconnected() {

    }

    @Override
    protected void onRobotConnected() {

    }

    @Override
    protected void onGestureCommand(double x, double y, double z) {

    }

    @Override
    public void onClick(View view) {
            switch(view.getId()) {
                case R.id.bntF: LOGGER.i("F");
                    break;
                case R.id.bntR: LOGGER.i("R");
                    break;
                case R.id.bntL: LOGGER.i("L");
                    break;
                case R.id.bntB: LOGGER.i("B");
                    break;
                case R.id.bntC: LOGGER.i("Connected");
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + view.getId());
            }
    }
}
