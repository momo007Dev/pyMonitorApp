package com.example.pymonitormobile;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

public class Networking {
    /**
     //--------------------------------------------------------
     // Function that checks if user is connected to internet
     //--------------------------------------------------------
     * @param activity
     * @return (boolean) true if user is connected, false otherwise
     */
    protected boolean isConnectedToInternet(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean result = networkInfo != null && networkInfo.isConnected();
        return result;
    }

    /**
     //----------------------------------------------------------------------------
     // Function that returns the ip of the raspberry pi domain name (jodie.local)
     //----------------------------------------------------------------------------
     * @param (String) Ip of the host
     * @return (boolean) true if user is connected, false otherwise
     */
    protected boolean isHostUp(String host_ip){
        try
        {   // (ping arguments) -c 1 => 1 echo request | -w 2 => Wait for 2 sec max
            Process  process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -w 1" + host_ip);
            int status = process.waitFor();
            System.out.println("Host is UP !");
            return true;
        }
        catch (InterruptedException | IOException ignore){}
        System.out.println("Host is DOWN...");
        return false;
    }
}
