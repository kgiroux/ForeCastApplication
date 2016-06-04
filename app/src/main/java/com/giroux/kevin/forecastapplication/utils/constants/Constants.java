package com.giroux.kevin.forecastapplication.utils.constants;

/**
 * Created by kevin on 18/04/2016. ForeCast Application
 */
public class Constants {

    public static final String TAG_ANDROID_HTTP_REQUEST = "Android HTTP request";
    public static final String DEFAULT_ENCODING_ANDROID_HTTP_REQUEST = "UTF-8";
    public static final int DEFAULT_TIMEOUT = 15000;
    public static final String METHOD_POST = "POST";
    public static final String METHOD_RESULT_OK = "OK";
    public static final String CST_TIMEOUT ="Timeout for the request : {} with the parameter {} . Try to check the URL or increase the timeout value";
    public static final String CST_NO_CONTENT = "No content to return from the server";
    public static final String CST_BAD_REQUEST = "There is a problem with the request {} with {} parameter. Please check the result" ;
    public static final String CST_INTERNAL_ERROR = "Serveur Error. Please check the status of the server";
    public static final String CST_OTHER_ERROR = "Another error appear during the request : {}";
    public static final String CST_BAD_METHOD = " This request {} not allowed to use {} method. Please check the server configuration or the method on the AndroidHttpRequest declaration";
    public static final String CST_NO_FOUND = "Nothing found for the following url {} with these parameter {} for this method {}";

    public static String createLog(String cst, String ...params){
        for (String param : params) {
            cst = cst.replaceFirst("\\{", param);
            cst = cst.replaceFirst("\\}", "");
        }
        return cst;
    }
}
