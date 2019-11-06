package org.tensorflow.lite.examples.detection;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import geist.re.mindlib.RobotControlActivity;
import geist.re.mindlib.RobotService;

public class RobotNavigator extends RobotControlActivity {
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
}
