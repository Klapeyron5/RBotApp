package space.klapeyron.rbotapp;

import android.util.Log;

import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;

public class LowLevelNavigationTasks {
    MainActivity mainActivity;
    LowLevelNavigationMethods lowLevelNavigationMethods;
    float startPath;

    ForwardTaskThread forwardTaskThread;

    LowLevelNavigationTasks(MainActivity m, LowLevelNavigationMethods l) {
        mainActivity = m;
        lowLevelNavigationMethods = l;
        startPath = mainActivity.passedWay;
    }

    public void setTask() {
        forwardTask();
  /*      if(forwardTaskThread.isAlive()) {
            try {
                forwardTaskThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        Log.i(MainActivity.TAG,"Now on wait "+forwardTaskThread.isAlive());
   /*     while (forwardTaskThread.isAlive()) {
        }
        Log.i(MainActivity.TAG,"Now on left "+forwardTaskThread.isAlive());*/
    //    leftTask();
    }

    public void leftTask() {
        lowLevelNavigationMethods.runOnKey(LowLevelNavigationMethods.LEFT);
    }

    public void forwardTask() {
        String key = LowLevelNavigationMethods.FORWARD_MOVE;
        startPath = mainActivity.passedWay;
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
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(lowLevelNavigationTasks.mainActivity.passedWay - lowLevelNavigationTasks.startPath > 0.41f) {
                        lowLevelNavigationTasks.lowLevelNavigationMethods.stopWheelsAction(LowLevelNavigationMethods.FORWARD_MOVE);
                        Log.i(MainActivity.TAG, "startPath: " + Float.toString(lowLevelNavigationTasks.startPath) + "  passedPath: " + Float.toString(lowLevelNavigationTasks.mainActivity.passedWay) + "  TASK: " + Float.toString(lowLevelNavigationTasks.mainActivity.passedWay - lowLevelNavigationTasks.startPath));
                        running = false;
                        leftTask();
                        return;
                    }
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
