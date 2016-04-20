package com.giroux.kevin.forecastapplication.utils.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.giroux.kevin.forecastapplication.R;
import com.giroux.kevin.forecastapplication.utils.AndroidHttpRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 20/04/2016. ForeCast Application
 */
    public class ForecastFragment extends Fragment {

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
            listForecast.add("Today 42 °C / 80 17");
            listForecast.add("Today 41 °C / 80 17");
            listForecast.add("Today 40 °C / 80 17");
            listForecast.add("Today 44 °C / 80 17");
            listForecast.add("Today 45 °C / 80 17");
            listForecast.add("Today 46 °C / 80 17");
            listForecast.add("Today 47 °C / 80 17");
            listForecast.add("Today 48 °C / 80 17");
            listForecast.add("Today 49 °C / 80 17");
            listForecast.add("Today 50 °C / 80 17");
            listForecast.add("Today 43 °C / 81 17");
            listForecast.add("Today 43 °C / 82 17");
            listForecast.add("Today 43 °C / 83 17");
            listForecast.add("Today 43 °C / 84 17");
            listForecast.add("Today 43 °C / 85 17");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,listForecast);
            ListView listViewForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
            listViewForecast.setAdapter(adapter);
            //http://api.openweathermap.org/data/2.5/forecast/daily?id=6444066&mode=json&units=metric&cnt=7&appid=2320d97c0bd0642e83e5f485369f61a5

            String url = "http://api.openweathermap.org/data/2.5/forecast/daily";
            String method = "GET";
            String param = "id=6444066&mode=json&units=metric&cnt=7&appid=2320d97c0bd0642e83e5f485369f61a5";

            Map<String,String> mapParam = new HashMap<>();
            mapParam.put("id","6444066");
            mapParam.put("mode","json");
            mapParam.put("units","metric");
            mapParam.put("cnt","7");
            mapParam.put("appid","2320d97c0bd0642e83e5f485369f61a5");

            AndroidHttpRequest androidHttpRequest = new AndroidHttpRequest(url,method,mapParam);
            androidHttpRequest.setEncoding("UTF-8");
            androidHttpRequest.setJSON(true);
            androidHttpRequest.execute();

            return rootView;
        }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    }