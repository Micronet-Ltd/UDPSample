package com.micronet.udpsampleappv2.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.micronet.udpsampleappv2.ClientKeepAliveMechanism;
import com.micronet.udpsampleappv2.LogViewModel;
import com.micronet.udpsampleappv2.R;
import com.micronet.udpsampleappv2.UDPClientSending;

import java.util.ArrayList;
import java.util.List;

import static com.micronet.udpsampleappv2.LogViewModel.getInternetState;

public class TransmitActionFragment extends Fragment {

    final static String TAG = "transmit-action";

    //Declare all the TextView.
    static TextView textViewActionTargetIP;
    static TextView textViewActionTargetPort;
    static TextView textViewActionMessage;
    static TextView textViewCycleTime;
    static TextView textViewRX;
    static TextView textViewTX;
    static EditText editTextInputMessage;
    static TextView textViewTransmitActionConnection;

    //Declare all the button controls being used in the fragment.
    Button btnSendUdp;
    ToggleButton toggleButtonCycle;
    SeekBar seekBarTimer;

    String logData;
    static String targetIP;
    static String targetPort;
    static String message;

    //Declare variable to store the time gap of UDP cycle, set min to 1 second.
    static int cycleWaitTime = 1000;

   //Declare all the custom objects that being used in this fragment.
    static ClientKeepAliveMechanism clientKeepAliveMechanism;
    private static List<String> internetStateList;
    static Observer<String> internetStateObserver;
    public LogViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transmit_action, container, false);

        //Locate elements in the UI.
        textViewActionTargetIP = view.findViewById(R.id.textViewActionTargetIP);
        textViewActionTargetPort = view.findViewById(R.id.textViewActionTargetPort);
        textViewCycleTime = view.findViewById(R.id.textViewCycleTime);
        textViewRX = view.findViewById(R.id.textViewTransmitRX);
        textViewTX = view.findViewById(R.id.textViewTransmitTX);
        textViewTransmitActionConnection = view.findViewById(R.id.textViewTransmitActionConnection);
        editTextInputMessage = view.findViewById(R.id.editTextInputMessage);

        btnSendUdp = view.findViewById(R.id.btnSendUDP);
        toggleButtonCycle = view.findViewById(R.id.toggleButtonCycle);
        seekBarTimer = view.findViewById(R.id.seekBarTimer);

        //Todo: create a new element on UI to check for internet connection in real-time.(DONE)
        //Todo: move the message input into this fragment for better usage.(DONE)
        //Todo: add warning message if packet not send.(DONE)

        updateConnectionStateOnUI();

        clientKeepAliveMechanism = new ClientKeepAliveMechanism();

        /**
         *
         Collecting all the target info from the TextView
         Call the constructor on in the UDPClientSendingRunnable, and pass in the target info.
         Start new thread and run.
         *
         **/
        btnSendUdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textViewActionTargetIP.getText().toString().equals("N/A") ||
                    textViewActionTargetPort.getText().toString().equals("N/A")){
                    Toast.makeText(getActivity(), "No Target Info Found", Toast.LENGTH_SHORT).show();
                    return;
                }

                targetIP = textViewActionTargetIP.getText().toString();
                targetPort = textViewActionTargetPort.getText().toString();
                message = editTextInputMessage.getText().toString();

                toggleButtonCycle.setChecked(false);

                UDPClientSending.UDPClientSendTestingRunnable udpClientSendTestingRunnable = new UDPClientSending.UDPClientSendTestingRunnable(targetIP, targetPort, message, 0,false);
                new Thread(udpClientSendTestingRunnable).start();

                clientKeepAliveMechanism = new ClientKeepAliveMechanism(getActivity(), targetIP, targetPort);
                clientKeepAliveMechanism.changeClientKeepAliveMechanism(true);
                new Thread(clientKeepAliveMechanism).start();

            }
        });

        /***
            *
            Collecting all the target info from the TextView
            Call the constructor on in the UDPClientCycleRunnable, and pass in the target info.
            Start new thread and run.
            toggleButton, isChecked = run cycle, !isChecked = stop cycle.
            *
            * **/
        toggleButtonCycle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (textViewActionTargetIP.getText().toString().equals("N/A")){
                    toggleButtonCycle.setChecked(false);
                    Toast.makeText(getContext(), "No Target Info Found", Toast.LENGTH_SHORT).show();
                    return;
                }

                targetIP = textViewActionTargetIP.getText().toString();
                targetPort = textViewActionTargetPort.getText().toString();
                message = editTextInputMessage.getText().toString();

                UDPClientSending.UDPClientSendTestingRunnable udpClientSendTestingRunnable =
                        new UDPClientSending.UDPClientSendTestingRunnable(targetIP, targetPort, message, cycleWaitTime,true);

                clientKeepAliveMechanism = new ClientKeepAliveMechanism(getActivity(), targetIP, targetPort);

                if (isChecked){
                    Log.d(TAG, "toggleButtonCycle is ON");
                    new Thread(udpClientSendTestingRunnable).start();
                    clientKeepAliveMechanism.changeClientKeepAliveMechanism(true);
                    new Thread(clientKeepAliveMechanism).start();
                }
                if(!isChecked){
                    Log.d(TAG, "toggleButtonCycle is OFF");
                    udpClientSendTestingRunnable.stopCycle();
                    clientKeepAliveMechanism.changeClientKeepAliveMechanism(false);
                }
            }
        });

        /**
         *Collect cycle wait-time from the seekBar
         * set the min wait-time to be no less than 1 seconds,
         * update waitTime on the progress,
         * display the readableTime on UI (waitTime divide by 1000 to get seconds),
         * since the readableTime is more accurate to the user, I'm go to depend on it for the cycle with time,
         * Times it by 1000 and send it back to UDPClientSending
         * **/
        seekBarTimer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int waitTime =0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int min = 1001;
                if (i<min){
                    waitTime = min;
                }else{
                    waitTime = i;
                    int readableTime = waitTime/1000;
                    textViewCycleTime.setText(Integer.toString(readableTime));
                    //cycleWaitTime = waitTime-700;
                    cycleWaitTime = (readableTime*1000);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "cycleTime: " + cycleWaitTime);
            }
        });
        return view;
    }

    /**
     * Method that update the connection status on the UI in real-time.
     * **/
    private void updateConnectionStateOnUI() {
        internetStateList = new ArrayList<>();
        for (int i=0;i<internetStateList.size(); i++){
            textViewTransmitActionConnection.setText(internetStateList.get(i));
        }
        if (internetStateObserver==null){
            internetStateObserver = new Observer<String>() {

                @Override
                public void onChanged(@Nullable final String internetState) {
                    textViewTransmitActionConnection.setText(internetState);
                    internetStateList.add(internetState);
                    String currentText = textViewTransmitActionConnection.getText().toString();
                    if(currentText == "ONLINE"){
                        textViewTransmitActionConnection.setTextColor(getResources().getColor(R.color.micronetGreen));
                    }else{
                        textViewTransmitActionConnection.setTextColor(getResources().getColor(R.color.colorAccent));
                    }
                }
            };
            getInternetState().observe(this, internetStateObserver);
        }
    }

    /**
     * Method to get target information from MainActivity,
     * Breakdown and assign data into related variables,
     * Display and set data onto the TextViews.
     **/
    public void getTargetInfo(String data){
        String targetInfoFormDelivery = data;

        Log.d(TAG, "getTargetInfo:" + targetInfoFormDelivery);
        String ip = targetInfoFormDelivery.split("-")[0];
        String port =targetInfoFormDelivery.split("-")[1];

        textViewActionTargetIP.setText(ip);
        textViewActionTargetPort.setText(port);
    }

    /**
     * Down belows are the methods to get transmit data from MainActivity,
     * post them onto TX and RX textViews.
     * **/
    public void updateTransmitActionTX(String data){
        String txDate = data;
        textViewTX.setText(txDate);
    }

    public void updateTransmitActionRX(String data){
       String rxData = data;
       textViewRX.setText(rxData);
    }

    public void displayIssueMessage(String issueMessage){
        Log.d(TAG, "displayIssueMessage: " + issueMessage);
    }
}
