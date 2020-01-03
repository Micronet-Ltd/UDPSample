package com.micronet.udpsampleappv2.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.micronet.udpsampleappv2.LogViewModel;
import com.micronet.udpsampleappv2.R;

import java.util.ArrayList;
import java.util.List;

import static com.micronet.udpsampleappv2.LogViewModel.getLogRecord;

public class LogFragment extends Fragment {

    final private static String TAG = "log-testing";

    TextView textViewLogTesting;

    static String logToDisplay = "";
    static Observer<String> logObserver;
    private static List<String> logRecordList;

    //Call the LogViewModel.
    public LogViewModel model;

    /**
     * Handling the display on the log fragment.
     * */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.log_testing, container, false);
        Log.d(TAG,"onCreate got called.");

        textViewLogTesting = view.findViewById(R.id.textViewLogTesting);
        textViewLogTesting.setSingleLine(false);

        //A For-loop to produce display items from the list.
        logRecordList = new ArrayList<>();
        for(int i=0; i<logRecordList.size();i++){
            textViewLogTesting.setText(logRecordList.get(i));
        }
        model = ViewModelProviders.of(this).get(LogViewModel.class);

        //Check if the observer is null, and create new object.
        if(logObserver==null){

            logObserver = new Observer<String>() {
                @Override
                public void onChanged(@Nullable final String newLog) {
                    textViewLogTesting.append(newLog+"\n");
                    Log.d(TAG, "newLog: " + newLog);
                    logRecordList.add(newLog);
                }
            };
            getLogRecord().observe(this, logObserver);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setLogToDisplay(String logToDisplay) {
        LogFragment.logToDisplay = logToDisplay;
    }
}