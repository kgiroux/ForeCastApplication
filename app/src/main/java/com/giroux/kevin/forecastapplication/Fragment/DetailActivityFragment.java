package com.giroux.kevin.forecastapplication.Fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.giroux.kevin.forecastapplication.R;
import com.giroux.kevin.forecastapplication.data.WeatherContract;
import com.giroux.kevin.forecastapplication.utils.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String fromIntent;
    private static String FORECAST_SHARE_HASHTAG ="#SunshineAPP";
    private ShareActionProvider mShareActionProvider;
    private final int DETAIL_LOADER = 0;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_WIND_SPEED = 6;
    private static final int COL_WEATHER_PRESSURE = 7;
    private static final int COL_WEATHER_DEGREES = 8;
    private static final int COL_WEATHER_CONDITION = 9;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null ) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d("DetailActivity", "Share Action Provider is null?");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                fromIntent  + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v("LOADER", "Create loader");
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            return new CursorLoader(getActivity(), intent.getData(), DETAIL_COLUMNS, null, null, null);
        } else
            return null;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v("LOADER", "In onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }
        String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));

        String weatherString = data.getString(COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(getContext());


        String max = Utility.formatTemperature(getActivity().getApplicationContext(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String min = Utility.formatTemperature(getActivity().getApplicationContext(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        fromIntent = String.format("%s - %s - %s/%s", dateString, weatherString, max, min);

        ViewHolder viewHolder = new ViewHolder(getView());

        viewHolder.highTempView.setText(max);
        int id = Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION));
        viewHolder.iconView.setImageResource(id);
        viewHolder.lowTempView.setText(min);
        viewHolder.humidityView.setText(getActivity().getString(R.string.format_humidity,data.getFloat(COL_WEATHER_HUMIDITY)));
        viewHolder.pressureView.setText(getActivity().getString(R.string.format_pressure,data.getDouble(COL_WEATHER_PRESSURE)));
        viewHolder.windView.setText(Utility.getFormattedWind(getContext(),data.getFloat(COL_WEATHER_WIND_SPEED),data.getFloat(COL_WEATHER_DEGREES)));
        viewHolder.descriptionView.setText(data.getString(COL_WEATHER_DESC));

        viewHolder.friendlyView.setText(Utility.getDayName(getContext(),data.getLong(COL_WEATHER_DATE)));
        viewHolder.dateView.setText(Utility.getFormattedMonthDay(getContext(),data.getLong(COL_WEATHER_DATE)));

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    private static class ViewHolder {
        ImageView iconView;
        TextView dateView;
        TextView descriptionView;
        TextView highTempView;
        TextView lowTempView;
        TextView pressureView;
        TextView humidityView;
        TextView windView;
        TextView friendlyView;

        public ViewHolder(View view) {
            friendlyView = (TextView)view.findViewById(R.id.list_item_date_textview_friendly);
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            humidityView = (TextView) view.findViewById(R.id.humidity_detail_activity);
            pressureView = (TextView) view.findViewById(R.id.presure_detail_activity);
            windView = (TextView) view.findViewById(R.id.wind_detail_activity);
        }
    }
}
