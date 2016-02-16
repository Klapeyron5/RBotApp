package space.klapeyron.rbotapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ru.rbot.android.bridge.service.robotcontroll.controllers.BodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.TwoWheelsBodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.data.TwoWheelState;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.listeners.TwoWheelBodyControllerStateListener;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;
import ru.rbot.android.bridge.service.robotcontroll.robots.listeners.RobotStateListener;

public class MainActivity extends Activity {
    static final String TAG = "TAG";

    MainActivity link = this;
    Robot robot;
    LowLevelNavigationMethods lowLevelNavigationMethods;
    LowLevelNavigationTasks lowLevelNavigationTasks;
    TTSManager ttsManager = null;

    public final static String UUID = "e91521df-92b9-47bf-96d5-c52ee838f6f6";

    //TODO
    public TextView path;
    public TextView X;
    public TextView Y;
    public TextView SpeedL;
    public TextView SpeedR;
    public TextView Angle;

    float passedWay;
    float currentX;
    float currentY;
    float wheelSpeedLeft;
    float wheelSpeedRight;
    float angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initRobot();
        initConstructor();
    }

    private void initRobot() {
        robot = new Robot(this);

        RobotStateListener robotStateListener = new RobotStateListener() {
            @Override
            public void onRobotReady() {
                odometryMethod();
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
    }

    private void initConstructor() {
        lowLevelNavigationMethods = new LowLevelNavigationMethods(this);
        /*tts*/
        ttsManager = new TTSManager();
        ttsManager.init(this);

        path   = (TextView) findViewById(R.id.textView7);
        X      = (TextView) findViewById(R.id.textView8);
        Y      = (TextView) findViewById(R.id.textView9);
        SpeedL = (TextView) findViewById(R.id.textView10);
        SpeedR = (TextView) findViewById(R.id.textView11);
        Angle  = (TextView) findViewById(R.id.textView12);

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnTouchListener(new navigationButtonTouch(LowLevelNavigationMethods.FORWARD_MOVE));

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnTouchListener(new navigationButtonTouch(LowLevelNavigationMethods.BACK_MOVE));

        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnTouchListener(new navigationButtonTouch(LowLevelNavigationMethods.TURN_LEFT));

        Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnTouchListener(new navigationButtonTouch(LowLevelNavigationMethods.TURN_RIGHT));

        Button button7 = (Button) findViewById(R.id.button7);
        button7.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if( robot.isControllerAvailable( BodyController.class ) )
                        {
                            BodyController bodyController = null;
                            bodyController = (BodyController) robot.getController( BodyController.class );
                            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
                            {
                                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                                wheelsController.turnAround(10f, (float) Math.PI);
                            }
                        }
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if( robot.isControllerAvailable( BodyController.class ) )
                        {
                            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
                            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
                            {
                                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                                wheelsController.setWheelsSpeeds(0f,0f);
                            }
                        }
                    }
                } catch (ControllerException e) {}
                return false;
            }
        });

        Button button8 = (Button) findViewById(R.id.button8);
        button8.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (robot.isControllerAvailable(BodyController.class)) {
                            BodyController bodyController = null;
                            bodyController = (BodyController) robot.getController(BodyController.class);
                            if (bodyController.isControllerAvailable(TwoWheelsBodyController.class)) {
                                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController(TwoWheelsBodyController.class);
                                wheelsController.moveForward(20f, 100f);
                            }
                        }
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (robot.isControllerAvailable(BodyController.class)) {
                            BodyController bodyController = (BodyController) robot.getController(BodyController.class);
                            if (bodyController.isControllerAvailable(TwoWheelsBodyController.class)) {
                                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController(TwoWheelsBodyController.class);
                                wheelsController.setWheelsSpeeds(0f, 0f);
                            }
                        }
                    }
                } catch (ControllerException e) {
                }
                return false;
            }
        });

        Button button6 = (Button) findViewById(R.id.button6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lowLevelNavigationTasks = new LowLevelNavigationTasks(link);
                try {
                    lowLevelNavigationTasks.setTask();
                } catch (ControllerException e) {}
            }
        });

        Button button10 = (Button) findViewById(R.id.button10);
        button10.setOnClickListener(new View.OnClickListener(){
            @Override
        public void onClick(View v){
                ttsManager.Greeting();
            }
        });

    }

    private void odometryMethod() {
        TwoWheelBodyControllerStateListener twoWheelBodyControllerStateListener = new TwoWheelBodyControllerStateListener() {
            @Override
            public void onWheelStateRecieved(TwoWheelState twoWheelState) {
                passedWay = twoWheelState.getOdometryInfo().getPath();
                currentX = (float) (-twoWheelState.getOdometryInfo().getX()+0.5*3+0.25);
                currentY = (float) (-twoWheelState.getOdometryInfo().getY()+0.5+0.25);
                wheelSpeedLeft = twoWheelState.getSpeed().getLWheelSpeed();
                wheelSpeedRight = twoWheelState.getSpeed().getRWheelSpeed();
                angle = twoWheelState.getOdometryInfo().getAngle();

                path.setText(Float.toString(passedWay));
                X.setText(Float.toString(currentX));
                Y.setText(Float.toString(currentY));
                SpeedL.setText(Float.toString(wheelSpeedLeft));
                SpeedR.setText(Float.toString(wheelSpeedRight));
                Angle.setText(Float.toString(angle));
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

    public void setTaskFromBT(int Y,int X) {
        lowLevelNavigationTasks = new LowLevelNavigationTasks(link);
        try {
            lowLevelNavigationTasks.setTaskFromBT(Y,X);
        } catch (ControllerException e) {}
    }

    class navigationButtonTouch implements View.OnTouchListener {
        private ThreadForSimpleNavigationButtons thread;
        private String key;

        navigationButtonTouch(String k) {
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
                    lowLevelNavigationMethods.stopWheelsAction(key);
                    thread.interrupt();
        //            Log.i(TAG, "INTERRUPT");
                    retry = false;
                }
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
          //              Log.i(TAG,"RUN");
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
       //     Log.i(TAG,"running = "+running);
        }
    }
}
