package bt.cxp_btdata;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.params.BlackLevelPattern;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    public static void disconnect(){
        if (connectedThread != null){
            connectedThread.cancel();
            connectedThread = null;
        }
    }

    public static void getHandler(Handler handler){
        mHandler = handler;
    }
    static Handler mHandler = new Handler();

    static ConnectedThread connectedThread;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //public static final UUID MY_UUID = UUID.fromString("00001801-0000-1000-8000-00805F9B34FB");
    //public static final UUID MY_UUID = UUID.fromString("00001800-0000-1000-8000-00805F9B34FB");

    protected static final int SUCCESS_CONNECT=0;
    protected static final int MESSAGE_READ=1;
    ListView listView;
    ArrayAdapter<String> listAdapter;
    static BluetoothAdapter btAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;
    IntentFilter filter;
    BroadcastReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        init();
        if(btAdapter==null){
            Toast.makeText(getApplicationContext(), "No BT detected", Toast.LENGTH_SHORT).show();
            finish();
        } else{
            if(!btAdapter.isEnabled()){
                turnOnBT();
            }
            getPairedDevices();
            startDiscovery();
        }
    }
    private void startDiscovery(){
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }
    private void turnOnBT(){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }
    private void getPairedDevices(){
        devicesArray = btAdapter.getBondedDevices();
        if(devicesArray.size()>0){
            for(BluetoothDevice device:devicesArray){
                pairedDevices.add(device.getName());
            }
        }
        listAdapter.notifyDataSetChanged();
    }
    private void init(){
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
        listView.setAdapter(listAdapter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //filter = new IntentFilter();
        devices = new ArrayList<BluetoothDevice>();
        //Setting up the Broadcast Receiver
        Log.i("Check", "BT init");

        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i("Check Action", action);

                if(BluetoothDevice.ACTION_FOUND.equals(action) ){

                    Log.i("Check", "DeviceFound");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.i("Dev Name:", device.getName());
                    devices.add(device);
                    String s = "";
                    for(int a = 0;a<pairedDevices.size();a++){
                        if(device.getName().equals(pairedDevices.get(a))){
                            s = "(Paired)";
                            break;
                        }
                    }
                    listAdapter.add(device.getName()+" "+s+" "+"\n"+device.getAddress());
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

                }else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if (btAdapter.getState() == btAdapter.STATE_OFF){
                        turnOnBT();
                    }
                }
            }
        };


        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

        //filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //filter.addAction(BluetoothDevice.ACTION_UUID);
        //registerReceiver(receiver, filter);
        Log.i("Check", "End init()");
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3){
        Log.i("Check", "onItemClick");
        if (btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
            Log.i("Check", "Discovery is cancelled");
        }
        if (listAdapter.getItem(arg2).contains("(Paired)")){
            BluetoothDevice selectedDevice = devices.get(arg2);
            Log.i("Check", selectedDevice.getName());
            ConnectThread connect = new ConnectThread(selectedDevice);
            connect.start();
            Log.i("Check", "ConnectThread.start()");
        } else {
            Toast.makeText(getApplicationContext(), "device is not paired", Toast.LENGTH_SHORT).show();
        }
    }
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final boolean secure;
        private BluetoothSocket fbSocket;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            mmDevice = device;
            secure = true;



            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                if (secure) tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                else tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);

                Log.i("Check", "create rfcommsocket");
            } catch (IOException e) {
                Log.e("ConnnectThread", "Error: ", e);
            }
            mmSocket = tmp;

        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                Log.i("Check", "socket connected");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.e("ConnnectThread", "connect IOException: ", connectException);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("ConnnectThread", "Close IOException: ", closeException);
                }
                Log.i("Check", "trying fallback");
                String sec;
                if (secure) sec = "";
                else sec = "Insecure";

                for(Integer port = 1; port <=5; port++){
                    try{
                        btAdapter.cancelDiscovery();
                        Class<?> clazz = mmSocket.getRemoteDevice().getClass();
                        Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
                        Method m = clazz.getMethod("create"+sec+"RfcommSocket",
                                paramTypes);
                        Object[] params = new Object[]{Integer.valueOf(port)};
                        fbSocket = (BluetoothSocket) m.invoke(mmSocket.getRemoteDevice(), params);
                        fbSocket.connect();
                        Log.i("check","Connection");
                        break;
                    } catch (NoSuchMethodException | InvocationTargetException |
                            IllegalAccessException ex){
                        Log.e("ConnnectThread", "Exception: ", ex);
                    } catch (IOException ex) {
                        Log.e("ConnnectThread", "IOException: ", ex);
                        try{
                            mmSocket.close();
                        } catch (IOException e){}
                    }
                }

                /*try{


                    mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket",
                            new Class[] {int.class} ).invoke(mmDevice,Integer.valueOf(3));
                    mmSocket.connect();

                } catch (NoSuchMethodException | InvocationTargetException |
                        IllegalAccessException ex){
                    Log.e("ConnnectThread", "Exception: ", ex);
                } catch (IOException ex) {
                    Log.e("ConnnectThread", "IOException: ", ex);
                    try{
                        mmSocket.close();
                    } catch (IOException e){}
                }*/
            }

            // Do work to manage the connection (in a separate thread)
            Log.i("Check", "success connect");
            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    static class ConnectedThread extends Thread {
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
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    try{
                        sleep(30);
                    } catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    buffer = new byte[1024];
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String income) {
            try {
                mmOutStream.write(income.getBytes());
                try{
                    Thread.sleep(20);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
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
