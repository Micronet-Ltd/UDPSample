package com.micronet.udpsampleappv2.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.micronet.udpsampleappv2.LogViewModel;
import com.micronet.udpsampleappv2.R;
import com.micronet.udpsampleappv2.ServerKeepAliveMechanism;
import com.micronet.udpsampleappv2.UDPClientSending;
import com.micronet.udpsampleappv2.UDPServerReceiver;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.micronet.udpsampleappv2.LogViewModel.getInternetState;
import static com.micronet.udpsampleappv2.LogViewModel.getDeviceIP;
import androidx.lifecycle.Observer;

public class UDPServiceFragment extends Fragment {

    final static String TAG = "udp-service-fragment";

    //TextView.
    TextView txtViewSimState;
    TextView txtViewsingleStrength;
    TextView txtViewInternetState;
    TextView txtViewPhoneNumber;
    TextView txtViewIMEINumber;
    TextView txtViewDeviceIP;
    TextView txtViewDevicePort;

    //UI-EditText.
    EditText editTextTargetIP;
    EditText editTextTargetPort;
    EditText editTextMessage;

    //UI-ButtonControl.
    Button btnLockConfig;
    Button btnResetConfig;
    RadioButton rBtnClient;
    RadioButton rBtnServer;
    RadioButton rBtn2Parties;
    RadioButton rBtn3Parties;

    RadioGroup rBtnGroup;
    RadioGroup rBtnGroupCommunicationType;

    //Variables.
    String targetIP=" ";
    String targetPort=" ";
    String message=" ";
    String packetReadyToSend;
    String device_unique_id;
    String IMEI;
    String singalLevel;
    static String signalTesting="";
    static String internetState = "";

    //Interface CallBack/
    udpServiceInterface callback;
    TelephonyManager tMgr;

    private static List<String> internetStateList;
    private static List<String> deviceIPList;
    private static List<String> signalStrengthList;
    private static List<String> simStateList;

    static Observer<String> internetObserver;
    static Observer<String> deviceIPObserver;
    static Observer<String> signalStrengthObserver;
    static Observer<String> simStateObserver;

    public LogViewModel internetStateModel;

    //Constructors we use in this fragment.
    static ServerKeepAliveMechanism serverKeepAliveMechanism;
    static UDPServerReceiver.UDPServerReceiverOnlyOneClient udpServerReceiverOnlyOneClient;
    static UDPServerReceiver.UDPServerReceiverRunnable udpServerReceiverRunnable;
    static UDPServerReceiver.UDPServerReceiver2Runnable udpServerReceiver2Runnable;


    // Threads we use in this fragment.
    static Thread twoPartiesThread;
    static Thread threePartiesThreadClientOne;
    static Thread threePartiesThreadClientTwo;


    public int mSignalStrength = 0;
    private static final int My_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;

    /**
     * Setting up Interface for passing Data onto MainActivity.
     * **/
    public void setUdpServiceInterface(udpServiceInterface callback){
        this.callback = callback;
    }
    public interface udpServiceInterface{
        void onUdpServiceUpdate(String targetINFO);
    }

