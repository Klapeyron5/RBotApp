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


    ArrayList<Integer> path;// = {1,1,2,1,0,1,2}; //0-right; 1-forward;2-left;

    LowLevelNavigationTasks(MainActivity m, LowLevelNavigationMethods l) {
        mainActivity = m;
        lowLevelNavigationMethods = l;
        startPath = mainActivity.passedWay;
        startAngle = mainActivity.angle;
        robot = mainActivity.robot;
    }

    public void setTask() {
        Navigation navigation = new Navigation();
        path = navigation.getPath();
        TaskThread taskThread = new TaskThread();
        taskThread.start();
    }

    class TaskThread extends Thread {
        @Override
        public void run() {
            for(int i=0;i<path.size();i++) {
                try {
                    switch(path.get(i)) {
                        case 0:
                            right();
                            break;
                        case 1:
                            distanceForward();
                            break;
                        case 2:
                            left();
                            break;
                    }
                } catch (ControllerException e) {}
                try {
                    sleep(3000);
                } catch (InterruptedException e) {}
            }
            this.interrupt();
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
