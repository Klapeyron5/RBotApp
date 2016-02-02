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
import ru.rbot.android.bridge.service.robotcontroll.controllers.system.listeners.Listener;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;
import ru.rbot.android.bridge.service.robotcontroll.robots.listeners.RobotStateListener;

public class MainActivity extends Activity {
    static final String TAG = "TAG";

    Robot robot;
    BodyController bodyController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initRobot();

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    button1_method();
                } catch (ControllerException e) {
                    e.printStackTrace();
                }
            }
        });

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    button2_method();
                } catch (ControllerException e) {
                    e.printStackTrace();
                }
            }
        });

        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnTouchListener(new View.OnTouchListener() {
            MyThread1 thread;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "button3 ACTION_DOWN");
                    thread = new MyThread1();
                    thread.setRunning(true);
                    thread.start();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "button3 ACTION_UP");
                    boolean retry = true;
                    while (retry) {
                        thread.setRunning(false);
                        thread.interrupt();
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        retry = false;
                    }
                    try {
                        button2_method();
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnTouchListener(new View.OnTouchListener() {
            MyThread2 thread;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "button4 ACTION_DOWN");

                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "button4 ACTION_UP");
                    try {
                        button2_method();
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
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
        bodyController = new BodyController(this,robot);
        robot.start();
        Log.i(TAG, "init robot finished");
    }

    private void button1_method()  throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                //      wheelsController.moveForward(4f, 0.5f);
                //        TwoWheelState.Speed twoWheelsSpeed = new TwoWheelState.Speed(4,4);
                wheelsController.setWheelsSpeeds(20f,20f);
            }
        }
    }

    private void button2_method()  throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                //      wheelsController.moveForward(4f, 0.5f);
                //        TwoWheelState.Speed twoWheelsSpeed = new TwoWheelState.Speed(4,4);
                wheelsController.setWheelsSpeeds(0.0f,0.0f);
            }
        }
        Log.i(TAG, "button2_method finished");
    }

    private void button3_method() throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                //      wheelsController.moveForward(4f, 0.5f);
                //        TwoWheelState.Speed twoWheelsSpeed = new TwoWheelState.Speed(4,4);
                wheelsController.setWheelsSpeeds(4f,4f);
            }
        }
        Log.i(TAG, "button2_method finished");
    }

    private void button4_method() throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                wheelsController.moveForward(4f, -0.001f);
                //        wheelsController.setWheelsSpeeds(1f,1f);
            }
        }
        Log.i(TAG, "button2_method finished");
    }





    class MyThread1 extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            while(true) {
                if(running)
                    try {
                        button1_method();
                        sleep(600);
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                else
                    return;
            }
        }

        public void setRunning(boolean b) {
            running = b;
            Log.i(TAG, "setRunning("+b+")");
        }
    }

    class MyThread2 extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            while(true) {
                if(running)
                    try {
                        button4_method();
                    } catch (ControllerException e) {
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
