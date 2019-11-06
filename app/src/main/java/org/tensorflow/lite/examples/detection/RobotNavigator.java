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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import geist.re.mindlib.RobotControlActivity;
import geist.re.mindlib.RobotService;

    private static final String TAG = "ControlApp";
    private static final String ROBOT_NAME = "SpoReiJo";

    public void connect(){
        if(robot.getConnectionState() != RobotService.CONN_STATE_CONNECTED &&
                robot.getConnectionState() != RobotService.CONN_STATE_CONNECTING) {
            new AsyncTask<Void, Void, Exception>() {
                ProgressDialog progress = new ProgressDialog(RobotNavigator.this);
                boolean dismissed = false;

                @Override
                protected void onPreExecute() {
                    progress.setMessage("Connecting...");
                    progress.setTitle("Connecting to robot");
                    progress.setCancelable(false);
                    progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dismissed = true;
                        }
                    });
                    progress.show();
                }

                @Override
                protected Exception doInBackground(Void... voids) {
                    Exception ex = null;
                    robot.connectToRobot(ROBOT_NAME);
                    while (robot.getConnectionState() != RobotService.CONN_STATE_CONNECTED) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (dismissed) break;
                        if (robot.getConnectionState() == RobotService.CONN_STATE_DISCONNECTED) {
                            robot.connectToRobot(ROBOT_NAME);
                        }
                    }
                    return ex;
                }

                @Override
                protected void onPostExecute(Exception e) {
                    progress.dismiss();
                }
            }.execute();
        }
    }

    public void forward(){
        robot.executeSyncTwoMotorTask(robot.motorA.run(30),robot.motorB.run(30));
    }

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
