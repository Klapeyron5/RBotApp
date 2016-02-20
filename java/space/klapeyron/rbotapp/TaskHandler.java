package space.klapeyron.rbotapp;

import android.util.Log;

import java.util.ArrayList;

import ru.rbot.android.bridge.service.robotcontroll.controllers.BodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.TwoWheelsBodyController;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;


public class TaskHandler {
    private MainActivity mainActivity;
    private Robot robot;
    public int currentDirection = 1; //0: positive direction on X; 1: positive dir on Y; 2: negative on X; 3: negative on Y;

    private final static float forwardDistance = 0.5f;

    private int[] arrayPath = {1,1,2,1,1,0,1};
    private ArrayList<Integer> path;//0-right; 1-forward; 2-left;

    TaskHandler(MainActivity m) {
        mainActivity = m;
        robot = mainActivity.robot;
    }

    public void setTask() throws ControllerException {
        Navigation navigation = new Navigation(this);
        path = navigation.getPath();

   //     path = new ArrayList<>();
   //     arrayInList();//TODO
        TaskThread taskThread = new TaskThread();
        taskThread.start();
    }

    //TODO
    /*public void setTaskFromBT(int Y,int X) throws ControllerException {
        Navigation navigation = new Navigation();
        navigation.setFinish(Y,X);
        path = navigation.getPath();
        TaskThread taskThread = new TaskThread();
        taskThread.start();
    }*/

    class TaskThread extends Thread {
        @Override
        public void run() {
            float startPath = mainActivity.passedWay;
            Log.i(MainActivity.TAG, "setTask start passedWay "+startPath);

            int straightLineCoeff = 0;
            for(int i=0;i<path.size();i++) {
                switch(path.get(i)) {
                    case 0:
                        turnRight();
                        break;
                    case 1:
                        straightLineCoeff++;
                        if (i == path.size() - 1) {
                            distanceForward(straightLineCoeff);
                            straightLineCoeff = 0;
                        } else
                            if (path.get(i + 1) != 1) {
                                distanceForward(straightLineCoeff);
                                straightLineCoeff = 0;
                            }
                        break;
                    case 2:
                        turnLeft();
                        break;
                }
            }
            Log.i(MainActivity.TAG, "setTask finish passedWay " + mainActivity.passedWay);
            Log.i(MainActivity.TAG, "setTask finish difference " + (mainActivity.passedWay-startPath));
        }
    }

    private void distanceForward(int straightLineCoeff) {
    /*    if (straightLineCoeff == 1) {
            ForwardThreadForSingleDistance forwardThreadForSingleDistance = new ForwardThreadForSingleDistance();
            forwardThreadForSingleDistance.start(); //acceleration on first forwardDistance
            try {
                forwardThreadForSingleDistance.join();
            } catch (InterruptedException e) {}

        } else {

            StartingForwardThread startingForwardThread = new StartingForwardThread();
            startingForwardThread.start(); //acceleration on first forwardDistance
            try {
                startingForwardThread.join();
            } catch (InterruptedException e) {}
            straightLineCoeff--;*/
            if (straightLineCoeff > 0) {
                ForwardThread forwardThread = new ForwardThread(straightLineCoeff);
                forwardThread.start();
                try {
                    forwardThread.join();
                } catch (InterruptedException e) {}
            }
    //    }
    }

    private void turnLeft() {
        LeftThread leftThread = new LeftThread();
        leftThread.start();
        try {
            leftThread.join();
        } catch (InterruptedException e) {}
    }

    private void turnRight() {
        RightThread rightThread = new RightThread();
        rightThread.start();
        try {
            rightThread.join();
        } catch (InterruptedException e) {}
    }

    class StartingForwardThread extends Thread {
        private float startPath;

        StartingForwardThread() {
            startPath = mainActivity.passedWay;
        }

