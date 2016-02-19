package space.klapeyron.rbotapp;

import android.util.Log;

import ru.rbot.android.bridge.service.robotcontroll.controllers.BodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.NeckController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.TwoWheelsBodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.neck.data.Neck;
import ru.rbot.android.bridge.service.robotcontroll.controllers.neck.data.NeckSegment;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;

public class LowLevelNavigationMethods {
    MainActivity mainActivity;
    Robot robot;

    public static final String FORWARD_MOVE = "FORWARD_MOVE";
    public static final String STOP_MOVE = "STOP_MOVE";
    public static final String BACK_MOVE = "BACK_MOVE";
    public static final String DISTANCE_FORWARD_MOVE = "DISTANCE_FORWARD_MOVE";
    public static final String TURN_LEFT = "TURN_LEFT";
    public static final String TURN_RIGHT = "TURN_RIGHT";
    public static final String NECK_UP = "NECK_UP";
    public static final String NECK_DOWN = "NECK_DOWN";
    public static final String LEFT = "LEFT";
    public static final String RIGHT = "RIGHT";

    LowLevelNavigationMethods(MainActivity m) {
        mainActivity = m;
        robot = m.robot;
    }

    public void runOnKey (String key) {
        try {
            switch (key) {
                case FORWARD_MOVE:
                    forwardMove();
                    break;
                case STOP_MOVE:
                    stopMove();
                    break;
                case BACK_MOVE:
                    backMove();
                    break;
                case DISTANCE_FORWARD_MOVE:
                    moveForDistance();
                    break;
                case TURN_LEFT:
                    turnLeft();
                    break;
                case TURN_RIGHT:
                    turnRight();
                    break;
                case NECK_UP:
                    neckUp();
                    break;
                case NECK_DOWN:
                    neckDown();
                    break;
                case LEFT:
                    left();
                    break;
                case RIGHT:
                    right();
                    break;
            }
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    public void stopWheelsAction(String key) {
        try {
            switch (key) {
                case FORWARD_MOVE:
                    stopMove();
                    break;
                case STOP_MOVE:
                    stopMove();
                    break;
                case BACK_MOVE:
                    stopMove();
                    break;
                case TURN_LEFT:
                    stopMove();
                    break;
                case TURN_RIGHT:
                    stopMove();
                    break;
            }
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    private void forwardMove()  throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                wheelsController.setWheelsSpeeds(0f,20f);
            }
        }
    }

    private void stopMove()  throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                Log.i(MainActivity.TAG, "stop move before");
                wheelsController.setWheelsSpeeds(0.0f,0.0f);
                Log.i(MainActivity.TAG, "stop move after");
            }
        }
    }

    private void backMove() throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                wheelsController.setWheelsSpeeds(20f,0f);
            }
        }
    }

    private void moveForDistance() throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                wheelsController.moveForward(20f,0.5f);
            }
        }
    }

    private void turnLeft() throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                wheelsController.setWheelsSpeeds(-20f,20f);
            }
        }
    }

    private void turnRight() throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                wheelsController.setWheelsSpeeds(20f, -20f);
            }
        }
    }

    private void neckUp() {
        try {
            NeckController neckController = (NeckController) robot.getController(NeckController.class);
            Neck neck = neckController.getNeck();
            int neckSegmentsCount = neck.getSegmentsCount();
            NeckSegment neckSegment = neck.getNeckSegment(0);
            neckSegment.setFlag((byte) 0x02);
            neckSegment.setSpeed(5);
            neckSegment.setAngle(0.5f);

            neckSegment = neck.getNeckSegment(1);
            neckSegment.setFlag((byte) 0x02);
            neckSegment.setSpeed(5);
            neckSegment.setAngle(0.5f);

            neckSegment = neck.getNeckSegment(2);
            neckSegment.setFlag((byte) 0x02);
            neckSegment.setSpeed(5);
            neckSegment.setAngle(0.5f);

            //    neckSegment.move();
            neckController.refreshNeckPosition();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    private void neckDown() {
        try {
            NeckController neckController = (NeckController) robot.getController(NeckController.class);
            Neck neck = neckController.getNeck();
            int neckSegmentsCount = neck.getSegmentsCount();
            NeckSegment neckSegment = neck.getNeckSegment(0);
            neckSegment.setFlag((byte) 0x02);
            neckSegment.setSpeed(5);
            neckSegment.setAngle(1f);

            neckSegment = neck.getNeckSegment(1);
            neckSegment.setFlag((byte) 0x02);
            neckSegment.setSpeed(5);
            neckSegment.setAngle(0.0f);

            neckSegment = neck.getNeckSegment(2);
            neckSegment.setFlag((byte) 0x02);
            neckSegment.setSpeed(5);
            neckSegment.setAngle(1f);

            //    neckSegment.move();
            neckController.refreshNeckPosition();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    private void left() throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                wheelsController.turnAround(20f,1.57f);
//                wheelsController.moveForward(29f,0.5f);
                Log.i(MainActivity.TAG, "leftleftleft");
            }
        }
    }

    private void right() throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                wheelsController.turnAround(20f, -1.57f);
            }
        }
    }
}
