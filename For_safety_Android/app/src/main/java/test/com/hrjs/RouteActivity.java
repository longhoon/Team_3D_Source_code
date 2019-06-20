package test.com.hrjs;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Heera on 2019-05-19.
 */

public class RouteActivity extends AppCompatActivity {
    public static Context mContext = null;

    private TMapGpsManager tmapgps = null;
    private TMapData Tmapdata = new TMapData();
    TMapView tmapview;
    LocationManager mLM;
    String mProvider = LocationManager.NETWORK_PROVIDER;
    private static String mApiKey = "ee56383b-e2ca-4bbc-a2d7-01b57abe4dce";

    private Button guide;
    ListView listView;
    TMapPolyLine tMapPolyLine;
    private TextView startname;
    private TextView destname;
    private TextView showdistance; //총거리
    private TextView showtime; //예상 소요 시간
    TMapPoint start_point = null;
    TMapPoint dest_point = null;
    TMapPoint stop_point = null;
    String dest_lat;
    String dest_lon;
    String start_lat;
    String start_lon;
    String stop_lat;
    String stop_lon;
    String mdes;
    int mturn;
    String start_add;
    String dest_add;

    //시간, 거리 계산하기 위한 변수 선언
    Double Ddistance;
    Double time;
    Double speed;
    int hour,minute;
    int km,m;
    String lefttime;
    String leftdistance;

    Intent intentTonavi;

