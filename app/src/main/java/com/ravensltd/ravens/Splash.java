package com.ravensltd.ravens;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ravensltd.ravens.home.HomeActivity;

public class Splash extends AppCompatActivity {

    private static int SPLASH_TIME_OUT=1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        Log.e("my apllication","in splash",null);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent loginIntent=new Intent(Splash.this, HomeActivity.class);
                startActivity(loginIntent);
                finish();
            }
        },SPLASH_TIME_OUT);
    }
}
