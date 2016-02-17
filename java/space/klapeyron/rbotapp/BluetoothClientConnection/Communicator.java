package space.klapeyron.rbotapp.BluetoothClientConnection;

public interface Communicator {
    void startCommunication();
    void write(String message);
    void stopCommunication();
}