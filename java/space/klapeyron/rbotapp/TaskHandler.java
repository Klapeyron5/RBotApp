package space.klapeyron.rbotapp;

import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import ru.rbot.android.bridge.service.robotcontroll.controllers.BodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.TwoWheelsBodyController;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;


public class TaskHandler {
    private MainActivity mainActivity;
    private Robot robot;
    private Navigation navigation;
    public int currentDirection = 1; //0: positive direction on X; 1: positive dir on Y; 2: negative on X; 3: negative on Y;

    private final static float forwardDistance = 0.5f;

    private int[] arrayPath = {1,1,2,1,1,0,1};
    private ArrayList<Integer> path;//0-right; 1-forward; 2-left;

    TaskHandler(MainActivity m) {
        mainActivity = m;
        robot = mainActivity.robot;
        navigation = new Navigation(this);
    }

    public void setTask(int sX, int sY, int fX, int fY, int dir) throws ControllerException {
        currentDirection = dir;
        navigation.setStart(sY,sX);
        navigation.setFinish(fY,fX);

        Log.i(MainActivity.TAG, "Start coordinates: " + navigation.getStart()[0] + " " + navigation.getStart()[1]);
        Log.i(MainActivity.TAG, "Finish coordinates: " + navigation.finish[0] + " " + navigation.finish[1]);

        path = navigation.getPath();

        Log.i(MainActivity.TAG,"PATH");

        for(int i=0;i<path.size();i++)
            Log.i(MainActivity.TAG,path.get(i)+"");

   //     path = new ArrayList<>();
   //     arrayInList();//TODO
        TaskThread taskThread = new TaskThread();
        taskThread.start();
    }