    /**
     * OnCreateView.
     * ***/
    @SuppressLint("ServiceCast")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.udp_service_configuration, container, false);

        txtViewSimState = view.findViewById(R.id.textViewSimState);
        txtViewsingleStrength = view.findViewById(R.id.textViewSingle);
        txtViewInternetState = view.findViewById(R.id.textViewInternetState);
        txtViewPhoneNumber = view.findViewById(R.id.textViewPhoneNumber);
        txtViewIMEINumber = view.findViewById(R.id.textViewIMEINumber);
        txtViewDeviceIP = view.findViewById(R.id.textViewDeviceIP);

        editTextTargetIP = view.findViewById(R.id.editTextTargetIP);
        editTextTargetPort = view.findViewById(R.id.editTextTargetPort);

        btnLockConfig = view.findViewById(R.id.btnLockConfig);
        btnResetConfig = view.findViewById(R.id.btnResetConfig);

        rBtnClient = view.findViewById(R.id.radioButtonClient);
        rBtnServer = view.findViewById(R.id.radioButtonServer);
        rBtn2Parties = view.findViewById(R.id.radioButton2Parties);
        rBtn3Parties = view.findViewById(R.id.radioButton3Parties);

        rBtnGroup = view.findViewById(R.id.radioGroupConfig);
        rBtnGroupCommunicationType = view.findViewById(R.id.radioGroupConfigCommunicationType);

        serverKeepAliveMechanism = new ServerKeepAliveMechanism();
        rBtnClient.isChecked();


        /**
         * Connecting to the viewModel to get update.
         * **/

        internetStateModel = ViewModelProviders.of(this).get(LogViewModel.class);

        updateInterStateOnUI();
        updateDeviceIPOnUI();
        updateSignalStrengthOnUI();
        updateSimStateOnUI();

        tMgr = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        /**Method calls to get device's info, need to be organized***/

        loadIMEI();
        //Todo: make all the data collection dynamic, update when status changes(DONE)
        //Todo: When switching between tabs and fragments, make sure the data is up to date.(DONE)



        rBtn2Parties.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Log.d(TAG, "Client to Server");
                }
            }
        });

        rBtn3Parties.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Log.d(TAG, "Client to Client");
                }
            }
        });

        /**
         * Radio Button for being as Client,
         * enable all the related controls on the UI
         * **/
        rBtnClient.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "As Client");

                    editTextTargetIP.setHint("N/A");
                    editTextTargetIP.setEnabled(true);
                    editTextTargetPort.setHint("N/A");
                    editTextTargetPort.setEnabled(true);
                }
            }
        });
        /**
         * Radio Button for being as Server,
         * - Disable IP and Message
         * **/
        rBtnServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "As Server");

                    editTextTargetIP.setHint("Not Available as Server");
                    editTextTargetIP.setEnabled(false);
                }
            }
        });

        btnLockConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call this method to handle Lock Configuration request.
                lockConfig();
            }
        });


        btnResetConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call this method to handle Reset Configuration request.
                resetConfig();
            }
        });

        return view;
    }

    /**
     * Method that handling update Sim Status on the UI in real-time.
     * **/
    private void updateSimStateOnUI() {
        simStateList = new ArrayList<>();
        for (int i=0; i< simStateList.size(); i++){
            txtViewSimState.setText(simStateList.get(i));
        }
        if (simStateObserver == null){
            simStateObserver = new Observer<String>() {
                @Override
                public void onChanged(@Nullable final String simState) {
                    txtViewSimState.setText(simState);
                    simStateList.add(simState);
                }
            };
            LogViewModel.getSimState().observe(this, simStateObserver);
        }
    }

    /**
     * Method that handling update Signal Strength on the UI in real-time.
     * **/
    private void updateSignalStrengthOnUI() {
        signalStrengthList = new ArrayList<>();
        for (int i=0; i<signalStrengthList.size(); i++){
            txtViewsingleStrength.setText(signalStrengthList.get(i));
        }
        if (signalStrengthObserver == null){
            signalStrengthObserver = new Observer<String>() {
                @Override
                public void onChanged(@Nullable final String signalStrength) {
                    txtViewsingleStrength.setText(signalStrength);
                    signalStrengthList.add(signalStrength);
                }
            };
            LogViewModel.getSignalStrength().observe(this, signalStrengthObserver);
        }
    }

    /**
     * Method that handling update Internet State on UI in real-time
     * **/
    private void updateInterStateOnUI(){
        internetStateList = new ArrayList<>();
        for (int i=0;i<internetStateList.size(); i++){
            txtViewInternetState.setText(internetStateList.get(i));
            txtViewInternetState.setTextColor(getResources().getColor(R.color.micronetGreen));
        }
        if (internetObserver==null){
            internetObserver = new Observer<String>() {

                @Override
                public void onChanged(@Nullable final String internetState) {
                    txtViewInternetState.setText(internetState);
                    internetStateList.add(internetState);
                    String currentText = txtViewInternetState.getText().toString();
                    if (currentText == "ONLINE"){
                        txtViewInternetState.setTextColor(getResources().getColor(R.color.micronetGreen));
                    }else{
                        txtViewInternetState.setTextColor(getResources().getColor(R.color.colorAccent));
                    }
                }
            };
            getInternetState().observe(this, internetObserver);
        }
    }

    private void updateDeviceIPOnUI(){
        deviceIPList = new ArrayList<>();
        for (int i=0;i<deviceIPList.size();i++){
            txtViewDeviceIP.setText(deviceIPList.get(i));
        }
        if (deviceIPObserver==null){
            deviceIPObserver = new Observer<String>() {
                @Override
                public void onChanged(@Nullable final String deviceIP) {
                    txtViewDeviceIP.setText(deviceIP);
                    deviceIPList.add(deviceIP);
                }
            };
            getDeviceIP().observe(this, deviceIPObserver);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void updateInternetState(String state){
        internetState = state;
    }


    /**
     * Method for handling Configuration reset.
     * Enable all the input on the UI
     * and reset TextVIew to its default state.
     * **/
    private void resetConfig(){
        //Checking if user inputs are empty.
        if(editTextTargetIP.getText().toString().equals("")&&
                editTextTargetPort.getText().toString().equals(""))
        {
            editTextTargetIP.setText("");
            editTextTargetPort.setText("");
            Log.d(TAG, "User Inputs are empty");
            return;
        }else{
            enableRadioButton(true);

            // Process of interrupting twoPartiesThread.
            if(rBtn2Parties.isChecked() && rBtnServer.isChecked()){
                Log.d(TAG, "Detected: Current thread running as Server with 2 parties communication..");

                if(twoPartiesThread != null){
                    Log.d(TAG, "Pausing twoPartiesThread..");
                        twoPartiesThread.interrupt();
                        Log.d(TAG, "twoPartiesThread has stop..");
                }
                Log.d(TAG, "Error: twoPartiesThread == null");
            }

            // Process of interrupt threePartiesThread.
            if(rBtn3Parties.isChecked() && rBtnServer.isChecked()){
                Log.d(TAG,"Detected: Current thread running as Server with 3 parties communication..");

                if(threePartiesThreadClientOne != null && threePartiesThreadClientTwo != null){
                    Log.d(TAG, "Pausing threePartiesThread..");
                        threePartiesThreadClientOne.interrupt();
                            Log.d(TAG, "threePartiesThreadClientOne has stop..");
                        threePartiesThreadClientTwo.interrupt();
                            Log.d(TAG, "threePartiesThreadClientTwo has stop..");
                }
                Log.d(TAG, "Error: threePartiesThread == null");
            }

            if(rBtnServer.isChecked()){
                editTextTargetIP.setEnabled(false);
                editTextTargetIP.setHint("Not Available as Server");
            }else{

                editTextTargetIP.setHint("N/A");
                editTextTargetIP.setEnabled(true);
            }
            editTextTargetPort.setEnabled(true);
            editTextTargetIP.setText("");
            editTextTargetPort.setText("");
            editTextTargetPort.setHint("N/A");

            targetIP = editTextTargetIP.getHint().toString();
            targetPort = "00";

            //Shutdown KeepAlive Mechanism
            serverKeepAliveMechanism.changeServerKeepAliveMechanismState(false);

            //Send empty target info back to main activity
            packetReadyToSend = (targetIP+"-"+targetPort);
            Log.d(TAG, "Reset Config" + packetReadyToSend);
            callback.onUdpServiceUpdate(packetReadyToSend);
        }
    }

    /**
     * Method for handling Configuration Lock-in.
     * keep user from changing TextView during the process.
     * When Server radio button is checked, start receiving from client.
     * When Client radio button is checked, check user input and pass them into MainActivity
     * **/
    private void lockConfig(){

        /**
         * Start as Server with 3 parties communication.
         * **/
        if (rBtnServer.isChecked() && rBtn3Parties.isChecked()){
            Log.d(TAG, "TESTING: Server and 3Parties");
            targetPort = editTextTargetPort.getText().toString();

            if (!targetPort.equals("")){
                Log.d(TAG, "Testing server selection: " + targetPort);
                String portNumberToSend = ("NA"+"-"+targetPort+"-"+"NA");
                callback.onUdpServiceUpdate(portNumberToSend);

                udpServerReceiverRunnable = new UDPServerReceiver.UDPServerReceiverRunnable();
                udpServerReceiver2Runnable = new UDPServerReceiver.UDPServerReceiver2Runnable();

                threePartiesThreadClientOne = new Thread(udpServerReceiverRunnable);
                threePartiesThreadClientTwo = new Thread(udpServerReceiver2Runnable);

                threePartiesThreadClientOne.start();
                threePartiesThreadClientTwo.start();


                /**
                 * Using this new structure here,
                 * Declared the ServerKeepAlive Class at the very beginning of the code,
                 * using the custom constructor here to grep the input and pass them into the runnable.
                 * **/
                serverKeepAliveMechanism = new ServerKeepAliveMechanism(getActivity(), editTextTargetPort.getText().toString());
                serverKeepAliveMechanism.changeServerKeepAliveMechanismState(true);
                new Thread(serverKeepAliveMechanism).start();

                editTextTargetPort.setEnabled(false);

                enableRadioButton(false);

                Toast.makeText(getActivity(), "Now Running as Server, open Port: " + targetPort, Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getActivity(), "Please enter target port", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        /**
         * Start as Server with 2 parties communication
         * **/
        if(rBtnServer.isChecked()&&rBtn2Parties.isChecked()){
            Log.d(TAG, "TESTING: Server and 2Parties");

            targetPort = editTextTargetPort.getText().toString();

            if (!targetPort.equals("")){
                Log.d(TAG, "Testing server selection: " + targetPort);
                String portNumberToSend = ("NA"+"-"+targetPort+"-"+"NA");
                callback.onUdpServiceUpdate(portNumberToSend);

                udpServerReceiverOnlyOneClient = new UDPServerReceiver.UDPServerReceiverOnlyOneClient();
                udpServerReceiverOnlyOneClient.startProcess();

                twoPartiesThread = new Thread(udpServerReceiverOnlyOneClient);
                twoPartiesThread.start();
                Log.d(TAG, "2PartiesThread start: " + twoPartiesThread);


                /**
                 * Using this new structure here,
                 * Declared the ServerKeepAlive Class at the very beginning of the code,
                 * using the custom constructor here to grep the input and pass them into the runnable.
                 * **/
                serverKeepAliveMechanism = new ServerKeepAliveMechanism(getActivity(), editTextTargetPort.getText().toString());
                serverKeepAliveMechanism.changeServerKeepAliveMechanismState(true);
                new Thread(serverKeepAliveMechanism).start();

                editTextTargetPort.setEnabled(false);

                enableRadioButton(false);

                Toast.makeText(getActivity(), "Now Running as Server, open Port: " + targetPort, Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getActivity(), "Please enter target port", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        /**
         * Start as Client with 2 and 3 Parties communication
         * **/
        if (rBtnClient.isChecked()){
            Log.d(TAG, "TESTING: Client and 3Parties");

            targetIP = editTextTargetIP.getText().toString();
            targetPort = editTextTargetPort.getText().toString();

            Log.d(TAG,"Info From User Input:"+targetIP+"/"+targetPort);

            //Check if the user inputs are empty.
            if(targetIP.equals("")||targetPort.equals("")){
                Log.d(TAG, "User Input Empty");
                Toast.makeText(getActivity(),"Please enter target info", Toast.LENGTH_SHORT).show();
            }
            else{

                packetReadyToSend = (targetIP+"-"+targetPort);
                Log.d(TAG, "Packet ready to be send to Interface: " + packetReadyToSend);

                //CallBack and Send to MainActivity.
                callback.onUdpServiceUpdate(packetReadyToSend);

                //Lock-up editText to prevent any changes being made on target config.
                editTextTargetIP.setEnabled(false);
                editTextTargetPort.setEnabled(false);

                enableRadioButton(false);

                Toast.makeText(getActivity(),"Target Configuration Locked",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Method to enable and disable radio buttons on the UI
     * **/
    public void enableRadioButton(boolean enable){

        rBtnClient.setEnabled(enable);
        rBtnServer.setEnabled(enable);
        rBtn2Parties.setEnabled(enable);
        rBtn3Parties.setEnabled(enable);
    }
    /**
     *Access user permission and provide the IMEI info of the device.
     * **/
    public void loadIMEI(){
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE)
        != PackageManager.PERMISSION_GRANTED){

            Toast.makeText(getActivity(), "Permission Denied: Some features may not be available.",Toast.LENGTH_SHORT).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_PHONE_STATE)){
            }else{
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE},
                        My_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        }else{
            TelephonyManager mngr = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            IMEI = mngr.getDeviceId();
            device_unique_id = Settings.Secure.getString(getActivity().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            Log.d(TAG, "Phone number testing: " + device_unique_id+"--"+mngr.getDeviceId()+"--"+mngr.getLine1Number()+"--"+
                    "--"+mngr.getSimState());
            String phoneNumber = mngr.getLine1Number();
            if (phoneNumber == null){
                Log.d(TAG, "No phoneNumber");
                txtViewIMEINumber.setText(mngr.getDeviceId());
                txtViewPhoneNumber.setText("Unavailable");
                return;
            }
            txtViewPhoneNumber.setText(mngr.getLine1Number());
            txtViewIMEINumber.setText(mngr.getDeviceId());
        }
    }

    /**
     *Get Ip address as IPV4 format
     * **/
    public static String getOneIPV4() {
        ArrayList<String> list = new ArrayList<String>();
        String result = "Unable to get IP..";
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address) {
                        if (!inetAddress.isLoopbackAddress()) {
                            list.add(inetAddress.getHostAddress());
                        }
                    }
                }
            }
        } catch (SocketException ex) {
        }
        if (list.size() > 0)
            result = list.get(0);
        return result;
    }

    /**
     * Get device signal strength
     * **/
    private static String getSignalStrength(Context context) throws SecurityException {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String strength = null;
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //This will give info of all sims present inside your mobile
        if(cellInfos != null) {
            for (int i = 0 ; i < cellInfos.size() ; i++) {
                if (cellInfos.get(i).isRegistered()) {
                    if (cellInfos.get(i) instanceof CellInfoWcdma) {
                        Log.d(TAG,"getSignalStrength: Wcdma");
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfos.get(i);
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthWcdma.getDbm());
                    } else if (cellInfos.get(i) instanceof CellInfoGsm) {
                        Log.d(TAG,"getSignalStrength: Gsm");
                        CellInfoGsm cellInfogsm = (CellInfoGsm) cellInfos.get(i);
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthGsm.getDbm());
                    } else if (cellInfos.get(i) instanceof CellInfoLte) {
                        Log.d(TAG,"getSignalStrength: Lte");
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfos.get(i);
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthLte.getDbm());
                    } else if (cellInfos.get(i) instanceof CellInfoCdma) {
                        Log.d(TAG,"getSignalStrength: Cdma");
                        CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfos.get(i);
                        CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthCdma.getDbm());
                    }
                }
            }
        }
        return strength;
    }

    /**
     * Check if the device has a internet connection
     * **/
    private String checkInternetState(){
        ConnectivityManager conMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        String internetState="";
        if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ) {
            internetState = "Online";
            Log.d(TAG,"ONLINE");
            return internetState;
        }
        else if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {
            internetState = "Offline";
            Log.d(TAG,"Offline");
            return internetState;
        }
        return internetState;
    }
}
