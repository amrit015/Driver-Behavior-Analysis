package com.example.uttam.driver_behaviour;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

// home page activity, login and signin views are displayed and accessed from here
// flipper for animating images
public class HomePageActivity extends AppCompatActivity {
    ViewFlipper flipper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        flipper = (ViewFlipper)findViewById(R.id.flip);
        flipper.setFlipInterval(1000);
        Animation in = AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.slide_out_right);
        flipper.setInAnimation(in);
        flipper.setOutAnimation(out);
        flipper.startFlipping();
    }

    // sign up view
    public void signUp(View view){
        Intent intent = new Intent(HomePageActivity.this,UserSignUpActivity.class);
        startActivity(intent);
    }

    // log in view, onClick is implemented using id at the activity_home_page
    public void logIn(View view){
        Intent intent = new Intent(HomePageActivity.this,UserLoginPageActivity.class);
        startActivity(intent);
    }



}
