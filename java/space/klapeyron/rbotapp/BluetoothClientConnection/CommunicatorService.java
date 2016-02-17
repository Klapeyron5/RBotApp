package space.klapeyron.rbotapp.BluetoothClientConnection;

import android.bluetooth.BluetoothSocket;

public interface CommunicatorService {
    Communicator createCommunicatorThread(BluetoothSocket socket);
}