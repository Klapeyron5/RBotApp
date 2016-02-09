package space.klapeyron.rbotapp;

import android.util.Log;

import java.util.ArrayList;

import ru.rbot.android.bridge.service.robotcontroll.controllers.BodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.TwoWheelsBodyController;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;


public class LowLevelNavigationTasks {
    MainActivity mainActivity;
    LowLevelNavigationMethods lowLevelNavigationMethods;
    float startPath;
    float startAngle;
    Robot robot;
    ForwardTaskThread forwardTaskThread;
    ForwardThread forwardThread;

    private final static float forwardDistance = 0.5f;


    int[] arrayPath = {1,2,1};
    ArrayList<Integer> path;//0-right; 1-forward;2-left;

    LowLevelNavigationTasks(MainActivity m, LowLevelNavigationMethods l) {
        mainActivity = m;
        lowLevelNavigationMethods = l;
        startPath = mainActivity.passedWay;
        startAngle = mainActivity.angle;
        robot = mainActivity.robot;
    }

    public void setTask() throws ControllerException {
        Navigation navigation = new Navigation();
        path = navigation.getPath();

       // path = new ArrayList<>();
       // arrayInList();//TODO
        TaskThread taskThread = new TaskThread();
        taskThread.start();
    }

    class TaskThread extends Thread {
        @Override
        public void run() {
            int straightLineCoeff = 1;
            distanceForwardThread(straightLineCoeff);
            for(int i=1;i<path.size();i++) {
                try {
                    switch(path.get(i)) {
                        case 0:
                            right();
                            sleep(2500);
                            break;
                        case 1:
                            if (path.get(i - 1) == 1)
                            straightLineCoeff++;
                            if (i == path.size() - 1) {
                                distanceForwardThread(straightLineCoeff);
                                straightLineCoeff = 1;
                            }
                            else
                                if (path.get(i + 1) != 1) {
                                    distanceForwardThread(straightLineCoeff);
                                    straightLineCoeff = 1;
                                }
                            break;
                        case 2:
                            left();
                            sleep(2500);
                            break;
                    }
                } catch (ControllerException e) {} catch (InterruptedException e) {}
            }
        }
    }

    private void distanceForwardThread(int straightLineCoeff) {
        Log.i(MainActivity.TAG, "forwardThread started "+straightLineCoeff);
        StartingForwardThread startingForwardThread = new StartingForwardThread();
        startingForwardThread.start(); //acceleration on first forwardDistance

        forwardThread = new ForwardThread(straightLineCoeff - 1);
        forwardThread.start();
        try {
                forwardThread.join();
                Log.i(MainActivity.TAG, "forwardThread finished");
        } catch (InterruptedException e) {}
    }

    class StartingForwardThread extends Thread {
        private float startPath;
        private float[] accelerationSpeeds = {};

        StartingForwardThread() {
            startPath = mainActivity.passedWay;
        }

        @Override
        public void run() {
            if( robot.isControllerAvailable( BodyController.class ) )
            {
                BodyController bodyController = null;
                try {
                    bodyController = (BodyController) robot.getController( BodyController.class );
                    if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
                    {
                        TwoWheelsBodyController wheelsController = null;
                        wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                        float i = 0;
                        while(true) {
                            if(i<20) //acceleration
                                i++;
                            if(mainActivity.passedWay - startPath < LowLevelNavigationTasks.forwardDistance)
                                try {
                                    wheelsController.setWheelsSpeeds(i,i);
                                    sleep(600);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            else {
                                //        lowLevelNavigationMethods.stopWheelsAction(lowLevelNavigationKey);
                                Log.i(MainActivity.TAG, Float.toString(mainActivity.passedWay - startPath));
                                return;
                            }
                        }
                    }
                } catch (ControllerException e) {}
            }
        }
    }


    class ForwardThread extends Thread {
        private float startPath;
        private float purposePath;

        ForwardThread(int straightLineCoeff) {
            startPath = mainActivity.passedWay;
            purposePath = straightLineCoeff * LowLevelNavigationTasks.forwardDistance;
        }

        @Override
        public void run() {
            Log.i(MainActivity.TAG,"RUN");
            while(true) {
                if(mainActivity.passedWay - startPath < purposePath)
                    try {
                        lowLevelNavigationMethods.runOnKey(LowLevelNavigationMethods.FORWARD_MOVE);
                        sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                else {
            //        lowLevelNavigationMethods.stopWheelsAction(lowLevelNavigationKey);
                    Log.i(MainActivity.TAG, Float.toString(mainActivity.passedWay - startPath));
                    return;
                }
            }
        }
    }

    private void distanceForward() throws ControllerException {
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

    private void left() throws ControllerException {
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            BodyController bodyController = (BodyController) robot.getController( BodyController.class );
            if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
            {
                TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                Log.i(MainActivity.TAG, "left move before");
                wheelsController.turnAround(20f, 1.57f);
                Log.i(MainActivity.TAG, "left move after");
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
                wheelsController.turnAround(20f,-1.57f);
            }
        }
    }

    //TODO
    private void arrayInList() {
        for(int i=0;i<arrayPath.length;i++)
            path.add(arrayPath[i]);
    }











    //TODO

    public void leftTask() {
        lowLevelNavigationMethods.runOnKey(LowLevelNavigationMethods.LEFT);
    }

    public void forwardTask() {
        String key = LowLevelNavigationMethods.TURN_LEFT;
        startPath = mainActivity.passedWay;
        startAngle = mainActivity.angle;
        forwardTaskThread = new ForwardTaskThread(key,this);
        forwardTaskThread.setRunning(true);
        forwardTaskThread.start();
    }

    class ForwardTaskThread extends Thread {
        private boolean running = false;
        private String lowLevelNavigationKey;
        private LowLevelNavigationTasks lowLevelNavigationTasks;

        ForwardTaskThread(String k,LowLevelNavigationTasks l) {
            lowLevelNavigationKey = k;
            lowLevelNavigationTasks = l;
        }

        @Override
        public void run() {
            while(true) {
                if(running) {
                    try {
                        lowLevelNavigationTasks.lowLevelNavigationMethods.runOnKey(lowLevelNavigationKey);
                        sleep(600);
                        if(Math.abs(lowLevelNavigationTasks.mainActivity.angle - lowLevelNavigationTasks.startAngle) > 1.57f) {
                            running = false;
                            lowLevelNavigationTasks.lowLevelNavigationMethods.runOnKey(LowLevelNavigationMethods.STOP_MOVE);
                            return;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                /*    if(lowLevelNavigationTasks.mainActivity.passedWay - lowLevelNavigationTasks.startPath > 0.41f) {
                        Log.i(MainActivity.TAG, "startPath: " + Float.toString(lowLevelNavigationTasks.startPath) + "  passedPath: " + Float.toString(lowLevelNavigationTasks.mainActivity.passedWay) + "  TASK: " + Float.toString(lowLevelNavigationTasks.mainActivity.passedWay - lowLevelNavigationTasks.startPath));
                        running = false;
                        Log.i(MainActivity.TAG,"BEFORE LEFT");
                        leftTask();
                        Log.i(MainActivity.TAG,"AFTER LEFT");
                        return;
                    }*/
                }
                else
                    return;
            }
        }

        public void setRunning(boolean b) {
            running = b;
        }

        public boolean isRunning() {
            return running;
        }
    }
}
