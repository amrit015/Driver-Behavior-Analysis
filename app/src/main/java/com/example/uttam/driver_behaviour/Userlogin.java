package com.example.uttam.driver_behaviour;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class Userlogin extends AppCompatActivity {
    ViewFlipper flipper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlogin);
        flipper = (ViewFlipper)findViewById(R.id.flip);
        flipper.setFlipInterval(1000);
        Animation in = AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.slide_out_right);
        flipper.setInAnimation(in);
        flipper.setOutAnimation(out);
        flipper.startFlipping();

    }

    public void signUp(View view){
        Intent intent = new Intent(Userlogin.this,UserSignUp.class);
        startActivity(intent);
    }

    public void logIn(View view){
        Intent intent = new Intent(Userlogin.this,UserLoginPage.class);
        startActivity(intent);
    }



}
