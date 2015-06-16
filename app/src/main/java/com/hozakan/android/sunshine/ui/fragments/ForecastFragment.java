package com.hozakan.android.sunshine.ui.fragments;

import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.hozakan.android.sunshine.R;
import com.hozakan.android.sunshine.tasks.FetchWeatherTask;
import com.hozakan.android.sunshine.ui.activities.DetailActivity;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements FetchWeatherTask.FetchWeatherTaskCallback {

    private static final String TAG = ForecastFragment.class.getSimpleName();

    public static ArrayList<String> content = new ArrayList<>(Arrays.asList("Monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"));

    //views
    private ListView mList;

    //technical
    private ArrayAdapter<String> mAdapter;

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
        mAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, new ArrayList<String>());
        mList.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(DetailActivity.createIntent(getActivity(), mAdapter.getItem(position)));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
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
        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();
        for (int i = 0; i < forecast.length; i++) {
            mAdapter.add(forecast[i]);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onWeatherTaskError() {

    }

    private void updateWeather() {
        FetchWeatherTask task = new FetchWeatherTask(getActivity(), this);
        task.execute(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getActivity().getString(R.string.pref_location_key), getActivity().getString(R.string.pref_location_default_value)));
    }
}