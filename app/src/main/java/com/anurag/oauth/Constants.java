package com.anurag.oauth;

public class Constants {
    private static boolean local = true;
    private static String localS = "http://10.0.2.2:3000";
    private static String remoteS = "https://droidoauth.onrender.com";
    public static String server = local ? localS : remoteS ;
}
