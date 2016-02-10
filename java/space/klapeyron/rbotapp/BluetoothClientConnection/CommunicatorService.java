package space.klapeyron.rbotapp.BluetoothClientConnection;

import android.bluetooth.BluetoothSocket;

interface CommunicatorService {
    Communicator createCommunicatorThread(BluetoothSocket socket);
}