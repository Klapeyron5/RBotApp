package space.klapeyron.rbotapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import ru.rbot.android.bridge.service.robotcontroll.controllers.BodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.TwoWheelsBodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.data.TwoWheelState;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.listeners.TwoWheelBodyControllerStateListener;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;
import ru.rbot.android.bridge.service.robotcontroll.robots.listeners.RobotStateListener;

public class MainActivity extends Activity {
    static final String TAG = "TAG";

    Robot robot;
    TwoWheelBodyControllerStateListener twoWheelBodyControllerStateListener;
    LowLevelNavigationMethods lowLevelNavigationMethods;
    public float passedWay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initRobot();
        lowLevelNavigationMethods = new LowLevelNavigationMethods(robot);

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnTouchListener(new ButtonTouch(LowLevelNavigationMethods.FORWARD_MOVE));

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnTouchListener(new ButtonTouch(LowLevelNavigationMethods.BACK_MOVE));

        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnTouchListener(new ButtonTouch(LowLevelNavigationMethods.TURN_LEFT));

        Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnTouchListener(new ButtonTouch(LowLevelNavigationMethods.TURN_RIGHT));

        Button button5 = (Button) findViewById(R.id.button5);
        button5.setOnTouchListener(new ButtonTouch(LowLevelNavigationMethods.NECK_UP));

        Button button6 = (Button) findViewById(R.id.button6);
        button6.setOnTouchListener(new ButtonTouch(LowLevelNavigationMethods.NECK_DOWN));

        Button button7 = (Button) findViewById(R.id.button7);
        button7.setOnTouchListener(new ButtonTouch(LowLevelNavigationMethods.WRITE_PATH));
    }



    private void initRobot() {
        robot = new Robot(this);

        RobotStateListener robotStateListener = new RobotStateListener() {
            @Override
            public void onRobotReady() {
                Log.i(TAG, "onRobotReady");
            }

            @Override
            public void onRobotInitError() {
                Log.i(TAG, "onRobotInitError");
            }

            @Override
            public void onRobotDisconnect() {
                Log.i(TAG, "onRobotDisconnect");
            }
        };

        robot.setRobotStateListener(robotStateListener);
        robot.start();

        twoWheelBodyControllerStateListener = new TwoWheelBodyControllerStateListener() {
            @Override
            public void onWheelStateRecieved(TwoWheelState twoWheelState) {
         //       float leftSpeed = twoWheelState.getSpeed().getLWheelSpeed();
         //       float rightSpeed = twoWheelState.getSpeed().getLWheelSpeed();
                passedWay = twoWheelState.getOdometryInfo().getPath();
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
    }


    class ButtonTouch implements View.OnTouchListener {
        private ThreadForSimpleNavigationButtons thread;
        private String key;

        ButtonTouch(String k) {
            key = k;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                thread = new ThreadForSimpleNavigationButtons(key);
                thread.setRunning(true);
                thread.start();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                boolean retry = true;
                while (retry) {
                    thread.setRunning(false);
                    thread.interrupt();
                    retry = false;
                }
                lowLevelNavigationMethods.stopWheelsAction(key);
      //          lowLevelNavigationMethods.runOnKey(LowLevelNavigationMethods.STOP_MOVE);
            }
            return false;
        }
    }


    class ThreadForSimpleNavigationButtons extends Thread {
        private boolean running = false;
        private String lowLevelNavigationKey;

        ThreadForSimpleNavigationButtons(String k) {
            lowLevelNavigationKey = k;
        }

        @Override
        public void run() {
            while(true) {
                if(running)
                    try {
                        lowLevelNavigationMethods.runOnKey(lowLevelNavigationKey);
                        sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                else
                    return;
            }
        }

        public void setRunning(boolean b) {
            running = b;
        }
    }
}
