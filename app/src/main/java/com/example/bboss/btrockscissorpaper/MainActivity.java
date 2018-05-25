package com.example.bboss.btrockscissorpaper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/*
TODO CANCELLA : TUTTE DIR BUILD...
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView textViewInfos;
    private Button btnServer;
    private Button btnClient;
    private String hostKind;
    private ListView listViewDiscovered;
    public static final String CLIENT="CLIENT";
    private List<BluetoothDevice> discovered = new ArrayList<>();
    private BluetoothDevice debugTargetDevice;  //DEBUG ONLY
    public static final String SERVER="SERVER";
    private static final int REQUEST_ENABLE_BT=1;   //passed with intent to bt os handler...retrived in onREsult...
    private BluetoothAdapter bluetoothAdapter;

    private final int DURATION=300;
    protected static final  UUID uuid= UUID.fromString("b8319a04-3632-4d0d-8bd5-47238a404a28");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnClient = findViewById(R.id.btnClient);
        btnServer = findViewById(R.id.btnServer);
        listViewDiscovered = findViewById(R.id.discovered);

    }

    @Override
    protected void onStart() {
        super.onStart();
        btnServer.setOnClickListener(this);
        btnClient.setOnClickListener(this);


    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btnClient:
                hostKind=CLIENT;
                break;
            case R.id.btnServer:
                hostKind=SERVER;
                break;
            default:
                throw new IllegalArgumentException("CLICK ONLY SERVER OR CLIENT BTN... :(");

            }
        this.connectTry();
    }
    private void connectTry(){
        System.out.println("SELECTED..."+hostKind);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            BTHandler.setupAllert("BT NOT SUPPORTED FROM DEVICE");
            finish();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        }
        //start BT and switch in server host logic
        if (hostKind.equals(SERVER))
            connectTryServer();
        else
            connectTryClient();
    }

    private void connectTryServer() {
        // andrea!
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DURATION);
        startActivity(discoverableIntent);
        //getting server socket
        boolean debugOn=true;
        if(!debugOn)
            return;
        BluetoothServerSocket bluetoothServerSocket= null;
        BluetoothSocket serverSocket = null; //blocking call... MUST BE IN ANTOHER TH!

        try {
            bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(this.getLocalClassName(),uuid);
            //SDP protocol where run bt? has localClassName and uuid specified
            serverSocket = bluetoothServerSocket.accept();

        } catch (IOException e) {
            BTHandler.setupAllert("ERROR IN CREATE COMUNICATION CHANNEL (SERVER)");
            e.printStackTrace();
        }
        try {
            bluetoothServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //debug try write "hello fuck bt word"
        try {
            serverSocket.getOutputStream().write("hello fuck bt word".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //getted connection==>close way to get more ... //TODO CHANGE FOR >=3 PLAYER!

    }
    //BROADCAST RECEIVER 4 CLIENT
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                discovered.add(device);
                debugTargetDevice=device;
                //discovered.add(device);
                //TODO ADD TO A LIST OF FOUNDED DEVICE (HAS TO BE CLEARED BEFORE START SCANNING!
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                System.out.println(deviceName+deviceHardwareAddress);
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                //TODO HANLDE ?
                ArrayAdapter<BluetoothDevice> arrayAdapter= new ArrayAdapter<BluetoothDevice>(context,R.id.textView,discovered);
                listViewDiscovered.setAdapter(arrayAdapter);

            }

        }
    };
    private void connectTryClient(){
        //livio
        //TODO THIS METHOD HAS TO WRAP CALLING TO ANOTHER CLASS WITCH WILL PERFORM ALL IN ANOTHER THREAD
        BluetoothDevice targetDevice=null;
        //getting paierd devices...         TODO ONLY DEBUG
        System.out.println("PAIRED DEVICES...");
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        discovered.addAll(pairedDevices);   //TODO DEBUG ONLY
        //debug take nokia6 from paireds Device.
        String debugTargetMac="7C:46:85:29:58:15"; //C9:50:76:8D:90:FD //oth
        Iterator<BluetoothDevice> iterator= pairedDevices.iterator();
        while (iterator.hasNext()){
            BluetoothDevice device= iterator.next();
            if (device.getAddress().equals(debugTargetMac)){
                //accoppiated device Nokia 6 founded => target 2 connect
                this.debugTargetDevice=device;
            }
        }
        /*TODO TEST
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                System.out.println("DEVICE"+deviceName+deviceHardwareAddress);
            }
        }
        */

        //DISCOVERING OTHER DEVICE...
        // Create a BroadcastReceiver for ACTION_FOUND.


        IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);  //TODO NERVER FOUND NOTHING ... :(
        IntentFilter filterDiscChanged = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filterFound);
        registerReceiver(mReceiver, filterDiscChanged);

        //getting runtime permission to access course location
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        listViewDiscovered.setVisibility(View.VISIBLE);

        boolean returnB= bluetoothAdapter.startDiscovery();   //manualy handle bt discvoery in code!! async call
        /* TODO LIVIO BIND DISCOVERED => LISTVIEW
        ArrayAdapter<BluetoothDevice> arrayAdapter= new ArrayAdapter<BluetoothDevice>(this,R.id.discovered,discovered);
        listViewDiscovered.setAdapter(arrayAdapter);
        Intent intentBluetooth = new Intent();
        intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        //startActivityForResult(intentBluetooth,11);//TODO CHECK START SYS DISCOVERY ACTIVITY!
        //unbinding receiver of broadcast bt discovery notify... TODO EVALUTATE DIFFERENT POSITION (LK ONDESTROY)
        //unregisterReceiver(mReceiver); //TODO ADD
        BluetoothSocket clientSocket = null;
        */
        //todo get socket

        /* TODO original   call 2 IO BT
        try {


            clientSocket= this.debugTargetDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            clientSocket.connect(); //TODO BLOCKING UNTIL CLIENT AND SERVER HAVE PAIRED...
            //IOEXEPTION FOR TIMEOUT---ERRORS
        } catch (IOException e)
        {
            BTHandler.setupAllert("ERROR IN CREATE COMUNICATION CHANNEL CLIENT");
            e.printStackTrace();
        }


        try {
            clientSocket.getInputStream().read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //todo cases with result canceled... exit or retry?(other way to get this..??)
        switch (requestCode){
            case REQUEST_ENABLE_BT: //TODO RELATION.. SAME CODE SENDED TO START ACTIVITY RETRIVED HERE
                //result from enabling BT..:D
                if (resultCode== Activity.RESULT_OK){
                    System.out.println("BT ENABLED SUSSFULY");
                }
                else if(resultCode==Activity.RESULT_CANCELED){      //pressed no on enabling bt..
                    BTHandler.setupAllert("ERROR IN ENABLING...:'((");
                }
                break;
            case (DURATION):{ //DOCS SAY RESULT OF BT DISCOVERABLE SWITCH USE THIS RET CODE...
                //server calling
                    if(resultCode==RESULT_CANCELED){
                        BTHandler.setupAllert("ERROR IN DISCOVERABILITY");
                    }
                    else if (resultCode==RESULT_OK){
                        System.out.println("OK DISCOVERABILITY SWITCH");
                    }
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
    }


}
