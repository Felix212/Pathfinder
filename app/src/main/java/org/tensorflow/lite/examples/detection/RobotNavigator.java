package org.tensorflow.lite.examples.detection;
import org.tensorflow.lite.examples.detection.env.Logger;
import android.view.View;



import geist.re.mindlib.RobotService;
import geist.re.mindlib.events.LightStateEvent;
import geist.re.mindlib.events.MotorStateEvent;
import geist.re.mindlib.exceptions.SensorDisconnectedException;
import geist.re.mindlib.hardware.Motor;
import geist.re.mindlib.hardware.Sensor;
import geist.re.mindlib.listeners.LightSensorListener;
import geist.re.mindlib.listeners.MotorStateListener;


public class RobotNavigator implements View.OnClickListener {
    private static final String TAG = "ControlApp";
    private static final String ROBOT_NAME = "NXT";
    private static final int speed = 10;
    private RobotService robot = new RobotService();
    private static boolean isRotating = false;
    private MotorStateEvent ASTATE;
    public RobotNavigator() {
        if(robot.getConnectionState() != RobotService.CONN_STATE_CONNECTED &&
                robot.getConnectionState() != RobotService.CONN_STATE_CONNECTING) {
            robot.connectToRobot(ROBOT_NAME);
        }
        registerMotorListener();
    }
    private static final Logger LOGGER = new Logger();

    private void registerMotorListener() {
        robot.motorA.registerListener(new MotorStateListener() {
            @Override
            public void onEventOccurred(MotorStateEvent e) {
                ASTATE = e;
                LOGGER.i(Integer.toString(ASTATE.getTachoCount()));
            }
        }, 100);
    }
    public void connect(){
        if(robot.getConnectionState() != RobotService.CONN_STATE_CONNECTED &&
                robot.getConnectionState() != RobotService.CONN_STATE_CONNECTING) {
            robot.connectToRobot(ROBOT_NAME);
        }
    }

    public void forward(){
        robot.executeSyncTwoMotorTask(robot.motorA.run(15),robot.motorB.run(15));
    }
    public void left() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(15),robot.motorB.run(10));
    }
    public void right() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(10),robot.motorB.run(15));
    }
    public void backwards() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(-15),robot.motorB.run(15));
    }
    private void waitcommand() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            while(robot.motorA.getState() == Motor.STATE_RUNNING && robot.motorB.getState() == Motor.STATE_RUNNING) {
                LOGGER.i("Waiting for command for last command to finish.");

        }
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void rotate() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(speed, 166), robot.motorB.run(-speed, 166));
        waitcommand();
    }
    public void rotateLeft() {
        if(!isRotating) {
            isRotating = true;
            robot.executeSyncTwoMotorTask(robot.motorA.run(-speed, 100), robot.motorB.run(-speed, 100));
            waitcommand();
            robot.executeSyncTwoMotorTask(robot.motorA.run(speed, 270), robot.motorB.run(-speed, 270));
            waitcommand();
            isRotating = false;
        }

    }
    public void rotateRight() {
        if(!isRotating) {
            isRotating = true;
            robot.executeSyncTwoMotorTask(robot.motorA.run(-speed, 100), robot.motorB.run(-speed, 100));
            waitcommand();
            robot.executeSyncTwoMotorTask(robot.motorA.run(-speed, 270), robot.motorB.run(speed, 270));
            waitcommand();
            isRotating = false;
        }

    }
    private void longlenght() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(speed, 2000), robot.motorB.run(speed, 2000));
        waitcommand();
    }
    private void shortlenght() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(speed, 1000), robot.motorB.run(speed, 1000));
        waitcommand();
    }
    public void testRoute() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                shortlenght();
                rotate();
                shortlenght();
                rotate();
                shortlenght();
                rotate();
                shortlenght();
                rotate();
            }
        };
        thread.start();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.bntF: LOGGER.i("F");
                forward(); break;
            case R.id.bntR: LOGGER.i("R"); rotateRight();
                break;
            case R.id.bntL: LOGGER.i("L"); rotateLeft();
                break;
            case R.id.bntB: LOGGER.i("B"); backwards();
                break;
            case R.id.bntC: LOGGER.i("testroute");
                testRoute();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }
}
