package com.bytecamp.experiencefeed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends Activity {
    private static final String EXTRA_ID = "extra_id";
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_DESC = "extra_desc";
    private static final String EXTRA_DATE = "extra_date";
    private static final String EXTRA_LOCATION = "extra_location";
    private static final String EXTRA_URL = "extra_url";
    private static final String EXTRA_HEIGHT = "extra_height";

    private ImageRepository repository;
    private boolean destroyed;

    static Intent intentFor(Context context, ImagePost post) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_ID, post.id);
        intent.putExtra(EXTRA_TITLE, post.title);
        intent.putExtra(EXTRA_DESC, post.description);
        intent.putExtra(EXTRA_DATE, post.dateLabel);
        intent.putExtra(EXTRA_LOCATION, post.location);
        intent.putExtra(EXTRA_URL, post.imageUrl);
        intent.putExtra(EXTRA_HEIGHT, post.previewHeightDp);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ImageRepository.get(this);

        ImagePost post = readPost();

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

        root.addView(createHeader(post));

        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackgroundColor(UiKit.PLACEHOLDER);
        imageView.setContentDescription(post.title);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                UiKit.dp(this, 480)
        );
        imageParams.setMargins(UiKit.dp(this, 16), 0, UiKit.dp(this, 16), 0);
        root.addView(imageView, imageParams);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackground(UiKit.rounded(UiKit.CARD_BG, 8, this));
        UiKit.pad(panel, 16, 16, 16, 18);
        LinearLayout.LayoutParams panelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        panelParams.setMargins(UiKit.dp(this, 16), UiKit.dp(this, 12), UiKit.dp(this, 16), UiKit.dp(this, 24));
        panel.setPadding(
                UiKit.dp(this, 16),
                UiKit.dp(this, 16),
                UiKit.dp(this, 16),
                UiKit.dp(this, 18) + UiKit.navigationBarHeight(this)
        );
        root.addView(panel, panelParams);

        TextView title = UiKit.text(this, post.title, 23, UiKit.TEXT_PRIMARY, Typeface.BOLD);
        panel.addView(title);

        TextView description = UiKit.text(this, post.description, 15, UiKit.TEXT_SECONDARY, Typeface.NORMAL);
        description.setLineSpacing(UiKit.dp(this, 2), 1f);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.setMargins(0, UiKit.dp(this, 6), 0, UiKit.dp(this, 10));
        panel.addView(description, descParams);

        TextView source = addInfoRow(panel, "加载来源", "准备加载");
        TextView cacheNote = addInfoRow(panel, "缓存说明", "等待加载结果");
        TextView date = addInfoRow(panel, "图片日期", post.dateLabel);
        TextView location = addInfoRow(panel, "拍摄位置", post.location);
        TextView size = addInfoRow(panel, "图片大小", "待加载");
        TextView path = addInfoRow(panel, "存储位置", repository.cacheFileFor(post).getAbsolutePath());
        date.setText(post.dateLabel);
        location.setText(post.location);

        TextView reload = UiKit.text(this, "重新下载图片", 14, UiKit.ACCENT, Typeface.BOLD);
        reload.setGravity(Gravity.CENTER);
        reload.setBackground(UiKit.roundedStroke(0x00FFFFFF, 8, UiKit.ACCENT, 1, this));
        UiKit.pad(reload, 10, 10, 10, 10);
        LinearLayout.LayoutParams reloadParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        reloadParams.setMargins(0, UiKit.dp(this, 14), 0, 0);
        panel.addView(reload, reloadParams);

        reload.setOnClickListener(view -> {
            repository.removeCache(post);
            imageView.setImageDrawable(null);
            imageView.setBackgroundColor(UiKit.PLACEHOLDER);
            source.setText("重新获取中");
            cacheNote.setText("已清除当前图片缓存，重新请求网络");
            size.setText("待加载");
            load(post, imageView, source, cacheNote, size, path);
        });

        setContentView(scrollView);
        load(post, imageView, source, cacheNote, size, path);
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }

    private ImagePost readPost() {
        Intent intent = getIntent();
        return new ImagePost(
                intent.getStringExtra(EXTRA_ID),
                intent.getStringExtra(EXTRA_TITLE),
                intent.getStringExtra(EXTRA_DESC),
                intent.getStringExtra(EXTRA_DATE),
                intent.getStringExtra(EXTRA_LOCATION),
                intent.getStringExtra(EXTRA_URL),
                intent.getIntExtra(EXTRA_HEIGHT, 300)
        );
    }

    private View createHeader(ImagePost post) {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setMinimumHeight(UiKit.statusBarHeight(this) + UiKit.dp(this, 52));
        header.setPadding(
                UiKit.dp(this, 16),
                UiKit.statusBarHeight(this),
                UiKit.dp(this, 16),
                0
        );

        TextView back = UiKit.text(this, "‹", 32, UiKit.TEXT_PRIMARY, Typeface.NORMAL);
        back.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        back.setIncludeFontPadding(false);
        back.setContentDescription("返回");
        back.setClickable(true);
        back.setFocusable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true);
            back.setForeground(getDrawable(typedValue.resourceId));
        }
        header.addView(back, new LinearLayout.LayoutParams(
                UiKit.dp(this, 40),
                UiKit.dp(this, 48)
        ));
        back.setOnClickListener(view -> finish());

        TextView title = UiKit.text(this, post.title, 17, UiKit.TEXT_PRIMARY, Typeface.BOLD);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setIncludeFontPadding(false);
        title.setSingleLine(true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0,
                UiKit.dp(this, 48),
                1f
        );
        titleParams.setMargins(0, 0, 0, 0);
        header.addView(title, titleParams);

        return header;
    }

    private TextView addInfoRow(LinearLayout panel, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, UiKit.dp(this, 8), 0, 0);

        TextView labelView = UiKit.text(this, label, 13, UiKit.TEXT_TERTIARY, Typeface.NORMAL);
        row.addView(labelView, new LinearLayout.LayoutParams(
                UiKit.dp(this, 78),
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView valueView = UiKit.text(this, value, 13, UiKit.TEXT_PRIMARY, Typeface.NORMAL);
        valueView.setLineSpacing(UiKit.dp(this, 1), 1f);
        row.addView(valueView, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        panel.addView(row, rowParams);
        return valueView;
    }

    private void load(
            ImagePost post,
            ImageView imageView,
            TextView source,
            TextView cacheNote,
            TextView size,
            TextView path
    ) {
        repository.load(post, new ImageRepository.Callback() {
            @Override
            public void onSuccess(ImageLoadResult result) {
                if (destroyed) {
                    return;
                }
                imageView.setImageBitmap(result.bitmap);
                source.setText(result.source.label);
                cacheNote.setText(cacheExplanation(result.source));
                size.setText(UiKit.readableBytes(result.sizeBytes));
                path.setText(result.cacheFile.getAbsolutePath());
            }

            @Override
            public void onError(Throwable throwable) {
                if (destroyed) {
                    return;
                }
                source.setText("加载失败");
                cacheNote.setText("请检查模拟器网络或代理后重试");
                Toast.makeText(DetailActivity.this, "图片加载失败：" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String cacheExplanation(CacheSource source) {
        switch (source) {
            case NETWORK:
                return "首次获取该图，已写入本地缓存";
            case DISK:
                return "应用重启后复用本地缓存，未重复下载";
            case MEMORY:
            default:
                return "列表刚加载过该图，详情页直接复用内存";
        }
    }
}
