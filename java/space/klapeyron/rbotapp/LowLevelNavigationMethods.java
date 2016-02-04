package space.klapeyron.rbotapp;

import android.util.Log;

import ru.rbot.android.bridge.service.robotcontroll.controllers.BodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.NeckController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.TwoWheelsBodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.data.TwoWheelState;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.listeners.TwoWheelBodyControllerStateListener;
import ru.rbot.android.bridge.service.robotcontroll.controllers.neck.data.Neck;
import ru.rbot.android.bridge.service.robotcontroll.controllers.neck.data.NeckSegment;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;

public class LowLevelNavigationMethods {
    Robot robot;

    private float passedWay;
    private float currentX;
    private float currentY;
    private float wheelSpeedLeft;
    private float wheelSpeedRight;
    private float angle;

    public static final String FORWARD_MOVE = "FORWARD_MOVE";
    public static final String STOP_MOVE = "STOP_MOVE";
    public static final String BACK_MOVE = "BACK_MOVE";
    public static final String TURN_LEFT = "TURN_LEFT";
    public static final String TURN_RIGHT = "TURN_RIGHT";
    public static final String NECK_UP = "NECK_UP";
    public static final String NECK_DOWN = "NECK_DOWN";
    public static final String WRITE_PATH = "WRITE_PATH";

    LowLevelNavigationMethods(Robot r) {
        robot = r;
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
//TODO
                case WRITE_PATH:
                    writePath();
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
                wheelsController.setWheelsSpeeds(20f,20f);
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
                wheelsController.setWheelsSpeeds(0.0f,0.0f);
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
                wheelsController.setWheelsSpeeds(-20f,-20f);
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
                wheelsController.setWheelsSpeeds(20f,-20f);
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

    private void writePath() {
        TwoWheelBodyControllerStateListener twoWheelBodyControllerStateListener = new TwoWheelBodyControllerStateListener() {
            @Override
            public void onWheelStateRecieved(TwoWheelState twoWheelState) {
                passedWay = twoWheelState.getOdometryInfo().getPath();
                currentX = twoWheelState.getOdometryInfo().getX();
                currentY = twoWheelState.getOdometryInfo().getY();
                wheelSpeedLeft = twoWheelState.getSpeed().getLWheelSpeed();
                wheelSpeedRight = twoWheelState.getSpeed().getRWheelSpeed();
                angle = twoWheelState.getOdometryInfo().getAngle();
            }
        };
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            try {
                BodyController bodyController = (BodyController) robot.getController( BodyController.class );
                if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
                {
                    TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                    wheelsController.setListener(twoWheelBodyControllerStateListener,100);
                }
            } catch (ControllerException e) {
                e.printStackTrace();
            }
        }
        Log.i(MainActivity.TAG,"Passed way: "+passedWay);
        Log.i(MainActivity.TAG,"Current X: "+currentX);
        Log.i(MainActivity.TAG,"Current Y: "+currentY);
        Log.i(MainActivity.TAG,"Wheel speed left : "+wheelSpeedLeft);
        Log.i(MainActivity.TAG,"Wheel speed right: "+wheelSpeedRight);
        Log.i(MainActivity.TAG,"angle: "+angle);
    }
}
