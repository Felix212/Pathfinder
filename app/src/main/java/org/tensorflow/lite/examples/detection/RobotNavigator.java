package org.tensorflow.lite.examples.detection;
import org.tensorflow.lite.examples.detection.env.Logger;
import android.view.View;


import java.util.concurrent.atomic.AtomicBoolean;

import geist.re.mindlib.RobotService;
import geist.re.mindlib.events.LightStateEvent;
import geist.re.mindlib.events.MotorStateEvent;
import geist.re.mindlib.exceptions.SensorDisconnectedException;
import geist.re.mindlib.hardware.Motor;
import geist.re.mindlib.hardware.Sensor;
import geist.re.mindlib.listeners.LightSensorListener;
import geist.re.mindlib.listeners.MotorStateListener;


public class RobotNavigator implements View.OnClickListener {
    private static final String ROBOT_NAME = "SpoReiJo";
    private int SPEED = 15;
    private int SLOWSPEED = 7;
    private int TURNVALUE = 8;
    private RobotService robot = new RobotService();
    private AtomicBoolean scanning = new AtomicBoolean(false);
    public Thread scanThread;
    public int rotationDirection = -1;
    public final int LEFT = 0;
    public final int RIGHT = 1;

    public RobotNavigator() {
        connect();
    }
    private static final Logger LOGGER = new Logger();


    public void connect(){
        if(robot.getConnectionState() != RobotService.CONN_STATE_CONNECTED &&
                robot.getConnectionState() != RobotService.CONN_STATE_CONNECTING) {
            robot.connectToRobot(ROBOT_NAME);
        }
    }

    // drive methods
    public void forward() {
        if(scanThread == null) {
            robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED),robot.motorB.run(SPEED));
        } else {
            this.scanning.set(false);
            this.scanThread.interrupt();
            robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED),robot.motorB.run(SPEED));
        }
    }
    public void left() {
        if(scanThread == null) {
            robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED),robot.motorB.run(SPEED-TURNVALUE));
        } else {
            this.scanning.set(false);
            this.scanThread.interrupt();
            robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED),robot.motorB.run(SPEED-TURNVALUE));
        }

    }
    public void right() {
        if(scanThread == null) {
            robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED-TURNVALUE),robot.motorB.run(SPEED));
        } else {
            this.scanning.set(false);
            this.scanThread.interrupt();
            robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED-TURNVALUE),robot.motorB.run(SPEED));
        }
    }
    public void backwards() {
        if(scanThread == null) {
            robot.executeSyncTwoMotorTask(robot.motorA.run(-SPEED),robot.motorB.run(-SPEED));
        } else {
            this.scanning.set(false);
            this.scanThread.interrupt();
            robot.executeSyncTwoMotorTask(robot.motorA.run(-SPEED),robot.motorB.run(-SPEED));
        }
    }
    public void stop() {
        robot.executeSyncTwoMotorTask(robot.motorA.stop(), robot.motorB.stop());
    }
    // wait before next command is executed
    private void waitCommand() {
        scanning.set(true);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(
                    "Thread was interrupted, Failed to complete operation");
        }
        while(robot.motorA.getState() == Motor.STATE_RUNNING && robot.motorB.getState() == Motor.STATE_RUNNING && scanning.get()) {
        }
        stop();
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(
                    "Thread was interrupted, Failed to complete operation");
        }
    }

    public void resume() {
        stop();
        waitCommand();
        forward();
    }

    public void rotateLeft() {
        this.scanning.set(false);
        this.scanThread.interrupt();
        stop();
        waitCommand();
        robot.executeSyncTwoMotorTask(robot.motorA.run(-SPEED, 20), robot.motorB.run(-SPEED, 20));
        waitCommand();
        robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED, 50), robot.motorB.run(-SPEED, 50));
        waitCommand();
    }
    public void rotateRight() {
        this.scanning.set(false);
        this.scanThread.interrupt();
        stop();
        waitCommand();
        robot.executeSyncTwoMotorTask(robot.motorA.run(-SPEED, 20), robot.motorB.run(-SPEED, 20));
        waitCommand();
        robot.executeSyncTwoMotorTask(robot.motorA.run(-SPEED, 50), robot.motorB.run(SPEED, 50));
        waitCommand();
    }
    public void rotateRightEndless() {
        this.scanning.set(false);
        this.scanThread.interrupt();
        rotationDirection = RIGHT;
        stop();
        waitCommand();
        robot.executeSyncTwoMotorTask(robot.motorA.run(-SLOWSPEED), robot.motorB.run(SLOWSPEED));
    }
    public void rotateLeftEndless() {
        this.scanning.set(false);
        this.scanThread.interrupt();
        rotationDirection = LEFT;
        stop();
        waitCommand();
        robot.executeSyncTwoMotorTask(robot.motorA.run(SLOWSPEED), robot.motorB.run(-SLOWSPEED));
    }
    public void lastForwardToDestination() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(SLOWSPEED, 400),robot.motorB.run(SLOWSPEED, 400));
        waitCommand();
        stop();
    }

    public void scan() {
        this.scanning.set(true);
        Thread thread = new Thread() {
            @Override
            public void run() {
                while(scanning.get()) {
                    if(rotationDirection == LEFT) {
                        robot.executeSyncTwoMotorTask(robot.motorA.run(SLOWSPEED, 600), robot.motorB.run(-SLOWSPEED, 600));
                        waitCommand();
                        robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED),robot.motorB.run(SPEED));
                        scanning.set(false);
                    }
                    if(rotationDirection == RIGHT) {
                        robot.executeSyncTwoMotorTask(robot.motorA.run(-SLOWSPEED, 600), robot.motorB.run(SLOWSPEED, 600));
                        waitCommand();
                        robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED),robot.motorB.run(SPEED));
                        scanning.set(false);
                    }
                    if(rotationDirection == -1) {
                        robot.executeSyncTwoMotorTask(robot.motorA.run(SLOWSPEED, 600), robot.motorB.run(-SLOWSPEED, 600));
                        waitCommand();
                        robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED),robot.motorB.run(SPEED));
                        scanning.set(false);
                    }
                }

                LOGGER.i("Finished Thread");

            }
        };
        this.scanThread = thread;
        thread.start();
    }
    //methods for route
    private void longLength() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED, 2000), robot.motorB.run(SPEED, 2000));
        waitCommand();
    }
    private void rotateTest() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED, 120), robot.motorB.run(-SPEED, 120));
        waitCommand();
    }
    private void shortLength() {
        robot.executeSyncTwoMotorTask(robot.motorA.run(SPEED, 1000), robot.motorB.run(SPEED, 1000));
        waitCommand();
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
