package com.giroux.kevin.forecastapplication.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.giroux.kevin.forecastapplication.R;
import com.giroux.kevin.forecastapplication.activity.DetailActivity;
import com.giroux.kevin.forecastapplication.activity.SettingsActivity;
import com.giroux.kevin.forecastapplication.data.WeatherContract;
import com.giroux.kevin.forecastapplication.utils.AndroidHttpRequest;
import com.giroux.kevin.forecastapplication.utils.ForecastAdapter;
import com.giroux.kevin.forecastapplication.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 20/04/2016. ForeCast Application
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private ForecastAdapter  adapter;
    private final int FORECAST_LOADER = 1;
    private ListView listViewForecast;
    public ForecastFragment(){

    }

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_LATITUDE,
            WeatherContract.LocationEntry.COLUMN_LOCATION_LONGITUDE
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_COORD_LAT = 7;
    public static final int COL_COORD_LONG = 8;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_main_activity,container,false);
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        String locationSetting = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
        locationSetting, System.currentTimeMillis());
        //Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri, null, null, null, sortOrder);
        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.
        adapter = new ForecastAdapter(getActivity(), null, 0);
        listViewForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        listViewForecast.setAdapter(adapter);
        //http://api.openweathermap.org/data/2.5/forecast/daily?id=6444066&mode=json&units=metric&cnt=7&appid=2320d97c0bd0642e83e5f485369f61a5

        listViewForecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if(cursor != null){
                    String locationSetting = Utility.getPreferredLocation(getContext());
                    Intent t = new Intent(getActivity(),DetailActivity.class);
                    t.setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting,cursor.getLong(COL_WEATHER_DATE)));
                    startActivity(t);
                }
            }
        });
        return rootView;
    }

    /*@Override
    public void onStart() {
        super.onStart();

        updateWeather();
    }*/

    private void updateWeather(){
        String url = "http://api.openweathermap.org/data/2.5/forecast/daily";
        String method = "GET";
        String param = "id=6444066&mode=json&units=metric&cnt=7&appid=2320d97c0bd0642e83e5f485369f61a5";

        //http://api.openweathermap.org/data/2.5/forecast?q=78210,fr&appid=2320d97c0bd0642e83e5f485369f61a5

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String zipCode = preferences.getString(getString(R.string.pref_key_location),getString(R.string.pref_value_location)) +",fr";

        Map<String,String> mapParam = new HashMap<>();
        mapParam.put("q",zipCode);
        mapParam.put("mode","json");
        mapParam.put("units","metric");
        mapParam.put("cnt","7");
        mapParam.put("appid","2320d97c0bd0642e83e5f485369f61a5");

        Map<String,Object> uiObject = new HashMap<>();
        uiObject.put("adapter",adapter);

        AndroidHttpRequest androidHttpRequest = new AndroidHttpRequest(url,method,mapParam);
        androidHttpRequest.setEncoding("UTF-8");
        androidHttpRequest.setJSON(false);
        androidHttpRequest.setmContext(getActivity().getApplicationContext());
        androidHttpRequest.setListUiUpdate(uiObject);
        androidHttpRequest.setObject(getActivity());
        androidHttpRequest.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       // super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment_menu, menu);

    }

    public void onLocationChange(){
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER,null,this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            updateWeather();
        }else if(id == R.id.action_settings){
            Intent t = new Intent(getActivity(), SettingsActivity.class);
            startActivity(t);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getContext(),weatherForLocationUri,FORECAST_COLUMNS,null,null,sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
