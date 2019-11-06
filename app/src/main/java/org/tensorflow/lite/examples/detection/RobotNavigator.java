package org.tensorflow.lite.examples.detection;
import org.tensorflow.lite.examples.detection.env.Logger;
import android.view.View;



import geist.re.mindlib.RobotService;


public class RobotNavigator implements View.OnClickListener {
    private static final String TAG = "ControlApp";
    private static final String ROBOT_NAME = "SpoReiJo";
    RobotService robot = new RobotService();

    private static final Logger LOGGER = new Logger();

    public void connect(){
        if(robot.getConnectionState() != RobotService.CONN_STATE_CONNECTED &&
                robot.getConnectionState() != RobotService.CONN_STATE_CONNECTING) {
            robot.connectToRobot(ROBOT_NAME);
        }

    }

    public void forward(){
        robot.executeSyncTwoMotorTask(robot.motorA.run(30),robot.motorB.run(30));
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.bntF: LOGGER.i("F");
                forward(); break;
            case R.id.bntR: LOGGER.i("R");
                break;
            case R.id.bntL: LOGGER.i("L");
                break;
            case R.id.bntB: LOGGER.i("B");
                break;
            case R.id.bntC: LOGGER.i("Connected");
                connect(); break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }
}
