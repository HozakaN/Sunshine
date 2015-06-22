package com.hozakan.android.sunshine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hozakan.android.sunshine.data.WeatherContract;
import com.hozakan.android.sunshine.tools.Utility;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_TODAY = 1;
    private static final int[] TYPES = {TYPE_NORMAL, TYPE_TODAY};

    private final boolean mIsMetric;
    private LayoutInflater mInflater;

    //display logic
    private int mTmpLayoutId;
    private int mTmpImageResId;
    private boolean mUseTodayLayout;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mIsMetric = Utility.isMetric(context);
        mInflater = LayoutInflater.from(context);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        switch (getItemViewType(cursor.getPosition())) {
            case TYPE_NORMAL:
                mTmpLayoutId = R.layout.list_item_forecast;
                break;
            case TYPE_TODAY:
                mTmpLayoutId = R.layout.list_item_forecast_today;
                break;
            default:
                mTmpLayoutId = -1;
                break;
        }
        View view = mInflater.inflate(mTmpLayoutId, parent, false);
//        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

//    /*
//        This is where we fill-in the views with the contents of the cursor.
//     */
//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//        // our view is pretty simple here --- just a text view
//        // we'll keep the UI functional with a simple (and slow!) binding.
//
//        ViewHolder holder = (ViewHolder) view.getTag();
//        ContentValues cv = new ContentValues();
//        DatabaseUtils.cursorRowToContentValues(cursor, cv);
////        locationId = cv.getAsLong(WeatherContract.LocationEntry._ID);
////        holder.date.setText(cv.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE));
//        holder.date.setText(Utility.formatDate(cv.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE)));
//        holder.forecast.setText(cv.getAsString(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
//        holder.high.setText(Utility.formatTemperature(cv.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP), mIsMetric));
//        holder.low.setText(Utility.formatTemperature(cv.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP), mIsMetric));
//    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        ViewHolder holder = (ViewHolder) view.getTag();

        ContentValues cv = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, cv);
        // Read weather icon ID from cursor
        int weatherId = cv.getAsInteger(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID); //cursor.getInt(WeatherContract.COL_WEATHER_CONDITION_ID);
        // Use placeholder image for now
        switch (getItemViewType(cursor.getPosition())) {
            case TYPE_NORMAL:
                mTmpImageResId = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
            case TYPE_TODAY:
                mTmpImageResId = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            default:
                mTmpImageResId = -1;
                break;
        }
        if (mTmpImageResId != -1) {
            holder.icon.setImageResource(mTmpImageResId);
        }

        holder.icon.setContentDescription(cv.getAsString(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));

        holder.date.setText(Utility.getFriendlyDayString(context, cv.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE))); //cursor.getLong(WeatherContract.COL_WEATHER_DATE)));

//        holder.forecast.setText(cursor.getString(WeatherContract.COL_WEATHER_DESC));
        holder.forecast.setText(cv.getAsString(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));

        // Read high temperature from cursor
//        double high = cursor.getDouble(WeatherContract.COL_WEATHER_MAX_TEMP);
        double high = cv.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
        holder.high.setText(Utility.formatTemperature(context, high, mIsMetric));

//        double low = cursor.getDouble(WeatherContract.COL_WEATHER_MIN_TEMP);
        double low = cv.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
        holder.low.setText(Utility.formatTemperature(context, low, mIsMetric));

    }

    @Override
    public int getViewTypeCount() {
        return TYPES.length;
//        return 1;
    }

    @Override
    public int getItemViewType(int position) {
//        return TYPE_NORMAL;
        return (position == 0 && mUseTodayLayout) ? TYPE_TODAY : TYPE_NORMAL;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.mUseTodayLayout = useTodayLayout;
    }

    private class ViewHolder {

        public final View itemView;
        public final ImageView icon;
        public final TextView date;
        public final TextView forecast;
        public final TextView high;
        public final TextView low;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
            this.icon = (ImageView) itemView.findViewById(R.id.list_item_icon);
            this.date = (TextView) itemView.findViewById(R.id.list_item_date_textview);
            this.forecast = (TextView) itemView.findViewById(R.id.list_item_forecast_textview);
            this.high = (TextView) itemView.findViewById(R.id.list_item_high_textview);
            this.low = (TextView) itemView.findViewById(R.id.list_item_low_textview);
        }
    }
}
