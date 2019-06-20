package test.com.hrjs;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Heera on 2019-05-19.
 */

public class NaviActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, TMapGpsManager.onLocationChangedCallback {

    public static String left_check = "-1";
    public static String right_check = "-1";
    private static String mApiKey = "ee56383b-e2ca-4bbc-a2d7-01b57abe4dce";

    public String URI_RECEIVE_DISTANCE_INFO;   // 두 점 사이의 거리 정보를 요청하기 위한 URL
    public String URI_RECEIVE_ACCIDENT_INFO;   // 내 위치와 TAAS 사고 다발 지역과의 거리 정보를 요청하기 위한 URL

    ArrayList<TMapPoint> list;         // 네비게이션 경유 마커의 좌표를 저장하는 Array
    ArrayList<String> turn_list;     // 네비게이션 방향 정보를 저장하는 Array
    ArrayList<String> desc_list;     // 네비게이션 설명 정보를 저장하는 Array
    ArrayList<Double> line_list;     // 네비게이션 구간별 거리를 저장하는 Array

    public static Context mContext = null;
    private boolean m_bTrackingMode = true;
    private TMapGpsManager tmapgps = null;
    TMapView tmapview;
    GpsInfo info_gps;
    private TMapData Tmapdata = new TMapData();
    TMapPoint dangerous;   // 임의로 설정한 사고다발지역

    String dest_lat;     // 도착지 경도
    String dest_lon;     // 도착지 위도
    String dest_add;     // 도착지 주소 정보
    String d_distance;
    private TextView dest_info;        // 도착지 주소 정보
    private TextView showtime;       // 총 남은 시간
    private TextView showdistance;      // 총 남은 거리
    private TextView marker_distance;       // 가장 가까운 마커와의 남은 거리
    private TextToSpeech myTTS;
    private TextView currenttime;      // 사고 다발지역 알림
    private ImageView arrow;         // 방향 표시 화살표

    Double time;
    Double distance;        //총 남은 거리
    Double fdistance;     // 내 위치에 따라 실시간으로 계산되는 총 남은 거리
    Double ddistance=0.0;     // 현재까지 경유한 마커까지의 이동거리
    int hour,minute;
    int km,m;
    int point_index = 1;    // 마커 인덱스, 마커를 경유하면 그 다음 마커와 거리를 측정하기 위해 값을 증가 시킴
    int entrance = 0;     // 한번 마커과 거리를 측정한 후 다시 측정하지 않도록 하는 변수
    int compare;    // 현재 내 위치과 가장 가까운 마커와의 거리
    int acci;       // 내 현재 위치과 사고 다발 지역과의 거리
    Double speed;
    double myspeed;
    String lefttime;
    String leftdistance;
    String s;    // URL로 받아온 거리 정보
    String accident;   // URL로 받아온 사고 다발 지역과의 거리 정보

    long now = System.currentTimeMillis();
    DecimalFormat df;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLocationChange(Location location) {
        // 내 위치가 변할때마다 지도의 중심점을 내 현재 위치로 설정
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
        }

        // URL 만들기
        URI_RECEIVE_DISTANCE_INFO = make_url(String.valueOf(format(location.getLongitude())), String.valueOf(format(location.getLatitude())),String.valueOf(format(list.get(point_index).getLongitude())),String.valueOf(format(list.get(point_index).getLatitude())));
        URI_RECEIVE_ACCIDENT_INFO = make_url(String.valueOf(format(location.getLongitude())), String.valueOf(format(location.getLatitude())),String.valueOf(format(dangerous.getLongitude())),String.valueOf(format(dangerous.getLatitude())));
        Log.i("url:",String.valueOf(format(info_gps.getLongitude())));
        Log.i("url2:",String.valueOf(format(list.get(point_index).getLongitude())));

