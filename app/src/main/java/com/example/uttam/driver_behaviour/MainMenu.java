package com.example.uttam.driver_behaviour;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// main menu
public class MainMenu extends AppCompatActivity{

    private Button btnNewTrip,btnHistory;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mRootReference;
    String Name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        btnNewTrip = (Button)findViewById(R.id.mainNewTrip);
        btnHistory = (Button)findViewById(R.id.mainHistory);
        Intent LoginIntent = getIntent();
        Name = LoginIntent.getStringExtra("userid");
        firebaseDatabase = FirebaseDatabase.getInstance();
        mRootReference = firebaseDatabase.getReference("Users").child(Name).child("Sessions");
        btnNewTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainMenu.this,MapsActivity.class);
                i.putExtra("userid",Name);
                startActivity(i);
            }
        });
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainMenu.this,ViewDatabase.class);
                i.putExtra("userid",Name);
                startActivity(i);
            }
        });
    }
}
