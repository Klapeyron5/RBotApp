package space.klapeyron.rbotapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import ru.rbot.android.bridge.service.robotcontroll.controllers.BodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.NeckController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.TwoWheelsBodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.data.TwoWheelState;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.listeners.TwoWheelBodyControllerStateListener;
import ru.rbot.android.bridge.service.robotcontroll.controllers.neck.data.Neck;
import ru.rbot.android.bridge.service.robotcontroll.controllers.neck.data.NeckSegment;
import ru.rbot.android.bridge.service.robotcontroll.controllers.system.listeners.Listener;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;
import ru.rbot.android.bridge.service.robotcontroll.robots.listeners.RobotStateListener;

public class MainActivity extends Activity {
    static final String TAG = "TAG";

    Robot robot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initRobot();

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnTouchListener(new View.OnTouchListener() {
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
                        retry = false;
                    }
                    try {
                        stopMove();
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnTouchListener(new View.OnTouchListener() {
            MyThread2 thread;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "button3 ACTION_DOWN");
                    thread = new MyThread2();
                    thread.setRunning(true);
                    thread.start();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "button3 ACTION_UP");
                    boolean retry = true;
                    while (retry) {
                        thread.setRunning(false);
                        thread.interrupt();
                        retry = false;
                    }
                    try {
                        stopMove();
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });


        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnTouchListener(new View.OnTouchListener() {
            MyThread3 thread;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "button3 ACTION_DOWN");
                    thread = new MyThread3();
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
                        stopMove();
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });


        Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnTouchListener(new View.OnTouchListener() {
            MyThread4 thread;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    thread = new MyThread4();
                    thread.setRunning(true);
                    thread.start();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
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
                        stopMove();
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });


        Button button5 = (Button) findViewById(R.id.button5);
        button5.setOnTouchListener(new View.OnTouchListener() {
            MyThread5 thread;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    thread = new MyThread5();
                    thread.setRunning(true);
                    thread.start();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
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
                }
                return false;
            }
        });

        Button button6 = (Button) findViewById(R.id.button6);
        button6.setOnTouchListener(new View.OnTouchListener() {
            MyThread6 thread;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    thread = new MyThread6();
                    thread.setRunning(true);
                    thread.start();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
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
        robot.start();
        Log.i(TAG, "init robot finished");
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
        Log.i(TAG, "stopMove() finished");
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
        Log.i(TAG, "backMove() finished");
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



    class MyThread1 extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            while(true) {
                if(running)
                    try {
                        forwardMove();
                        sleep(600);
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
            //            e.printStackTrace();
                    }
                else
                    return;
            }
        }

        public void setRunning(boolean b) {
            running = b;
        }
    }

    class MyThread2 extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            while(true) {
                if(running)
                    try {
                        backMove();
                        sleep(600);
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
         //               e.printStackTrace();
                    }
                else
                    return;
            }
        }

        public void setRunning(boolean b) {
            running = b;
        }
    }

    class MyThread3 extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            while(true) {
                if(running)
                    try {
                        turnLeft();
                        sleep(600);
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        //               e.printStackTrace();
                    }
                else
                    return;
            }
        }

        public void setRunning(boolean b) {
            running = b;
        }
    }

    class MyThread4 extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            while(true) {
                if(running)
                    try {
                        turnRight();
                        sleep(600);
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        //            e.printStackTrace();
                    }
                else
                    return;
            }
        }

        public void setRunning(boolean b) {
            running = b;
        }
    }

    class MyThread5 extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            while(true) {
                if(running)
                    try {
                        neckUp();
                        sleep(600);
                    } catch (InterruptedException e) {
                        //            e.printStackTrace();
                    }
                else
                    return;
            }
        }

        public void setRunning(boolean b) {
            running = b;
        }
    }

    class MyThread6 extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            while(true) {
                if(running)
                    try {
                        neckDown();
                        sleep(600);
                    } catch (InterruptedException e) {
                        //            e.printStackTrace();
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
