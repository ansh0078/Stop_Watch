package com.sambhav2358.testing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Bundle;
import android.os.Handler;
import android.se.omapi.Session;

import com.airbnb.lottie.LottieAnimationView;

public class Main_Page extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((LottieAnimationView) findViewById(R.id.data1)).resumeAnimation();
                Intent intent= new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

            }
        }, 2000);
    }
}