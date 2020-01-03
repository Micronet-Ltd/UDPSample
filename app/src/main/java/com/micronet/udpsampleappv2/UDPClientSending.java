package com.micronet.udpsampleappv2;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.micronet.udpsampleappv2.Fragments.TransmitActionFragment;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static com.micronet.udpsampleappv2.LogViewModel.getLogRecord;

public class UDPClientSending {

    final static String TAG = "udp-client";
    static int testPort;

    public interface clientInterface {
        void onClientUpdate(String log);

        void onClientRXUpdate(String log);
    }

    static UDPClientSending.clientInterface callback;
    static UDPClientSending.clientInterface callbackRX;

    public void setClientInterface(UDPClientSending.clientInterface callback) {
        this.callback = callback;
    }

    public int getTestPort() {
        return testPort;
    }

    /**
     * Handling the message decode,
     * Receive all the data after message got send,
     * and format a complete log format for display and saving.
     **/
    public static String decodeMsgForLog(DatagramPacket datagramPacket, DateFormat dateFormat, String client) {
        String decodeMessage = "";

        String time = dateFormat.format(System.currentTimeMillis());
        String iP = datagramPacket.getAddress().toString();
        String message = new String(datagramPacket.getData());
        int port = datagramPacket.getPort();

        decodeMessage = (time + "  ,  " + client + "  ,  " + iP + "  ,  " + port + "  ,  " + message);
        return decodeMessage;
    }

    public static class UDPClientReceivingRunnqable implements Runnable {

        DatagramSocket clientReceiveSocket;
        String receiveMessage = "";
        int port;
        UDPClientSending udpClientSending = new UDPClientSending();


        private void setPort(int port) {
            this.port = port;
        }

        private int getPort() {
            return port;
        }


        public void ClientReceive() {
            //Todo: I'm not sure why I need this method,
            Log.d(TAG, "ClientReceive method running");
            Log.d(TAG, "Checking for port again..." + testPort);

            int anotherTestingPort = udpClientSending.getTestPort();
            Log.d(TAG, "Please works : " + anotherTestingPort);
            setPort(testPort);
            try {
                clientReceiveSocket = new DatagramSocket(port);
                Log.d(TAG, "Client esting receiveRunnable on port: " + port);
                byte[] receiveData = new byte[3000];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientReceiveSocket.receive(receivePacket);
                receiveMessage = new String(receivePacket.getData());

                Log.d(TAG, "Receive from Server! " + receiveMessage);

            } catch (Exception e) {
                Log.d(TAG, "Client Receive Error: " + e);
            }
        }

        @Override
        public void run() {

            Log.d(TAG, "run port: " + testPort);
            ClientReceive();
        }
    }

    /**
     * This is a testing of combining both send and cycle together.
     **/
    public static class UDPClientSendTestingRunnable implements Runnable {

        static DatagramSocket datagramSocket;
        String targetIP;
        String targetPort;
        String message;
        String send = "SEND";
        String received = "RECEIVED";
        String sendingError = "Fail";
        int timeToWait;
        DateFormat dateFormat;
        String receiveMessage;

        static boolean stayCycle;

        public UDPClientSendTestingRunnable(String targetIP, String targetPort, String message, int timeToWait, boolean isCycle) {
            this.targetIP = targetIP;
            this.targetPort = targetPort;
            this.message = message;
            this.timeToWait = timeToWait;

            if (isCycle) {
                stayCycle = true;
            } else if (!isCycle) {
                stayCycle = false;
            }
        }

