package com.hozakan.android.sunshine.ui.fragments;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hozakan.android.sunshine.ForecastAdapter;
import com.hozakan.android.sunshine.R;
import com.hozakan.android.sunshine.tools.Utility;
import com.hozakan.android.sunshine.data.WeatherContract;
import com.hozakan.android.sunshine.tasks.FetchWeatherTask;
import com.hozakan.android.sunshine.ui.activities.DetailActivity;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements FetchWeatherTask.FetchWeatherTaskCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ForecastFragment.class.getSimpleName();

    private static final int LOADER_ID = 1;

    public static ArrayList<String> content = new ArrayList<>(Arrays.asList("Monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"));

    //views
    private ListView mList;

    //technical
    private ForecastAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mList = ((ListView)rootView.findViewById(R.id.listview_forecast));
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);

        mAdapter = new ForecastAdapter(getActivity(), cur, 0);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                startActivity(DetailActivity.createIntent(getActivity(), mAdapter.getItem(position)));
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(WeatherContract.COL_WEATHER_DATE)
                            ));
                    startActivity(intent);
                }
            }
        });

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
//        updateWeather();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateWeather();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWeatherTaskEnded(String[] forecast) {
//        mAdapter.setNotifyOnChange(false);
//        mAdapter.clear();
//        for (int i = 0; i < forecast.length; i++) {
//            mAdapter.add(forecast[i]);
//        }
//        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onWeatherTaskError() {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String location = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(location, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                WeatherContract.FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void updateWeather() {
        com.hozakan.android.sunshine.FetchWeatherTask task = new com.hozakan.android.sunshine.FetchWeatherTask(getActivity());
        task.execute(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getActivity().getString(R.string.pref_location_key), getActivity().getString(R.string.pref_location_default_value)));
    }
}
