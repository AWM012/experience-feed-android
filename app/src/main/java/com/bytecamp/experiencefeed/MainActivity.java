package com.bytecamp.experiencefeed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Build;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String WEATHER_PREFERENCES = "weather_preferences";
    private static final String WEATHER_CITY_KEY = "weather_city";
    private static final String DEFAULT_WEATHER_CITY = "上海";

    private ImageRepository repository;
    private WeatherRepository weatherRepository;
    private TextView weatherCity;
    private TextView weatherTemperature;
    private TextView weatherCondition;
    private TextView weatherMeta;
    private TextView weatherChange;
    private String selectedWeatherCity = DEFAULT_WEATHER_CITY;
    private boolean destroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ImageRepository.get(this);
        weatherRepository = WeatherRepository.get(this);
        selectedWeatherCity = getSharedPreferences(WEATHER_PREFERENCES, MODE_PRIVATE)
                .getString(WEATHER_CITY_KEY, DEFAULT_WEATHER_CITY);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(UiKit.PAGE_BG);
        UiKit.makeLightStatusBar(scrollView);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.PAGE_BG);
        scrollView.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        root.addView(createTopTabs());
        root.addView(createTitleBlock());
        root.addView(createWeatherCard());
        root.addView(createFeedColumns());
        setContentView(scrollView);
        loadWeather(selectedWeatherCity);
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }

    private View createTopTabs() {
        HorizontalScrollView wrapper = new HorizontalScrollView(this);
        wrapper.setHorizontalScrollBarEnabled(false);
        wrapper.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        tabs.setMinimumHeight(UiKit.statusBarHeight(this) + UiKit.dp(this, 52));
        tabs.setPadding(
                UiKit.dp(this, 16),
                UiKit.statusBarHeight(this) + UiKit.dp(this, 8),
                UiKit.dp(this, 16),
                UiKit.dp(this, 8)
        );

        tabs.addView(tab("关注", false));
        tabs.addView(tab("推荐", false));
        tabs.addView(tab("经验", true));
        tabs.addView(tab("附近", false));
        tabs.addView(tab("热点", false));

        wrapper.addView(tabs, new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.WRAP_CONTENT,
                HorizontalScrollView.LayoutParams.WRAP_CONTENT
        ));
        return wrapper;
    }

    private TextView tab(String text, boolean selected) {
        TextView tab = UiKit.text(this, text, selected ? 20 : 17,
                selected ? UiKit.TEXT_PRIMARY : UiKit.TEXT_SECONDARY,
                selected ? Typeface.BOLD : Typeface.NORMAL);
        tab.setGravity(Gravity.CENTER);
        UiKit.pad(tab, 10, 4, 10, 6);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, UiKit.dp(this, 12), 0);
        tab.setLayoutParams(params);
        if (selected) {
            tab.setBackground(UiKit.roundedStroke(
                    0x00FFFFFF,
                    1,
                    UiKit.ACCENT,
                    0,
                    this
            ));
        }
        return tab;
    }

    private View createTitleBlock() {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        UiKit.pad(block, 16, 4, 16, 12);

        TextView title = UiKit.text(this, "今天想收集的生活瞬间", 24, UiKit.TEXT_PRIMARY, Typeface.BOLD);
        title.setLetterSpacing(0f);
        block.addView(title);

        TextView subtitle = UiKit.text(this, "12 张图片 · 双列浏览", 13, UiKit.TEXT_TERTIARY, Typeface.NORMAL);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, UiKit.dp(this, 2), 0, 0);
        block.addView(subtitle, subtitleParams);

        return block;
    }

    private View createWeatherCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiKit.rounded(UiKit.CARD_BG, 8, this));
        UiKit.pad(card, 14, 12, 14, 12);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(
                UiKit.dp(this, 12),
                0,
                UiKit.dp(this, 12),
                UiKit.dp(this, 14)
        );
        card.setLayoutParams(cardParams);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView label = UiKit.text(this, "实时天气", 13, UiKit.TEXT_SECONDARY, Typeface.BOLD);
        header.addView(label, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        weatherChange = UiKit.text(this, "修改地点", 12, UiKit.ACCENT, Typeface.BOLD);
        weatherChange.setGravity(Gravity.CENTER);
        weatherChange.setBackground(UiKit.roundedStroke(
                0x00FFFFFF,
                8,
                UiKit.ACCENT,
                1,
                this
        ));
        UiKit.pad(weatherChange, 10, 5, 10, 5);
        weatherChange.setOnClickListener(view -> showCityDialog());
        header.addView(weatherChange);
        card.addView(header);

        LinearLayout current = new LinearLayout(this);
        current.setOrientation(LinearLayout.HORIZONTAL);
        current.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams currentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        currentParams.setMargins(0, UiKit.dp(this, 6), 0, 0);

        weatherTemperature = UiKit.text(this, "--°", 38, UiKit.TEXT_PRIMARY, Typeface.BOLD);
        weatherTemperature.setIncludeFontPadding(false);
        current.addView(weatherTemperature);

        LinearLayout detail = new LinearLayout(this);
        detail.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        detailParams.setMargins(UiKit.dp(this, 14), 0, 0, 0);

        weatherCity = UiKit.text(this, selectedWeatherCity, 17, UiKit.TEXT_PRIMARY, Typeface.BOLD);
        weatherCity.setMaxLines(1);
        detail.addView(weatherCity);

        weatherCondition = UiKit.text(this, "正在获取实时天气", 13, UiKit.TEXT_SECONDARY, Typeface.NORMAL);
        weatherCondition.setMaxLines(1);
        detail.addView(weatherCondition);

        current.addView(detail, detailParams);
        card.addView(current, currentParams);

        weatherMeta = UiKit.text(this, "正在查询城市并更新天气", 11, UiKit.TEXT_TERTIARY, Typeface.NORMAL);
        weatherMeta.setMaxLines(2);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        metaParams.setMargins(0, UiKit.dp(this, 8), 0, 0);
        card.addView(weatherMeta, metaParams);

        return card;
    }

    private void loadWeather(String city) {
        String requestedCity = city == null ? DEFAULT_WEATHER_CITY : city.trim();
        weatherCity.setText(requestedCity);
        weatherTemperature.setText("--°");
        weatherCondition.setText("正在获取实时天气");
        weatherMeta.setText("正在查询城市并更新天气");
        weatherChange.setEnabled(false);
        weatherChange.setAlpha(0.55f);

        weatherRepository.load(requestedCity, new WeatherRepository.Callback() {
            @Override
            public void onSuccess(WeatherRepository.WeatherResult result) {
                if (destroyed) {
                    return;
                }
                selectedWeatherCity = result.city;
                getSharedPreferences(WEATHER_PREFERENCES, MODE_PRIVATE)
                        .edit()
                        .putString(WEATHER_CITY_KEY, result.city)
                        .apply();

                weatherCity.setText(result.city);
                weatherTemperature.setText(String.format(Locale.CHINA, "%.0f°", result.temperature));
                weatherCondition.setText(result.condition + " · 体感 "
                        + String.format(Locale.CHINA, "%.0f°", result.apparentTemperature));
                String areaPrefix = result.area.isEmpty() ? "" : result.area + " · ";
                weatherMeta.setText(areaPrefix + "湿度 " + result.humidity + "% · 风速 "
                        + String.format(Locale.CHINA, "%.1f km/h", result.windSpeed)
                        + " · " + result.updateTime);
                finishWeatherLoading();
            }

            @Override
            public void onError(Throwable throwable) {
                if (destroyed) {
                    return;
                }
                weatherCondition.setText("天气加载失败");
                weatherMeta.setText(readableWeatherError(throwable));
                finishWeatherLoading();
                Toast.makeText(
                        MainActivity.this,
                        "天气加载失败，可点击“修改地点”重试",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void finishWeatherLoading() {
        weatherChange.setEnabled(true);
        weatherChange.setAlpha(1f);
    }

    private String readableWeatherError(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return "暂时无法获取天气，请稍后重试";
        }
        return message;
    }

    private void showCityDialog() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        UiKit.pad(content, 24, 0, 24, 0);

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("例如：上海、北京、杭州");
        input.setText(selectedWeatherCity);
        input.setSelectAllOnFocus(true);
        content.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("修改天气地点")
                .setView(content)
                .setNegativeButton("取消", null)
                .setPositiveButton("查询", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String city = input.getText().toString().trim();
                    if (city.isEmpty()) {
                        input.setError("请输入城市名称");
                        return;
                    }
                    dialog.dismiss();
                    loadWeather(city);
                }));
        dialog.show();
    }

    private View createFeedColumns() {
        LinearLayout columns = new LinearLayout(this);
        columns.setOrientation(LinearLayout.HORIZONTAL);
        columns.setGravity(Gravity.TOP);
        columns.setPadding(
                UiKit.dp(this, 12),
                0,
                UiKit.dp(this, 12),
                UiKit.dp(this, 18) + UiKit.navigationBarHeight(this)
        );

        LinearLayout left = column();
        LinearLayout right = column();
        Space gap = new Space(this);

        columns.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        columns.addView(gap, new LinearLayout.LayoutParams(UiKit.dp(this, 10), 1));
        columns.addView(right, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        int leftHeight = 0;
        int rightHeight = 0;
        List<ImagePost> posts = FeedData.posts();
        for (ImagePost post : posts) {
            View card = createCard(post);
            int estimatedHeight = post.previewHeightDp + 130;
            if (leftHeight <= rightHeight) {
                left.addView(card);
                leftHeight += estimatedHeight;
            } else {
                right.addView(card);
                rightHeight += estimatedHeight;
            }
        }
        return columns;
    }

    private LinearLayout column() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        return column;
    }

    private View createCard(ImagePost post) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setClipToOutline(true);
        card.setBackground(UiKit.rounded(UiKit.CARD_BG, 8, this));
        card.setClickable(true);
        card.setFocusable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
            card.setForeground(getDrawable(typedValue.resourceId));
        }

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, UiKit.dp(this, 12));
        card.setLayoutParams(cardParams);

        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackgroundColor(UiKit.PLACEHOLDER);
        imageView.setContentDescription(post.title);
        card.addView(imageView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                UiKit.dp(this, post.previewHeightDp)
        ));

        TextView title = UiKit.text(this, post.title, 15, UiKit.TEXT_PRIMARY, Typeface.BOLD);
        title.setMaxLines(2);
        UiKit.pad(title, 10, 10, 10, 0);
        card.addView(title);

        TextView desc = UiKit.text(this, post.description, 12, UiKit.TEXT_SECONDARY, Typeface.NORMAL);
        desc.setMaxLines(2);
        UiKit.pad(desc, 10, 2, 10, 0);
        card.addView(desc);

        TextView meta = UiKit.text(this, post.dateLabel + " · 待加载 · " + post.location,
                11,
                UiKit.TEXT_TERTIARY,
                Typeface.NORMAL);
        meta.setMaxLines(2);
        UiKit.pad(meta, 10, 4, 10, 0);
        card.addView(meta);

        TextView storage = UiKit.text(this, "存储：待缓存",
                10,
                UiKit.TEXT_TERTIARY,
                Typeface.NORMAL);
        storage.setSingleLine(true);
        UiKit.pad(storage, 10, 3, 10, 0);
        card.addView(storage);

        TextView badge = UiKit.text(this, "准备加载", 11, UiKit.ACCENT, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER_VERTICAL);
        badge.setBackground(UiKit.rounded(0xFFFFEEF0, 99, this));
        UiKit.pad(badge, 8, 4, 8, 4);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        badgeParams.setMargins(UiKit.dp(this, 10), UiKit.dp(this, 8), UiKit.dp(this, 10), UiKit.dp(this, 10));
        card.addView(badge, badgeParams);

        card.setOnClickListener(view -> {
            Intent intent = DetailActivity.intentFor(this, post);
            startActivity(intent);
        });

        repository.load(post, new ImageRepository.Callback() {
            @Override
            public void onSuccess(ImageLoadResult result) {
                if (destroyed) {
                    return;
                }
                imageView.setImageBitmap(result.bitmap);
                meta.setText(post.dateLabel + " · " + UiKit.readableBytes(result.sizeBytes) + " · " + post.location);
                storage.setText("存储：" + shortCachePath(result.cacheFile));
                badge.setText(result.source.label);
            }

            @Override
            public void onError(Throwable throwable) {
                if (destroyed) {
                    return;
                }
                badge.setText("加载失败");
                meta.setText(post.dateLabel + " · " + throwable.getMessage());
                storage.setText("存储：待缓存");
                Toast.makeText(MainActivity.this, "图片加载失败：" + post.title, Toast.LENGTH_SHORT).show();
            }
        });

        return card;
    }

    private String shortCachePath(java.io.File cacheFile) {
        java.io.File parent = cacheFile.getParentFile();
        if (parent == null) {
            return cacheFile.getName();
        }
        return parent.getName() + "/" + cacheFile.getName();
    }
}
