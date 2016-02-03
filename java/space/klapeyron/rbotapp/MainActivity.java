package space.klapeyron.rbotapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;
import ru.rbot.android.bridge.service.robotcontroll.robots.listeners.RobotStateListener;

public class MainActivity extends Activity {
    static final String TAG = "TAG";

    Robot robot;
    LowLevelNavigationMethods lowLevelNavigationMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initRobot();
        lowLevelNavigationMethods = new LowLevelNavigationMethods(robot);

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnTouchListener(new View.OnTouchListener() {
            ThreadForSimpleNavigationButtons thread;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    thread = new ThreadForSimpleNavigationButtons(LowLevelNavigationMethods.FORWARD_MOVE);
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
                    lowLevelNavigationMethods.runOnKey(LowLevelNavigationMethods.STOP_MOVE);
                }
                return false;
            }
        });

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnTouchListener(new View.OnTouchListener() {
            ThreadForSimpleNavigationButtons thread;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    thread = new ThreadForSimpleNavigationButtons(LowLevelNavigationMethods.BACK_MOVE);
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
                    lowLevelNavigationMethods.runOnKey(LowLevelNavigationMethods.STOP_MOVE);
                }
                return false;
            }
        });


        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnTouchListener(new View.OnTouchListener() {
            ThreadForSimpleNavigationButtons thread;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    thread = new ThreadForSimpleNavigationButtons(LowLevelNavigationMethods.TURN_LEFT);
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
                    lowLevelNavigationMethods.runOnKey(LowLevelNavigationMethods.STOP_MOVE);
                }
                return false;
            }
        });


        Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnTouchListener(new View.OnTouchListener() {
            ThreadForSimpleNavigationButtons thread;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    thread = new ThreadForSimpleNavigationButtons(LowLevelNavigationMethods.TURN_RIGHT);
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
                    lowLevelNavigationMethods.runOnKey(LowLevelNavigationMethods.STOP_MOVE);
                }
                return false;
            }
        });


        Button button5 = (Button) findViewById(R.id.button5);
        button5.setOnTouchListener(new View.OnTouchListener() {
            ThreadForSimpleNavigationButtons thread;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    thread = new ThreadForSimpleNavigationButtons(LowLevelNavigationMethods.NECK_UP);
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
            ThreadForSimpleNavigationButtons thread;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    thread = new ThreadForSimpleNavigationButtons(LowLevelNavigationMethods.NECK_DOWN);
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
