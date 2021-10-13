package com.example.gatromanagerclient.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isEmptyOrNull(TextInputEditText textInputEditText) {
        if (Objects.requireNonNull(textInputEditText.getText()).length() == 0)
            return true;
        if (textInputEditText.getText() == null)
            return true;
        if (textInputEditText.getText().toString().equals(""))
            return true;
        return false;
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static void showSnackBar(View parentLayout,String message){
        Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG).show();
    }

    public static boolean checkServer(String serverAddress, int serverPort) {
        boolean exists = false;
        try {
            SocketAddress socketAddress = new InetSocketAddress(serverAddress, serverPort);
            Socket sock = new Socket();
            int timeoutMs = 5000;   // 5 seconds
            sock.connect(socketAddress, timeoutMs);
            exists = true;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return exists;
    }

    public static boolean isNetworkConnected(Context context) {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        } else
            connected = false;
        return connected;
    }

    public static boolean isValidIPAddress(String ip) {
        String zeroTo255
                = "(\\d{1,2}|(0|1)\\"
                + "d{2}|2[0-4]\\d|25[0-5])";

        String regex
                = zeroTo255 + "\\."
                + zeroTo255 + "\\."
                + zeroTo255 + "\\."
                + zeroTo255;

        Pattern p = Pattern.compile(regex);
        if (ip == null)
            return false;
        Matcher m = p.matcher(ip);
        return m.matches();
    }

    public static boolean checkServerReachable(String serverAddress, int serverPort){
        try {
            String[] ipSplits = serverAddress.split("\\.");
            if (ipSplits.length < 4)
                return false;
            int a = Integer.parseInt(ipSplits[0]);
            int b = Integer.parseInt(ipSplits[1]);
            int c = Integer.parseInt(ipSplits[2]);
            int d = Integer.parseInt(ipSplits[3]);
            byte[] ipAddressBytes = new byte[]{(byte) a, (byte) b, (byte) c, (byte) d};

            InetAddress inetAddress = InetAddress.getByAddress("Localhost", ipAddressBytes);
            Socket socket = new Socket(inetAddress, serverPort);
            return socket.isConnected();

        } catch (UnknownHostException e1) {
            e1.printStackTrace();
            return false;
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }
    }
}
