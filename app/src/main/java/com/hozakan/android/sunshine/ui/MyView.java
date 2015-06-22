package com.hozakan.android.sunshine.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.text.DynamicLayout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.hozakan.android.sunshine.R;
import com.hozakan.android.sunshine.tools.Utility;

/**
 * Created by gimbert on 15-06-23.
 */
public class MyView extends View {

    private Paint mPaint;

    private float windDirection;
    private String windDirectionText = "";

    public MyView(Context context) {
        super(context);
        init();
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpec = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpec = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = widthSpec;
        int height = heightSpec;

        Rect bounds = new Rect();
        mPaint.getTextBounds(windDirectionText, 0, windDirectionText.length(), bounds);

        if (widthMode != MeasureSpec.EXACTLY) {
            width = (int) (mPaint.measureText(windDirectionText, 0, windDirectionText.length()) + getPaddingLeft() + getPaddingRight());
//            width = bounds.width() + getPaddingLeft() + getPaddingRight();
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSpec);
            }
        }

        if (heightMode != MeasureSpec.EXACTLY) {
//            height = bounds.height() + getPaddingTop() + getPaddingBottom();
            height = (int) (-mPaint.ascent() + mPaint.descent()) + getPaddingTop()
                    + getPaddingBottom();
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSpec);
            }
        }
//        if (heightMode == MeasureSpec.AT_MOST) {
//            height = Math.min(height, bounds.height());


//        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.ascent();
//        canvas.drawCircle(0f, 0f, 50, mPaint);
        canvas.drawText(windDirectionText, getPaddingLeft(), getPaddingTop() - mPaint.ascent(), mPaint);
//        canvas.drawText(windDirectionText, getPaddingLeft(), getMeasuredHeight(), mPaint);
//        canvas.drawLine(0, 0, getMeasuredWidth(), 0, mPaint);
//        canvas.drawLine(0, 0, 0, getMeasuredHeight(), mPaint);
//        canvas.drawLine(0, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight(), mPaint);
//        canvas.drawLine(getMeasuredWidth(), 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.getText().add(windDirectionText);
        return true;
    }

    public void setWindDirection(float windDirection) {
        final String oldWindDirectionText = windDirectionText;
        this.windDirection = windDirection;
        this.windDirectionText = Utility.getFormattedWindDirection(windDirection);
        if (!windDirectionText.equals(oldWindDirectionText)) {
            requestLayout(); //call to onMeasure for the parent hierarchy
            invalidate(); // call to onDraw for this view
        }
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(dp2px(22));
//        mPaint.setTextSize(150);
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
