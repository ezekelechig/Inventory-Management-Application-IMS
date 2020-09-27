package com.inventorymanagementsystem.ims.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by dac-android on 7/20/16.
 */
public class FontAwesomeTextView extends TextView {

    public FontAwesomeTextView(Context context) {
        super(context);

        applyCustomFont(context);
    }

    public FontAwesomeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        applyCustomFont(context);
    }

    public FontAwesomeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        applyCustomFont(context);
    }

    private void applyCustomFont(Context context) {
        Typeface customFont = FontManager.getTypeface(context, FontManager.FONTAWESOME);
        setTypeface(customFont);
    }
}