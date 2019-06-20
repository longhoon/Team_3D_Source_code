package test.com.hrjs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback{

    public static Context mContext = null;
    private boolean m_bTrackingMode = true;
    private TMapGpsManager tmapgps = null;
    TMapView tmapview;
    private String mConnectedDeviceName = null;
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";
    LocationManager mLM;
    String mProvider = LocationManager.NETWORK_PROVIDER;
    ConnectedTask mConnectedTask = null;
    private static String mApiKey = "ee56383b-e2ca-4bbc-a2d7-01b57abe4dce"; //Tmap API 키 설정
    static BluetoothAdapter mBluetoothAdapter;
    private Button route;
    private Button emergencybell;
    private Button bluetooth;
    private Button savenumBtn;
    private EditText input_start;
    private EditText input_dest;
    private EditText input_stop;

    TMapPoint start_point = null;
    TMapPoint dest_point = null;
    TMapPoint stop_point = null;


    public String num;
    private ArrayAdapter<String> adapter;
    List<String> Array = new ArrayList<String>();
    private String a = "2";
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ChildEventListener mChild;
    private String html = "";
    private Handler mHandler;

    private Socket socket;
    private String name;
    private BufferedReader networkReader;
    private BufferedWriter networkWriter;
    private String ip = "192.168.43.32"; // IP
    private int port = 9999; // PORT번호



    @Override
    public void onLocationChange(Location location) {
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_BLUETOOTH_ENABLE){
            if (resultCode == RESULT_OK){
                //BlueTooth is now Enabled
                showPairedDevicesListDialog();
            }
            if(resultCode == RESULT_CANCELED){ ;
                Log.d("recv","F");
            }
        }

        switch (requestCode){
            case 0:
                String address = data.getStringExtra("input");   // 검색한 위치의 주소
                double lat = Double.parseDouble(data.getStringExtra("lat"));    // 검색한 위치의 경도
                double  lon = Double.parseDouble(data.getStringExtra("lon"));   //  검색한 위치의 위도
                input_start.setText(address);
                start_point = new TMapPoint(lat,lon);
                break;
            case 1:
                address = data.getStringExtra("input");
                lat = Double.parseDouble(data.getStringExtra("lat"));
                lon = Double.parseDouble(data.getStringExtra("lon"));
                input_dest.setText(address);
                dest_point = new TMapPoint(lat, lon);
                break;
            case 2:
                address = data.getStringExtra("input");
                lat = Double.parseDouble(data.getStringExtra("lat"));
                lon = Double.parseDouble(data.getStringExtra("lon"));
                input_stop.setText(address);
                stop_point = new TMapPoint(lat, lon);
                break;
            default:
                break;
        }
    }
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
    protected void onDestroy() {
        super.onDestroy();
        mReference.removeEventListener(mChild);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;

        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mHandler = new Handler();

        SocketThread socketThread = new SocketThread();
        socketThread.start();

        checkUpdate.start();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //showErrorDialog("This device is not implement Bluetooth.");
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


        mContext = this;
        tmapview = (TMapView) findViewById(R.id.map_view);
        input_start = (EditText) findViewById(R.id.search_sta);
        input_dest = (EditText) findViewById(R.id.search_dest);
        input_stop = (EditText) findViewById(R.id.search_stop);
        emergencybell = (Button) findViewById(R.id.emergencybell);
        savenumBtn = (Button) findViewById(R.id.savenumBtn);
        route = (Button) findViewById(R.id.route);   // 설정한 출발지와 도착지간의 경로를 받아오기 위한 버튼
        bluetooth = (Button) findViewById(R.id.bluetooth);

        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        tmapview.setOnApiKeyListener(new TMapView.OnApiKeyListenerCallback() {
            @Override
            public void SKTMapApikeySucceed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupMap();
                    }
                });
            }

            @Override
            public void SKTMapApikeyFailed(String s) {

            }
        });
        tmapview.setSKTMapApiKey(mApiKey);

        TMapPolyLine polyLine = new TMapPolyLine();
        polyLine.setLineWidth(3);




        input_start.setOnClickListener(new View.OnClickListener() {
            // edit text 를 클릭시 장소를 검색할 수 있는 액티비티로 이동
            @Override
            public void onClick(View v) {
                Intent start_intent = new Intent(MainActivity.this, SearchLocationActivity.class);
                startActivityForResult(start_intent,0);
            }
        });

        input_dest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dest_intent = new Intent(MainActivity.this, SearchLocationActivity.class);
                startActivityForResult(dest_intent,1);
            }
        });
        input_stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent stop_intent = new Intent(mContext, dbtest.class);
                startActivityForResult(stop_intent,2);
            }
        });
        bluetooth.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, Bluetooth.class);
                mContext.startActivity(intent);
            }
        });
        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //경로 찾기 버튼 클릭시 intent로 출발지, 목적지의 위도,경도,주소이름을 route activity로 넘김
                Intent intent = new Intent(mContext, RouteActivity.class);

                intent.putExtra("start_address",String.valueOf(input_start.getText()));
                Log.d("js", String.valueOf(input_start));
                intent.putExtra("dest_address", String.valueOf(input_dest.getText()));
                Log.d("js", String.valueOf(input_dest));
                intent.putExtra("stop_address", String.valueOf(input_stop.getText()));
                Log.d("js", String.valueOf(input_stop));
                intent.putExtra("start_lat",String.valueOf(start_point.getLatitude()));
                intent.putExtra("start_lon",String.valueOf(start_point.getLongitude()));
                intent.putExtra("dest_lat",String.valueOf(dest_point.getLatitude()));
                intent.putExtra("dest_lon",String.valueOf(dest_point.getLongitude()));
                intent.putExtra("stop_lat",String.valueOf(stop_point.getLatitude()));
                intent.putExtra("stop_lon",String.valueOf(stop_point.getLongitude()));

                mContext.startActivity(intent);

            }
        });
        savenumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NumSaveActivity.class);
                startActivity(intent);
            }
        });

        emergencybell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SendmsgActivity.class);
                /*Iterator<String> it = Array.iterator();*/

                intent.putExtra("Numsaved", (Serializable) Array);
                startActivity(intent);
                /*while(it.hasNext()) {
                    String str = it.next();
                    intent.putExtra("Numsaved", str);
                    startActivity(intent);
                    }*/
                sendMessage(String.valueOf(a));
                PrintWriter out = new PrintWriter(networkWriter,true);
                out.println(String.valueOf(a));
            }
        });
    }
    void sendMessage(String msg){

        if ( mConnectedTask != null ) {
            mConnectedTask.write(msg);
            Log.d(TAG, "send message: " + msg);
            //mConversationArrayAdapter.insert("Me:  " + msg, 0);
        }
    }

    boolean isInitialized = false;

    //지도 초기 값 설정
    private void setupMap() {
        isInitialized = true;
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setCompassMode(true);
        tmapview.setIconVisibility(true);
        tmapview.setZoomLevel(15);
        tmapview.setTrackingMode(true);
        tmapview.setSightVisible(true);

        tmapgps = new TMapGpsManager(MainActivity.this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);


        //tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);    //연결된 인터넷으로 위치 파악
        tmapgps.setProvider(tmapgps.GPS_PROVIDER);     //GPS로 위치 파악
        tmapgps.OpenGps();
        if (cacheLocation != null) {
            moveMap(cacheLocation.getLatitude(), cacheLocation.getLongitude());
            setMyLocation(cacheLocation.getLatitude(), cacheLocation.getLongitude());
        }

    }

    @Override
    //액티비티 실행 시 인터넷 연결여부 확인, 위치확인
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = mLM.getLastKnownLocation(mProvider);
        if (location != null) {
            mListener.onLocationChanged(location);
        }
        mLM.requestSingleUpdate(mProvider, mListener, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLM.removeUpdates(mListener);
        // TODO Auto-generated method stub
        super.onStop();
        try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    Location cacheLocation = null;

    //지도 중심 설정
    private void moveMap(double lat, double lng) {
        tmapview.setCenterPoint(lng, lat);
    }

    private void setMyLocation(double lat, double lng) {
        tmapview.setLocationPoint(lng, lat);
        tmapview.setIconVisibility(true);
    }

    //지도를 움직였을 때 지도 중심점과 내 위치 설정
    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (isInitialized) {
                moveMap(location.getLatitude(), location.getLongitude());
                setMyLocation(location.getLatitude(), location.getLongitude());
            } else {
                cacheLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }


    };

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            mConnectedDeviceName = bluetoothDevice.getName();

            //SPP
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                Log.d( TAG, "create socket for "+mConnectedDeviceName);

            } catch (IOException e) {
                Log.e( TAG, "socket create failed " + e.getMessage());
            }
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
                //showErrorDialog("Unable to connect device");
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
            //mConnectionStatus.setText( "connected to "+mConnectedDeviceName);
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
                                    Intent intent = new Intent(MainActivity.this, SendmsgActivity.class);
                                    intent.putExtra("Numsaved", (Serializable) Array);
                                    startActivity(intent);
                                    sendMessage(String.valueOf(a));
                                    PrintWriter out = new PrintWriter(networkWriter,true);
                                    out.println(String.valueOf(a));
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

            //mConversationArrayAdapter.insert(mConnectedDeviceName + ": " + recvMessage[0], 0);
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            super.onPostExecute(isSucess);

            if ( !isSucess ) {


                closeSocket();
                Log.d(TAG, "Device connection was lost");
                isConnectionError = true;
                //showErrorDialog("Device connection was lost");
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

            //mInputEditText.setText(" ");
        }
    }
    public void showPairedDevicesListDialog()
    {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if ( pairedDevices.length == 0 ){
            //showQuitDialog( "No devices have been paired.\n"
            //+"You must pair it with another device.");
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
    private final int REQUEST_BLUETOOTH_ENABLE = 100;

    private Thread checkUpdate = new Thread() {
        public void run() {
            try {
                String line;
                Log.w("ChattingStart", "Start Thread");
                while (true) {

                    Log.w("Chatting is running", "chatting is running");
                    line = networkReader.readLine();
                    html = line;
                    mHandler.post(showUpdate);
                }

            } catch (Exception e) {
            }
        }
    };

    private Runnable showUpdate = new Runnable() {
        public void run() {
            Toast.makeText(MainActivity.this, "Coming word: " + html,
                    Toast.LENGTH_SHORT).show();
        }
    };

    public void setSocket(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);
            networkWriter =
                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            networkReader =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    class SocketThread extends Thread{
        public void run(){
            try{
                setSocket(ip,port);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}