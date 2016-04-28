package com.giroux.kevin.forecastapplication.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.giroux.kevin.forecastapplication.R;
import com.giroux.kevin.forecastapplication.utils.constants.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by kevin on 18/04/2016. ForeCast Application
 */
public class AndroidHttpRequest extends AsyncTask<String[], Void, Object> {

    private boolean JSON;
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
        this.JSON = JSON;
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

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setListUiUpdate(Map<String, Object> listUiUpdate) {
        this.listUiUpdate = listUiUpdate;
    }

    public Object getObject() {
        return object;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isJSON() {
        return JSON;
    }

    public String getParamStr() {
        return paramStr;
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
        this.setJSON(isJSON);
        this.setEncoding(encoding);
        this.setObject(object);
        this.setTimeout(timeout);
        this.setUrl(url);
        this.setMethod(method);
        this.setListParam(paramStr);
        this.setListUiUpdate(new HashMap<String,Object>());
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
        this.url = (url);
        this.method = (method);
        this.JSON = true;
        this.encoding = Constants.DEFAULT_ENCODING_ANDROID_HTTP_REQUEST;
        this.timeout = Constants.DEFAULT_TIMEOUT;
        this.object = (null);
        this.listUiUpdate = new HashMap<>();
        this.listParam = (paramStr);
        builderURL = new Uri.Builder();
    }

    @Override
    protected Object doInBackground(String[]... params) {
        return executeRequest(createParamString(this.getListParam()));
    }



    /**
     * Method that perform the request
     *
     * @param uri      url for the request
     * @return Object
     */
    private Object executeRequest(Uri uri) {

        Log.d(Constants.TAG_ANDROID_HTTP_REQUEST, "executeRequest: perform a request");
        displayParamaterForTheRequest();
        URL url;
        HttpURLConnection urlConnection = null;
        JSONObject json = null;
        OutputStream os;

        try {
            //Création de la connexion et de l'url de la requête
            url = new URL(uri.toString());
            this.setUrl(new URL(uri.toString()).toString());
            urlConnection = (HttpURLConnection) url.openConnection();

            //Définition des paramètres de connexion
            urlConnection.setReadTimeout(this.timeout);
            urlConnection.setConnectTimeout(this.timeout);
            urlConnection.setDoInput(true);

            //Send parameters
            urlConnection.setRequestMethod(this.method);
            if (this.JSON)
                urlConnection.setRequestProperty("Content-Type", "application/json; " + this.encoding);
            else
                urlConnection.setRequestProperty("Content-Type", "text; " + this.encoding);


            if (this.method.equals(Constants.METHOD_POST)) {
                urlConnection.setDoOutput(true);
                if (this.getParamStr() != null) {
                    os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, this.encoding));
                    writer.write(this.getParamStr());
                    writer.flush();
                    writer.close();
                    os.close();
                }
            }
            //Récupération des iformations de retour du serveur
            urlConnection.connect();
            json = performedCheckCodeMessage(urlConnection);
        } catch (IOException | JSONException ex ) {
            Log.e(Constants.TAG_ANDROID_HTTP_REQUEST, "Error decoding stream", ex);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return json;
    }

