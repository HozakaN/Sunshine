package com.hozakan.android.sunshine.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.*;
import android.widget.TextView;
import com.hozakan.android.sunshine.R;
import com.hozakan.android.sunshine.data.WeatherContract;
import com.hozakan.android.sunshine.tools.WeatherDataParser;

public class DetailActivity extends AppCompatActivity {

    private static final String EXTRA_DATA_KEY = "EXTRA_DATA_KEY";

    private ShareActionProvider mShareActionProvider;

    public static Intent createIntent(Context context, String data) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_DATA_KEY, data);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, PlaceholderFragment.newInstance(getIntent().getStringExtra(EXTRA_DATA_KEY)))
                    .add(R.id.container, PlaceholderFragment.newInstance(getIntent().getDataString()))
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(SettingsActivity.createIntent(this));
                break;
            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String ARG_DATA_KEY = "ARG_DATA_KEY";

        private static final int DETAIL_LOADER = 1;

        public static PlaceholderFragment newInstance(String data) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_DATA_KEY, data);
            fragment.setArguments(args);
            return fragment;
        }

        //views
        private TextView mTvForecast;
        private ShareActionProvider mShareActionProvider;

        //model attributes
        private String mData;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mData = getArguments().getString(ARG_DATA_KEY);

            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            mTvForecast = (TextView) rootView.findViewById(R.id.tv_forecast);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
//            mTvForecast.setText(mData);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_detail_fragment, menu);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(),
                    Uri.parse(mData),
                    WeatherContract.FORECAST_COLUMNS,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data.moveToFirst()) {
                mTvForecast.setText(WeatherDataParser.convertCursorRowToUXFormat(getActivity(), data));
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareForecastIntent());
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mTvForecast.setText("");
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mTvForecast.getText().toString() + " #SunshineApp");
            return shareIntent;
        }
    }
}
