package test.com.hrjs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {

    private final int REQUEST_BLUETOOTH_ENABLE = 100;

    private TextView mConnectionStatus;
    private EditText mInputEditText;

    ConnectedTask mConnectedTask = null;
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";
    private String a = "1";

    //public String num;
    private ArrayAdapter<String> adapter;
    List<String> Array = new ArrayList<String>();

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ChildEventListener mChild;

    private void initDatabase() {

        mDatabase = FirebaseDatabase.getInstance();

        mReference = mDatabase.getReference("log");
        mReference.child("log").setValue("check");

        mChild = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mReference.addChildEventListener(mChild);
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);

        Button sendButton = (Button)findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String sendMessage = mInputEditText.getText().toString();
                if ( sendMessage.length() > 0 ) {
                    sendMessage(sendMessage);
                }
                else sendMessage(String.valueOf(a));
            }
        });
        mConnectionStatus = (TextView)findViewById(R.id.connection_status_textview);
        mInputEditText = (EditText)findViewById(R.id.input_string_edittext);
        ListView mMessageListview = (ListView) findViewById(R.id.message_listview);

        mConversationArrayAdapter = new ArrayAdapter<>( this,
                android.R.layout.simple_list_item_1 );
        mMessageListview.setAdapter(mConversationArrayAdapter);


        Log.d( TAG, "Initalizing Bluetooth adapter...");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showErrorDialog("This device is not implement Bluetooth.");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
        }
        else {
            Log.d(TAG, "Initialisation successful.");

            showPairedDevicesListDialog();
        }
        initDatabase();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());


        mReference = mDatabase.getReference("Phonenum");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();
                //String value = dataSnapshot.getValue(String.class);
                //Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                //Log.i("Value is: " , String.valueOf(map));

                for (DataSnapshot messageData : dataSnapshot.getChildren()) {
                    String msg2 = messageData.getValue().toString();
                    Array.add(msg2);
                    adapter.add(msg2);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( mConnectedTask != null ) {

            mConnectedTask.cancel(true);
        }
    }


    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;
        private ArrayList<UUID> mUuids;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            mConnectedDeviceName = bluetoothDevice.getName();

            mUuids = new ArrayList<UUID>();
            // 7 randomly-generated UUIDs. These must match on both server and client.
            mUuids.add(UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
            mUuids.add(UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
            mUuids.add(UUID.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"));
            mUuids.add(UUID.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"));
            //SPP
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                Log.d( TAG, "create socket for "+mConnectedDeviceName);

            } catch (IOException e) {
                Log.e( TAG, "socket create failed " + e.getMessage());
            }

            mConnectionStatus.setText("connecting...");
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mBluetoothSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " +
                            " socket during connection failure", e2);
                }

                return false;
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean isSucess) {

            if ( isSucess ) {
                connected(mBluetoothSocket);
            }
            else{

                isConnectionError = true;
                Log.d( TAG,  "Unable to connect device");
                showErrorDialog("Unable to connect device");
            }
        }
    }


    public void connected( BluetoothSocket socket ) {
        mConnectedTask = new ConnectedTask(socket);
        mConnectedTask.execute();
    }



    private class ConnectedTask extends AsyncTask<Void, String, Boolean> {

        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private BluetoothSocket mBluetoothSocket = null;

        ConnectedTask(BluetoothSocket socket){

            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "socket not created", e );
            }
            Log.d( TAG, "connected to "+mConnectedDeviceName);
            mConnectionStatus.setText( "connected to "+mConnectedDeviceName);
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            byte [] readBuffer = new byte[1024];
            int readBufferPosition = 0;


            while (true) {

                if ( isCancelled() ) return false;

                try {

                    int bytesAvailable = mInputStream.available();

                    if(bytesAvailable > 0) {

                        byte[] packetBytes = new byte[bytesAvailable];

                        mInputStream.read(packetBytes);

                        for(int i=0;i<bytesAvailable;i++) {

                            byte b = packetBytes[i];
                            if(b == '\n')
                            {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0,
                                        encodedBytes.length);
                                String recvMessage = new String(encodedBytes, "UTF-8");

                                readBufferPosition = 0;

                                if(recvMessage.contains("Dangerous")){
                                    Intent intent = new Intent(Bluetooth.this, SendmsgActivity.class);
                                    intent.putExtra("Numsaved", (Serializable) Array);
                                    startActivity(intent);
                                }
                                Log.d(TAG, "recv message: " + recvMessage);
                                publishProgress(recvMessage);
                            }
                            else
                            {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {

                    Log.e(TAG, "disconnected", e);
                    return false;
                }
            }

        }

        @Override
        protected void onProgressUpdate(String... recvMessage) {

            mConversationArrayAdapter.insert(mConnectedDeviceName + ": " + recvMessage[0], 0);
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            super.onPostExecute(isSucess);

            if ( !isSucess ) {


                closeSocket();
                Log.d(TAG, "Device connection was lost");
                isConnectionError = true;
                showErrorDialog("Device connection was lost");
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);

            closeSocket();
        }

        void closeSocket(){

            try {

                mBluetoothSocket.close();
                Log.d(TAG, "close socket()");

            } catch (IOException e2) {

                Log.e(TAG, "unable to close() " +
                        " socket during connection failure", e2);
            }
        }

        void write(String msg){

            msg += "\n";

            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during send", e );
            }

            mInputEditText.setText(" ");
        }
    }


    public void showPairedDevicesListDialog()
    {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if ( pairedDevices.length == 0 ){
            showQuitDialog( "No devices have been paired.\n"
                    +"You must pair it with another device.");
            return;
        }

        String[] items;
        items = new String[pairedDevices.length];
        for (int i=0;i<pairedDevices.length;i++) {
            items[i] = pairedDevices[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select device");
        builder.setCancelable(false);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                ConnectTask task = new ConnectTask(pairedDevices[which]);
                task.execute();
            }
        });
        builder.create().show();
    }



    public void showErrorDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if ( isConnectionError  ) {
                    isConnectionError = false;
                    finish();
                }
            }
        });
        builder.create().show();
    }


    public void showQuitDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

    void sendMessage(String msg){

        if ( mConnectedTask != null ) {
            mConnectedTask.write(msg);
            Log.d(TAG, "send message: " + msg);
            mConversationArrayAdapter.insert("Me:  " + msg, 0);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_BLUETOOTH_ENABLE){
            if (resultCode == RESULT_OK){
                //BlueTooth is now Enabled
                showPairedDevicesListDialog();
            }
            if(resultCode == RESULT_CANCELED){
                showQuitDialog( "You need to enable bluetooth");
            }
        }
    }

    /*public static void main(String[] args){

        log("Local Bluetooth device...\n");

        LocalDevice local = null;
        try {

            local = LocalDevice.getLocalDevice();
        } catch (BluetoothStateException e2) {

        }

        log( "address: " + local.getBluetoothAddress() );
        log( "name: " + local.getFriendlyName() );


        Runnable r = new ServerRunable();
        Thread thread = new Thread(r);
        thread.start();

    }


    private static void log(String msg) {

        System.out.println("["+(new Date()) + "] " + msg);
    }

}


class ServerRunable implements Runnable{

    //UUID for SPP
    final UUID uuid = new UUID("0000110100001000800000805F9B34FB", false);
    final String CONNECTION_URL_FOR_SPP = "btspp://localhost:"
            + uuid +";name=SPP Server";

    private StreamConnectionNotifier mStreamConnectionNotifier = null;
    private StreamConnection mStreamConnection = null;
    private int count = 0;


    @Override
    public void run() {

        try {

            mStreamConnectionNotifier = (StreamConnectionNotifier) Connector
                    .open(CONNECTION_URL_FOR_SPP);

            log("Opened connection successful.");
        } catch (IOException e) {

            log("Could not open connection: " + e.getMessage());
            return;
        }


        log("Server is now running.");



        while(true){

            log("wait for client requests...");

            try {

                mStreamConnection = mStreamConnectionNotifier.acceptAndOpen();
            } catch (IOException e1) {

                log("Could not open connection: " + e1.getMessage() );
            }


            count++;
            log("현재 접속 중인 클라이언트 수: " + count);


            new Receiver(mStreamConnection).start();
        }

    }



    class Receiver extends Thread {

        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private String mRemoteDeviceString = null;
        private StreamConnection mStreamConnection = null;


        Receiver(StreamConnection streamConnection){

            mStreamConnection = streamConnection;

            try {

                mInputStream = mStreamConnection.openInputStream();
                mOutputStream = mStreamConnection.openOutputStream();

                log("Open streams...");
            } catch (IOException e) {

                log("Couldn't open Stream: " + e.getMessage());

                Thread.currentThread().interrupt();
                return;
            }


            try {

                RemoteDevice remoteDevice
                        = RemoteDevice.getRemoteDevice(mStreamConnection);

                mRemoteDeviceString = remoteDevice.getBluetoothAddress();

                log("Remote device");
                log("address: "+ mRemoteDeviceString);

            } catch (IOException e1) {

                log("Found device, but couldn't connect to it: " + e1.getMessage());
                return;
            }

            log("Client is connected...");
        }


        @Override
        public void run() {

            try {

                Reader mReader = new BufferedReader(new InputStreamReader
                        ( mInputStream, Charset.forName(StandardCharsets.UTF_8.name())));

                boolean isDisconnected = false;

                Sender("에코 서버에 접속하셨습니다.");
                Sender( "보내신 문자를 에코해드립니다.");

                while(true){

                    log("ready");


                    StringBuilder stringBuilder = new StringBuilder();
                    int c = 0;


                    while ( '\n' != (char)( c = mReader.read()) ) {

                        if ( c == -1){

                            log("Client has been disconnected");

                            count--;
                            log("현재 접속 중인 클라이언트 수: " + count);

                            isDisconnected = true;
                            Thread.currentThread().interrupt();

                            break;
                        }

                        stringBuilder.append((char) c);
                    }

                    if ( isDisconnected ) break;

                    String recvMessage = stringBuilder.toString();
                    log( mRemoteDeviceString + ": " + recvMessage );

                    Sender(recvMessage);
                }

            } catch (IOException e) {

                log("Receiver closed" + e.getMessage());
            }
        }


        void Sender(String msg){

            PrintWriter printWriter = new PrintWriter(new BufferedWriter
                    (new OutputStreamWriter(mOutputStream,
                            Charset.forName(StandardCharsets.UTF_8.name()))));

            printWriter.write(msg+"\n");
            printWriter.flush();

            log( "Me : " + msg );
        }
    }


    private static void log(String msg) {

        System.out.println("["+(new Date()) + "] " + msg);
    }*/
}
