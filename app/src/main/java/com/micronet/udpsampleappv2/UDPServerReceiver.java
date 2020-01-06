package com.micronet.udpsampleappv2;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServerReceiver {

    final static String TAG = "udp-server";
    static int portNumber;
    static String receivedMsgOne = "";
    static String receiveMsgTwo = "";
    static InetAddress clientOneIP;
    static InetAddress clientTwoIP;

    static int realTimePort;

    static boolean flag = true;

    public void getPortInfo(String data) {
        portNumber = Integer.parseInt(data.split("-")[1]);
        Log.d(TAG, "UDP-Server PortNumber:" + portNumber);
    }

    public void updateRealTimePort(int realTimePort) {
        this.realTimePort = realTimePort;
        Log.d(TAG, "Testing Real-Time Client Port: " + this.realTimePort);
    }

    public static class UDPServerReceiverOnlyOneClient implements Runnable{

        DatagramSocket datagramSocket;
        static boolean kill = false;

        private void receiveFromClientOnlyOneClient() {

            try {
                         byte[] receiveData = new byte[3000];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        datagramSocket.setSoTimeout(2000);
                        datagramSocket.receive(receivePacket);

                        receivedMsgOne = new String(receivePacket.getData()).trim();
                        clientOneIP = receivePacket.getAddress();
                        int clientOnePort = receivePacket.getPort();
                        Log.d(TAG, "Received from Client -- Message: " + receivedMsgOne + " /IP: " + clientOneIP + " /Port: " + clientOnePort);

                        byte[] sendData = "RECEIVED BY SERVER".getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientOneIP, clientOnePort);
                        datagramSocket.send(sendPacket);
                        Log.d(TAG, "Confirmation message sent to client" );

            } catch (Exception e) {
                Log.d(TAG, "Client Receiver Error : " + e);
            }
        }
        public void createDatagramSocket(){
            try {
                datagramSocket = new DatagramSocket(portNumber);
                datagramSocket.setReuseAddress(true);
                Log.d(TAG, "datagramSocket is listening at port: " + portNumber);
            }catch(Exception e){
                Log.d(TAG, "createDatagramSocket error: " + e);
            }
        }

        public void destroyDatagramSocket(){
            if (datagramSocket!=null){
                Log.d(TAG, "DatagramSocket destroyed");
                datagramSocket.close();
            }else {
                Log.d(TAG, "No DatagramSocket was found");
            }
        }

        public void killProcess(){
            this.kill = true;
            Log.d(TAG, "Kill Process - Kill status changed: " + this.kill);
        }

        public void startProcess(){
            this.kill = false;
            Log.d(TAG, "Start Process - Kill status changed: " +this.kill);
        }

        @Override
        public void run() {

            createDatagramSocket();

            while(!Thread.currentThread().isInterrupted()){
                Log.d(TAG, "receiveFromClientOnlyOneClient running..");
                receiveFromClientOnlyOneClient();
            }

            destroyDatagramSocket();

        }

    }

    /**
     * This class is handling the client ONE receiving.
     * it will store the client's ip, opening port and message,
     * After received from client ONE, it will response back with Client TWO message.
     **/
    public static class UDPServerReceiverRunnable implements Runnable {

        DatagramSocket datagramSocket1;

        private void receiveFromClientOne() {

                    try {
                        byte[] receiveData = new byte[3000];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        datagramSocket1.setSoTimeout(2000);
                        datagramSocket1.receive(receivePacket);

                        receivedMsgOne = new String(receivePacket.getData()).trim();
                        clientOneIP = receivePacket.getAddress();
                        int clientOnePort = receivePacket.getPort();
                        Log.d(TAG, "Received from Client 1 -- Message: " + receivedMsgOne + " /IP: " + clientOneIP + " /Port: " + clientOnePort);

                        if(receiveMsgTwo != ""){
                            byte[] sendData = receiveMsgTwo.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientOneIP, clientOnePort);
                            datagramSocket1.send(sendPacket);
                            Log.d(TAG, "Send Client2 message to Client1 : Message: " + receiveMsgTwo);
                        }else{
                            Log.d(TAG, "No response was found");
                        }

                    } catch (Exception e) {
                        Log.d(TAG, "Client receiver ONE error" + e);
                    }
                }

        public void createDatagramSocket1(){
            try {
                datagramSocket1 = new DatagramSocket(portNumber);
                Log.d(TAG, "datagramSocket1 is listening at port: " + portNumber);
            }catch(Exception e){
                Log.d(TAG, "createDatagramSocket1 error: " + e);
            }
        }

        public void destroyDatagramSocket1(){

            if (datagramSocket1!=null){
                Log.d(TAG, "DatagramSocket1 destroyed");
                datagramSocket1.close();
            }else {
                Log.d(TAG, "No DatagramSocket was found");
            }
        }

        @Override
        public void run() {
            createDatagramSocket1();
            while(!Thread.currentThread().isInterrupted()){
                Log.d(TAG, "receiveFromClientOne running..");
                receiveFromClientOne();
            }
            destroyDatagramSocket1();
        }
    }


    /**
     * This class is handling the client TWO receiving.
     * For convenience purpose,
     * The listening port on this datagramSocket will be Client ONE Port + 1,
     * (For Example, if Client ONE port is 7501, Client TWO port will be 7502).
     * it will store the client's ip, opening port and message,
     * After received from client TWO, it will response back with Client ONE message.
     **/
    public static class UDPServerReceiver2Runnable implements Runnable {

        DatagramSocket datagramSocket2;
        int portNumberPlus = portNumber + 1;

        private void receiveFromClientTwo() {

                    try {
                        byte[] receiveData = new byte[3000];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        datagramSocket2.setSoTimeout(2000);
                        datagramSocket2.receive(receivePacket);

                        receiveMsgTwo = new String(receivePacket.getData()).trim();
                        clientTwoIP = receivePacket.getAddress();
                        int clientTwoPort = receivePacket.getPort();

                        Log.d(TAG, "Received from Client 2 -- Message: " + receiveMsgTwo + " /IP: " + clientTwoIP + " /Port: " + clientTwoPort);

                        if(receivedMsgOne != "") {
                            byte[] sendData = receivedMsgOne.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientTwoIP, clientTwoPort);
                            datagramSocket2.send(sendPacket);
                            Log.d(TAG, "Send Client1 message to Client2 : Message: " + receivedMsgOne);
                        }else{
                            Log.d(TAG, "No response was found");
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Client Receiver TWO Error: " + e);
                    }
        }


        public void createDatagramSocket2(){
            try {
                datagramSocket2 = new DatagramSocket(portNumberPlus);
                Log.d(TAG, "datagramSocket2 is listening at port: " + portNumberPlus);
            }catch(Exception e){
                Log.d(TAG, "createDatagramSocket2 error: " + e);
            }
        }

        public void destroyDatagramSocket2(){

            if (datagramSocket2!=null){
                Log.d(TAG, "DatagramSocket2 destroyed");
                datagramSocket2.close();
            }else {
                Log.d(TAG, "No DatagramSocket was found");
            }
        }

        @Override
        public void run() {
            createDatagramSocket2();
            while(!Thread.currentThread().isInterrupted()){
                Log.d(TAG, "receiveFromClientTwo running..");
                receiveFromClientTwo();
            }
            destroyDatagramSocket2();
        }
    }
}


