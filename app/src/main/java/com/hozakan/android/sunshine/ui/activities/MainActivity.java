package com.hozakan.android.sunshine.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.hozakan.android.sunshine.R;
import com.hozakan.android.sunshine.data.WeatherContract;
import com.hozakan.android.sunshine.sync.SunshineSyncAdapter;
import com.hozakan.android.sunshine.tools.Utility;
import com.hozakan.android.sunshine.ui.fragments.DetailFragment;
import com.hozakan.android.sunshine.ui.fragments.ForecastFragment;

public class MainActivity extends AppCompatActivity implements ForecastFragment.ForecastFragmentCallback {

    private static final String FORECASTFRAGMENT_TAG = "FORECASTFRAGMENT_TAG";
    private static final String DETAILFRAGMENT_TAG = "DETAILFRAGMENT_TAG";

    //model attributes
    private String mLocation;

    //display logic
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;

//            if (savedInstanceState == null) {
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.weather_detail_container, DetailFragment.newInstance(null), DETAILFRAGMENT_TAG)
//                        .commit();
//            }
        } else {
            mTwoPane = false;
//            getSupportActionBar().setElevation(0f);
        }

        ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        if (ff != null) {
            ff.setUseTodayLayout(!mTwoPane);
            ff.setTwoPage(mTwoPane);
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mLocation = preferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default_value));

        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String lastLocation = Utility.getPreferredLocation(this);
        if (lastLocation != null && !lastLocation.equals(mLocation)) {
            ((ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast)).onLocationChanged();
            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (df != null) {
                df.onLocationChanged(lastLocation);
            }
            mLocation = lastLocation;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(SettingsActivity.createIntent(this));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onItemSelected(final long date) {
        if (!mTwoPane) {
            Intent intent = DetailActivity.createIntent(this, date);

            startActivity(intent);
        } else {
            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (df == null) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.weather_detail_container, DetailFragment.newInstance(date), DETAILFRAGMENT_TAG)
                                .commit();
                    }
                });
            } else {
                df.onDateChanged(date);
            }
        }
    }

    private void showMap() {
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", mLocation).build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
