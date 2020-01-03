package com.micronet.udpsampleappv2;

import android.content.Context;
import android.content.IntentFilter;
import android.provider.ContactsContract;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientKeepAliveMechanism implements Runnable {

    final static String TAG = "client-alive";
    DatagramSocket datagramSocket;
    String targetIP;
    String port;
    int errorCount = 0;
    static boolean isLoop = true;
    private Context con;


    /**
     * Empty Constructor for the class
     * **/
    public ClientKeepAliveMechanism(){

    }

    /**
     * Main Constructor for the class
     * **/
    public ClientKeepAliveMechanism(Context con, String targetIP, String port){
        this.con = con;
        this.targetIP = targetIP;
        this.port = port;
    }

    /**
     * Main method to handle keepAlive mechanism.
     * Set target server port to be user input - 10.
     * send heartbeat message to server and for response, time-out in 3 seconds.
     * **/
    private void heartbeat(){
        try{
            datagramSocket = new DatagramSocket();
            byte[] data = new byte[1];
            InetAddress serverIP = InetAddress.getByName(targetIP);
            int serverPort = Integer.parseInt(port);
            serverPort = serverPort - 10;
            DatagramPacket dataPacket = new DatagramPacket(data, data.length, serverIP, serverPort);
            Log.d(TAG, "serverPort: " + serverPort);
            datagramSocket.send(dataPacket);
            Log.d(TAG, "heartbeat send.");

            dataPacket = new DatagramPacket(data, dataPacket.getLength());
            datagramSocket.setSoTimeout(3000);
            datagramSocket.receive(dataPacket);
            Log.d(TAG, "AckBack arrived from server");

        }catch(Exception e){
            Log.d(TAG, "Heartbeat Error: " +e);
        }
    }

    /**
     * Method to shut down keepAlive mechanism, by reading the flag status.
     * **/
    public void changeClientKeepAliveMechanism(boolean isLoop){
        this.isLoop = isLoop;
        Log.d(TAG, "chnageClientKeepAliveMechanism: " + this.isLoop);
    }

    /**
     * Set errorCount limit to be 10, when limit reached, restart service, connection and reset errorCount to 0.
     * Set keepAlive mechanism sleep for 0.5 second for every successfully cycle, because we don't want to boom the server..
     * **/
    @Override
    public void run() {
        while(isLoop){
        try {
            if(errorCount >= 10){
                Log.d(TAG, "System TimeOut, re-establish connection.");
                errorCount = 0;
            }else{
                heartbeat();
                Thread.sleep(500);
            }
        }catch (Exception e ){
            Log.d(TAG, "Client KeepAlive Running Error: " +e);
        }
    }
    }
}