        try{
            // URL로 정보 받아오기
            s = url_connetion(URI_RECEIVE_DISTANCE_INFO);
            accident = url_connetion(URI_RECEIVE_ACCIDENT_INFO);
            Log.i("compare1:",s);

            // 변수에 거리 정보 INT 자료형으로 저장
            compare = Integer.parseInt(s);
            acci = Integer.parseInt(accident);
        } catch (NumberFormatException e){
            e.printStackTrace();
        } catch (RuntimeException e){
            e.printStackTrace();
        }

        Log.i("compare:",String.valueOf(compare));
        // 실시간으로 변하는 내 위치에 따라 목적지까지 총 남은 거리 계산
        fdistance = distance - (line_list.get(point_index) - compare) - ddistance;

        // 내 현재 위치와 가장 가까운 마커와의 거리 표시
        marker_distance.setText(String.valueOf(compare)+"m 후");

        // 내 위치와 가장 가까운 마커와의 거리가 50M 이하일 때, 그 경유지에서의 방향 정보와 네비게이션 정보를 음성으로 송출
        if(compare<=50){
            if (entrance == 0){
                myTTS.speak(desc_list.get(point_index), TextToSpeech.QUEUE_ADD, null);
                if (turn_list.get(point_index).equals("11")){
                    arrow.setImageDrawable(ContextCompat.getDrawable(NaviActivity.mContext,R.drawable.upward));
                    entrance = 1;
                }
                else if(turn_list.get(point_index).equals("12")){
                    arrow.setImageDrawable(ContextCompat.getDrawable(NaviActivity.mContext,R.drawable.back));
                    entrance = 1;
                }
                else if(turn_list.get(point_index).equals("13")){
                    arrow.setImageDrawable(ContextCompat.getDrawable(NaviActivity.mContext,R.drawable.forward));
                    entrance = 1;
                }
                else {
                    arrow.setImageDrawable(ContextCompat.getDrawable(NaviActivity.mContext,R.drawable.forward));
                    entrance = 1;
                }

            }
        }

        // 내 현재 위치와 가장 가까운 마커의 거리가 7M 이하일 때, 다음 마커와 거리를 계산하여 알림을 줄 수 있도록 마커 포인트 인덱스를 증가 시킴
        if (compare <= 7){

            for(int i=0; i<(point_index-1); i++){
                ddistance = ddistance+line_list.get(i);
            }
            point_index++;
            entrance = 0;
        }

        time = fdistance/speed;
        hour = (int)(Math.round(time)/3600.0);
        minute = (int)(Math.round(time)%3600.0/60.0);
        km = (int)(Math.round(fdistance)/1000.0);
        m = (int)(Math.round(fdistance)%1000.0/100.0);

        if(hour==0)
            lefttime = minute + "분";
        else
            lefttime = hour + "시간 " + minute + "분";
        if(km==0)
            leftdistance = m*100 + "m";
        else
            leftdistance = km + "." + m + "km";

        showtime.setText(lefttime);
        showdistance.setText(leftdistance);

        // 내 현재 위치와 사고 다발 지역 좌표와의 거리가 30M 이하일 때 사고 다발 지역 경고 알림 송출
        if (acci <= 30){
            myTTS.speak("전방 삼십 미터 앞 사고다발지역입니다", TextToSpeech.QUEUE_ADD, null);
            currenttime.setText("사고 다발 지역 근방");

        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);

        left_RetrieveRequestCon ret_l = new left_RetrieveRequestCon("dongguk-capstone","left_light");
        ret_l.start();
        right_RetrieveRequestCon ret_r = new right_RetrieveRequestCon("dongguk-capstone","right_light");
        ret_r.start();
        left_blink left = new left_blink();
        left.start();
        right_blink right = new right_blink();
        right.start();

        tmapview = (TMapView) findViewById(R.id.map_view3);

        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setCompassMode(true);
        tmapview.setIconVisibility(true);
        tmapview.setZoomLevel(15);
        tmapview.setTrackingMode(true);
        tmapview.setSightVisible(true);

