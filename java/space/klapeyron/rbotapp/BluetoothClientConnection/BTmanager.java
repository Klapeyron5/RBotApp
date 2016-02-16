package space.klapeyron.rbotapp.BluetoothClientConnection;

import android.bluetooth.BluetoothSocket;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import space.klapeyron.rbotapp.LowLevelNavigationTasks;
import space.klapeyron.rbotapp.MainActivity;

public class BTmanager {
    MainActivity mainActivity;

    BTmanager(MainActivity m) {
        mainActivity = m;
    }

    public void setTaskForRobot(int Y, int X) {
        mainActivity.setTaskFromBT(Y,X);
    }
}
