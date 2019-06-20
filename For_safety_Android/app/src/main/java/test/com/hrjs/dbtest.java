package test.com.hrjs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class dbtest extends AppCompatActivity implements TMapView.OnClickListenerCallback {
    public static Context mContext = null;
    DatabaseReference locationDb = FirebaseDatabase.getInstance().getReference(); // Referencing the root of the database.
    // Referencing the "hrjs-c58dc" node under the root.
    DatabaseReference myRef = locationDb.getRoot();
    Double map1, map2;
    public Double letitude, longitude;
    ArrayList<TMapPoint> saveRoutePoint = new ArrayList<TMapPoint>();
    ArrayList<TMapPoint> uzi = new ArrayList<TMapPoint>();
    TMapView tmapview;
    int id = 0;
    private Button bt2;
    private String address;
    private TMapPoint center_p;
    private TMapPoint stop_p;
    private TMapData Tmapdata = new TMapData();
    Intent intent_select;

    @Override
    public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        Log.i("asd",String.valueOf(arrayList));
        Log.i("asd",String.valueOf(tMapPoint));
        stop_p = tMapPoint;
        Tmapdata.convertGpsToAddress(stop_p.getLatitude(), stop_p.getLongitude(), new TMapData.ConvertGPSToAddressListenerCallback() {
            @Override
            public void onConvertToGPSToAddress(String s) {
                address = s;
            }
        });
        return false;
    }

    @Override
    public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        return false;
    }

    public dbtest() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public dbtest(double letitude, double longitude) {
        this.letitude = letitude;
        this.longitude = longitude;
    }

    private void addMarker(double lat, double lng, String title) {
        saveRoutePoint.add(new TMapPoint(lat, lng));

    }

    public void showMarker() {
        for (int i = 0; i < saveRoutePoint.size(); i++) {
            TMapPoint point = new TMapPoint(saveRoutePoint.get(i).getLatitude(), saveRoutePoint.get(i).getLongitude());
            Log.d("js", String.valueOf(saveRoutePoint.get(i).getLatitude()));
            Log.d("js", String.valueOf(saveRoutePoint.get(i).getLongitude()));
            Log.d("js", String.valueOf(saveRoutePoint.get(i)));
            TMapMarkerItem item2 = new TMapMarkerItem();
            Bitmap bitmap = null;
            item2.setTMapPoint(point);

            item2.setVisible(item2.VISIBLE);
            //item1.setCalloutTitle(saveRoutePoint.get(i).getName());
            item2.setCanShowCallout(true);
            tmapview.addMarkerItem("m" + id, item2);
            id++;

        }
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbtest);

        // 모든 데이터삭제
        /*myRef.getDatabase().getReference().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid){
                Toast.makeText(dbtest.this," 삭제 성공", Toast.LENGTH_SHORT).show();
            }
        });*/
        tmapview = (TMapView) findViewById(R.id.map_view);
        //center_p = tmapview.getCenterPoint();
        bt2 = (Button)  findViewById(R.id.bt2);

        // Read from the database


        bt2.setOnClickListener(new View.OnClickListener() {
            // 현재 중앙 마커가 가리키고 있는 위치 좌표 정보를 넘김
            @Override
            public void onClick(View v) {
                intent_select = new Intent();
                intent_select.putExtra("input", address);
                intent_select.putExtra("lat", Double.toString(stop_p.getLatitude()));
                intent_select.putExtra("lon", Double.toString(stop_p.getLongitude()));
                setResult(2, intent_select);
                finish();
            }
        });
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String value = dataSnapshot.getValue(String.class);
                for(int a = 58; a<58; a++) {
                    try {
                        map1 = (Double) dataSnapshot.child("bell").child(String.valueOf(a)).child("위도").getValue();
                        map2 = (Double) dataSnapshot.child("bell").child(String.valueOf(a)).child("경도").getValue();
                        Log.i("Value is: ", String.valueOf(map1));

                        letitude = map1;
                        longitude = map2;
                        Log.i("Value is", String.valueOf(letitude) + String.valueOf(longitude));
                        //마커 생성
                        addMarker(letitude, longitude, "test");
                        showMarker();

                    } catch (NumberFormatException e) {
                        Log.i("error", "js");

                    }
                }
                for(int a = 300; a<380; a++) {
                    try {
                        map1 = (Double) dataSnapshot.child("cctv").child(String.valueOf(a)).child("위도").getValue();
                        map2 = (Double) dataSnapshot.child("cctv").child(String.valueOf(a)).child("경도").getValue();
                        Log.i("Value is: ", String.valueOf(map1));

                        letitude = map1;
                        longitude = map2;
                        Log.i("Value is", String.valueOf(letitude) + String.valueOf(longitude));
                        //마커 생성
                        addMarker(letitude, longitude, "test");
                        showMarker();

                    } catch (NumberFormatException e) {
                        Log.i("error", "js");

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Failed to read value.", error.toException());
            }
        });
    }
}
