package com.example.uttam.driver_behaviour;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Uttam on 11/14/17.
 */

public class UserSignUp extends AppCompatActivity implements  View.OnClickListener{
    private static final String TAG = "UserSignUpPage";
    FirebaseDatabase database;
    DatabaseReference databaseReference,RegistrationChildRef;
    private EditText mEmail,mPassword,Username;
    private Button btnSignUp;
    boolean flag;
    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_signup);
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Users");
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        Username = (EditText)findViewById(R.id.username);
        btnSignUp = (Button)findViewById(R.id.signUp);
        btnSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == btnSignUp) {
            registerUser();
        }
    }

    private void registerUser() {
        email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        final String UserName = Username.getText().toString();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(UserName)){
                    flag = true;
                }
                else{
                    flag = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (flag) {
            Username.setError("User name is already in use");
            Username.requestFocus();
        } else if(!validateEmail(email)) {
            mEmail.setError("Enter Valid Email");
            mEmail.requestFocus();
        } else if ((password.length() < 6) || (password.length() > 15)) {
            mPassword.setError("Password should be between 6 and 15 characters in length");
            mPassword.requestFocus();
        }
        else {
            RegistrationChildRef = database.getReference("Users").child(UserName);
            RegistrationChildRef.child("UserName").setValue(UserName);
            RegistrationChildRef.child("Password").setValue(password);
            RegistrationChildRef.child("Email").setValue(email);
            RegistrationChildRef.child("Sessions").setValue(" ");

            Toast.makeText(this, "Registration Successful", Toast.LENGTH_LONG).show();
        }
    }
    public boolean validateEmail(String Email)
    {
        String EmailPattern  = "^[A-Za-z][A-Za-z0-9]*([._-]?[A-Za-z0-9]+)@[A-Za-z]+.[A-Za-z]{0,3}$";

        Pattern pattern = Pattern.compile(EmailPattern);
        Matcher matcher = pattern.matcher(Email);

        return matcher.matches();
    }

}

