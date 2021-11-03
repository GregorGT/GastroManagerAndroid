package com.example.gatromanagerclient.ui.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.gatromanagerclient.MainActivity;
import com.example.gatromanagerclient.R;
import com.example.gatromanagerclient.ui.splash.SplashActivity;
import com.example.gatromanagerclient.util.Util;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText etServerIp, etPortNumber, etWaiterName;
    private TextInputLayout tlfServerIp, tlfPortNumber, tlfWaiterName;
    private AppCompatButton btnSubmit;
    private Dialog progressDialog;
    private CheckServerConnection checkServerConnection;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private SharedPreferences.Editor editor;
    private boolean isValidServer = false;

    public static String currency;
    public static Double salestaxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
    }

    private void initViews() {
        etServerIp = findViewById(R.id.et_server_ip);
        etPortNumber = findViewById(R.id.et_port_number);
        etWaiterName = findViewById(R.id.et_waitress_name);
        progressDialog = new Dialog(this);

        tlfServerIp = findViewById(R.id.text_layout_server_ip);
        tlfPortNumber = findViewById(R.id.text_layout_port_number);
        tlfWaiterName = findViewById(R.id.text_layout_waitress_name);

        if (!SplashActivity.SERVER_IP.isEmpty() && SplashActivity.SERVER_PORT != -1) {
            etServerIp.setText(SplashActivity.SERVER_IP);
            etPortNumber.setText(String.valueOf(SplashActivity.SERVER_PORT));
            etWaiterName.setText(String.valueOf(SplashActivity.NAME));
            isValidServer = SplashActivity.SERVER_IS_VALID;
        }

        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Util.isNetworkConnected(SettingsActivity.this)) {
                    Toast.makeText(
                            SettingsActivity.this,
                            "Check your Internet",
                            Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (validateInputFields()) {
                    setProgressbar(true);
                    checkServerConnection = new CheckServerConnection();
                    checkServerConnection.execute();
                }
            }
        });
    }

    private void updateUi(boolean response) {
        if (response) {
            isValidServer = true;
            SplashActivity.SERVER_IP = etServerIp.getText().toString();
            SplashActivity.SERVER_PORT = Integer.parseInt(etPortNumber.getText().toString());
            SplashActivity.NAME = etWaiterName.getText().toString();
            SplashActivity.SERVER_IS_VALID = true;

            editor.putString("serverIp", etServerIp.getText().toString());
            editor.putInt("serverPort", Integer.parseInt(etPortNumber.getText().toString()));
            editor.putString("name", etWaiterName.getText().toString());
            editor.putBoolean("isValid", true);
            editor.apply();

            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            isValidServer = false;
            tlfServerIp.setError("Server is not reachable.Try another");
            tlfPortNumber.setError("Try valid port number");
        }
    }

    private boolean validateInputFields() {
        tlfServerIp.setError(null);
        tlfPortNumber.setError(null);
        tlfWaiterName.setError(null);

        if (Util.isEmptyOrNull(etServerIp)) {
            tlfServerIp.setError("Enter Server IP");
            return false;
        }
        if (!Util.isValidIPAddress(etServerIp.getText().toString())) {
            tlfServerIp.setError("Enter Valid Server IP");
            return false;
        }
        if (Util.isEmptyOrNull(etPortNumber)) {
            tlfPortNumber.setError("Enter Port Number");
            return false;
        }

        if (Util.isEmptyOrNull(etWaiterName)) {
            tlfWaiterName.setError("Enter your name");
            return false;
        }

        return true;
    }

    public void setProgressbar(boolean showStatus) {
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        progressDialog.setCancelable(false);
        if (!showStatus) {
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
        } else {
            progressDialog.show();
        }
    }

    private class CheckServerConnection extends AsyncTask<Void, Void, Boolean> {
        boolean response;

        @Override
        protected Boolean doInBackground(Void... voids) {
            response = Util.checkServer(etServerIp.getText().toString(),
                    Integer.parseInt(etPortNumber.getText().toString()));
            return response;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            setProgressbar(false);
            updateUi(result);
            super.onPostExecute(result);
        }
    }

    @Override
    public void onBackPressed() {
        if (isValidServer) {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(
                    SettingsActivity.this,
                    "Enter Valid Server IP/Port",
                    Toast.LENGTH_LONG).show();
        }
    }
}
