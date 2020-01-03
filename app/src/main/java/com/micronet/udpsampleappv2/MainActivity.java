package com.micronet.udpsampleappv2;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.micronet.udpsampleappv2.Fragments.DeviceInfoFragment;
import com.micronet.udpsampleappv2.Fragments.LogFragment;
import com.micronet.udpsampleappv2.Fragments.SectionSelector;
import com.micronet.udpsampleappv2.Fragments.TransmitActionFragment;
import com.micronet.udpsampleappv2.Fragments.UDPServiceFragment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity implements UDPServiceFragment.udpServiceInterface,
        UDPClientSending.clientInterface, ServerKeepAliveMechanism.serverKeepAliveInterface{

    final static String TAG="main-activity";

    private SectionSelector sectionSelector;
    private ViewPager viewPager;
    public static String fileName = "";
    public static File logFileReadyToUse;

    static String appTitle = "";


    private static String internetState = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /**
         * Section for setting up Tabs Activity.
         * **/

        sectionSelector = new SectionSelector(getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        //This line of code keeps all the fragments alive during switching tabs.
        viewPager.setOffscreenPageLimit(3);
        setupViewPager(viewPager);

        int versionCode = BuildConfig.VERSION_CODE;
        appTitle = "UDP Sample Application - v."+versionCode;

        this.setTitle(appTitle);

        Log.d(TAG, "title : " + appTitle);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);



        //
        UDPClientSending udpClientSending = new UDPClientSending();
        udpClientSending.setClientInterface(this);

        ServerKeepAliveMechanism serverKeepAliveMechanism = new ServerKeepAliveMechanism();
        serverKeepAliveMechanism.setServerKeepAliveInterface(this);

        //Call the method to create log folder into device.
        createLogDirectory();
        logFileReadyToUse = createLogFile();

        //Todo: on log_testing.xml, the layout is not work correctly after rotation, work on to it.
    }

   @Override
    protected void onStart() {
        super.onStart();
        DeviceDataReceiver deviceDataReceiver = new DeviceDataReceiver();
        IntentFilter internetStateFliter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(deviceDataReceiver.singalStrengthReceiver, internetStateFliter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "Configuration changed: " + newConfig.toString());

    }


    private void setupViewPager(ViewPager viewPager){
        SectionSelector adapter = new SectionSelector(getSupportFragmentManager());
        adapter.addFragment(new UDPServiceFragment(), "Service Config");
        adapter.addFragment(new TransmitActionFragment(), "Transmit Action");
        adapter.addFragment(new LogFragment(), "Log");
        adapter.addFragment(new DeviceInfoFragment(), "Device INFO");

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof UDPServiceFragment){
            UDPServiceFragment udpServiceFragment = (UDPServiceFragment)fragment;
            udpServiceFragment.setUdpServiceInterface(this);
        }
    }

    public void updateInternetState(String state){
        internetState = state;
        Log.d(TAG, "updateInternetState: " + internetState);
    }

    /**
     * Handle the creation of the log directory into device
     * **/
    public void createLogDirectory(){

        try {
            File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "UDP_LOG");
            if (!dir.exists()) {
                dir.mkdir();
                Log.d(TAG, "UDP_LOG Created");
            }else{
                Log.d(TAG, "File already created.");
                Log.d(TAG, "Location: " + dir.getAbsolutePath());
            }

        }catch(Exception e){
            Log.d(TAG, "Folder Creation Error: " +e);
        }
    }

    /**
     * Handle the creation of the log file which will store the log record during the transmit process
     * log file will be named based on the time it's created,
     * save as  csv file
     * **/
    public File createLogFile(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd'T'HH_mm_ss");
        String fileCreatedTime = dateFormat.format(System.currentTimeMillis());
        fileName = (fileCreatedTime+"_log.csv");
        Log.d(TAG, "File Name Testing-- " +fileName);

        File logFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)+"/UDP_LOG/"+fileName);
        if (!logFile.exists()){
            try{
                logFile.createNewFile();
                Log.d(TAG,"File Location: " + logFile.getAbsolutePath());
                return logFile;
            }catch(Exception e){
                Log.d(TAG, "File Creation Error: " +e);
            }
        }
        return null;
    }

    /**
     * Handle writing new data into the log file
     * **/
    public void writeToFile(String log){
        if (!logFileReadyToUse.exists()){
            return;
        }else{
            try{
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFileReadyToUse, true));
                buf.append(log);
                buf.newLine();
                buf.close();
            }catch(Exception e){
                Log.d(TAG, "Write File Error: " +e);
            }
        }
    }

    /**
     * Receiving data from the UDPServiceFragment
     * and share those data to TransmitActionFragment and UDPServerReceiver,
     * **/
    @Override
    public void onUdpServiceUpdate(String targetINFO) {
        String packetFromInterface = targetINFO;
        Log.d(TAG, "Packet received from UDP Service Interface: "+packetFromInterface);

        TransmitActionFragment transmitActionFragment = new TransmitActionFragment();
        transmitActionFragment.getTargetInfo(packetFromInterface);

        UDPServerReceiver udpServerReceiver = new UDPServerReceiver();
        udpServerReceiver.getPortInfo(packetFromInterface);
    }
    /**
     * write outgoing log data into log file,
     * send log data back to LogFragment,
     * Create new runOnUiThread, and post on TransmitAction TX.
     * **/
    @Override
    public void onClientUpdate(String log) {
        final String displaySendMsg = log;
        writeToFile(displaySendMsg);
        LogFragment logTestingFragment = new LogFragment();
        logTestingFragment.setLogToDisplay(displaySendMsg);

        /**Since the MainActivity is trying to access UI element on another fragment,
         * Need to declare runOnUIThread to do it.
         * **/
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TransmitActionFragment transmitActionFragment = new TransmitActionFragment();
                transmitActionFragment.updateTransmitActionTX(displaySendMsg);
            }
        });
    }

    /**
     * write incoming log data into log file,
     * send log data back to LogFragment,
     * Create new runOnUiThread, and post on TransmitAction RX.
     * **/
    @Override
    public void onClientRXUpdate(String log) {
        final String displaySendMsg = log;
        writeToFile(displaySendMsg);
        LogFragment logTestingFragment = new LogFragment();
        logTestingFragment.setLogToDisplay(displaySendMsg);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TransmitActionFragment transmitActionFragment = new TransmitActionFragment();
                transmitActionFragment.updateTransmitActionRX(displaySendMsg);
            }
        });
    }

    @Override
    public void onClientPortUpdate(String connectionReport) {
        Log.d(TAG, "ServerKeepAlive connection issue message received:  " + connectionReport);
        String message = connectionReport;
        TransmitActionFragment transmitActionFragment = new TransmitActionFragment();
        transmitActionFragment.displayIssueMessage(message);
       /* runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TransmitActionFragment transmitActionFragment = new TransmitActionFragment();
                transmitActionFragment.displayIssueMessage(message);
            }
        });*/
    }
}