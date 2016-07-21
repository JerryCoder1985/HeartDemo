package com.jerry.studio.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.jerry.studio.R;
import com.jerry.studio.view.BitmapPool;
import com.jerry.studio.view.HeartView;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private HeartView mHeartView;

    private Timer mTimer;

    private TimerTask mTimerTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BitmapPool.fillPool(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHeartView = (HeartView) findViewById(R.id.heartView);

        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHeartView.addHeart(false);
            }
        };
    }

    public void addHeart(View view) {
        mHeartView.addHeart(true);
    }

    public void addOtherHeart(View view) {
        mTimer.schedule(mTimerTask, 10, 200);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }
}