        @Override
        public void run() {
            Log.i(MainActivity.TAG, "StartingForwardThread started");
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
                        CheckThread checkThread;
                        while(true) {
                            if(i<10) //acceleration
                                i++;
                            wheelsController.setWheelsSpeeds(i, i);
                            checkThread = new CheckThread(startPath,wheelsController);
                            checkThread.setRunning(true);
                            checkThread.start();
                            sleep(500);
                            checkThread.setRunning(false);
                            if (checkThread.stopFlag)
                                return;
                        }
                    }
                } catch (ControllerException e) {} catch (InterruptedException e) {}
            }
        }

        class CheckThread extends Thread {
            private float startPath;
            private TwoWheelsBodyController wheelsController;
            private boolean running = false;
            public boolean stopFlag = false;
            CheckThread(float s,TwoWheelsBodyController w) {
                startPath = s;
                wheelsController = w;
            }
            @Override
            public void run() {
                while(running) {
                    if(!(mainActivity.passedWay - startPath < TaskHandler.forwardDistance)) {
                        stopFlag = true;
                        return;
                    }
                }
            }
            public void setRunning(boolean r) {
                running = r;
            }
        }
    }

    class ForwardThreadForSingleDistance extends Thread {
        private float startPath;

        ForwardThreadForSingleDistance() {
            startPath = mainActivity.passedWay;
        }

        @Override
        public void run() {
            Log.i(MainActivity.TAG,"ForwardThreadForSingleDistance started");
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
                        CheckThread checkThread;
                        while(true) {
                            if(i<10) //acceleration
                                i++;
                            wheelsController.setWheelsSpeeds(i, i);
                            checkThread = new CheckThread(startPath,wheelsController);
                            checkThread.setRunning(true);
                            checkThread.start();
                            sleep(500);
                            checkThread.setRunning(false);
                            if (checkThread.stopFlag)
                                return;
                        }
                    }
                } catch (ControllerException e) {} catch (InterruptedException e) {}
            }
        }

        class CheckThread extends Thread {
            private float startPath;
            private TwoWheelsBodyController wheelsController;
            private boolean running = false;
            public boolean stopFlag = false;
            CheckThread(float s,TwoWheelsBodyController w) {
                startPath = s;
                wheelsController = w;
            }
            @Override
            public void run() {
                while(running) {
                    if(!(mainActivity.passedWay - startPath < TaskHandler.forwardDistance)) {
                        wheelsController.setWheelsSpeeds(0.0f, 0.0f);
                        stopFlag = true;
                        return;
                    }
                }
            }
            public void setRunning(boolean r) {
                running = r;
            }
        }
    }

    class ForwardThread extends Thread {
        private float startPath;
        private float purposePath;
        private float standartSpeed = 7.0f;
        private float correctionSpeed = 0.2f;
        private float correctionSpeedCorrection = 0.1f;
        private float correctionDistance = 0.01f;
        private float constX = 0;//(float)-Math.PI; //TODO

        ForwardThread(int straightLineCoeff) {
            startPath = mainActivity.passedWay;
            purposePath = straightLineCoeff * TaskHandler.forwardDistance;
        }

        @Override
        public void run() {
            constX = mainActivity.angle;
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
                        CheckThread checkThread;
                        float dtAngle = mainActivity.angle;
                        float corrSpeedLeft = 0;
                        float corrSpeedRight = 0;
                        while(true) {
                            //TODO //correction direction
                            Log.i(mainActivity.TAG," ");
                            Log.i(mainActivity.TAG,""+mainActivity.angle);
                            if (Math.abs(mainActivity.angle - constX) < correctionDistance) {
                                wheelsController.setWheelsSpeeds(standartSpeed, standartSpeed);
                                Log.i(mainActivity.TAG,"OK");
                            }
                            else
                                if (!(mainActivity.angle > correctionDistance)) {
                                    Log.i(mainActivity.TAG, "LEFT "+ mainActivity.angle);
                                    if (-mainActivity.angle + dtAngle > 0)
                                        corrSpeedLeft += correctionSpeedCorrection;
                                    else {
                                        if (corrSpeedLeft != 0)
                                            corrSpeedLeft -= correctionSpeedCorrection;
                                    }
                                    wheelsController.setWheelsSpeeds(standartSpeed, standartSpeed + correctionSpeed + corrSpeedLeft);
                                    dtAngle = mainActivity.angle;
                                } else {
                                    Log.i(mainActivity.TAG, "RIGHT "+mainActivity.angle);
                                    if (-dtAngle + mainActivity.angle > 0)
                                        corrSpeedRight += correctionSpeedCorrection;
                                    else
                                        if (corrSpeedLeft != 0)
                                            corrSpeedRight -= correctionSpeedCorrection;
                                    wheelsController.setWheelsSpeeds(standartSpeed + correctionSpeed + corrSpeedRight, standartSpeed);
                                    dtAngle = mainActivity.angle;
                                }

                            checkThread = new CheckThread(startPath,wheelsController);
                            checkThread.setRunning(true);
                            checkThread.start();
                            sleep(500);
                            checkThread.setRunning(false);
                            if (checkThread.stopFlag)
                                return;
                        }
                    }
                } catch (ControllerException e) {} catch (InterruptedException e) {}
            }
        }

        class CheckThread extends Thread {
            private float startPath;
            private TwoWheelsBodyController wheelsController;
            private boolean running = false;
            public boolean stopFlag = false;
            CheckThread(float s,TwoWheelsBodyController w) {
                startPath = s;
                wheelsController = w;
            }
            @Override
            public void run() {
                while(running) {
                    if(!(mainActivity.passedWay - startPath < purposePath)) {
                        wheelsController.setWheelsSpeeds(0.0f, 0.0f);
                        stopFlag = true;
                        return;
                    }
                }
            }
            public void setRunning(boolean r) {
                running = r;
            }
        }
    }

    class LeftThread extends Thread {
        private float startAngle;
        private float purposeAngle;

        LeftThread() {
            startAngle = mainActivity.angle;
            purposeAngle = (float) Math.PI / 2;
        }

        @Override
        public void run() {
            Log.i(MainActivity.TAG, "LeftThread started ----->>>>>>" + startAngle);

            int flagVariant;
            if ((startAngle >= 0) && (startAngle < Math.PI / 2)) {
                flagVariant = 1;
                Log.i(MainActivity.TAG, "1");
            } else if (startAngle >= Math.PI / 2) {
                flagVariant = 2;
                Log.i(MainActivity.TAG, "2");
            } else if (startAngle < -Math.PI / 2) {
                flagVariant = 3;
                Log.i(MainActivity.TAG, "3");
            } else {
                flagVariant = 4;
                Log.i(MainActivity.TAG, "4");
            }

            if (robot.isControllerAvailable(BodyController.class)) {
                BodyController bodyController = null;
                try {
                    bodyController = (BodyController) robot.getController(BodyController.class);
                    if (bodyController.isControllerAvailable(TwoWheelsBodyController.class)) {
                        TwoWheelsBodyController wheelsController = null;
                        wheelsController = (TwoWheelsBodyController) bodyController.getController(TwoWheelsBodyController.class);
                        wheelsController.turnAround(10f, (float) Math.PI / 2);
                        while (true) {
                            if (new FlagVariant(flagVariant).getFlag()) {
                            } else {
                                wheelsController.setWheelsSpeeds(0.0f, 0.0f);
                                try {
                                    sleep(200);
                                } catch (InterruptedException e) {}
                                Log.i(MainActivity.TAG, "LeftThread finished ------------>>>>> " + new FlagVariant(flagVariant).getDimension());
                                return;
                            }
                        }
                    }
                } catch (ControllerException e) {
                }
            }
        }

        class FlagVariant {
            private int variant = 0;

            FlagVariant(int v) {
                variant = v;
            }

            public boolean getFlag() {
                float currentAngle = mainActivity.angle;
                switch (this.variant) {
                    case 1:
                        return ((currentAngle - startAngle) < purposeAngle);
                    case 2:
                        if (currentAngle < 0)
                            currentAngle += 2 * Math.PI;
                        return ((currentAngle - startAngle) < purposeAngle);
                    case 3:
                        return ((currentAngle - startAngle) < purposeAngle);
                    case 4:
                        return ((currentAngle - startAngle) < purposeAngle);
                    default:
                        return true;
                }
            }

            public float getDimension() {
                float currentAngle = mainActivity.angle;
                switch (this.variant) {
                    case 1:
                        return (currentAngle - startAngle);
                    case 2:
                        if (currentAngle < 0)
                            currentAngle += 2 * Math.PI;
                        return (currentAngle - startAngle);
                    case 3:
                        return (currentAngle - startAngle);
                    case 4:
                        return (currentAngle - startAngle);
                    default:
                        return 0;
                }
            }
        }
    }

    class RightThread extends Thread {
        private float startAngle;
        private float purposeAngle;

        RightThread() {
            startAngle = mainActivity.angle;
            purposeAngle = (float) -Math.PI / 2;
        }

        @Override
        public void run() {
            Log.i(MainActivity.TAG, "RightThread started ----->>>>>>" + startAngle);

            int flagVariant;
            if ((startAngle <= 0) && (startAngle > -Math.PI / 2)) {
                flagVariant = 1;
                Log.i(MainActivity.TAG, "1");
            } else if (startAngle <= -Math.PI / 2) {
                flagVariant = 2;
                Log.i(MainActivity.TAG, "2");
            } else if (startAngle > Math.PI / 2) {
                flagVariant = 3;
                Log.i(MainActivity.TAG, "3");
            } else {
                flagVariant = 4;
                Log.i(MainActivity.TAG, "4");
            }

            if (robot.isControllerAvailable(BodyController.class)) {
                BodyController bodyController = null;
                try {
                    bodyController = (BodyController) robot.getController(BodyController.class);
                    if (bodyController.isControllerAvailable(TwoWheelsBodyController.class)) {
                        TwoWheelsBodyController wheelsController = null;
                        wheelsController = (TwoWheelsBodyController) bodyController.getController(TwoWheelsBodyController.class);
                        wheelsController.turnAround(10f,(float)-Math.PI/2);
                        while (true) {
                            if (new FlagVariant(flagVariant).getFlag()) {
                            } else {
                                wheelsController.setWheelsSpeeds(0.0f, 0.0f);
                                try {
                                    sleep(200);
                                } catch (InterruptedException e) {}
                                Log.i(MainActivity.TAG, "RightThread finished ------------>>>>> " + new FlagVariant(flagVariant).getDimension());
                                return;
                            }
                        }
                    }
                } catch (ControllerException e) {
                }
            }
        }

        class FlagVariant {
            private int variant = 0;

            FlagVariant(int v) {
                variant = v;
            }

            public boolean getFlag() {
                float currentAngle = mainActivity.angle;
                switch (this.variant) {
                    case 1:
                        return ((currentAngle - startAngle) > purposeAngle);
                    case 2:
                        if (currentAngle > 0)
                            currentAngle -= 2 * Math.PI;
                        return ((currentAngle - startAngle) > purposeAngle);
                    case 3:
                        return ((currentAngle - startAngle) > purposeAngle);
                    case 4:
                        return ((currentAngle - startAngle) > purposeAngle);
                    default:
                        return true;
                }
            }

            public float getDimension() {
                float currentAngle = mainActivity.angle;
                switch (this.variant) {
                    case 1:
                        return Math.abs(currentAngle - startAngle);
                    case 2:
                        if (currentAngle < 0)
                            currentAngle -= 2 * Math.PI;
                        return Math.abs(currentAngle - startAngle);
                    case 3:
                        return Math.abs(currentAngle - startAngle);
                    case 4:
                        return Math.abs(currentAngle - startAngle);
                    default:
                        return 0;
                }
            }
        }
    }

    //TODO
    private void arrayInList() {
        for(int i=0;i<arrayPath.length;i++)
            path.add(arrayPath[i]);
    }
}