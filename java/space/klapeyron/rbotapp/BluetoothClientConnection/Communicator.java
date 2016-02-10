package space.klapeyron.rbotapp.BluetoothClientConnection;

interface Communicator {
    void startCommunication();
    void write(String message);
    void stopCommunication();
}