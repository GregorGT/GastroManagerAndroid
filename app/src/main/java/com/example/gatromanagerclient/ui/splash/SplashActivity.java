/*Copyright 2021 GastroRice

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


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
    public static String NAME = "";
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
                    NAME = prefs.getString("name", "No Name");
                    SERVER_IS_VALID = prefs.getBoolean("isValid", false);
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2500);

    }
}