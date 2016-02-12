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
//    ForwardThread forwardThread;

    private final static float forwardDistance = 0.5f;


    int[] arrayPath = {1,1};
    ArrayList<Integer> path;//0-right; 1-forward;2-left;

    LowLevelNavigationTasks(MainActivity m, LowLevelNavigationMethods l) {
        mainActivity = m;
        lowLevelNavigationMethods = l;
        startPath = mainActivity.passedWay;
        startAngle = mainActivity.angle;
        robot = mainActivity.robot;
    }

    public void setTask() throws ControllerException {
   //     Navigation navigation = new Navigation();
    //    path = navigation.getPath();

        path = new ArrayList<>();
        arrayInList();//TODO
        TaskThread taskThread = new TaskThread();
        taskThread.start();
  /*      try {
            taskThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
   /*     Log.i(MainActivity.TAG, "Global passedWay " + Float.toString(mainActivity.passedWay));
        lowLevelNavigationMethods.runOnKey(LowLevelNavigationMethods.STOP_MOVE);
        Log.i(MainActivity.TAG, "Global passedWay " + Float.toString(mainActivity.passedWay));
        Log.i(MainActivity.TAG, "----------------------");*/
    }

    class TaskThread extends Thread {
        @Override
        public void run() {
            int straightLineCoeff = 1;
        //    distanceForward(straightLineCoeff);
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
                                distanceForward(straightLineCoeff);
                                straightLineCoeff = 1;
                            }
                            else
                                if (path.get(i + 1) != 1) {
                                    distanceForward(straightLineCoeff);
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

    private void distanceForward(int straightLineCoeff) {
       // Log.i(MainActivity.TAG, "forwardThread started "+straightLineCoeff);
    //    StartingForwardThread startingForwardThread = new StartingForwardThread();
    //    startingForwardThread.start(); //acceleration on first forwardDistance
    //    try {
    //        startingForwardThread.join();
    //    } catch (InterruptedException e) {}

        float startPath = mainActivity.passedWay;
        Log.i(MainActivity.TAG, "----------------------");
        Log.i(MainActivity.TAG, "Global passedWay " + Float.toString(startPath));
        ForwardThread forwardThread = new ForwardThread(straightLineCoeff - 1);
        forwardThread.start();
        try {
                forwardThread.join();
     //           Log.i(MainActivity.TAG, "forwardThread finished");
        } catch (InterruptedException e) {}
        Log.i(MainActivity.TAG, "Global passedWay " + Float.toString(mainActivity.passedWay));
        Log.i(MainActivity.TAG, "Global passedWay " + Float.toString(mainActivity.passedWay - startPath));
        Log.i(MainActivity.TAG, "----------------------");
        Log.i(MainActivity.TAG, "Global passedWay " + Float.toString(mainActivity.passedWay));
        lowLevelNavigationMethods.runOnKey(LowLevelNavigationMethods.STOP_MOVE);
        Log.i(MainActivity.TAG, "Global passedWay " + Float.toString(mainActivity.passedWay));
        Log.i(MainActivity.TAG, "----------------------");
    }

    class StartingForwardThread extends Thread {
        private float startPath;
        private float[] accelerationSpeeds = {};

        StartingForwardThread() {
            startPath = mainActivity.passedWay;
        }

        @Override
        public void run() {
            Log.i(MainActivity.TAG, "StartingForwardThread started ");
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
                                Log.i(MainActivity.TAG, "StartingForwardThread finished " + Float.toString(mainActivity.passedWay - startPath));
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
            Log.i(MainActivity.TAG, "ForwardThread started");
            if( robot.isControllerAvailable( BodyController.class ) )
            {
                BodyController bodyController = null;
                try {
                    bodyController = (BodyController) robot.getController( BodyController.class );
                    if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
                    {
                        TwoWheelsBodyController wheelsController = null;
                        wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                        while(true) {
                        if(mainActivity.passedWay - startPath < purposePath)
                            try {
                                wheelsController.setWheelsSpeeds(20f,20f);
                   //             Log.i(MainActivity.TAG, "ForwardThread move--->>");
                                sleep(100);
                            } catch (InterruptedException e) {}
                        else {
                     //       lowLevelNavigationMethods.stopWheelsAction(LowLevelNavigationMethods.STOP_MOVE);
                  //          Log.i(MainActivity.TAG, "ForwardThread prepare to stop!@!@!@!");
                            wheelsController.setWheelsSpeeds(0.0f,0.0f);
                            Log.i(MainActivity.TAG, "ForwardThread finished "+Float.toString(mainActivity.passedWay - startPath));
                            return;
                        }
                    }
                    }
                } catch (ControllerException e) {}
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
                wheelsController.turnAround(20f, -1.57f);
            }
        }
    }

    private void leftInThread() {
        LeftThread leftThread = new LeftThread();
        leftThread.start();
        try {
            leftThread.join();
        } catch (InterruptedException e) {}
    }

    class LeftThread extends Thread {
        private float startAngle;

        LeftThread() {
            startAngle = mainActivity.angle;
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
                        while(true) {
                            if(Math.abs(mainActivity.angle - startAngle) < 1.57) //TODO
                                try {
                                    wheelsController.setWheelsSpeeds(-20f,20f);
                                    sleep(600);
                                } catch (InterruptedException e) {}
                            else {
                                //        lowLevelNavigationMethods.stopWheelsAction(lowLevelNavigationKey);
                                Log.i(MainActivity.TAG, Float.toString(mainActivity.angle - startAngle));
                                return;
                            }
                        }
                    }
                } catch (ControllerException e) {}
            }
        }
    }

    //TODO
    private void arrayInList() {
        for(int i=0;i<arrayPath.length;i++)
            path.add(arrayPath[i]);
    }
}
