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
    private static int speed = 15;
    private static int slowSpeed = 7;
    private static int turnvalue = 8;
    private RobotService robot = new RobotService();
    private static boolean isRotating = false;
    private MotorStateEvent ASTATE;
    public RobotNavigator() {
        connect();
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

    // drive methods
    public void forward(){
        robot.executeSyncTwoMotorTask(robot.motorA.run(speed),robot.motorB.run(speed));
    }
    public void left() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(speed),robot.motorB.run(speed-turnvalue));
    }
    public void right() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(speed-turnvalue),robot.motorB.run(speed));
    }
    public void backwards() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(-speed),robot.motorB.run(-speed));
    }
    public void stop() {
        robot.executeSyncTwoMotorTask(robot.motorA.stop(), robot.motorB.stop());
    }
    // wait before next command is executed
    private void waitCommand() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            while(robot.motorA.getState() == Motor.STATE_RUNNING && robot.motorB.getState() == Motor.STATE_RUNNING) {
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void rotateLeft() {
        stop();
        waitCommand();
        robot.executeSyncTwoMotorTask(robot.motorA.run(-speed, 100), robot.motorB.run(-speed, 100));
        waitCommand();
        robot.executeSyncTwoMotorTask(robot.motorA.run(speed, 270), robot.motorB.run(-speed, 270));
        waitCommand();
    }
    public void rotateRight() {
        stop();
        waitCommand();
        robot.executeSyncTwoMotorTask(robot.motorA.run(-speed, 100), robot.motorB.run(-speed, 100));
        waitCommand();
        robot.executeSyncTwoMotorTask(robot.motorA.run(-speed, 270), robot.motorB.run(speed, 270));
        waitCommand();
    }
    public void lastForwardToDestination() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(slowSpeed, 400),robot.motorB.run(slowSpeed, 400));
        waitCommand();
        stop();
    }

    //methods for route
    private void longLength() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(speed, 2000), robot.motorB.run(speed, 2000));
        waitCommand();
    }
    private void rotateTest() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(speed, 166), robot.motorB.run(-speed, 166));
        waitCommand();
    }
    private void shortLength() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(speed, 1000), robot.motorB.run(speed, 1000));
        waitCommand();
    }

    public void scan() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                robot.executeSyncTwoMotorTask(robot.motorA.run(slowSpeed, 1200), robot.motorB.run(-slowSpeed, 1200));
                waitCommand();
                forward();
            }
        };
        thread.start();
    }
    public void testRoute() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                shortLength();
                rotateTest();
                longLength();
                rotateTest();
                shortLength();
                rotateTest();
                longLength();
                rotateTest ();
            }
        };
        thread.start();
    }
    //Button listener for testing
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.bntF: LOGGER.i("Forward");
                forward(); break;
            case R.id.bntR: LOGGER.i("Right"); rotateRight();
                break;
            case R.id.bntL: LOGGER.i("Left"); rotateLeft();
                break;
            case R.id.bntB: LOGGER.i("Back"); backwards();
                break;
            case R.id.bntStop: LOGGER.i("Stop"); stop();
                break;
            case R.id.bntTest: LOGGER.i("Route test");
                testRoute();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }
}
