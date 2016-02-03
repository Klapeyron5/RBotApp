package space.klapeyron.rbotapp;

import ru.rbot.android.bridge.service.robotcontroll.controllers.BodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.TwoWheelsBodyController;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;

public class LowLevelNavigationMethods {
    Robot robot;

    public static final String FORWARD_MOVE = "FORWARD_MOVE";
    public static final String STOP_MOVE = "STOP_MOVE";
    public static final String BACK_MOVE = "BACK_MOVE";

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
}
