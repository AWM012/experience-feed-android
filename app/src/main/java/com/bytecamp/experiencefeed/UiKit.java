package com.bytecamp.experiencefeed;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

final class UiKit {
    static final int PAGE_BG = Color.rgb(248, 247, 242);
    static final int CARD_BG = Color.WHITE;
    static final int TEXT_PRIMARY = Color.rgb(28, 28, 28);
    static final int TEXT_SECONDARY = Color.rgb(112, 112, 112);
    static final int TEXT_TERTIARY = Color.rgb(150, 150, 150);
    static final int ACCENT = Color.rgb(230, 62, 73);
    static final int PLACEHOLDER = Color.rgb(234, 229, 219);

    private UiKit() {
    }

    static int dp(Context context, float value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics()
        );
    }

    static TextView text(Context context, String value, float sp, int color, int style) {
        TextView textView = new TextView(context);
        textView.setText(value);
        textView.setTextColor(color);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        textView.setTypeface(Typeface.DEFAULT, style);
        textView.setIncludeFontPadding(true);
        return textView;
    }

    static GradientDrawable rounded(int color, float radiusDp, Context context) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    static GradientDrawable roundedStroke(
            int color,
            float radiusDp,
            int strokeColor,
            float strokeWidthDp,
            Context context
    ) {
        GradientDrawable drawable = rounded(color, radiusDp, context);
        drawable.setStroke(dp(context, strokeWidthDp), strokeColor);
        return drawable;
    }

    static void pad(View view, int left, int top, int right, int bottom) {
        Context context = view.getContext();
        view.setPadding(dp(context, left), dp(context, top), dp(context, right), dp(context, bottom));
    }

    static int statusBarHeight(Context context) {
        return systemDimension(context, "status_bar_height");
    }

    static int navigationBarHeight(Context context) {
        return systemDimension(context, "navigation_bar_height");
    }

    private static int systemDimension(Context context, String name) {
        int resourceId = context.getResources().getIdentifier(name, "dimen", "android");
        if (resourceId == 0) {
            return 0;
        }
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    static String readableBytes(long bytes) {
        if (bytes <= 0) {
            return "待加载";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format(java.util.Locale.US, "%.1f KB", kb);
        }
        return String.format(java.util.Locale.US, "%.1f MB", kb / 1024.0);
    }

    static void makeLightStatusBar(View root) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}
