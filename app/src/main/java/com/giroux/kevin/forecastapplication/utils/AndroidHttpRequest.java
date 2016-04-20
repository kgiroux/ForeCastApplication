package com.giroux.kevin.forecastapplication.utils;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.giroux.kevin.forecastapplication.utils.constants.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 18/04/2016. ForeCast Application
 */
public class AndroidHttpRequest extends AsyncTask<String[], Void, Object> {

    private boolean isJSON;
    private String encoding;
    private Object object;
    private int timeout;
    private String url;
    private String method;
    private Map<String, Object> listUiUpdate;
    private Map<String, String> listParam;
    private String paramStr;
    private Uri.Builder builderURL;

    public Map<String, String> getListParam() {
        return listParam;
    }

    public void setListParam(Map<String, String> listParam) {
        this.listParam = listParam;
    }

    public void setJSON(boolean JSON) {
        isJSON = JSON;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Map<String, Object> getListUiUpdate() {
        return listUiUpdate;
    }

    public void setListUiUpdate(Map<String, Object> listUiUpdate) {
        this.listUiUpdate = listUiUpdate;
    }

    public boolean isJSON() {
        return isJSON;
    }

    public Object getObject() {
        return object;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Method with all available parameter
     *
     * @param isJSON   if param is json
     * @param encoding if encoding is not UTF-8
     * @param object   return object type;
     * @param timeout  if default timeout is not 15000
     * @param url      URL for the request
     * @param method   Methode for the request
     * @param paramStr list of parameter Use createParamString static method for generate this paramter
     */
    public AndroidHttpRequest(boolean isJSON, String encoding, Object object, int timeout, String url, String method, Map<String, String> paramStr) {
        this.isJSON = isJSON;
        this.encoding = encoding;
        this.object = object;
        this.timeout = timeout;
        this.url = url;
        this.method = method;
        this.listUiUpdate = new HashMap<>();
        this.listParam = paramStr;
        builderURL = new Uri.Builder();
    }


    /**
     * Method with only minimal parameter
     *
     * @param url      url for the request
     * @param method   method for the request
     * @param paramStr list of parameter. Use createParamString static method for generate this paramter
     */
    public AndroidHttpRequest(String url, String method, Map<String, String> paramStr) {
        this.url = url;
        this.method = method;
        this.isJSON = true;
        this.encoding = Constants.DEFAULT_ENCODING_ANDROID_HTTP_REQUEST;
        this.timeout = Constants.DEFAULT_TIMEOUT;
        this.object = null;
        this.listUiUpdate = new HashMap<>();
        this.listParam = paramStr;
        builderURL = new Uri.Builder();
    }

    @Override
    protected Object doInBackground(String[]... params) {

        return executeRequest(createParamString(this.getListParam()), this.method, this.isJSON, this.encoding, this.timeout);
    }

    /**
     * Method that perform the request
     *
     * @param uri      url for the request
     * @param method   method for the request
     * @param isJSON   if it is json
     * @param encoding UTF-8 by default. Can be change with the setEncoding method
     * @param timeout  15 second by default Can be change with the setTimeout method
     * @return Object
     */
    private Object executeRequest(Uri uri, String method, boolean isJSON, String encoding, int timeout) {

        Log.d(Constants.TAG_ANDROID_HTTP_REQUEST, "executeRequest: perform a request");
        displayParamaterForTheRequest();
        URL url;
        HttpURLConnection urlConnection = null;
        String str;
        JSONObject json = null;
        OutputStream os;
        InputStream in;


        try {
            //Envoie des paramètre dans la requête au serveur
            //if ("POST".equals(method) && builderURL != null) {
            //Création de la connection et de l'url de la requête
            url = new URL(uri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();

            //Définition des paramètres de connexion
            urlConnection.setReadTimeout(timeout);
            urlConnection.setConnectTimeout(timeout);
            urlConnection.setDoInput(true);

            //Send parameters
            urlConnection.setRequestMethod(method);
            if (isJSON)
                urlConnection.setRequestProperty("Content-Type", "application/json; " + encoding);
            else
                urlConnection.setRequestProperty("Content-Type", "text; " + encoding);

            if (this.getMethod().equals(Constants.METHOD_POST)) {
                urlConnection.setDoOutput(true);
                if (paramStr != null) {
                    os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, encoding));
                    writer.write(paramStr);
                    writer.flush();
                    writer.close();
                    os.close();
                }
            }

            //Récupération des iformations de retour du serveur
            urlConnection.connect();
            in = new BufferedInputStream(urlConnection.getInputStream());
            str = streamToString(in);
            in.close();

            json = new JSONObject(str);

        } catch (Exception ex) {
            Log.e(Constants.TAG_ANDROID_HTTP_REQUEST, "Error decoding stream", ex);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return json;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (result instanceof JSONObject) {
            JSONObject resultFrom = (JSONObject) result;
            if (resultFrom.has("city")) {
                if (resultFrom.has("list")) {
                    try {
                        if (this.getObject() instanceof TextView) {
                            Log.d(Constants.TAG_ANDROID_HTTP_REQUEST, String.valueOf(this.getObject().getClass()));
                            TextView textView = (TextView) this.getObject();
                            textView.setText(((resultFrom.getJSONObject("city")).getString("name")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "AndroidHttpRequest{" +
                "isJSON=" + isJSON +
                ", encoding='" + encoding + '\'' +
                ", object=" + object +
                ", timeout=" + timeout +
                ", url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", listUiUpdate=" + listUiUpdate +
                '}';
    }

    public void displayParamaterForTheRequest() {
        String toDisplay = "AndroidHttpRequest : " +
                "\nJSON activated : " + isJSON +
                ", \nencoding : '" + encoding + '\'' +
                ", \nUI object : " + object +
                ", \nTimeout : " + timeout +
                ", \nUrl : '" + url + '\'' +
                ", \nMethod : '" + method + '\'' +
                ", \nlistUiUpdate : '" + listUiUpdate + '\'';
        Log.d(Constants.TAG_ANDROID_HTTP_REQUEST, toDisplay);
    }

    /**
     * Method that can help user to update view
     *
     * @param key   key on the result Object
     * @param value UI object that need to be update
     */
    public void addUIObjectToUpdate(String key, Object value) {
        this.listUiUpdate.put(key, value);
    }

    /**
     * Method that can help user to create a string for parameter
     *
     * @param params Map with list of parameter and the key for the request
     * @param isJson if it is json
     * @return list of paramater in String
     */
    @Deprecated
    public static String createParamString(Map<String, String> params, boolean isJson) {
        JSONObject result = new JSONObject();
        StringBuilder toSendGet = new StringBuilder();
        boolean isFirst = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            //Si premier element ne pas ajouter le signe &
            if (!isFirst && !isJson)
                toSendGet.append("&");
            else
                isFirst = false;
            //Encodage des informations du paramètre en UTF (clé, valeur)
            try {
                if (isJson)
                    result.put(entry.getKey(), entry.getValue());
                else {
                    toSendGet.append(entry.getKey());
                    toSendGet.append("=");
                    String textTOGet = entry.getValue();
                    toSendGet.append(textTOGet);
                }
            } catch (Exception ex) {
                Log.e(Constants.TAG_ANDROID_HTTP_REQUEST, "Create Param :", ex);
            }
        }
        if (isJson) {
            return result.toString();
        } else {
            return toSendGet.toString();
        }
    }


    public String getMethod() {
        return method;
    }

    private Uri createParamString(Map<String, String> listParam) {
        Uri url = Uri.parse(this.getUrl());
        builderURL.scheme(url.getScheme()).authority(url.getAuthority()).appendEncodedPath(url.getPath());

        for (Map.Entry<String, String> entrySet : listParam.entrySet()) {
            builderURL.appendQueryParameter(entrySet.getKey(), entrySet.getValue()).build();
        }

        if (this.getMethod().equals(Constants.METHOD_POST)) {
            this.paramStr = builderURL.build().getEncodedQuery();
        } else {
            url = builderURL.build();
        }
        return url;
    }

    /**
     * Method that convert a inputStream into a String
     *
     * @param in input stream
     * @return String with the result
     */
    private
    @NonNull
    String streamToString(@NonNull InputStream in) {
        InputStreamReader rd = new InputStreamReader(in);
        BufferedReader buffer = new BufferedReader(rd);
        StringBuilder strb = new StringBuilder();
        try {
            String str;
            while ((str = buffer.readLine()) != null)
                strb.append(str);
        } catch (Exception ex) {
            Log.e(Constants.TAG_ANDROID_HTTP_REQUEST, "Error decoding stream", ex);
        }
        return strb.toString();
    }
}
