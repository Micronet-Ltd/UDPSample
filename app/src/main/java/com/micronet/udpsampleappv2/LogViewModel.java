package com.micronet.udpsampleappv2;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class LogViewModel extends ViewModel {
    /**
     * This class is handling all the LiveData that is lifeCycle-Aware.
     * Which means, these data needed to be update on the UI in real-time.
     * **/
    final private static String TAG = "logview-model";

    // Declare all the target live data as MutableLiveData<String>.
   private static MutableLiveData<String> logRecord = new MutableLiveData<>();
   private static MutableLiveData<String> internetState = new MutableLiveData<>();
   private static MutableLiveData<String> deviceIP = new MutableLiveData<>();
   private static MutableLiveData<String> signalStrength = new MutableLiveData<>();
   private static MutableLiveData<String> simState = new MutableLiveData<>();

   //Getting the LogRecord for LogFragment
    public static MutableLiveData<String> getLogRecord(){
        if (logRecord == null){
            logRecord = new MutableLiveData<>();
        }
        return logRecord;
    }

    /**
     * The following methods will handle device information to be used in the UDPServiceFragment.
     * **/
    public static MutableLiveData<String> getInternetState(){
        if (internetState == null){
            internetState = new MutableLiveData<>();
        }
        return internetState;
    }

    public static MutableLiveData<String> getDeviceIP(){
        if (deviceIP == null){
            deviceIP = new MutableLiveData<>();
        }
        return deviceIP;
    }

    public static MutableLiveData<String> getSignalStrength(){
        if (signalStrength == null){
            signalStrength = new MutableLiveData<>();
        }
        return signalStrength;
    }

    public static MutableLiveData<String> getSimState(){
        if (simState == null){
            simState = new MutableLiveData<>();
        }
        return simState;
    }
}
