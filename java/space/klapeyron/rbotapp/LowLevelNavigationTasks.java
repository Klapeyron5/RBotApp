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


    int[] arrayPath = {1,1,1};
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
     //   path = navigation.getPath()ж

        path = new ArrayList<>();
        arrayInList();//TODO
    /*    int straightLineCoeff = 1;
        for (int i = 1; i < path.size(); i++) {
            switch (path.get(i)) {
                case 0:
                    right();
                    break;
                case 1:
                    Log.i(MainActivity.TAG,Integer.toString(i));
                    if (path.get(i - 1) == 1)
                        straightLineCoeff++;
                    if (i == path.size() - 1)
                        distanceForwardThread(straightLineCoeff);
                    else
                        if (path.get(i + 1) != 1)
              //              Log.i(MainActivity.TAG,"straight "+Integer.toString(straightLineCoeff));
                            distanceForwardThread(straightLineCoeff);
                    break;
            }
        }*/
        TaskThread taskThread = new TaskThread();
        taskThread.start();
    }

    class TaskThread extends Thread {
        int straightLineCoeff = 1;
        @Override
        public void run() {
            for(int i=1;i<path.size();i++) {
                try {
                    switch(path.get(i)) {
                        case 0:
                            right();
                            break;
                        case 1:
                            if (path.get(i - 1) == 1)
                            straightLineCoeff++;
                            if (i == path.size() - 1) {
                                distanceForwardThread(straightLineCoeff);
                                try {
                                    if(forwardThread.isAlive()) {
                                        forwardThread.join();
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            else
                                if (path.get(i + 1) != 1) {
                                    distanceForwardThread(straightLineCoeff);
                                    try {
                                        if(forwardThread.isAlive()) {
                                            forwardThread.join();
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            break;
                        case 2:
                            left();
                            break;
                    }
                } catch (ControllerException e) {}

             /*   try {
                    sleep(3000);
                } catch (InterruptedException e) {}*/

                try {
                    left();
                } catch (ControllerException e) {
                    e.printStackTrace();
                }
            }
            this.interrupt();
        }
    }

    private void distanceForwardThread(int straightLineCoeff) {
        forwardThread = new ForwardThread(LowLevelNavigationMethods.FORWARD_MOVE,straightLineCoeff);
        forwardThread.start();
    }
    class ForwardThread extends Thread {
        private String lowLevelNavigationKey;
        private float startPath;
        private float purposePath;

        ForwardThread(String k, int straightLineCoeff) {
            lowLevelNavigationKey = k;
            startPath = mainActivity.passedWay;
            purposePath = straightLineCoeff * LowLevelNavigationTasks.forwardDistance;
        }

        @Override
        public void run() {
            Log.i(MainActivity.TAG,"RUN");
            while(true) {
                if(mainActivity.passedWay - startPath < purposePath)
                    try {
                        lowLevelNavigationMethods.runOnKey(lowLevelNavigationKey);
                        sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                else {
                    lowLevelNavigationMethods.stopWheelsAction(lowLevelNavigationKey);
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
                wheelsController.turnAround(20f, 1.57f);
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
