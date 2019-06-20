package test.com.hrjs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NumSaveActivity extends AppCompatActivity implements Serializable {

    private Button numsendbt;
    private EditText numeditet;
    private Button msgsendbt;
    public String num;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    List<String> Array = new ArrayList<String>();

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ChildEventListener mChild;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numsave);

        numsendbt = (Button) findViewById(R.id.button1);
        numeditet = (EditText) findViewById(R.id.editText1);
        listView = (ListView) findViewById(R.id.listviewmsg1);
        msgsendbt = (Button) findViewById(R.id.buttonsend);

        initDatabase();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        listView.setAdapter(adapter);

        numsendbt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                num = numeditet.getText().toString();
                databaseReference.child("Phonenum").push().setValue(num);

            }
        });


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
                listView.setSelection(adapter.getCount() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
        msgsendbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NumSaveActivity.this, SendmsgActivity.class);
                /*Iterator<String> it = Array.iterator();*/

                intent.putExtra("Numsaved", (Serializable) Array);
                startActivity(intent);
                /*while(it.hasNext()) {
                    String str = it.next();
                    intent.putExtra("Numsaved", str);
                    startActivity(intent);
                    }*/
            }
        });
    }

    ;

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
}