        private void send() {
            Log.d(TAG, "Send!");

            String logMsgToPost = "";
            DatagramPacket sendPacket = null;
            try {
                datagramSocket = new DatagramSocket();
                InetAddress targetIPAddress = InetAddress.getByName(targetIP);
                int targetPortNumber = Integer.parseInt(targetPort);
                byte[] sendData = message.getBytes();

                dateFormat = new SimpleDateFormat("yyyy-MM-dd' , 'HH:mm:ss");

                sendPacket = new DatagramPacket(sendData, sendData.length, targetIPAddress, targetPortNumber);
                datagramSocket.send(sendPacket);

                UDPClientSending.testPort = datagramSocket.getLocalPort();
                Log.d(TAG, "Client test port: " + testPort);


                Log.d(TAG, "Successfully Send! Message: " + sendPacket.getData().toString() + " /TargetIP: " + targetIPAddress + " /TargetPort: " + targetPortNumber);
                logMsgToPost = decodeMsgForLog(sendPacket, dateFormat, send);
                Log.d(TAG, "logMsgToPost: " + logMsgToPost);
                callback.onClientUpdate(logMsgToPost);
                Log.d(TAG, "Callback happened: " + logMsgToPost);
                getLogRecord().postValue(logMsgToPost);


                byte[] receiveData = new byte[3000];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                datagramSocket.receive(receivePacket);
                receiveMessage = new String(receivePacket.getData()).trim();
                if (receiveMessage.equals("")) {
                    Log.d(TAG, "No responsing on Server.");
                    return;
                }
                Log.d(TAG, "Response Received: " + receiveMessage);
                logMsgToPost = decodeMsgForLog(receivePacket, dateFormat, received);
                callback.onClientRXUpdate(logMsgToPost);
                getLogRecord().postValue(logMsgToPost);

            } catch (Exception e) {
                logMsgToPost = decodeMsgForLog(sendPacket, dateFormat, sendingError);
                callback.onClientUpdate(logMsgToPost);
                getLogRecord().postValue(logMsgToPost);
                Log.d(TAG, "Error message: " + logMsgToPost);
                Log.d(TAG, "ClientSend--Error : " + e);
                //Todo: at the Error, post a error message on the UI, and add a time stamp.(Done)
                //Todo: Put the receiving response into a new thread and keep it running.(Still need to look into it, not sure if I need it or not.)
                //Todo: Try to use Flag and loop to control the send method, make it into one method(Done)
            }
        }

        /**
         * Method to handle UDP cycle.
         * **/
        private void cycle() {
            Log.d(TAG, "In Cycle!");
            String logMsgToPost = "";
            DatagramPacket sendPacket = null;
            try {
                datagramSocket = new DatagramSocket();
                InetAddress targetIPAddress = InetAddress.getByName(targetIP);
                int targetPortNumber = Integer.parseInt(targetPort);
                byte[] sendData = message.getBytes();

                dateFormat = new SimpleDateFormat("yyyy-MM-dd' , 'HH:mm:ss");

                sendPacket = new DatagramPacket(sendData, sendData.length, targetIPAddress, targetPortNumber);
                datagramSocket.send(sendPacket);
                Log.d(TAG, "Successfully Cycle: /Message: " + sendPacket.getData().toString() + " /TargetIP: " + targetIPAddress + " /TargetPort: " + targetPort);
                logMsgToPost = decodeMsgForLog(sendPacket, dateFormat, send);
                Log.d(TAG, "TX logMsgToPost: " + logMsgToPost);

                callback.onClientUpdate(logMsgToPost);
                getLogRecord().postValue(logMsgToPost);

                byte[] receiveData = new byte[3000];
                Log.d(TAG, "timeToWait: " + timeToWait);
                datagramSocket.setSoTimeout(500);
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    datagramSocket.receive(receivePacket);
                    Log.d(TAG, "Response Received from Server: " + receivePacket);
                    receiveMessage = new String(receivePacket.getData()).trim();
                    Log.d(TAG, "Cycle received message: " + receiveMessage);
                    logMsgToPost = decodeMsgForLog(receivePacket, dateFormat, received);
                    Log.d(TAG, "RX logMsgToPost: " + logMsgToPost);
                    callback.onClientRXUpdate(logMsgToPost);
                    getLogRecord().postValue(logMsgToPost);
                    datagramSocket.setSoTimeout(timeToWait);

                } catch (SocketTimeoutException e) {
                    Log.d(TAG, "Socket TimeOut: " + e);
                }
            } catch (Exception e) {
                logMsgToPost = decodeMsgForLog(sendPacket, dateFormat, sendingError);
                callback.onClientUpdate(logMsgToPost);
                getLogRecord().postValue(logMsgToPost);
                Log.d(TAG, "Error message: " + logMsgToPost);
                Log.d(TAG, "Client-Cycle Error : " + e);
            }
        }

        /**Method to kill cycle by updating Flag state**/
        public void stopCycle() {
            stayCycle = false;
            Log.d(TAG, "Stop Cycle!");
        }

        /**
         *RUN
         * **/
        @Override
        public void run() {

            if (stayCycle) {
                Log.d(TAG, "SendTestingRunnable: stayCycle = " + stayCycle);
                while (stayCycle) {
                    try {
                        cycle();
                        Thread.sleep(timeToWait);

                    } catch (Exception e) {
                        Log.d(TAG, "Error  :" + e);
                    }
                }
            } else if (!stayCycle) {
                Log.d(TAG, "SendTestingRunnable: stayCycleFalse = " + stayCycle);
                send();
            }
        }
    }
}
