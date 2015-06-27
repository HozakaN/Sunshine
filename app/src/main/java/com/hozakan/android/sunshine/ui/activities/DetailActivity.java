package com.hozakan.android.sunshine.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.*;

import com.hozakan.android.sunshine.R;
import com.hozakan.android.sunshine.ui.fragments.DetailFragment;

public class DetailActivity extends AppCompatActivity {

    private static final String EXTRA_DATE_KEY = "EXTRA_DATE_KEY";

    public static Intent createIntent(Context context, long date) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_DATE_KEY, date);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, DetailFragment.newInstance(getIntent().getLongExtra(EXTRA_DATE_KEY, 0)))
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
}
