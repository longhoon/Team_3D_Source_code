package test.com.hrjs;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPOIItem;

import java.util.ArrayList;

/**
 * Created by Heera on 2019-05-19.
 */

public class SearchLocationActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback{
    private TMapData Tmapdata = new TMapData();
    private TMapGpsManager tmapgps2 = null;

    private EditText input_location;   // 검색창
    private String location;       // 검색된 문자열 저장
    private Button location_bt;     // 검색창에 입력된 내용을 검색
    private Button current_loc;     // 현재위치 받아오기
    private Button select_loc;      // 지도에서 위치 지정
    private String address;
    String start_lat;
    String start_lon;
    Intent intent_return;
    ListView listView = null;

    private GpsInfo start_dot;

    @Override
    public void onLocationChange(Location location) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 0:
                String address = data.getStringExtra("input");
                String lat = data.getStringExtra("lat");
                String  lon = data.getStringExtra("lon");

                intent_return = new Intent();
                intent_return.putExtra("input", address);
                intent_return.putExtra("lat", lat);
                intent_return.putExtra("lon", lon);
                setResult(2, intent_return);
                finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchlocation);

        tmapgps2 = new TMapGpsManager(SearchLocationActivity.this);
        tmapgps2.setMinTime(1000);
        tmapgps2.setMinDistance(5);
        //tmapgps2.setProvider(tmapgps2.NETWORK_PROVIDER);    //연결된 인터넷으로 위치 파악
        tmapgps2.setProvider(tmapgps2.GPS_PROVIDER);     //GPS로 위치 파악
        tmapgps2.OpenGps();

        final ListViewAdapter adapter;
        adapter = new ListViewAdapter();

        listView = (ListView) findViewById(R.id.listview1);
        listView.setAdapter(adapter);

        current_loc = (Button) findViewById(R.id.current_loc);
        current_loc.setOnClickListener(new View.OnClickListener() {
            // 내 위치로 설정 버튼, 현재 위치를 받아와 출발지나 목적지로 설정 가능
            @Override
            public void onClick(View v) {
                start_dot = new GpsInfo(SearchLocationActivity.this);
                if (start_dot.isGetLocation()) {
                    start_lat = Double.toString(start_dot.getLatitude());
                    start_lon = Double.toString(start_dot.getLongitude());


                    if (start_lat != "0.0" || start_lon != "0.0") {

                        Tmapdata.convertGpsToAddress(start_dot.getLatitude(), start_dot.getLongitude(), new TMapData.ConvertGPSToAddressListenerCallback() {
                            @Override
                            public void onConvertToGPSToAddress(String s) {
                                address = s;
                                Log.i("eee:" + start_lat, start_lon);

                            }
                        });

                    }
                    if (address != null) {
                        Log.i("eee:" + start_lat, start_lon);
                        intent_return = new Intent();
                        intent_return.putExtra("input", address);
                        Toast.makeText(SearchLocationActivity.this, address, Toast.LENGTH_SHORT).show();
                        intent_return.putExtra("lat", start_lat);
                        intent_return.putExtra("lon", start_lon);
                        setResult(1, intent_return);
                        finish();

                    }

                } else {
                    start_dot.showSettingsAlert();
                }
            }
        });

        select_loc = (Button) findViewById(R.id.select_loc);
        select_loc.setOnClickListener(new View.OnClickListener() {
            // 지도에서 지정 버튼, 지도를 움직여 가운데 마커에 해당하는 위치를 출발지나 목적지로 설정 가능
            @Override
            public void onClick(View v) {
                Intent intent_map = new Intent(SearchLocationActivity.this, SelectMapLocationActivity.class);
                startActivityForResult(intent_map,0);
            }
        });

        input_location = (EditText)findViewById(R.id.search_location);

        location_bt = (Button)findViewById(R.id.location_bt);
        location_bt.setOnClickListener(new View.OnClickListener() {
            // TMAP에서 제공하는 POI에 검색창에 입력한 주소나 장소로 검색하여 받아온 정보를 LISTVIEW에 삽입
            @Override
            public void onClick(View v) {
                adapter.clearItem();
                location = input_location.getText().toString();
                Tmapdata.findAllPOI(location, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {
                        for (int i = 0; i < arrayList.size(); i++) {
                            TMapPOIItem item = arrayList.get(i);
                            Log.d("주소로 찾기", "POI Name: " + item.getPOIName().toString() + ", " + "Address: " + item.getPOIAddress().replace("null", "") + ", " + "Point: " + item.getPOIPoint().toString());

                            adapter.addItem(item.getPOIName(), item.getPOIAddress(), item.getPOIPoint().getLatitude(), item.getPOIPoint().getLongitude());

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.nofity();
                            }
                        });
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            // LISTVIEW에서 클릭된 ROW에 해당하는 주소 좌표 정보를 출발지나 목적지로 설정 가능
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListViewItem item = (ListViewItem) parent.getItemAtPosition(position);

                String titleStr = item.getTitle();
                String latStr = String.valueOf(item.getLat());
                String lonStr = String.valueOf(item.getLon());

                intent_return = new Intent();
                intent_return.putExtra("input", titleStr);
                intent_return.putExtra("lat", latStr);
                intent_return.putExtra("lon", lonStr);
                setResult(2, intent_return);
                finish();
            }
        });
    }
}
