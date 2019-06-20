package test.com.hrjs;

import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SendmsgActivity<pn> extends AppCompatActivity {

    private GpsInfo gps;
    private String address;
    private String html = "";
    private Handler mHandler;
    private String a = "2";
    private Socket socket;
    private String name;
    private BufferedReader networkReader;
    private BufferedWriter networkWriter;
    private String ip = "192.168.43.32"; // IP
    private int port = 9999; // PORT번호
    private SoundPool sp;
    private int soundID;

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendmsg);

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

        /*PrintWriter out = new PrintWriter(networkWriter,true);
        String return_msg = a;
        out.println(return_msg);*/


        Serializable serial = getIntent().getSerializableExtra("Numsaved");
        ArrayList<pn> phoneNumList = (ArrayList<pn>) serial;
        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        soundID = sp.load(SendmsgActivity.this, R.raw.police_s,1);

        gps = new GpsInfo(this);

        if (gps.isGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            Geocoder gCoder = new Geocoder(this, Locale.getDefault());
            List<Address> addr = null;
            try {
                addr = gCoder.getFromLocation(latitude, longitude, 1);
                Address a = addr.get(0);

                for (int i = 0; i <= a.getMaxAddressLineIndex(); i++) {
                    address = a.getAddressLine(i);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addr != null) {
                if (addr.size() == 0) {
                    Toast.makeText(this, "주소정보없음", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            gps.showSettingsAlert();
        }

        sp.play(soundID,1,1,0,0,1);
        Iterator<String> it = (Iterator<String>) phoneNumList.iterator();
        while (it.hasNext()) {
            sendSMS(it.next(), "긴급상황발생!\r위치는 " + address + "입니다");
        }
    }

    private void sendSMS(String phoneNumber, String message) {

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
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
            Toast.makeText(SendmsgActivity.this, "Coming word: " + html,
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