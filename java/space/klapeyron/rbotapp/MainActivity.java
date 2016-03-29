package space.klapeyron.rbotapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import space.klapeyron.rbotapp.InteractiveMap.InteractiveMapView;

public class MainActivity extends Activity {
    public static final String TAG = "TAG";

    public String serverState;
    public static final String SERVER_WAITING_ROBOT = "waiting robot";
    public static final String SERVER_WAITING_NEW_TASK = "waiting new task";
    public static final String SERVER_EXECUTING_TASK = "executing task";

    private String serverActivityState;
    public static final  String ACTIVITY_STATE_MAIN_XML = "main.xml";
    public static final  String ACTIVITY_STATE_INTERACTIVE_MAP = "interactive map";

    public String robotConnectionState;
    public static final String OnConnectedRobotState = "connected";

    public String clientConnectionState;



    private static final int REQUEST_ENABLE_BT = 0; //>=0 for run onActivityResult from startActivityForResult
    private static final String UUID = "e91521df-92b9-47bf-96d5-c52ee838f6f6";
    private static final String SERVICE_NAME = "Local RBot Android Server $key: hello berzin klapeyron$"; //имя приложения сервера (для проверки входящих блютуз-запросов)

    BluetoothAdapter bluetoothAdapter; //локальный БТ адаптер
    private Set<BluetoothDevice> pairedDevices; //спаренные девайсы
    private BluetoothDevice clientDevice; //девайс клиента (для восстановления связи при потере сокета)
    private BluetoothSocket clientSocket; //канал соединения с последним клиентом
    AcceptThread acceptThread; //поток для серверной прослушки запросов на БТ-соединение
//    ConnectedThread connectedThread; //поток для принятия кооринат цели и отправления данных

    ru.rbot.android.bridge.service.robotcontroll.robots.Robot robot;
    RobotWrap robotWrap;

    MainActivity link = this;
    InteractiveMapView interactiveMapView;
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
        serverActivityState = ACTIVITY_STATE_MAIN_XML;

        Log.i(TAG, "OnCreate()");

        initConstructor();

        robotWrap = new RobotWrap(this);
        taskHandler = new TaskHandler(link);

