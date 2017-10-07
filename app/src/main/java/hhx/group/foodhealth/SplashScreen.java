package hhx.group.foodhealth;

/**
 * Created by Xiaoting Huang on 2017-10-07
 **/


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    private int SPLASH_TIME_OUT = 2000;
    private SharedPreferences mPref;
    private boolean firstTimeUse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        mPref = getSharedPreferences("storage", Context.MODE_PRIVATE);

        firstTimeUse = mPref.getBoolean("first_time_use", true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent;
                // jump to guide for first time user
                if (firstTimeUse){
                    intent = new Intent(SplashScreen.this, FirstStartActivity.class);
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putBoolean("first_time_use", false);
                    editor.commit();
                } else {
                    intent = new Intent(SplashScreen.this, MainActivity.class);
                }
                startActivity(intent);
                finish();

            }
        }, SPLASH_TIME_OUT);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

    }

    @Override
    public void onBackPressed() {
    }
}
