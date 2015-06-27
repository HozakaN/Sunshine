package com.hozakan.android.sunshine.ui.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
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
import com.hozakan.android.sunshine.data.WeatherContract;
import com.hozakan.android.sunshine.sync.SunshineSyncAdapter;
import com.hozakan.android.sunshine.tools.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ForecastFragment.class.getSimpleName();

    private static final int LOADER_ID = 1;

    private static final String POSITION_KEY = "POSITION_KEY";

//    public static ArrayList<String> content = new ArrayList<>(Arrays.asList("Monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"));

    public interface ForecastFragmentCallback {
        void onItemSelected(long date);
    }

    //views
    private ListView mList;

    //technical
    private ForecastAdapter mAdapter;
    private ForecastFragmentCallback mCallback;

    //display logic
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;
    private boolean mTwoPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (ForecastFragmentCallback) activity;
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
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

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
        }

        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

//        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
//                null, null, null, sortOrder);

        mAdapter = new ForecastAdapter(getActivity(), null, 0);
        mAdapter.setUseTodayLayout(mUseTodayLayout);
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
                    ContentValues cv = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cursor, cv);
//                            ((ForecastFragmentCallback) getActivity()).onItemSelected(cursor.getLong(WeatherContract.COL_WEATHER_DATE));
                    if (mCallback != null) {
                        mCallback.onItemSelected(cv.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE));
                    }
//
//                    Intent intent = new Intent(getActivity(), DetailActivity.class)
//                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                                    locationSetting, cursor.getLong(WeatherContract.COL_WEATHER_DATE)
//                            ));
//                    startActivity(intent);
                    mPosition = position;
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
//        getLoaderManager().initLoader(LOADER_ID, null, this);
        if (mAdapter.getCount() > mPosition) {
            if (mPosition != ListView.INVALID_POSITION) {
//            mList.setSelection(mPosition);
                mList.smoothScrollToPosition(mPosition);
            } else if (mTwoPage) {
                setSelectionForTwoPaneMode(0);
            }
        }
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
//            case R.id.action_refresh:
//                updateWeather();
//                break;
            case R.id.action_show_map:
                openPreferredLocationInMap();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(POSITION_KEY, mPosition);
        }
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
        if (mPosition != ListView.INVALID_POSITION && mAdapter.getCount() > mPosition) {
//            mList.setSelection(mPosition);
            if (mTwoPage) {
                setSelectionForTwoPaneMode(mPosition);
            }
            mList.smoothScrollToPosition(mPosition);
        } else if (mTwoPage) {
            setSelectionForTwoPaneMode(0);
            mList.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public void setTwoPage(boolean twoPage) {
        mTwoPage = twoPage;
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mAdapter != null) {
            mAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    private void updateWeather() {
//        com.hozakan.android.sunshine.FetchWeatherTask task = new com.hozakan.android.sunshine.FetchWeatherTask(getActivity());
//        task.execute(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getActivity().getString(R.string.pref_location_key), getActivity().getString(R.string.pref_location_default_value)));
//        Intent serviceIntent = SunshineService.createIntent(
//                getActivity(), Utility.getPreferredLocation(getActivity()));
//        getActivity().startService(serviceIntent);
//        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//        Intent intent = SunshineService.AlarmReceiver.createIntent(getActivity(), Utility.getPreferredLocation(getActivity()));
//        PendingIntent pIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pIntent);
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if ( null != mAdapter ) {
            Cursor c = mAdapter.getCursor();
            if ( null != c ) {
                c.moveToPosition(0);
                String posLat = c.getString(WeatherContract.COL_COORD_LAT);
                String posLong = c.getString(WeatherContract.COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    private void setSelectionForTwoPaneMode(int position) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        if (cursor != null) {
            ContentValues cv = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, cv);
            mList.setItemChecked(position, true);
            if (mCallback != null) {
                mCallback.onItemSelected(cv.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE));
            }
            mPosition = position;
        }
    }
}
