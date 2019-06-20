package test.com.hrjs;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

/**
 * Created by Heera on 2019-05-19.
 */

public class SelectMapLocationActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {
    private boolean m_bTrackingMode = true;

    private Context mContext = null;
    private TMapGpsManager tmapgps = null;
    private TMapData Tmapdata = new TMapData();
    private TMapView tmapview = null;
    private TMapPoint center_p;     // 마커가 위치하는 중앙점

    private TextView txt1;    // 중앙 좌표가 변함에 따라 그 주소를 표시
    private String address;
    private Button bt1;
    Intent intent_select;

    @Override
    public void onLocationChange(Location location) {
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window win = getWindow();
        win.setContentView(R.layout.activity_selectmaplocation);

        mContext = this;


        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.map_view2);
        txt1 = (TextView)findViewById(R.id.txt1);
        bt1 = (Button)findViewById(R.id.bt1);

        tmapview = new TMapView(this);
        linearLayout.addView(tmapview);

        tmapview.setCompassMode(true);
        tmapview.setIconVisibility(true);
        tmapview.setZoomLevel(15);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);
        tmapgps = new TMapGpsManager(SelectMapLocationActivity.this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);

        //tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);    //연결된 인터넷으로 위치 파악
        tmapgps.setProvider(tmapgps.GPS_PROVIDER);     //GPS로 위치 파악
        tmapgps.OpenGps();
        tmapview.setTrackingMode(true);
        tmapview.setSightVisible(false);
        tmapview.setIconVisibility(false);
        tmapview.setCompassMode(false);
        center_p = tmapview.getCenterPoint();

        // 화면 중앙에 마커를 고정
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linear = (LinearLayout)inflater.inflate(R.layout.fixed_mark,null);

        LinearLayout.LayoutParams paramlinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);
        win.addContentView(linear, paramlinear);


        tmapview.setOnTouchListener(new View.OnTouchListener(){
            // 지도가 터치되어 중앙 좌표 값이 변할때마다 그 좌표의 주소 값을 표시
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                center_p = tmapview.getCenterPoint();
                Log.i("TOUCH:",center_p.toString());
                Tmapdata.convertGpsToAddress(center_p.getLatitude(), center_p.getLongitude(), new TMapData.ConvertGPSToAddressListenerCallback() {
                    @Override
                    public void onConvertToGPSToAddress(String s) {
                        address = s;

                    }

                });

                if(address != null){
                    txt1.setText(address);

                }
                return false;
            }



        });

        bt1.setOnClickListener(new View.OnClickListener() {
            // 현재 중앙 마커가 가리키고 있는 위치 좌표 정보를 넘김
            @Override
            public void onClick(View v) {
                intent_select = new Intent();
                intent_select.putExtra("input", address);
                intent_select.putExtra("lat", Double.toString(center_p.getLatitude()));
                intent_select.putExtra("lon", Double.toString(center_p.getLongitude()));
                setResult(1, intent_select);
                finish();

            }
        });
    }
    boolean isInitialized = false;

}
