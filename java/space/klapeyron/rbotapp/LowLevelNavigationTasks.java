package space.klapeyron.rbotapp;

import android.util.Log;

import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;

public class LowLevelNavigationTasks {
    MainActivity mainActivity;
    LowLevelNavigationMethods lowLevelNavigationMethods;
    float startPath;

    LowLevelNavigationTasks(MainActivity m, LowLevelNavigationMethods l) {
        mainActivity = m;
        lowLevelNavigationMethods = l;
        startPath = mainActivity.passedWay;
    }

    public void doTask() {
        String key = LowLevelNavigationMethods.FORWARD_MOVE;
        startPath = mainActivity.passedWay;
        TaskThread taskThread;
        taskThread = new TaskThread(key,this);
        taskThread.setRunning(true);
        taskThread.start();
    }

    class TaskThread extends Thread {
        private boolean running = false;
        private String lowLevelNavigationKey;
        private LowLevelNavigationTasks lowLevelNavigationTasks;

        TaskThread(String k,LowLevelNavigationTasks l) {
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
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            //        if (startPath == 0)
            //            startPath = mainActivity.passedWay;
                    Log.i(MainActivity.TAG,"startPath: "+Float.toString(lowLevelNavigationTasks.startPath)+"  passedPath: "+Float.toString(lowLevelNavigationTasks.mainActivity.passedWay)+"  TASK: "+Float.toString(lowLevelNavigationTasks.startPath - lowLevelNavigationTasks.mainActivity.passedWay));
                    if(lowLevelNavigationTasks.mainActivity.passedWay - lowLevelNavigationTasks.startPath > 0.5f) {
                        running = false;
                        lowLevelNavigationTasks.lowLevelNavigationMethods.stopWheelsAction(LowLevelNavigationMethods.FORWARD_MOVE);
                    }
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