    private JSONObject performedCheckCodeMessage(HttpURLConnection urlConnection) throws IOException, JSONException{
        InputStream in;
        String str;
        JSONObject json = new JSONObject();
        switch(urlConnection.getResponseCode()){
            //200
            case HttpURLConnection.HTTP_OK :

                Log.e(Constants.TAG_ANDROID_HTTP_REQUEST," Response OK");
                in = new BufferedInputStream(urlConnection.getInputStream());
                str = streamToString(in);
                in.close();
                json = new JSONObject(str);
                break;
            // 204
            case HttpURLConnection.HTTP_NO_CONTENT:
                Log.d(Constants.TAG_ANDROID_HTTP_REQUEST,Constants.createLog(Constants.CST_NO_CONTENT));
                break;
            // 400
            case HttpURLConnection.HTTP_BAD_REQUEST :
                Log.e(Constants.TAG_ANDROID_HTTP_REQUEST,Constants.createLog(Constants.CST_BAD_REQUEST,url ,this.method));
                break;
            //401
            case HttpURLConnection.HTTP_NOT_FOUND :
                Log.e(Constants.TAG_ANDROID_HTTP_REQUEST,Constants.createLog(Constants.CST_NO_FOUND,url,this.paramStr,this.method));
                break;
            // 405
            case HttpURLConnection.HTTP_BAD_METHOD :
                Log.d(Constants.TAG_ANDROID_HTTP_REQUEST,Constants.createLog(Constants.CST_BAD_METHOD, url, this.method));
                break;
            // 408
            case HttpURLConnection.HTTP_CLIENT_TIMEOUT :
                // Retry the request
                urlConnection.disconnect();
                urlConnection.connect();
                if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK){
                    Log.e(Constants.TAG_ANDROID_HTTP_REQUEST,Constants.createLog(Constants.CST_TIMEOUT, url,this.paramStr));
                }
                break;
            // 500
            case HttpURLConnection.HTTP_INTERNAL_ERROR :
                Log.e(Constants.TAG_ANDROID_HTTP_REQUEST,Constants.createLog(Constants.CST_INTERNAL_ERROR, url,this.paramStr));
                break;
            default :
                Log.e(Constants.TAG_ANDROID_HTTP_REQUEST,Constants.createLog(Constants.CST_OTHER_ERROR, String.valueOf(urlConnection.getResponseCode())));
                break;
        }
        return json;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onPostExecute(Object result) {

        List<String> listWeather = getWeatherDataFromJson((JSONObject) result);

        if (this.listUiUpdate.get("adapter") instanceof ArrayAdapter<?>) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) this.listUiUpdate.get("adapter");
            if (adapter != null) {
                adapter.clear();
                adapter.addAll(listWeather);
            }
        }
    }

    @Override
    public String toString() {
        return "AndroidHttpRequest{" +
                "isJSON=" + JSON +
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
                "\nJSON activated : " + JSON +
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
        JSONObject object = new JSONObject();
        builderURL.scheme(url.getScheme()).appendEncodedPath(url.getPath());
        if(this.isJSON()){
            try{

                for(Map.Entry<String,String> entrySet : listParam.entrySet()){
                    object.put(entrySet.getKey(),entrySet.getValue());
                }
                this.paramStr = object.toString();
            }catch(JSONException ex){
                Log.e(Constants.TAG_ANDROID_HTTP_REQUEST,"Error during rendering JSON",ex);
            }
        }else{
            for (Map.Entry<String, String> entrySet : listParam.entrySet()) {
                builderURL.appendQueryParameter(entrySet.getKey(), entrySet.getValue()).build();
            }

        }
        if (this.getMethod().equals(Constants.METHOD_POST) && !this.isJSON()) {
            this.paramStr = builderURL.build().getQuery();
        } else if(!this.isJSON()){
            this.paramStr = builderURL.build().getQuery();
            url = builderURL.authority(url.getAuthority()).build();
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


    private ArrayList<String> getWeatherDataFromJson(JSONObject object) {
        ArrayList<String> listWeather = new ArrayList<>();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        if (object.has("list")) {
            JSONArray arrayJson;
            try {
                arrayJson = object.getJSONArray("list");
                String description = "";
                String day;
                Double max = 0.0;
                Double min = 0.0;
                for (int i = 0; i < arrayJson.length(); i++) {
                    JSONObject temp = arrayJson.getJSONObject(i);
                    gregorianCalendar.add(GregorianCalendar.DAY_OF_YEAR, 1);
                    day = getDayReadable(gregorianCalendar.getTime());
                    if (temp.has("weather"))
                        description = temp.getJSONArray("weather").getJSONObject(0).getString("description");
                    if (temp.has("temp") && temp.getJSONObject("temp").has("max") && temp.getJSONObject("temp").has("min")) {
                        max = temp.getJSONObject("temp").getDouble("max");
                        min = temp.getJSONObject("temp").getDouble("min");
                    }

                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(((Activity)this.getObject()).getApplicationContext());
                                String unitType = sharedPrefs.getString(
                                        ((Activity)this.getObject()).getString(R.string.pref_units_key),
                                        ((Activity)this.getObject()).getString(R.string.pref_units_metric));

                    formatHighLows(max,min,unitType);
                    String toStore = day + " " + description + " " + formatHighLows(max,min,unitType);;
                    listWeather.add(toStore);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return listWeather;
    }

    private String formatHighLows(double high, double low, String unitType) {

        if (unitType.equals(((Activity)this.getObject()).getString(R.string.pref_units_imperial))) {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        } else if (!unitType.equals(((Activity)this.getObject()).getString(R.string.pref_units_metric))) {
            Log.d(Constants.TAG_ANDROID_HTTP_REQUEST, "Unit type not found: " + unitType);
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        return roundedHigh + "/" + roundedLow;
    }


    private String getDayReadable(Date time) {
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd", Locale.FRANCE);
        return shortenedDateFormat.format(time);
    }
}