        setClientConnectionState("hasn't been connected");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = bluetoothAdapter.getBondedDevices(); //получаем список сопряженных устройств
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                initConstructor();
                acceptThread = new AcceptThread();
                acceptThread.start(); //запускаем серверную прослушку входящих БТ запросов
            } else {
                //start BT
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            //TODO device does not support BT
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            robotWrap = new RobotWrap(this);
            taskHandler = new TaskHandler(link);
            acceptThread = new AcceptThread();
            acceptThread.start(); //запускаем серверную прослушку входящих БТ запросов
        }
        if (resultCode == RESULT_CANCELED) {
            robotWrap = new RobotWrap(this);
            taskHandler = new TaskHandler(link);
            setServerState("bluetooth off");
            setRobotConnectionState("bluetooth off");
            setClientConnectionState("bluetooth off");
        }
        super.onActivityResult(requestCode, resultCode, data);
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

        Button buttonMap = (Button) findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
         //       Intent intent = new Intent(MainActivity.this, InteractiveMapActivity.class);
         //       startActivity(intent);
                interactiveMapView = new InteractiveMapView(link,robotWrap.currentCellX,robotWrap.currentCellY);
                setContentView(interactiveMapView);
                serverActivityState = ACTIVITY_STATE_INTERACTIVE_MAP;
            }
        });

        Button buttonReconnectToRobot = (Button) findViewById(R.id.buttonReconnectToRobot);
        buttonReconnectToRobot.setOnClickListener(new View.OnClickListener() {
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
                    Log.i(TAG,"1");
                    int fY = Integer.parseInt(editTextFinishY.getText().toString());
                    int fX = Integer.parseInt(editTextFinishX.getText().toString());
                    Log.i(TAG,"2");
                    taskHandler.setTask(fX, fY);
                } catch (ControllerException e) {e.printStackTrace();}
            }
        });

        final Button buttonSendIsReady = (Button) findViewById(R.id.buttonIsReady);
        buttonSendIsReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBackWhatServerIsReady();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //replaces the default 'Back' button action
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            switch(serverActivityState) {
                case ACTIVITY_STATE_MAIN_XML:
                    break;
                case ACTIVITY_STATE_INTERACTIVE_MAP:
                    setContentView(R.layout.main);
                    initConstructor();
                    setServerState(serverState);
                    setRobotConnectionState(robotConnectionState);
                    setClientConnectionState(clientConnectionState);
                    editTextFinishX.setText(Integer.toString(taskHandler.finishX));
                    editTextFinishY.setText(Integer.toString(taskHandler.finishY));
                    robotWrap.writeCurrentPositionOnServerDisplay();
                    serverActivityState = ACTIVITY_STATE_MAIN_XML;
                    break;
            }
        }
        return false;
    }

    public void displayRobotPosition() {
        switch(serverActivityState) {
            case ACTIVITY_STATE_MAIN_XML:
                Log.i(TAG,"displayRobotPosition() ACTIVITY_STATE_MAIN_XML");
                synchronized (this) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            editTextStartX.setText(Integer.toString(robotWrap.currentCellX));
                            editTextStartY.setText(Integer.toString(robotWrap.currentCellY));
                            editTextDirection.setText(Integer.toString(robotWrap.currentDirection));
                        }
                    });
                }
                break;
            case ACTIVITY_STATE_INTERACTIVE_MAP:
                Log.i(TAG,"displayRobotPosition() ACTIVITY_STATE_INTERACTIVE_MAP");
                synchronized (this) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            interactiveMapView = new InteractiveMapView(link,robotWrap.currentCellX,robotWrap.currentCellY);
                            setContentView(interactiveMapView);
                        }
                    });
                }
                break;
        }
    }

    public void makeDiscoverable(View view) {
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(i);
    }

    public void setServerState(String state) {
        serverState = state;
        textViewServerState.setText(state);
    }

    public void setRobotConnectionState(String state) {
        robotConnectionState = state;
        textViewRobotConnectionState.setText(state);
    }

    public void setClientConnectionState(String state) {
        clientConnectionState = state;
        textViewClientConnectionState.setText(state);
    }









    //братный звонок, что сервер принял синал и готов к выполнению задания
    private void callBackWhatServerIsReady() {
        ConnectedThread connectedThread = new ConnectedThread(clientSocket);
        connectedThread.write(("/ready/0/0/").getBytes());
        Log.i(TAG,"callBackWhatServerIsReady");
        connectedThread.start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            Log.i(TAG, "AcceptThread.Constructor()");
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, java.util.UUID.fromString(UUID));
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "AcceptThread.run()");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                Log.i(TAG, "AcceptThread while(true) BEFORE");
                try {
                    Log.i(TAG, "1");
                    socket = mmServerSocket.accept();
                    Log.i(TAG, "2");
                } catch (IOException e) {
                    Log.i(TAG, "3");
                    break;
                }
                Log.i(TAG, "4");
                // If a connection was accepted
                if (socket != null) {
                    clientDevice = socket.getRemoteDevice(); //запоминаем клиента
                    clientSocket = socket; //запоминаем текущий рабочий сокет с клиентом
                    // Do work to manage the connection (in a separate thread)
                    //        manageConnectedSocket(socket);
                    Log.i("TAG", "CONNECTED--------------------->>>>>>>"); //TODO ответить клиенту что сервер готов к работе и ждать координат
                    callBackWhatServerIsReady();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setClientConnectionState("Saw customer"); //клиент выбра сервер но еще не задал задачи
                        }
                    });
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            Log.i(TAG, "AcceptThread.run() finished");
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }



    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    final String str = new String(buffer,"UTF-8");
                    Log.i(TAG,"reading:  "+str);
                    String[] a = str.split("/");
                    Log.i(TAG,"!"+a[0]+"!"+a[1]+"!"+a[2]+"!"+a[3]);
                    final String key = a[1];
                    if (key.equals("task")) {
                        final String X = a[2];
                        final String Y = a[3];
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editTextFinishX.setText(X);
                                editTextFinishY.setText(Y);
                            }
                        });
                        Log.i(TAG, "setTask now");
                        int fY = Integer.parseInt(X.toString());
                        int fX = Integer.parseInt(Y.toString());
                        Log.i(TAG,""+fX+" "+fY);
                        taskHandler.setTask(fX,fY);
                    }
                    // Send the obtained bytes to the UI activity
                    //        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.i(TAG,"IOException");
                    break;
                } catch (ControllerException e) {
                    e.printStackTrace();
                    Log.i(TAG,"ControllerException");
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
