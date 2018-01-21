package com.example.uttam.driver_behaviour;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * To fetch the previous data (history) saved on the Firebase
 */

public class ViewDatabase extends AppCompatActivity {
    private static final String TAG = "ViewDatabase";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private ListView mListView;
    private List<String> list;
    ArrayAdapter adapter;
    int counter = 0;
    String UName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_database_layout);
        Intent i = getIntent();
        UName = i.getStringExtra("userid");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference("Users").child(UName).child("Sessions");
        mListView =(ListView)findViewById(R.id.list_View);
        list = new ArrayList<>();
        adapter = new ArrayAdapter(getApplicationContext(),R.layout.listitem,R.id.textView,list);
        mListView.setAdapter(adapter);

        /*myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                counter++;
                String s0 = dataSnapshot.child("0").getValue().toString();
                String s7 = dataSnapshot.child("7").getValue().toString();
                String s8 = dataSnapshot.child("8").getValue().toString();

                list.add("              Session: "+counter);
                list.add(s8);
                list.add(s0);
                list.add(s7);
                adapter.notifyDataSetChanged();
                //Toast.makeText(getApplicationContext(),list.toStringText(),Toast.LENGTH_SHORT).show();
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
        });
    }
}
