package com.micronet.udpsampleappv2.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.micronet.udpsampleappv2.BuildConfig;
import com.micronet.udpsampleappv2.R;

public class DeviceInfoFragment extends Fragment {

    final static String TAG = "device-info";

    TextView txtViewOSV;
    TextView txtViewAndroidBuild;
    TextView txtViewDeviceModel;
    TextView txtViewSerial;
    TextView txtViewAppVersion;
    TextView txtViewHardwareVersion;
    TextView txtViewCanBusVersion;
    TextView txtViewDeviceName;

    // Declare variables to store device's info.
    String serial;
    String model;
    String androidBuildVersion;
    String osv;
    String appVersion;
    String deviceName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_info, container, false);

        //Getting device information.
        model = Build.MODEL;
        androidBuildVersion = Build.VERSION.RELEASE;
        osv = Build.DISPLAY;
        appVersion = BuildConfig.VERSION_NAME;

        /**
         * Get Serial Number
         *
         * Since new version android os requires permission to get the device serial number,
         * First check the device os version,
         * if is or newer then Android O, check for user permission and get serial number.
         * **/
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.d(TAG, "OS is too new, need user permission");
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "permission denied");
                serial = "N/A";
            }else{
                serial = Build.getSerial();
            }
        }else{
            serial = Build.SERIAL;
        }

        //This switch case is going to match the device name with the device model.
        switch(model){
            case "TREQr_5":
                deviceName = "SmartHub";
                Log.d(TAG, "deviceName: " +deviceName);
                break;
            case "TREQ_5":
                deviceName = "SmartTab";
                Log.d(TAG, "deviceName: " + deviceName);
                break;
            case "MSTab8":
                deviceName = "SmartTab 8";
                Log.d(TAG, "deviceName: " + deviceName);
                break;
            default:
                deviceName = "Unknown";
                Log.d(TAG,"deviceName: " + deviceName);
                break;
        }

        //Locate the related TextView on the UI.
        txtViewOSV = view.findViewById(R.id.txtViewOSV);
        txtViewAndroidBuild = view.findViewById(R.id.txtViewAndroidBuild);
        txtViewDeviceModel = view.findViewById(R.id.txtViewDeviceModel);
        txtViewSerial = view.findViewById(R.id.txtViewSerial);
        txtViewAppVersion = view.findViewById(R.id.txtViewAppVersion);
        txtViewHardwareVersion = view.findViewById(R.id.txtViewHardwareVersion);
        txtViewCanBusVersion = view.findViewById(R.id.txtViewCanBusVersion);
        txtViewDeviceName = view.findViewById(R.id.txtViewDeviceName);

        //Setting text into TextView;
        txtViewSerial.setText(serial);
        txtViewDeviceModel.setText(model);
        txtViewDeviceName.setText(deviceName);
        txtViewAndroidBuild.setText(androidBuildVersion);
        txtViewOSV.setText(osv);
        txtViewAppVersion.setText(appVersion);

        return view;
    }
}
