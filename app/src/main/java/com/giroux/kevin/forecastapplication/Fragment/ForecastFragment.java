package com.giroux.kevin.forecastapplication.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.giroux.kevin.forecastapplication.R;
import com.giroux.kevin.forecastapplication.activity.DetailActivity;
import com.giroux.kevin.forecastapplication.activity.MainActivity;
import com.giroux.kevin.forecastapplication.activity.SettingsActivity;
import com.giroux.kevin.forecastapplication.utils.AndroidHttpRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 20/04/2016. ForeCast Application
 */
public class ForecastFragment extends Fragment {
    private ArrayAdapter<String> adapter;
    private ListView listViewForecast;
    public ForecastFragment(){

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_main_activity,container,false);

        ArrayList<String> listForecast = new ArrayList<>();
        listForecast.add("Today 43 °C / 80 17");

        adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, listForecast);
        listViewForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        listViewForecast.setAdapter(adapter);

        listViewForecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getContext(), adapter.getItem(position), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT,adapter.getItem(position));
                startActivity(intent);
            }
        });
            //http://api.openweathermap.org/data/2.5/forecast/daily?id=6444066&mode=json&units=metric&cnt=7&appid=2320d97c0bd0642e83e5f485369f61a5

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        initAsyncTask();
    }

    private void initAsyncTask(){
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
        androidHttpRequest.setListUiUpdate(uiObject);
        androidHttpRequest.setObject(getActivity());
        androidHttpRequest.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       // super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment_menu, menu);

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.forecast_fragment_menu, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            initAsyncTask();
        }else if(id == R.id.action_settings){
            Intent t = new Intent(getActivity(), SettingsActivity.class);
            startActivity(t);
        }

        return super.onOptionsItemSelected(item);
    }
    }