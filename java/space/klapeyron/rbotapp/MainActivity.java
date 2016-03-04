package space.klapeyron.rbotapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ru.rbot.android.bridge.service.robotcontroll.controllers.BodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.TwoWheelsBodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.data.TwoWheelState;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.listeners.TwoWheelBodyControllerStateListener;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.listeners.RobotStateListener;
import space.klapeyron.rbotapp.BluetoothClientConnection.ClientThread;
import space.klapeyron.rbotapp.BluetoothClientConnection.Communicator;
import space.klapeyron.rbotapp.BluetoothClientConnection.CommunicatorImpl;
import space.klapeyron.rbotapp.BluetoothClientConnection.CommunicatorService;
import space.klapeyron.rbotapp.BluetoothClientConnection.ServerThread;

public class MainActivity extends Activity {
    static final String TAG = "TAG";

    public String serverState;
    public static final String LoadedServerState = "loaded";

    public String robotConnectionState;
    public static final String OnConnectedRobotState = "connected";

    public String clientConnectionState;



    public static final int MY_BLUETOOTH_ENABLE_REQUEST_ID = 6;
    public final static String UUID = "e91521df-92b9-47bf-96d5-c52ee838f6f6";
    public static String hashString = "go";
    private ServerThread serverThread;
    private ClientThread clientThread;
    private BluetoothAdapter bluetoothAdapter;

    ru.rbot.android.bridge.service.robotcontroll.robots.Robot robot;

    MainActivity link = this;
    TaskHandler taskHandler;
    TTSManager ttsManager = null;


    //customizing server interface
    public TextView textViewCountedPath;
    public TextView textViewOdometryPath;
    public TextView textViewOdometryAngle;
    public TextView textViewOdometryX;
    public TextView textViewOdometryY;
    public TextView textViewOdometrySpeedL;
    public TextView textViewOdometrySpeedR;
    public TextView textViewServerState;
    public TextView textViewClientConnectionState;
    public EditText editTextFinishX;
    public EditText editTextFinishY;
    public EditText editTextStartX;
    public EditText editTextStartY;
    public EditText editTextDirection;

    //counted info
    float countedPath;
    float countedSpeedL = 0;
    float countedSpeedR;

