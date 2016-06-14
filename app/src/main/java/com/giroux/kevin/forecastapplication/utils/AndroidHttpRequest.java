package com.giroux.kevin.forecastapplication.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.giroux.kevin.forecastapplication.R;
import com.giroux.kevin.forecastapplication.data.WeatherContract;
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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

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

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    private  Context mContext;

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

        getWeatherDataFromJson((JSONObject) result);

        if (this.listUiUpdate.get("adapter") instanceof ArrayAdapter<?>) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) this.listUiUpdate.get("adapter");
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


    private void getWeatherDataFromJson(JSONObject object) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();

        if (object.has("list")) {
            JSONArray arrayJson;
            try {
                JSONObject cityList = object.getJSONObject("city");
                arrayJson = object.getJSONArray("list");
                String description = "";
                String day;
                Double max = 0.0;
                Double min = 0.0;
                Vector<ContentValues> cVVector = new Vector<>(arrayJson.length());
                for (int i = 0; i < arrayJson.length(); i++) {
                    JSONObject forcastObject = arrayJson.getJSONObject(i);
                    gregorianCalendar.add(GregorianCalendar.DAY_OF_YEAR, 0);
                    day = getDayReadable(gregorianCalendar.getTime().getTime());
                    if (forcastObject.has("weather"))
                        description = forcastObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    if (forcastObject.has("temp") && forcastObject.getJSONObject("temp").has("max") && forcastObject.getJSONObject("temp").has("min")) {
                        max = forcastObject.getJSONObject("temp").getDouble("max");
                        min = forcastObject.getJSONObject("temp").getDouble("min");
                    }
                    Log.e("Test",forcastObject.toString());
                    int humidity = forcastObject.getInt("humidity");
                    int pressure = forcastObject.getInt("pressure");
                    int windSpeed = forcastObject.getInt("speed");
                    int windDirection = forcastObject.getInt("deg");

                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(((Activity)this.getObject()).getApplicationContext());
                    String locationSetting = sharedPrefs.getString(((Activity)this.getObject()).getString(R.string.pref_key_location),((Activity)this.getObject()).getString(R.string.pref_value_location));
                    long weatherId = forcastObject.getJSONArray("weather").getJSONObject(0).getLong("id");
                    String cityName = cityList.getString("name");
                    double cityLatitude = cityList.getJSONObject("coord").getDouble("lat");
                    double cityLongitude = cityList.getJSONObject("coord").getDouble("lon");

                    long locationId = addLocation(locationSetting,cityName,cityLatitude,cityLongitude);

                    ContentValues weatherValues = new ContentValues();

                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, gregorianCalendar.getTime().getTime()+1);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, max);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, min);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                    weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                    cVVector.add(weatherValues);

                }
                int inserted = 0;
                if ( cVVector.size() > 0 ) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    inserted = mContext.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
                }

                Log.d("Test", "FetchWeatherTask Complete. " + inserted + " Inserted");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    private String getDayReadable(Long time) {
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd", Locale.FRANCE);
        return shortenedDateFormat.format(time);
    }


    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName        A human-readable city name, e.g "Mountain View"
     * @param lat             the latitude of the city
     * @param lon             the longitude of the city
     * @return the row ID of the added location.
     */
    public long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;

        // First, check if the location with this city name exists in the db
        Cursor locationCursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues locationValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_LATITUDE, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_LONGITUDE, lon);

            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();
        // Wait, that worked?  Yes!
        return locationId;
    }
}