    ArrayList<String> saveDescription = new ArrayList<String>();
    ArrayList<String> saveTurn = new ArrayList<String>();
    ArrayList<Double> saveLineString = new ArrayList<Double>();
    ArrayList<TMapPoint> saveRoutePoint = new ArrayList<TMapPoint>();
    ArrayList<TMapPoint> stopPoint = new ArrayList<TMapPoint>();

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    /***
     * HTTP CLIENT
     ***/
    public String URI_RECEIVE_USER_ID = "https://api2.sktelecom.com/tmap/routes/pedestrian?version=1&format=json&appKey=" + mApiKey+"&";
    public static HttpClient httpclient;
    HttpPost httppost;
    private JSONArray countriesArray;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showroute);

        start();
        mContext = this;
        listView = (ListView)findViewById(R.id.navilist);
        guide = (Button)findViewById(R.id.guide);
        intentTonavi = new Intent(RouteActivity.this, NaviActivity.class);

        tmapview = (TMapView) findViewById(R.id.map_view2);


        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //API키가 확인되었을 때 지도 출력
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

        //MainActivity에서 intent로 값 받아오기
        Intent intent = getIntent();
        start_lat= intent.getExtras().getString("start_lat");
        start_lon= intent.getExtras().getString("start_lon");
        dest_lat= intent.getExtras().getString("dest_lat");
        dest_lon= intent.getExtras().getString("dest_lon");
        stop_lat= intent.getExtras().getString("stop_lat");
        stop_lon= intent.getExtras().getString("stop_lon");
        start_point = new TMapPoint(Double.parseDouble(start_lat),Double.parseDouble(start_lon));
        dest_point = new TMapPoint(Double.parseDouble(dest_lat),Double.parseDouble(dest_lon));
        stop_point = new TMapPoint(Double.parseDouble(stop_lat),Double.parseDouble(stop_lon));
        stopPoint.add(stop_point);

        startname = (TextView) findViewById(R.id.startname);
        destname = (TextView) findViewById(R.id.destname);
        showdistance = (TextView) findViewById(R.id.showdistance);
        showtime = (TextView) findViewById(R.id.showtime);

        start_add = intent.getExtras().getString("start_address"); //출발지 주소
        Log.d("js",start_add);
        dest_add = intent.getExtras().getString("dest_address"); //도착지 주소
        Log.d("js",dest_add);

        startname.setText(start_add);
        destname.setText(dest_add);

        showRoute(); //출발지부터 목적지까지 길 표시해주는 함수

        //출발지부터 목적지까지 걸리는 시간과 거리 계산
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                speed = 15000.0/3600.0; //자전거의 평균 속도 계산
                time = Ddistance/speed;

                hour = (int)(Math.round(time)/3600.0);
                minute = (int)(Math.round(time)%3600.0/60.0);
                km = (int)(Math.round(Ddistance)/1000.0);
                m = (int)(Math.round(Ddistance)%1000.0/100.0);

                if(hour==0)
                    lefttime = minute + "분";
                else
                    lefttime = hour + "시간 " + minute + "분";
                if(km==0)
                    leftdistance = m*100 + "m";
                else
                    leftdistance = km + "." + m + "km";

                showdistance.setText(leftdistance);
                showtime.setText(lefttime);
            }
        }, 3000);
    }

    public void showRoute(){

        //경로 그리기
        Tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, start_point, dest_point, stopPoint, 0, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                //tmapview.removeAllMarkerItem();
                tMapPolyLine.setLineColor(Color.BLUE);
                tMapPolyLine.setLineWidth(10);
                tMapPolyLine.setID("path");
                Ddistance = tMapPolyLine.getDistance();
                tmapview.addTMapPath(tMapPolyLine);
                tmapview.setTrackingMode(true);
                setMyLocation(start_point.getLatitude(),start_point.getLongitude());
            }
        });

        //url통신으로 설정한 출발지, 목적지의 위도,경도값으로 길찾기
        Send_Login_Info(String.valueOf(start_point.getLongitude()), String.valueOf(start_point.getLatitude()), String.valueOf(dest_point.getLongitude()), String.valueOf(dest_point.getLatitude()), "heera", "juseok");

    }

    public void Send_Login_Info(String _start_x, String _start_y, String _end_x, String _end_y, String _start_name, String _end_name) {
        Log.i("psj", "js : 00001");
        RequestRoad requestlogin = new RequestRoad();
        requestlogin.execute(URI_RECEIVE_USER_ID, _start_x, _start_y, _end_x, _end_y, _start_name, _end_name);

    }

    public class RequestRoad extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            String uri = params[0];
            String start_x = params[1];
            String start_y = params[2];
            String end_x = params[3];
            String end_y = params[4];
            String start_name = params[5];
            String end_name = params[6];
            String result = "";
            String pro = "";
            String proo = "";
            int turn ;
            String prox = "";
            String proy = "";
            String temp = "";
            Double linestring=0.0;
            Double dx=0.0;
            Double dy=0.0;


            /*** Add data to send ***/
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("startX", start_x));
            nameValuePairs.add(new BasicNameValuePair("startY", start_y));
            nameValuePairs.add(new BasicNameValuePair("endX", end_x));
            nameValuePairs.add(new BasicNameValuePair("endY", end_y));
            nameValuePairs.add(new BasicNameValuePair("startName", start_name));
            nameValuePairs.add(new BasicNameValuePair("endName", end_name));
            StringBuilder builder = new StringBuilder();

            /*** Send post message ***/
            httppost = new HttpPost(uri);
            try {
                UrlEncodedFormEntity urlendoeformentity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
                httppost.setEntity(urlendoeformentity);
                HttpResponse response = httpclient.execute(httppost);
                StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));

                    String line;
                    int j = 0;

                    while ((line = reader.readLine()) != null) {
                        Log.i("psj", "server result1 " + line);
                        if (j > 0) {
                            temp = temp + "\n";
                        }
                        temp = temp + line;
                        j++;
                    }
                    Log.i("psj", "server result2 " + temp);
                    builder.append(temp);

                }


            } catch (Exception e) {
                Log.i("psj", "Exception try1:" + e.getStackTrace());
                e.printStackTrace();
            }

             /* -- Treat JSON data to string array  --*/
            try {
                JSONObject root = new JSONObject(builder.toString());
                countriesArray = root.getJSONArray("features");
                       /* -- No data --*/

                if (countriesArray.length() < 1) {
                    return "FALSE";
                }
                Log.i("js","test"+countriesArray);

                  /* -- Save data --*/
                for (int i = 0; i < countriesArray.length(); i++) {

                    JSONObject JObject = countriesArray.getJSONObject(i);

                    //JSON형식의 경유지들의 위도 경도값 받아오기
                    prox = JObject.getJSONObject("geometry").getJSONArray("coordinates").getString(0);
                    proy = JObject.getJSONObject("geometry").getJSONArray("coordinates").getString(1);
                    pro = JObject.getJSONObject("geometry").getString("type");


                    Log.i("js:","testx"+prox);
                    Log.i("js:","testy"+proy);



                    try{
                        dx = Double.parseDouble(prox);
                        dy = Double.parseDouble(proy);

                        //마커 생성
                        /*addMarker(dy,dx,proo);
                        showMarker();*/

                    } catch (NumberFormatException e){

                    }

                    if(pro.equals("Point")){ //방향 종류 가지고 오기(우회전, 좌회전 등)
                        turn = JObject.getJSONObject("properties").getInt("turnType");
                        String navidesc = JObject.getJSONObject("properties").getString("description");
                        Log.i("ghkrdls:",String.valueOf(turn));

                        saveArray(turn, navidesc);

                    }

                    if(pro.equals("LineString")){ //거리값 받아오기
                        linestring = JObject.getJSONObject("properties").getDouble("distance");
                        Log.i("line:",String.valueOf(linestring));
                        saveLineString.add(linestring);

                    }
                }


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //return "true";
            if(result.equals("1"))
            {
                return "TRUE";
            }
            else{
                return "FALSE";
            }

        }

        protected void onPostExecute(String result) {
            Log.i("psj", "js : login 00001 ttt"+result);

            NaviListViewAdapter adapter;

            adapter = new NaviListViewAdapter();

            listView = (ListView)findViewById(R.id.navilist);
            listView.setAdapter(adapter);
            adapter.clearItem();

            //방향종류에 따라 화살표 이미지와 남은 거리 값 띄우기
            for(int i=0; i<saveTurn.size(); i++){
                Log.i("xjs:",String.valueOf(saveTurn.get(i)));
                if (saveTurn.get(i).equals("11")){
                    adapter.addItem(ContextCompat.getDrawable(RouteActivity.mContext,R.drawable.upward), saveDescription.get(i));
                }
                else if(saveTurn.get(i).equals("12") || saveTurn.get(i).equals("212")){
                    adapter.addItem(ContextCompat.getDrawable(RouteActivity.mContext,R.drawable.back), saveDescription.get(i));
                }
                else if(saveTurn.get(i).equals("13") || saveTurn.get(i).equals("213")){
                    adapter.addItem(ContextCompat.getDrawable(RouteActivity.mContext,R.drawable.forward), saveDescription.get(i));
                }
                else {
                    adapter.addItem(ContextCompat.getDrawable(RouteActivity.mContext,R.drawable.forward), saveDescription.get(i));
                }

            }



        }
    }

    private void addMarker(double lat, double lng, String title) {
        saveRoutePoint.add(new TMapPoint(lat,lng));

    }

    public void showMarker() {
        for(int i=0;i<saveRoutePoint.size();i++){
            TMapPoint point = new TMapPoint(saveRoutePoint.get(i).getLatitude(),saveRoutePoint.get(i).getLongitude());
            Log.d("js", String.valueOf(saveRoutePoint.get(i).getLatitude()));
            Log.d("js", String.valueOf(saveRoutePoint.get(i).getLongitude()));
            Log.d("js", String.valueOf(saveRoutePoint.get(i)));
            TMapMarkerItem item1 = new TMapMarkerItem();
            Bitmap bitmap = null;
            item1.setTMapPoint(point);

            item1.setVisible(item1.VISIBLE);
            //item1.setCalloutTitle(saveRoutePoint.get(i).getName());
            item1.setCanShowCallout(true);
            tmapview.addMarkerItem("m" + id, item1);
            id++;

        }
    }

    int id = 0;

    boolean isInitialized = false;

    //지도 초기 생성 값 지정
    private void setupMap() {
        isInitialized = true;
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);
        tmapview.setCompassMode(true);
        tmapview.setIconVisibility(true);
        tmapview.setZoomLevel(15);
        tmapview.setTrackingMode(true);
        tmapview.setSightVisible(true);

        tmapgps = new TMapGpsManager(RouteActivity.this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);    //연결된 인터넷으로 위치 파악
        //tmapgps.setProvider(tmapgps.GPS_PROVIDER);     //GPS로 위치 파악
        tmapgps.OpenGps();

        if (cacheLocation != null) {
            moveMap(cacheLocation.getLatitude(), cacheLocation.getLongitude());
            setMyLocation(cacheLocation.getLatitude(), cacheLocation.getLongitude());
        }

    }

    public void start() {
        httpclient = new DefaultHttpClient();
        /***  time out  ***/
        httpclient.getParams().setParameter("http.protocol.expect-continue", false);
        httpclient.getParams().setParameter("http.connection.timeout", 10000);
        httpclient.getParams().setParameter("http.socket.timeout", 10000);
        Log.i("psj", "js : 00002");

    }


    @Override
    //activity 시작시 네트워크 연결 확인
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
    }

    Location cacheLocation = null;

    private void moveMap(double lat, double lng) {
        tmapview.setCenterPoint(lng, lat);
    }

    private void setMyLocation(double lat, double lng) {
        tmapview.setLocationPoint(lng, lat);

    }

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


    public void saveArray(int turn, String navidesc){
        mdes = navidesc;
        mturn = turn;
        saveTurn.add(String.valueOf(mturn));
        saveDescription.add(mdes);
    }
}
