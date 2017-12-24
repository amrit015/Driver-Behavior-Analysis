package com.example.uttam.driver_behaviour;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * user log in page, checking user on database
 */

public class UserLoginPageActivity extends AppCompatActivity implements  View.OnClickListener{

    private EditText mEmail,mPassword;
    private Button btnLogin;
    FirebaseDatabase database;
    DatabaseReference LoginRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login_page);

        database = FirebaseDatabase.getInstance();
        LoginRef = database.getReference("Users");
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button)findViewById(R.id.logIn);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == btnLogin) {
            // logging users
           Login();
        }
    }


    public void Login() {
        // getting users info
        final String userid = mEmail.getText().toString();
        final String password = mPassword.getText().toString();

        // checking users
        if (userid.isEmpty()) {
            mEmail.setError("Username cannot be blank");
        } else if (password.isEmpty()) {
            mPassword.setError("Password cannot be blank");
        } else {

            // checking if users exists on database
            LoginRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(userid)) {
                        String pass = dataSnapshot.child(userid).child("Password").getValue().toString();
                        String UserN = dataSnapshot.child(userid).child("UserName").getValue().toString();

                        if (password.equals(pass)) {
                            mPassword.setError(null);
                            Toast.makeText(getApplicationContext(),"Correct",Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(UserLoginPageActivity.this,MainMenu.class);
                            i.putExtra("userid",UserN);
                            startActivity(i);
                            LoginRef.removeEventListener(this);
                        } else {
                            mPassword.setError("Enter Correct Password");
                        }
                    } else {
                        mEmail.setError("Enter Correct Email");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}

