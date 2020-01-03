package com.micronet.udpsampleappv2;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ServerKeepAliveMechanism implements Runnable {

    final static String TAG = "server-alive";
    String port;
    DatagramSocket datagramSocket;
    int errorCount = 0;
    static boolean isLoop = true;
    private Context con;
    private final String connectionIssueMessage = "Internet Connection Issue: Server will reconnect to the internet.";

    /**Empty Constructor for the class**/
    public ServerKeepAliveMechanism() {

    }

    /**Main Constructor for the class**/
    public ServerKeepAliveMechanism(Context con, String port) {
        this.con = con;
        this.port = port;
    }
    /**
     * Down below is the interface for the class,
     * designed for sending keepAlive status back to main activity, and to further broadcast the message to other activities.
     * **/
    public interface serverKeepAliveInterface {
        void onClientPortUpdate(String connectionReport);
    }

    static serverKeepAliveInterface callback;

    public void setServerKeepAliveInterface(serverKeepAliveInterface callback) {
        this.callback = callback;
    }

    /**
     * Main method to handle the heartbeat sending and receiving.
     * Set the listening port to be user input - 10.
     * Bind the port and set the address to be reusable(User might want to use the same port for different tasks)
     * Set time-out limit to be 3 seconds, once limit reached, restart cycle.
     * **/
    private void heartbeatReceiver() {

        int serverPort = Integer.parseInt(port);
        int heartbeatPort = serverPort - 10;
        byte[] data = new byte[1000];
        boolean isActive = false;

        Log.d(TAG, "Start listening to heartbeat..");
        try {
            datagramSocket = new DatagramSocket(null);
            datagramSocket.setReuseAddress(true);
            datagramSocket.setBroadcast(true);
            datagramSocket.bind(new InetSocketAddress(heartbeatPort));
            datagramSocket.setSoTimeout(3000);
            Log.d(TAG, "datagramSocket status: " + heartbeatPort);

            DatagramPacket receivePacket = new DatagramPacket(data, data.length);
            datagramSocket.receive(receivePacket);

            InetAddress clientIP = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();
            Log.d(TAG, "ClientIP: " + clientIP + " - ClientPort:" + clientPort);

            byte[] ackBackData = new byte[1];
            receivePacket = new DatagramPacket(ackBackData, ackBackData.length, clientIP, clientPort);
            datagramSocket.send(receivePacket);
            Log.d(TAG, "Ack Back");
            errorCount = 0;
        } catch (Exception e) {
            Log.d(TAG, "Error - datagramSocket status: " + e);
            datagramSocket.disconnect();
            datagramSocket.close();
            errorCount = errorCount + 1;
            Log.d(TAG, "errorCount: " + errorCount);

        }
    }

    /**
     * Method to handle Internet reconnection,
     * It's capable of cutting and reconnecting the internet.
     * **/
    private int reconnectInternet(boolean mobileDataEnabled) {
        int resultCode = 0;
        //isLoop = false;
        Log.d(TAG, "reconnectInternet Called.");
        try {
            TelephonyManager telephonyService = (TelephonyManager) con.getSystemService(Context.TELEPHONY_SERVICE);
            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (null != setMobileDataEnabledMethod) {
                Log.d(TAG, "null != setMobileDataEnabledMethod");
                setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
                Log.d(TAG, "reconnectInternet done");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error - reconnectInternet: " + e);
        }
        return resultCode;
    }

    /**Method to kill the keepAlive Mechanism by updating the Flag state.**/
    public void changeServerKeepAliveMechanismState(boolean isLoop) {
        this.isLoop = isLoop;
        Log.d(TAG, "killServerKeepAliveMechanism: " + isLoop);
    }


    /**
     * As long as the Flag is set to true, service will keep running.
     * errorCount limit set to 10, once it's reached, fire the reconnectInternet() and reset the errorCount.
     * Force thread to sleep for 0.5 second for every successfully cycle, because we don't want to boom the client.
     * **/
    @Override
    public void run() {
        while (isLoop) {
            try {
                if (errorCount >= 10) {
                    Log.d(TAG, "System TimeOut! Something went wrong, reconnecting!");
                    callback.onClientPortUpdate(connectionIssueMessage);
                    reconnectInternet(false);
                    Log.d(TAG, "Turned off mobile data");
                    Thread.sleep(2000);
                    reconnectInternet(true);
                    Log.d(TAG, "Turned on mobile data");
                    Thread.sleep(2000);
                    errorCount = 0;
                } else {
                    heartbeatReceiver();
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                Log.d(TAG, "Running Error" + e);
            }
        }
    }
}
