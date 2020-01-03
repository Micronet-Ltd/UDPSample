package com.micronet.udpsampleappv2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.micronet.udpsampleappv2.Fragments.UDPServiceFragment;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.micronet.udpsampleappv2.LogViewModel.getDeviceIP;
import static com.micronet.udpsampleappv2.LogViewModel.getInternetState;

public class DeviceDataReceiver {
    final static String TAG = "data-broadcast";



    static UDPServiceFragment udpServiceFragment = new UDPServiceFragment();
    MainActivity mainActivity = new MainActivity();

    public BroadcastReceiver singalStrengthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkInternetState(context);
            getOneIPV4();
            getSignalStrength(context);
            getSimState(context);
        }
    };

    public String checkInternetState(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        String internetState = "UNKNOWN";

        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED){
            internetState = "ONLINE";
            //mainActivity.updateInternetState(internetState);
            //udpServiceFragment.setTextForInternetState(internetState);
            getInternetState().postValue(internetState);
            Log.d(TAG, "checkInternetState: " + internetState);
            return internetState;
        }else if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED){
            internetState = "OFFLINE";
            //mainActivity.updateInternetState(internetState);
            //udpServiceFragment.setTextForInternetState(internetState);
            getInternetState().postValue(internetState);
            Log.d(TAG, "checkInternetState: "+internetState);
        }
        return internetState;
    }

    public String getOneIPV4(){
        ArrayList<String> list = new ArrayList<String>();
        String result = "Unavailable";
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
        getDeviceIP().postValue(result);
        return result;
    }

    public String getSignalStrength(Context context) throws SecurityException {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String strength = "Unavailable";
        int mSignalStrength = 0;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo Info = cm.getActiveNetworkInfo();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            //

            if (Info == null || !Info.isConnectedOrConnecting()) {
                Log.i(TAG, "No connection");
            } else {
                int netType = Info.getType();
                int netSubtype = Info.getSubtype();

                if (netType == ConnectivityManager.TYPE_WIFI) {
                    Log.i(TAG, "Wifi connection");
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

                    List<ScanResult> scanResult = wifiManager.getScanResults();
                    for (int i = 0; i < scanResult.size(); i++) {
                        Log.d("scanResult", "Speed of wifi"+scanResult.get(i).level);//The db level of signal
                    }


                    // Need to get wifi strength
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                    List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
                    Log.i(TAG, "GPRS/3G connection");

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
                    // Need to get differentiate between 3G/GPRS

                    strength = (strength + " dBm");
                    LogViewModel.getSignalStrength().postValue(strength);
                    //Todo: signal strength will not update after enable and disable airplane mode, work on this.
                }
            }
        }else{

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
            strength = (strength + " dBm");
            LogViewModel.getSignalStrength().postValue(strength);
        }
        return strength;
    }

    public String getSimState(Context context){
        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String simStateText = "N/A";
        int simState = telMgr.getSimState();
        Log.d(TAG, "Got here");

        switch(simState){
            case TelephonyManager.SIM_STATE_ABSENT:
                Log.d(TAG,"SIM_ABSENT");
                simStateText = "Absent";
                LogViewModel.getSimState().postValue(simStateText);
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                Log.d(TAG,"SIM_LOCKED");
                simStateText = "Locked";
                LogViewModel.getSimState().postValue(simStateText);
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                Log.d(TAG,"SIM_PIN_REQUIRED");
                simStateText = "Pin Required";
                LogViewModel.getSimState().postValue(simStateText);
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                Log.d(TAG,"SIM_PUK_REQUIRED");
                simStateText = "Puk Required";
                LogViewModel.getSimState().postValue(simStateText);
                break;
            case TelephonyManager.SIM_STATE_READY:
                Log.d(TAG,"SIM_READY");
                simStateText = "Ready";
                LogViewModel.getSimState().postValue(simStateText);
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                Log.d(TAG,"SIM_UNKNOWN");
                simStateText = "Unknown";
                LogViewModel.getSimState().postValue(simStateText);
                break;
        }
        LogViewModel.getSimState().postValue(simStateText);
        return simStateText;
    }

}


