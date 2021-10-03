package com.example.gatromanagerclient.ui.splash;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.example.gatromanagerclient.MainActivity;
import com.example.gatromanagerclient.R;
import com.example.gatromanagerclient.ui.settings.SettingsActivity;

public class SplashActivity extends AppCompatActivity {

    private ImageView splashView;
    public static String SERVER_IP = "";
    public static int SERVER_PORT = -1;
    public static boolean SERVER_IS_VALID = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splashView = findViewById(R.id.splash_view);

        Glide.with(this)
                .load(R.drawable.splash_icon)
                .into(splashView);

        SharedPreferences prefs = getSharedPreferences(SettingsActivity.MY_PREFS_NAME, MODE_PRIVATE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (prefs.getAll() == null || prefs.getAll().size() == 0) {
                    Intent intent = new Intent(SplashActivity.this, SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(intent);
                } else {
                    SERVER_IP = prefs.getString("serverIp", "192.168.1.5");
                    SERVER_PORT = prefs.getInt("serverPort", 5000);
                    SERVER_IS_VALID = prefs.getBoolean("isValid", false);
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2500);

    }
}