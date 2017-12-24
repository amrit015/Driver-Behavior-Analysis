package com.example.uttam.driver_behaviour;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activity to sign up new users. Users are stored in the firebase database and checked through database during log in
 */

public class UserSignUpActivity extends AppCompatActivity implements  View.OnClickListener{
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
        setContentView(R.layout.activity_user_signup);
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
            //register user
            registerUser();
        }
    }

    private void registerUser() {
        // get username, email and password
        email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        final String UserName = Username.getText().toString();
        // adding to database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // checking if username already exists
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
        // if user exists, display error message, else perform validation and register
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
            // registering user with details
            RegistrationChildRef = database.getReference("Users").child(UserName);
            RegistrationChildRef.child("UserName").setValue(UserName);
            RegistrationChildRef.child("Password").setValue(password);
            RegistrationChildRef.child("Email").setValue(email);
            RegistrationChildRef.child("Sessions").setValue(" ");

            Toast.makeText(this, "Registration Successful", Toast.LENGTH_LONG).show();
        }
    }

    // checking and validating email
    public boolean validateEmail(String Email)
    {
        String EmailPattern  = "^[A-Za-z][A-Za-z0-9]*([._-]?[A-Za-z0-9]+)@[A-Za-z]+.[A-Za-z]{0,3}$";

        Pattern pattern = Pattern.compile(EmailPattern);
        Matcher matcher = pattern.matcher(Email);

        return matcher.matches();
    }
}