    class TaskThread extends Thread {
        @Override
        public void run() {
            float startPath = mainActivity.passedWay;
            Log.i(MainActivity.TAG, "setTask start passedWay "+startPath);

            int straightLineCoeff = 0;
            for(int i=0;i<path.size();i++) {
                switch(path.get(i)) {
                    case 0:
                        if(currentDirection!=3)
                            currentDirection++;
                        else
                            currentDirection = 0;
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
                        if(currentDirection!=0)
                            currentDirection--;
                        else
                            currentDirection = 3;
                        turnLeft();
                        break;
                }
            }

            navigation.setStart(navigation.finish[0], navigation.finish[1]);
         //   mainActivity.editTextStartX.setText(navigation.finish[1]);
         //   mainActivity.editTextStartY.setText(navigation.finish[0]);
         //   mainActivity.editTextDirection.setText(Integer.toString(currentDirection));

            synchronized (this) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.editTextStartX.setText(Integer.toString(navigation.finish[1]));
                        mainActivity.editTextStartY.setText(Integer.toString(navigation.finish[0]));
                        mainActivity.editTextDirection.setText(Integer.toString(currentDirection));
                    }
                });
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
        private float standardSpeed = 7.0f;
        private float correctionSpeed = 0.2f;
        private float correctionSpeedCorrection = 0.1f;
        private float correctionDistance = 0.01f;
        private float constAngle = 0;//(float)-Math.PI; //TODO
        private TwoWheelsBodyController wheelsController = null;
        private float dtAngle = mainActivity.angle; //angle with the last iteration (for derivative counting)
        private float corrSpeedLeft = 0;
        private float corrSpeedRight = 0;

        ForwardThread(int straightLineCoeff) {
            startPath = mainActivity.passedWay;
            purposePath = straightLineCoeff * TaskHandler.forwardDistance;
        }

        @Override
        public void run() {
        //    constAngle = mainActivity.angle;
            switch(currentDirection) {
                case 0:
                    constAngle = 0;
                    break;
                case 1:
                    constAngle = (float)-Math.PI/2;
                    break;
                case 2:
                    constAngle = (float)Math.PI;
                    break;
                case 3:
                    constAngle = (float)Math.PI/2;
                    break;
            }
            Log.i(MainActivity.TAG, "ForwardThread started "+purposePath+" m;  direction "+currentDirection);
            if( robot.isControllerAvailable( BodyController.class ) )
            {
                BodyController bodyController = null;
                try {
                    bodyController = (BodyController) robot.getController( BodyController.class );
                    if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
                    {
                        wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                        CheckThread checkThread;
                        dtAngle = mainActivity.angle;
                        corrSpeedLeft = 0;
                        corrSpeedRight = 0;
                        while(true) {
                            correctionCode(currentDirection); //TODO
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

        private void correctionCode(int currentDirection) {
            switch(currentDirection) {
                case 0:
                    Log.i(mainActivity.TAG, "\"_________________case0_________________\"");
                    Log.i(mainActivity.TAG,"angle: "+mainActivity.angle);
                    if (Math.abs(mainActivity.angle - constAngle) < correctionDistance) {
                        wheelsController.setWheelsSpeeds(standardSpeed, standardSpeed);
                        Log.i(mainActivity.TAG,"OK");
                    } else
                        if (!(mainActivity.angle > correctionDistance)) {
                            Log.i(mainActivity.TAG, "LEFT "+ mainActivity.angle);
                            if (-mainActivity.angle + dtAngle > 0) {
                                corrSpeedLeft += correctionSpeedCorrection;
                                Log.i(mainActivity.TAG, "derivative > 0");
                            } else {
                                if (corrSpeedLeft != 0)
                                    corrSpeedLeft -= correctionSpeedCorrection;
                                Log.i(mainActivity.TAG, "derivative < 0");
                            }
                            wheelsController.setWheelsSpeeds(standardSpeed, standardSpeed + correctionSpeed + corrSpeedLeft);
                            dtAngle = mainActivity.angle;
                        } else {
                            Log.i(mainActivity.TAG, "RIGHT "+mainActivity.angle);
                            if (-dtAngle + mainActivity.angle > 0) {
                                corrSpeedRight += correctionSpeedCorrection;
                                Log.i(mainActivity.TAG, "derivative > 0");
                            } else {
                                if (corrSpeedLeft != 0)
                                    corrSpeedRight -= correctionSpeedCorrection;
                                Log.i(mainActivity.TAG, "derivative < 0");
                            }
                            wheelsController.setWheelsSpeeds(standardSpeed + correctionSpeed + corrSpeedRight, standardSpeed);
                            dtAngle = mainActivity.angle;
                        }
                    break;
                case 1:
                    Log.i(mainActivity.TAG, "\"__________________case1__________________\"");
                    Log.i(mainActivity.TAG,"angle: "+mainActivity.angle);
                    if (Math.abs(mainActivity.angle - constAngle) < correctionDistance) {
                        wheelsController.setWheelsSpeeds(standardSpeed, standardSpeed);
                        Log.i(mainActivity.TAG,"OK");
                    }
                    else
                    if (!(mainActivity.angle - constAngle > correctionDistance)) {
                        Log.i(mainActivity.TAG, "LEFT "+ mainActivity.angle);

                        //correction depending on derivation started
                        if (-mainActivity.angle + dtAngle > 0) {
                            corrSpeedLeft += correctionSpeedCorrection;
                            Log.i(mainActivity.TAG, "derivative > 0");
                        }
                        else {
                            if (corrSpeedLeft != 0)
                                corrSpeedLeft -= correctionSpeedCorrection;
                            Log.i(mainActivity.TAG, "derivative < 0");
                        }
                        //correction depending on derivation finished

                        wheelsController.setWheelsSpeeds(standardSpeed, standardSpeed + correctionSpeed + corrSpeedLeft);
                        dtAngle = mainActivity.angle;
                    } else {
                        Log.i(mainActivity.TAG, "RIGHT "+mainActivity.angle);

                        //correction depending on derivation started
                        if (-dtAngle + mainActivity.angle > 0) {
                            corrSpeedRight += correctionSpeedCorrection;
                            Log.i(mainActivity.TAG, "derivative > 0");
                        } else {
                            if (corrSpeedLeft != 0)
                                corrSpeedRight -= correctionSpeedCorrection;
                            Log.i(mainActivity.TAG, "derivative < 0");
                        }
                        //correction depending on derivation finished

                        wheelsController.setWheelsSpeeds(standardSpeed + correctionSpeed + corrSpeedRight, standardSpeed);
                        dtAngle = mainActivity.angle;
                    }
                    break;
                case 2:
                    Log.i(mainActivity.TAG, "\"__________________case2__________________\"");
                    Log.i(mainActivity.TAG,"angle: "+mainActivity.angle);
                    float mAangel = anglePlus2PI(mainActivity.angle); //mainActivity.angle
                    if (Math.abs(mAangel - constAngle) < correctionDistance) {
                        wheelsController.setWheelsSpeeds(standardSpeed, standardSpeed);
                        Log.i(mainActivity.TAG,"OK");
                    }
                    else
                    if (constAngle - mAangel > correctionDistance) {
                        Log.i(mainActivity.TAG, "LEFT "+ mainActivity.angle);

                        //correction depending on derivation started
                        if (mAangel - dtAngle > 0) {
                            corrSpeedLeft += correctionSpeedCorrection;
                            Log.i(mainActivity.TAG, "derivative > 0");
                        }
                        else {
                            if (corrSpeedLeft != 0)
                                corrSpeedLeft -= correctionSpeedCorrection;
                            Log.i(mainActivity.TAG, "derivative < 0");
                        }
                        //correction depending on derivation finished

                        wheelsController.setWheelsSpeeds(standardSpeed, standardSpeed + correctionSpeed + corrSpeedLeft);
                        dtAngle = anglePlus2PI(mainActivity.angle);
                    } else {
                        Log.i(mainActivity.TAG, "RIGHT "+mainActivity.angle);

                        //correction depending on derivation started
                        if (dtAngle - mAangel > 0) {
                            corrSpeedRight += correctionSpeedCorrection;
                            Log.i(mainActivity.TAG, "derivative > 0");
                        } else {
                            if (corrSpeedLeft != 0)
                                corrSpeedRight -= correctionSpeedCorrection;
                            Log.i(mainActivity.TAG, "derivative < 0");
                        }
                        //correction depending on derivation finished

                        wheelsController.setWheelsSpeeds(standardSpeed + correctionSpeed + corrSpeedRight, standardSpeed);
                        dtAngle = anglePlus2PI(mainActivity.angle);
                    }
                    break;
                case 3:
                    Log.i(mainActivity.TAG, "\"__________________case3__________________\"");
                    Log.i(mainActivity.TAG,"angle: "+mainActivity.angle);
                    if (Math.abs(mainActivity.angle - constAngle) < correctionDistance) {
                        wheelsController.setWheelsSpeeds(standardSpeed, standardSpeed);
                        Log.i(mainActivity.TAG,"OK");
                    }
                    else
                    if (!(mainActivity.angle - constAngle > correctionDistance)) {
                        Log.i(mainActivity.TAG, "LEFT "+ mainActivity.angle);

                        //correction depending on derivation started
                        if (dtAngle - mainActivity.angle > 0) {
                            corrSpeedLeft += correctionSpeedCorrection;
                            Log.i(mainActivity.TAG, "derivative > 0");
                        } else {
                            if (corrSpeedLeft != 0)
                                corrSpeedLeft -= correctionSpeedCorrection;
                            Log.i(mainActivity.TAG, "derivative < 0");
                        }
                        //correction depending on derivation finished

                        wheelsController.setWheelsSpeeds(standardSpeed, standardSpeed + correctionSpeed + corrSpeedLeft);
                        dtAngle = mainActivity.angle;
                    } else {
                        Log.i(mainActivity.TAG, "RIGHT "+mainActivity.angle);

                        //correction depending on derivation started
                        if (mainActivity.angle - dtAngle > 0) {
                            corrSpeedRight += correctionSpeedCorrection;
                            Log.i(mainActivity.TAG, "derivative > 0");
                        } else {
                            if (corrSpeedLeft != 0)
                                corrSpeedRight -= correctionSpeedCorrection;
                            Log.i(mainActivity.TAG, "derivative < 0");
                        }
                        //correction depending on derivation finished

                        wheelsController.setWheelsSpeeds(standardSpeed + correctionSpeed + corrSpeedRight, standardSpeed);
                        dtAngle = mainActivity.angle;
                    }
                    break;
            }
        }

        private float anglePlus2PI(float angle) {
            if (angle < 0)
                angle += (float)2*Math.PI;
            return angle;
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
