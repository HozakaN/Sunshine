package com.hozakan.android.sunshine.ui.fragments;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import com.hozakan.android.sunshine.R;
import com.hozakan.android.sunshine.data.WeatherContract;
import com.hozakan.android.sunshine.tools.Utility;
import com.hozakan.android.sunshine.tools.WeatherDataParser;
import com.hozakan.android.sunshine.ui.MyView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 1;

    private static final String ARG_DATE_KEY = "ARG_DATE_KEY";
    private static final String LOCATION_KEY = "LOCATION_KEY";

    public static DetailFragment newInstance(long date) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DATE_KEY, date);
        fragment.setArguments(args);
        return fragment;
    }

    //views
    private ImageView mIcon;
    private TextView mTvForecast;
    private TextView mTvDay;
    private TextView mTvDate;
    private TextView mTvHigh;
    private TextView mTvLow;
    private TextView mTvHumidity;
    private MyView mTvWind;
//    private TextView mTvWind;
    private TextView mTvPressure;
    private ShareActionProvider mShareActionProvider;

    //display logic
    private String mForeCast;
//    private Uri mUri;

    //model attributes
    private long mDate;
    private String mLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDate = getArguments().getLong(ARG_DATE_KEY);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIcon = (ImageView) rootView.findViewById(R.id.icon);
        mTvForecast = (TextView) rootView.findViewById(R.id.forecast_textview);
        mTvDay = (TextView) rootView.findViewById(R.id.day_textview);
        mTvDate = (TextView) rootView.findViewById(R.id.date_textview);
        mTvHigh = (TextView) rootView.findViewById(R.id.high_textview);
        mTvLow = (TextView) rootView.findViewById(R.id.low_textview);
        mTvHumidity = (TextView) rootView.findViewById(R.id.humidity_textview);
//        mTvWind = (TextView) rootView.findViewById(R.id.wind_textview);
        mTvWind = (MyView) rootView.findViewById(R.id.wind_textview);
        mTvPressure = (TextView) rootView.findViewById(R.id.pressure_textview);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null &&
                !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_fragment, menu);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
        mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LOCATION_KEY, mLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                mLocation, mDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                WeatherContract.FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            updateScreen(data);
            if (mShareActionProvider != null) {
                mForeCast = WeatherDataParser.convertCursorRowToUXFormat(getActivity(), data);
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    public void onDateChanged(long newDate) {
        mDate = newDate;
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }

    public void onLocationChanged(String newLocation) {
        mLocation = newLocation;
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForeCast + " #SunshineApp");
        return shareIntent;
    }

    private void updateScreen(Cursor cursor) {
        boolean isMetric = Utility.isMetric(getActivity());
        mTvDay.setText(Utility.getFriendlyDayString(getActivity(), cursor.getLong(WeatherContract.COL_WEATHER_DATE)));
        mTvDate.setText(Utility.getFormattedMonthDay(getActivity(), cursor.getLong(WeatherContract.COL_WEATHER_DATE)));
        mTvHigh.setText(Utility.formatTemperature(getActivity(), cursor.getDouble(WeatherContract.COL_WEATHER_MAX_TEMP), isMetric));
        mTvLow.setText(Utility.formatTemperature(getActivity(), cursor.getDouble(WeatherContract.COL_WEATHER_MIN_TEMP), isMetric));
        mTvHumidity.setText(getString(R.string.format_humidity, cursor.getFloat(WeatherContract.COL_WEATHER_HUMIDITY)));
//        mTvWind.setText(Utility.getFormattedWind(getActivity(), cursor.getFloat(WeatherContract.COL_WEATHER_WIND), cursor.getFloat(WeatherContract.COL_WEATHER_DEGREES)));
        mTvWind.setWindDirection(cursor.getFloat(WeatherContract.COL_WEATHER_DEGREES));
        mTvPressure.setText(getString(R.string.format_pressure, cursor.getFloat(WeatherContract.COL_WEATHER_PRESSURE)));
        int artResource = Utility.getArtResourceForWeatherCondition(cursor.getInt(WeatherContract.COL_WEATHER_CONDITION_ID));
        if (artResource != -1) {
            mIcon.setImageResource(artResource);
        }
        mIcon.setContentDescription(cursor.getString(WeatherContract.COL_WEATHER_DESC));
        mTvForecast.setText(cursor.getString(WeatherContract.COL_WEATHER_DESC));
    }
}
