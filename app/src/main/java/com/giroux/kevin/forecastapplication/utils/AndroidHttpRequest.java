package com.giroux.kevin.forecastapplication.utils;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.giroux.kevin.forecastapplication.utils.constants.Constants;

import org.json.JSONArray;
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
import java.util.Map;

/**
 * Created by kevin on 18/04/2016. ForeCast Application
 */
public class AndroidHttpRequest extends AsyncTask<String[], Void, Object> {

    private boolean isJSON;
    private String encoding;
    private boolean needOutput;
    private Object object;
    private int timeout;
    private String url;
    private String method;
    private String paramStr;

    public void setJSON(boolean JSON) {
        isJSON = JSON;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setNeedOutput(boolean needOutput) {
        this.needOutput = needOutput;
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

    /**
     *
     * @param isJSON if param is json
     * @param encoding if encoding is not UTF-8
     * @param needOutput if need a result;
     * @param object return object type;
     * @param timeout if default timeout is not 15000
     * @param url  URL for the request
     * @param method Methode for the request
     * @param paramStr list of parameter
     */
    public AndroidHttpRequest(boolean isJSON, String encoding, boolean needOutput, Object object, int timeout, String url, String method, String paramStr) {
        this.isJSON = isJSON;
        this.encoding = encoding;
        this.needOutput = needOutput;
        this.object = object;
        this.timeout = timeout;
        this.url = url;
        this.method = method;
        this.paramStr = paramStr;
    }

    public boolean isJSON() {
        return isJSON;
    }

    public boolean isNeedOutput() {
        return needOutput;
    }

    public Object getObject() {
        return object;
    }

    public AndroidHttpRequest(String url, String method, String paramStr) {
        this.url = url;
        this.method = method;
        this.paramStr = paramStr;
        this.isJSON = true;
        this.needOutput = true;
        this.encoding = Constants.DEFAULT_ENCODING_ANDROID_HTTP_REQUEST;
        this.timeout = Constants.DEFAULT_TIMEOUT;
        this.object = null;
    }

    @Override
    protected Object doInBackground(String[]... params) {
        return executeRequest(this.url, this.method, this.paramStr, this.isJSON, this.needOutput, this.encoding, this.timeout);
    }




    public Object executeRequest(String url, String method,String paramsStr, boolean isJSON, boolean needOutput,String encoding, int timeout){

        Log.d(Constants.TAG_ANDROID_HTTP_REQUEST, "executeRequest: perform a request");

        URL uri;
        HttpURLConnection urlConnection = null;
        String str;
        JSONObject json = null;
        OutputStream os;
        InputStream in;
        try {
            //Envoie des paramètre dans la requête au serveur
            if ("POST".equals(method) && paramsStr != null) {
                //Création de la connection et de l'url de la requête
                uri = new URL(url);
                urlConnection = (HttpURLConnection) uri.openConnection();

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

                if (needOutput)
                    urlConnection.setDoOutput(true);

                //Ecriture dans le flux de la chaîne des paramètres
                os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, encoding));
                writer.write(paramsStr);
                writer.flush();
                writer.close();
                os.close();

            } else {
                //Création de la connection et de l'url de la requête
                String newUrl = url + "?" + paramsStr;
                uri = new URL(newUrl);
                urlConnection = (HttpURLConnection) uri.openConnection();

                //Définition des paramètres de connexion
                urlConnection.setReadTimeout(timeout);
                urlConnection.setConnectTimeout(timeout);
                urlConnection.setDoInput(true);

                urlConnection.setRequestMethod("GET");
                if (isJSON)
                    urlConnection.setRequestProperty("Content-Type", "application/json; " + encoding);
                else
                    urlConnection.setRequestProperty("Content-Type", "text; " + encoding);

                if (needOutput)
                    urlConnection.setDoOutput(true);
            }

            //Récupération des iformations de retour du serveur
            urlConnection.connect();
            in = new BufferedInputStream(urlConnection.getInputStream());
            str = streamToString(in);
            in.close();

            json = new JSONObject(str);

        } catch (Exception ex) {
            Log.e(Constants.TAG_ANDROID_HTTP_REQUEST,"Error decoding stream",ex);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return json;
    }


    @Override
    protected void onPostExecute(Object result) {

        if(result instanceof JSONObject){
            JSONObject resultFrom = (JSONObject) result;
            if(resultFrom.has("city")){
                if(resultFrom.has("list")){
                    try {
                        if(object instanceof TextView){
                            TextView textView = (TextView)object;
                            textView.setText(((resultFrom.getJSONObject("city")).getString("name")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


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


    private String streamToString(InputStream in) {
        InputStreamReader rd = new InputStreamReader(in);
        BufferedReader buffer = new BufferedReader(rd);
        StringBuilder strb = new StringBuilder();
        try {
            String str;
            while ((str = buffer.readLine()) != null)
                strb.append(str);
        } catch (Exception ex) {
            Log.e(Constants.TAG_ANDROID_HTTP_REQUEST,"Error decoding stream" ,ex);
        }
        return strb.toString();
    }
}
