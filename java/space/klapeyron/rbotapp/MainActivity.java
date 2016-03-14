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

import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import space.klapeyron.rbotapp.BluetoothClientConnection.ClientThread;
import space.klapeyron.rbotapp.BluetoothClientConnection.Communicator;
import space.klapeyron.rbotapp.BluetoothClientConnection.CommunicatorImpl;
import space.klapeyron.rbotapp.BluetoothClientConnection.CommunicatorService;
import space.klapeyron.rbotapp.BluetoothClientConnection.ServerThread;

public class MainActivity extends Activity {
    static final String TAG = "TAG";

    public String serverState;
    public static final String SERVER_WAITING_ROBOT = "waiting robot";
    public static final String SERVER_WAITING_NEW_TASK = "waiting new task";
    public static final String SERVER_EXECUTING_TASK = "executing task";

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
    RobotWrap robotWrap;

    MainActivity link = this;
    TaskHandler taskHandler;
    TTSManager ttsManager = null;


    //customizing server interface
    private TextView textViewServerState;
    private TextView textViewRobotConnectionState;
    private TextView textViewClientConnectionState;
    public TextView textViewCountedPath;
    public TextView textViewOdometryPath;
    public TextView textViewOdometryAngle;
    public TextView textViewOdometryX;
    public TextView textViewOdometryY;
    public TextView textViewOdometrySpeedL;
    public TextView textViewOdometrySpeedR;
    public EditText editTextFinishX;
    public EditText editTextFinishY;
    public EditText editTextStartX;
    public EditText editTextStartY;
    public EditText editTextDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initConstructor();
        robotWrap = new RobotWrap(this);
        taskHandler = new TaskHandler(link);

        setClientConnectionState("hasn't been connected");
  /*      bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, MY_BLUETOOTH_ENABLE_REQUEST_ID);*/
    }

    @Override
    public void onResume() {
        super.onResume();
        serverThread = new ServerThread(communicatorService);
        serverThread.start();
    }

    private void initConstructor() {
        textViewServerState = (TextView) findViewById(R.id.textViewServerState);
        textViewClientConnectionState = (TextView) findViewById(R.id.textViewClientConnectionState);
        textViewRobotConnectionState = (TextView) findViewById(R.id.textViewRobotConnectionState);
        textViewCountedPath = (TextView) findViewById(R.id.textViewCountedPath);
        textViewOdometryPath = (TextView) findViewById(R.id.textViewOdometryPath);
        textViewOdometryX = (TextView) findViewById(R.id.textViewOdometryX);
        textViewOdometryY = (TextView) findViewById(R.id.textViewOdometryY);
        textViewOdometrySpeedL = (TextView) findViewById(R.id.textViewOdometrySpeedL);
        textViewOdometrySpeedR = (TextView) findViewById(R.id.textViewOdometrySpeedR);
        textViewOdometryAngle = (TextView) findViewById(R.id.textViewOdometryAngle);

        editTextFinishX = (EditText) findViewById(R.id.editTextFinishX);
        editTextFinishY = (EditText) findViewById(R.id.editTextFinishY);
        editTextStartX = (EditText) findViewById(R.id.editTextStartX);
        editTextStartY = (EditText) findViewById(R.id.editTextStartY);
        editTextDirection = (EditText) findViewById(R.id.editTextStartDirection);

        Button reconnectToRobot = (Button) findViewById(R.id.buttonReconnectToRobot);
        reconnectToRobot.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                robotWrap.reconnect();
            }
        });

        Button buttonBTOpen = (Button) findViewById(R.id.buttonBtOpen);
        buttonBTOpen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                makeDiscoverable(v);
            }
        });

        Button buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO //current state is wrong (coordinates)
                taskHandler.runningThread.interrupt();
            }
        });

        Button buttonSetTask = (Button) findViewById(R.id.buttonSetTask);
        buttonSetTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int fY = Integer.parseInt(editTextFinishY.getText().toString());
                    int fX = Integer.parseInt(editTextFinishX.getText().toString());
                    taskHandler.setTask(fX, fY);
                } catch (ControllerException e) {}
            }
        });
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
                            setClientConnectionState(message);
                            String[] recievedMessage = message.split("/", 3);
                            Log.i(MainActivity.TAG,"Message:  "+recievedMessage[0]+" "+recievedMessage[1]+" "+recievedMessage[2]);
                            int fX = Integer.parseInt(recievedMessage[1]);
                            int fY = Integer.parseInt(recievedMessage[2]);
                            Log.i(MainActivity.TAG,"finish X: "+fX);
                            Log.i(MainActivity.TAG,"finish Y: "+fY);
                            editTextFinishX.setText(recievedMessage[1]);
                            editTextFinishY.setText(recievedMessage[2]);
                            try {
                                taskHandler.setTask(fX, fY);
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

    public void setServerState(String state) {
        textViewServerState.setText(state);
    }

    public void setRobotConnectionState(String state) {
        textViewRobotConnectionState.setText(state);
    }

    public void setClientConnectionState(String state) {
        textViewClientConnectionState.setText(state);
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
