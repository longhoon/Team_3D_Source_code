package test.com.hrjs;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class firebaseDB extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback{

    ListView list_excel;
    boolean isInitialized = false;
    private TMapGpsManager tmapgps = null;
    Location cacheLocation = null;
    private boolean m_bTrackingMode = true;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<TMapPoint> saveCCTVPoint = new ArrayList<TMapPoint>();
    ArrayList<TMapMarkerItem> tItem = new ArrayList<TMapMarkerItem>();
    TMapView tmapview;
    private static String mApiKey = "ee56383b-e2ca-4bbc-a2d7-01b57abe4dce"; //Tmap API 키 설정


    @Override
    public void onLocationChange(Location location) {
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        tmapview = (TMapView) findViewById(R.id.map_view);

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

    }
    //지도 초기 값 설정
    private void setupMap() {
        isInitialized = true;
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setCompassMode(true);
        tmapview.setIconVisibility(true);
        tmapview.setZoomLevel(15);
        tmapview.setTrackingMode(true);
        tmapview.setSightVisible(true);

        tmapgps = new TMapGpsManager(firebaseDB.this);
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
    //지도 중심 설정
    private void moveMap(double lat, double lng) {
        tmapview.setCenterPoint(lng, lat);
    }

    private void setMyLocation(double lat, double lng) {
        tmapview.setLocationPoint(lng, lat);
        tmapview.setIconVisibility(true);
    }

}