    //odometry info
    float odometryPath;
    float odometryAngle;
    float odometryAbsoluteX;
    float odometryAbsoluteY;
    float odometryWheelSpeedLeft;
    float odometryWheelSpeedRight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initConstructor();
        initRobot();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, MY_BLUETOOTH_ENABLE_REQUEST_ID);
    }

    @Override
    public void onResume() {
        super.onResume();
        serverThread = new ServerThread(communicatorService);
        serverThread.start();
    }

    private void initRobot() {
        robot = new Robot(this);
        taskHandler = new TaskHandler(link);

        final RobotStateListener robotStateListener = new RobotStateListener() {
            @Override
            public void onRobotReady() {
                Log.i(MainActivity.TAG, "onRobotReady1");
                odometryMethod();
                Log.i(MainActivity.TAG, "onRobotReady2");
            }

            @Override
            public void onRobotInitError() {
                Log.i(MainActivity.TAG, "onRobotInitError");
            }

            @Override
            public void onRobotDisconnect() {
                Log.i(MainActivity.TAG, "onRobotDisconnect");
            }
        };

        robot.setRobotStateListener(robotStateListener);
        robot.start();
    }

    private void initConstructor() {
        textViewOdometryPath = (TextView) findViewById(R.id.textViewPath);
        textViewOdometryX = (TextView) findViewById(R.id.textViewX);
        textViewOdometryY = (TextView) findViewById(R.id.textViewY);
        textViewOdometrySpeedL = (TextView) findViewById(R.id.textViewSpeedL);
        textViewOdometrySpeedR = (TextView) findViewById(R.id.textViewSpeedR);
        textViewOdometryAngle = (TextView) findViewById(R.id.textViewAngle);
        textViewServerState = (TextView) findViewById(R.id.textViewServerState);
        textViewClientConnectionState = (TextView) findViewById(R.id.textViewClientConnectionState);

        editTextFinishX = (EditText) findViewById(R.id.editTextFinishX);
        editTextFinishY = (EditText) findViewById(R.id.editTextFinishY);
        editTextStartX = (EditText) findViewById(R.id.editTextStartX);
        editTextStartY = (EditText) findViewById(R.id.editTextStartY);
        editTextDirection = (EditText) findViewById(R.id.editTextStartDirection);

        Button buttonSetTask = (Button) findViewById(R.id.buttonSetTask);
        buttonSetTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int fY = Integer.parseInt(editTextFinishY.getText().toString());
                    int fX = Integer.parseInt(editTextFinishX.getText().toString());
                    int sY = Integer.parseInt(editTextStartY.getText().toString());
                    int sX = Integer.parseInt(editTextStartX.getText().toString());
                    int dir = Integer.parseInt(editTextDirection.getText().toString());
                    taskHandler.setTask(sX, sY, fX, fY, dir);
                } catch (ControllerException e) {
                }
            }
        });

        Button buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        Button buttonBTOpen = (Button) findViewById(R.id.buttonBtOpen);
        buttonBTOpen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                makeDiscoverable(v);
            }
        });
    }

    private void odometryMethod() {
        TwoWheelBodyControllerStateListener twoWheelBodyControllerStateListener = new TwoWheelBodyControllerStateListener() {
            @Override
            public void onWheelStateRecieved(TwoWheelState twoWheelState) {
                odometryPath = twoWheelState.getOdometryInfo().getPath();
                odometryAngle = twoWheelState.getOdometryInfo().getAngle();
                odometryAbsoluteX = (float) (-twoWheelState.getOdometryInfo().getX()+0.5*3+0.25);
                odometryAbsoluteY = (float) (-twoWheelState.getOdometryInfo().getY()+0.5+0.25);
                odometryWheelSpeedLeft = twoWheelState.getSpeed().getLWheelSpeed();
                odometryWheelSpeedRight = twoWheelState.getSpeed().getRWheelSpeed();

                textViewOdometryPath.setText(Float.toString(odometryPath));
                textViewOdometryAngle.setText(Float.toString(odometryAngle));
                textViewOdometryX.setText(Float.toString(odometryAbsoluteX));
                textViewOdometryY.setText(Float.toString(odometryAbsoluteY));
                textViewOdometrySpeedL.setText(Float.toString(odometryWheelSpeedLeft));
                textViewOdometrySpeedR.setText(Float.toString(odometryWheelSpeedRight));
            }
        };
        if( robot.isControllerAvailable( BodyController.class ) )
        {
            try {
                BodyController bodyController = (BodyController) robot.getController( BodyController.class );
                if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
                {
                    TwoWheelsBodyController wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                    wheelsController.setListener(twoWheelBodyControllerStateListener,100);
                }
            } catch (ControllerException e) {
                e.printStackTrace();
            }
        }
    }

    private final CommunicatorService communicatorService = new CommunicatorService() {
        @Override
        public Communicator createCommunicatorThread(BluetoothSocket socket) {
            return new CommunicatorImpl(socket, new CommunicatorImpl.CommunicationListener() {
                @Override
                public void onMessage(final String message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewClientConnectionState.setText(message); // просмотр строк сообщений
                            String[] recievedMessage = message.split("/", 3);
                            Log.i(MainActivity.TAG,"Message:  "+recievedMessage[0]+" "+recievedMessage[1]+" "+recievedMessage[2]);
                            int fX = Integer.parseInt(recievedMessage[1]);
                            int fY = Integer.parseInt(recievedMessage[2]);
                            int sX = Integer.parseInt(editTextStartX.getText().toString());
                            int sY = Integer.parseInt(editTextStartY.getText().toString());
                            int dir = Integer.parseInt(editTextDirection.getText().toString());
                            Log.i(MainActivity.TAG,"finish X: "+fX);
                            Log.i(MainActivity.TAG,"finish Y: "+fY);
                            editTextFinishX.setText(recievedMessage[1]);
                            editTextFinishY.setText(recievedMessage[2]);
                            try {
                                taskHandler.setTask(sX, sY, fX, fY, dir);
                            } catch (ControllerException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
    };

    public void makeDiscoverable(View view) {
        Intent i = new Intent(
                BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(i);
    }







  /*      Button btnTextSpeech = (Button) findViewById(R.id.buttonTextSpeech);
        btnTextSpeech.setOnClickListener(new View.OnClickListener(){
            @Override
        public void onClick(View v){
                ttsManager.Greeting();
            }
        });*/
    /*@Override
    public void onPause() {
        super.onPause();
        bluetoothAdapter.cancelDiscovery();

        if (discoverDevicesReceiver != null) {
            try {
                unregisterReceiver(discoverDevicesReceiver);
            } catch (Exception e) {
                Log.d("MainActivity", "Не удалось отключить ресивер " + discoverDevicesReceiver);
            }
        }

        if (clientThread != null) {
            clientThread.cancel();
        }
        if (serverThread != null) serverThread.cancel();
    }*/
}