        tmapgps = new TMapGpsManager(NaviActivity.this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);    //연결된 인터넷으로 위치 파악
        //tmapgps.setProvider(tmapgps.GPS_PROVIDER);     //GPS로 위치 파악
        tmapgps.OpenGps();
        tmapview.setSKTMapApiKey(mApiKey);

        dangerous = new TMapPoint(37.558235,127.000176);

        // 사고다발지역 표시
        TMapCircle tcircle = new TMapCircle();
        tcircle.setCenterPoint(dangerous);
        tcircle.setRadius(3);
        tcircle.setLineColor(Color.RED);
        tcircle.setAreaColor(Color.RED);
        tcircle.setCircleWidth(2);
        tcircle.setRadiusVisible(true);
        tmapview.addTMapCircle("dan",tcircle);

        myTTS = new TextToSpeech(this, this);
        arrow = (ImageView)findViewById(R.id.arrow);
        marker_distance = (TextView)findViewById(R.id.direct_info);
        df = new DecimalFormat("0.000000");

        info_gps = new GpsInfo(NaviActivity.this);

        mContext = this;

        Intent naviTointent = getIntent();
        dest_lat = naviTointent.getExtras().getString("dest_lat");
        dest_lon = naviTointent.getExtras().getString("dest_lon");
        dest_add = naviTointent.getExtras().getString("dest_address");
        list = (ArrayList<TMapPoint>) getIntent().getSerializableExtra("list");
        turn_list = (ArrayList<String>) getIntent().getSerializableExtra("turn_list");
        desc_list = (ArrayList<String>) getIntent().getSerializableExtra("desc_list");
        line_list = (ArrayList<Double>) getIntent().getSerializableExtra("line_list");

        TMapPoint dest_point = new TMapPoint(Double.parseDouble(dest_lat),Double.parseDouble(dest_lon));
        TMapPoint start_point = new TMapPoint(list.get(0).getLatitude(),list.get(0).getLongitude());

