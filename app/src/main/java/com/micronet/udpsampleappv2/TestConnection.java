package com.micronet.udpsampleappv2;

import java.net.DatagramSocket;

public class TestConnection {

    public static class ClientTestConnection implements Runnable{
    static DatagramSocket datagramSocket;
    String targetIP;
    String targetPort;

        public ClientTestConnection(String targetIP, String targetPort){
                this.targetIP = targetIP;
                this.targetPort = targetPort;
    }

    private void testConnect(){
            try{
                datagramSocket = new DatagramSocket();
            }catch(Exception e){

            }
    }
        @Override
        public void run() {

        }
    }
}
