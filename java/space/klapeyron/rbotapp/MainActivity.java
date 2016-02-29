package space.klapeyron.rbotapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
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
import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;
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

    MainActivity link = this;
    Robot robot;
    TaskHandler taskHandler;
    TTSManager ttsManager = null;

    private ServerThread serverThread;
    private ClientThread clientThread;
    private BluetoothAdapter bluetoothAdapter;

    public static final int MY_BLUETOOTH_ENABLE_REQUEST_ID = 6;
    public final static String UUID = "e91521df-92b9-47bf-96d5-c52ee838f6f6";
    public static String hashString = "go";

    //customizing server interface
    public TextView textViewPath;
    public TextView textViewAngle;
    public TextView textViewX;
    public TextView textViewY;
    public TextView textViewSpeedL;
    public TextView textViewSpeedR;
    public TextView textViewServerState;
 //   public TextView textData;
    public EditText editTextFinishX;
    public EditText editTextFinishY;
    public EditText editTextStartX;
    public EditText editTextStartY;
    public EditText editTextDirection;

    //odometry info
    float passedWay;
    float angle;
    float currentX;
    float currentY;
    float wheelSpeedLeft;
    float wheelSpeedRight;

    String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, MY_BLUETOOTH_ENABLE_REQUEST_ID);

        initRobot();
        initConstructor();

        serverState = LoadedServerState;
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
                odometryMethod();
                Log.i(TAG, "onRobotReady");
                robotConnectionState = OnConnectedRobotState;
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
    }

    private void initConstructor() {
        /*tts*/
        ttsManager = new TTSManager();
        ttsManager.init(this);

        textViewPath   = (TextView) findViewById(R.id.textViewPath);
        textViewX      = (TextView) findViewById(R.id.textViewX);
        textViewY      = (TextView) findViewById(R.id.textViewY);
        textViewSpeedL = (TextView) findViewById(R.id.textViewSpeedL);
        textViewSpeedR = (TextView) findViewById(R.id.textViewSpeedR);
        textViewAngle  = (TextView) findViewById(R.id.textViewAngle);
    //    textData = (TextView) findViewById(R.id.textView16);
        textViewServerState = (TextView) findViewById(R.id.textViewServerState);

        editTextFinishX = (EditText) findViewById(R.id.editTextFinishX);
        editTextFinishY = (EditText) findViewById(R.id.editTextFinishY);
        editTextStartX = (EditText) findViewById(R.id.editTextStartX);
        editTextStartY = (EditText) findViewById(R.id.editTextStartY);
        editTextDirection = (EditText) findViewById(R.id.editTextStartDirection);

        Button btnSetTask = (Button) findViewById(R.id.buttonSetTask);
        btnSetTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    taskHandler.setTask();
                } catch (ControllerException e) {}
            }
        });

  /*      Button btnTextSpeech = (Button) findViewById(R.id.buttonTextSpeech);
        btnTextSpeech.setOnClickListener(new View.OnClickListener(){
            @Override
        public void onClick(View v){
                ttsManager.Greeting();
            }
        });*/
    }

    private void odometryMethod() {
        TwoWheelBodyControllerStateListener twoWheelBodyControllerStateListener = new TwoWheelBodyControllerStateListener() {
            @Override
            public void onWheelStateRecieved(TwoWheelState twoWheelState) {
                passedWay = twoWheelState.getOdometryInfo().getPath();
                angle = twoWheelState.getOdometryInfo().getAngle();
                currentX = (float) (-twoWheelState.getOdometryInfo().getX()+0.5*3+0.25);
                currentY = (float) (-twoWheelState.getOdometryInfo().getY()+0.5+0.25);
                wheelSpeedLeft = twoWheelState.getSpeed().getLWheelSpeed();
                wheelSpeedRight = twoWheelState.getSpeed().getRWheelSpeed();

                textViewPath.setText(Float.toString(passedWay));
                textViewAngle.setText(Float.toString(angle));
                textViewX.setText(Float.toString(currentX));
                textViewY.setText(Float.toString(currentY));
                textViewSpeedL.setText(Float.toString(wheelSpeedLeft));
                textViewSpeedR.setText(Float.toString(wheelSpeedRight));
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

    //Bluetooth needed things:
   private class WriteTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... args) {
            try {
                clientThread.getCommunicator().write(args[0]);
            } catch (Exception e) {
                Log.d("MainActivity", e.getClass().getSimpleName() + " " + e.getLocalizedMessage());
            }
            return null;
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
                    //        textData.setText(message); // просмотр строк сообщений
                            String[] recievedMessage = message.split("/", 3);
                            //Прослушка с порта bt
                            if (hashString.equals(recievedMessage[0])) {
                                Navigation navigation = new Navigation();
                                taskHandler = new TaskHandler(link);

                                int X = Integer.parseInt(recievedMessage[1]);
                                int Y = Integer.parseInt(recievedMessage[2]);
                                navigation.setFinish(Y,X);

                                status = "Connected";
                    //            Status.setText(status);
                                try {
                                    taskHandler.setTask();
                                } catch (ControllerException e) {
                                }

                            }
                            else {
                                status = recievedMessage[0]+"+"+recievedMessage[1]+"+"+recievedMessage[2];
                    //            Status.setText(status);
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