        // 지도에 경로 라인 표시
        Tmapdata.findPathData(start_point, dest_point, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tmapview.removeAllMarkerItem();
                tMapPolyLine.setLineColor(Color.BLUE);
                tMapPolyLine.setLineWidth(10);
                tMapPolyLine.setID("path");

                tmapview.addTMapPath(tMapPolyLine);
                tmapview.setTrackingMode(true);

            }
        });

        distance = naviTointent.getExtras().getDouble("totalDistance");
        fdistance = distance;
        Log.d("minig", String.valueOf(distance));
        speed = 15000.0/3600.0;
        time = fdistance/speed;
        hour = (int)(Math.round(time)/3600.0);
        minute = (int)(Math.round(time)%3600.0/60.0);
        km = (int)(Math.round(fdistance)/1000.0);
        m = (int)(Math.round(fdistance)%1000.0/100.0);
        myspeed = 0;

        dest_info = (TextView) findViewById(R.id.dest_info);
        showtime = (TextView) findViewById(R.id.totaltime);
        showdistance = (TextView) findViewById(R.id.totaldistance);
        currenttime = (TextView) findViewById(R.id.time);

        if(hour==0)
            lefttime = minute + "분";
        else
            lefttime = hour + "시간 " + minute + "분";
        if(km==0)
            leftdistance = m*100 + "m";
        else
            leftdistance = km + "." + m + "km";

        Log.i("whkvy1",dest_lat);
        Log.i("whkvy2",dest_lon);
        Log.i("whkvy3",String.valueOf(info_gps.getLongitude()));

        dest_info.setText(dest_add);

        Log.d("minig", String.valueOf(hour));
        Log.d("minig", String.valueOf(minute));
        Log.d("minig", String.valueOf(km));
        Log.d("minig", String.valueOf(m));

    }

    public void do_left(){
        Log.d("error", "left");
        left_RetrieveRequestCon ret_l = new left_RetrieveRequestCon("dongguk-capstone","left_light");
        ret_l.start();
    }

    public void do_right(){
        Log.d("error", "right");
        right_RetrieveRequestCon ret_r = new right_RetrieveRequestCon("dongguk-capstone","right_light");
        ret_r.start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            TextView left_light = (TextView) findViewById(R.id.left_light );
            switch (msg.what) {
                case 1:     // 메시지로 넘겨받은 파라미터, 이 값으로 어떤 처리를 할지 결정
                    String str = (String)msg.obj;
                    if(str.equals("0")){
                        left_light.setBackgroundColor(Color.WHITE);
                    }
                    if(str.equals("1")){
                        left_light.setBackgroundColor(Color.YELLOW);
                    }
                    if(str.equals("2")){
                        left_light.setBackgroundColor(Color.RED);
                    }
            }
        }
    };

    Handler handlerr = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            TextView right_light = (TextView) findViewById(R.id.right_light );
            switch (msg.what) {
                case 1:     // 메시지로 넘겨받은 파라미터, 이 값으로 어떤 처리를 할지 결정
                    String str = (String)msg.obj;
                    if(str.equals("0")){
                        right_light.setBackgroundColor(Color.WHITE);
                    }
                    if(str.equals("1")){
                        right_light.setBackgroundColor(Color.YELLOW);
                    }
                    if(str.equals("2")){
                        right_light.setBackgroundColor(Color.RED);
                    }
            }
        }
    };
    class left_blink extends Thread{
        @Override
        public void run(){
            int check = -1;
            while(true) {
                Log.d("heera","left = " + left_check);
                if (left_check.equals("0") && check != 0) {
                    handler.sendMessage(Message.obtain(handler, 1, "0"));
                    check = 0;
                }
                if (left_check.equals("1")) {
                    handler.sendMessage(Message.obtain(handler, 1, "1"));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendMessage(Message.obtain(handler, 1, "0"));
                    check = 1;
                }
                if (left_check.equals("2")) {
                    handler.sendMessage(Message.obtain(handler, 1, "2"));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendMessage(Message.obtain(handler, 1, "0"));
                    check = 2;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class right_blink extends Thread{
        @Override
        public void run(){
            int check = -1;
            while(true) {
                Log.d("heera","right = " + right_check);
                if (right_check.equals("0") && check != 0) {
                    handlerr.sendMessage(Message.obtain(handlerr, 1, "0"));
                    check = 0;
                }
                if (right_check.equals("1")) {
                    handlerr.sendMessage(Message.obtain(handlerr, 1, "1"));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handlerr.sendMessage(Message.obtain(handlerr, 1, "0"));
                    check = 1;
                }
                if (right_check.equals("2")) {
                    handlerr.sendMessage(Message.obtain(handlerr, 1, "2"));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handlerr.sendMessage(Message.obtain(handlerr, 1, "0"));
                    check = 2;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class MobiusConfig{
        public final static String MOBIUS_ROOT_URL = "http://203.253.128.151:7579/mobius-yt";
    }

    public interface IReceived{
        void getResponseBody(String msg);
    }

    class RetrieveRequest extends Thread {

        private final Logger LOG = Logger.getLogger(RetrieveRequest.class.getName());

        private IReceived receiver;

        private String ae_name = ""; //change to your ae name
        private String container_name = ""; //change to your sensing data container name

        public RetrieveRequest(String aeName, String containerName){
            this.ae_name = aeName;
            this.container_name = containerName;
        }

        public void setReceiver(IReceived hanlder){
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try{
                StringBuilder sb = new StringBuilder();
                sb.append(MobiusConfig.MOBIUS_ROOT_URL).append("/")
                        .append(ae_name).append("/")
                        .append(container_name).append("/")
                        .append("latest");

                URL mUrl = new URL(sb.toString());

                HttpURLConnection conn = (HttpURLConnection)mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", "SOrigin");
                conn.setRequestProperty("nmtype", "long");

                conn.connect();

                String strResp = "";

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String strLine;
                while((strLine = in.readLine()) != null) {
                    strResp += strLine;
                }

                if(receiver != null){
                    receiver.getResponseBody(strResp);
                }
                conn.disconnect();

            }catch(Exception exp){
                LOG.log(Level.WARNING, exp.getMessage());
            }
        }
    }

    class left_RetrieveRequestCon extends Thread {

        private final Logger LOG = Logger.getLogger(RetrieveRequest.class.getName());

        private IReceived receiver;

        private String ae_name = ""; //change to your ae name
        private String container_name = ""; //change to your sensing data container name
        int start;
        int finish;

        public left_RetrieveRequestCon(String aeName, String containerName){
            this.ae_name = aeName;
            this.container_name = containerName;
        }

        public void setReceiver(IReceived hanlder){
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try{
                while(true){
                    String con;
                    int con_check = -1;
                    StringBuilder sb = new StringBuilder();
                    sb.append(MobiusConfig.MOBIUS_ROOT_URL).append("/")
                            .append("dongguk-capstone").append("/")
                            .append("left_light").append("/")
                            .append("latest");
                    URL mUrl = new URL(sb.toString());
                    HttpURLConnection conn = (HttpURLConnection)mUrl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setDoOutput(false);
                    conn.setRequestProperty("Accept", "application/xml");
                    conn.setRequestProperty("X-M2M-RI", "12345");
                    conn.setRequestProperty("X-M2M-Origin", "SOrigin");
                    conn.setRequestProperty("nmtype", "long");
                    conn.connect();
                    String strResp = "";
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        strResp += strLine;
                    }
                    // 받아온 데이터에서 원하는 값만을 뽑아올 수 있도록 한다.
                    start = strResp.indexOf("<con>");
                    finish = strResp.indexOf("</con>");
                    con = strResp.substring(start + 5, finish);
                    Log.d("heera","left_check = " + con);
                    if( con.equals("0") && con_check != 0){
                        NaviActivity.left_check = "0";
                        con_check = 0;
                    }
                    if( con.equals("1") && con_check != 1){
                        NaviActivity.left_check = "1";
                        con_check = 1;
                    }
                    if( con.equals("2") && con_check != 2){
                        NaviActivity.left_check = "2";
                        con_check = 2;
                    }
                    Thread.sleep(1000);
                    conn.disconnect();
                }
            }catch(Exception exp){
                LOG.log(Level.WARNING, exp.getMessage());
                do_left();
                Log.d("error","doleft");
            }
        }
    }

    class right_RetrieveRequestCon extends Thread {

        private final Logger LOG = Logger.getLogger(RetrieveRequest.class.getName());

        private IReceived receiver;

        private String ae_name = ""; //change to your ae name
        private String container_name = ""; //change to your sensing data container name
        int start;
        int finish;

        public right_RetrieveRequestCon(String aeName, String containerName){
            this.ae_name = aeName;
            this.container_name = containerName;
        }

        public void setReceiver(IReceived hanlder){
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try{
                while(true){
                    String con;
                    int con_check = -1;
                    StringBuilder sb = new StringBuilder();
                    sb.append(MobiusConfig.MOBIUS_ROOT_URL).append("/")
                            .append(ae_name).append("/")
                            .append(container_name).append("/")
                            .append("latest");
                    URL mUrl = new URL(sb.toString());
                    HttpURLConnection conn = (HttpURLConnection)mUrl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setDoOutput(false);
                    conn.setRequestProperty("Accept", "application/xml");
                    conn.setRequestProperty("X-M2M-RI", "12345");
                    conn.setRequestProperty("X-M2M-Origin", "SOrigin");
                    conn.setRequestProperty("nmtype", "long");
                    conn.connect();
                    String strResp = "";
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        strResp += strLine;
                    }
                    // 받아온 데이터에서 원하는 값만을 뽑아올 수 있도록 한다.
                    start = strResp.indexOf("<con>");
                    finish = strResp.indexOf("</con>");
                    con = strResp.substring(start + 5, finish);
                    Log.d("heera","right_check = " + con);
                    if( con.equals("0") && con_check!=0){
                        NaviActivity.right_check = "0";
                        con_check = 0;
                    }
                    if( con.equals("1") && con_check!=1){
                        NaviActivity.right_check = "1";
                        con_check = 1;
                    }
                    if( con.equals("2") && con_check != 2){
                        NaviActivity.right_check = "2";
                        con_check = 2;
                    }
                    Thread.sleep(1000);
                    conn.disconnect();
                }
            }catch(Exception exp){
                LOG.log(Level.WARNING, exp.getMessage());
                do_right();
                Log.d("error","doright");
            }
        }
    }
    public String make_url(String start_lon, String start_lat, String destination_lon, String destination_lat){
        String url = "https://apis.skplanetx.com/tmap/routes/distance?startX="+start_lon+"&startY="+start_lat+"&endX="+destination_lon+"&reqCoordType=WGS84GEO&endY="+destination_lat+"&callback=&version=1&format=json&appKey=" + mApiKey;

        return url;
    }

    public String url_connetion(String input_url){

        try
        {
            URL url = new URL(input_url);
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpUrlConnection = (HttpURLConnection)urlConnection;

            int responseCode = httpUrlConnection.getResponseCode();

            Log.i("tkdxo:",String.valueOf(responseCode));
            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                InputStream inputStream = httpUrlConnection.getInputStream();
                String str = convertStreamToString(inputStream);


                try{
                    JSONObject root = new JSONObject(str);
                    Log.i("strr1:",root.getString("distanceInfo"));
                    Log.i("strr2:",root.getJSONObject("distanceInfo").getString("distance"));
                    d_distance = root.getJSONObject("distanceInfo").getString("distance");
                    Log.i("strr3:",d_distance);

                }catch (JSONException e){
                    Log.e("error", "JSONException");
                }
                Log.i("strr:", str);
            }
        } catch (MalformedURLException e) {
            Log.e("error", "url fault");
            e.printStackTrace();

        } catch (IOException e) {
            Log.e("error", "I/O exception");
            e.printStackTrace();
        }

        return d_distance;

    }

    public static String convertStreamToString(InputStream inputStream) {

        if (inputStream != null)
        {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];

            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                int n;

                while ((n=reader.read(buffer))!=-1) {
                    writer.write(buffer, 0, n);

                }

                return writer.toString();

            } catch (UnsupportedEncodingException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();

            }
        }

        return null;

    }

    @Override
    public void onInit(int status) {
        // 처음 액티비티에 진입했을 때 안내 멘트 송출
        String myText1 = "길 안내를 시작합니다.";
        String myText2 = "";
        String myText3 = "";
        String myText4 = "안전운전 하세요.";
        if(hour==0)
            myText3 = "총 소요시간은 " + minute + "분 입니다.";
        else
            myText3 = "총 소요시간은 " + hour + "시간" + minute + "분 입니다.";
        if(km==0)
            myText2 = "목적지까지의 거리는 " + m*100 + "미터";
        else
            myText2 = "목적지까지의 거리는 " + km + "쩜" + m + "킬로미터";
        myTTS.speak(myText1, TextToSpeech.QUEUE_FLUSH, null);
        myTTS.speak(myText2, TextToSpeech.QUEUE_ADD, null);
        myTTS.speak(myText3, TextToSpeech.QUEUE_ADD, null);
        myTTS.speak(myText4, TextToSpeech.QUEUE_ADD, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myTTS.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public String format(double number){
        return df.format(number);
    }
}
