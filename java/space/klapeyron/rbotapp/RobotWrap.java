package space.klapeyron.rbotapp;

import android.content.Context;
import android.util.Log;

import ru.rbot.android.bridge.service.robotcontroll.controllers.BodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.TwoWheelsBodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.data.TwoWheelState;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.listeners.TwoWheelBodyControllerStateListener;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.listeners.RobotStateListener;


public class RobotWrap extends ru.rbot.android.bridge.service.robotcontroll.robots.Robot {
    //Robot states list:
    public String ROBOT_STATE;
    private static final String ROBOT_CONNECTED = "connected";
    private static final String ROBOT_INIT_ERROR = "init error";
    private static final String ROBOT_DISCONNECTED = "disconnected";

    private MainActivity mainActivity;

    //counted odometry info
    float countedPath;
    public int currentDirection;

    //primary odometry info
    float odometryPath;
    float odometryAngle;
    float odometryAbsoluteX;
    float odometryAbsoluteY;
    float odometryWheelSpeedLeft;
    float odometryWheelSpeedRight;

    public final RobotStateListener robotStateListener = new RobotStateListener() {
        @Override
        public void onRobotReady() {
            setReadingOdometry();
            ROBOT_STATE = ROBOT_CONNECTED;
            mainActivity.setServerState(MainActivity.SERVER_WAITING_TASK);
            mainActivity.setRobotConnectionState(ROBOT_STATE);
            Log.i(mainActivity.TAG, ROBOT_STATE);
        }

        @Override
        public void onRobotInitError() {
            ROBOT_STATE = ROBOT_INIT_ERROR;
            mainActivity.setServerState(MainActivity.SERVER_LOADED);
            mainActivity.setRobotConnectionState(ROBOT_STATE);
            Log.i(mainActivity.TAG, ROBOT_STATE);
            //TODO //dialog with instruction to run Bridge
        }

        @Override
        public void onRobotDisconnect() {
            ROBOT_STATE = ROBOT_DISCONNECTED;
            Log.i(mainActivity.TAG, ROBOT_STATE);
            synchronized (this) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.setServerState(MainActivity.SERVER_LOADED);
                        mainActivity.setRobotConnectionState(ROBOT_STATE);
                    }
                });
            }
        }
    };

    public RobotWrap(Context pContext) {
        super(pContext);
        mainActivity = (MainActivity) pContext;
        setRobotStateListener(robotStateListener);
        start();
    }

    @Override
    public void setRobotStateListener(RobotStateListener pOnRobotReadyListener) {
        super.setRobotStateListener(pOnRobotReadyListener);
    }

    public void setReadingOdometry() {
        TwoWheelBodyControllerStateListener twoWheelBodyControllerStateListener = new TwoWheelBodyControllerStateListener() {
            @Override
            public void onWheelStateRecieved(TwoWheelState twoWheelState) {
                odometryPath = twoWheelState.getOdometryInfo().getPath();
                odometryAngle = twoWheelState.getOdometryInfo().getAngle();
                odometryAbsoluteX = (float) (-twoWheelState.getOdometryInfo().getX()+0.5*3+0.25);
                odometryAbsoluteY = (float) (-twoWheelState.getOdometryInfo().getY()+0.5+0.25);
                odometryWheelSpeedLeft = twoWheelState.getSpeed().getLWheelSpeed();
                odometryWheelSpeedRight = twoWheelState.getSpeed().getRWheelSpeed();


                mainActivity.textViewCountedPath.setText(Float.toString(countedPath));
                mainActivity.textViewOdometryPath.setText(Float.toString(odometryPath));
                mainActivity.textViewOdometryAngle.setText(Float.toString(odometryAngle));
                mainActivity.textViewOdometryX.setText(Float.toString(odometryAbsoluteX));
                mainActivity.textViewOdometryY.setText(Float.toString(odometryAbsoluteY));
                mainActivity.textViewOdometrySpeedL.setText(Float.toString(odometryWheelSpeedLeft));
                mainActivity.textViewOdometrySpeedR.setText(Float.toString(odometryWheelSpeedRight));
            }
        };
        if( this.isControllerAvailable( BodyController.class ) )
        {
            try {
                BodyController bodyController = (BodyController) this.getController( BodyController.class );
                if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
                {
                    TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                    wheelsController.setListener(twoWheelBodyControllerStateListener,100);
                }
            } catch (ControllerException e) {
                e.printStackTrace();
            }
        }
    }
}
