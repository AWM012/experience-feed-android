package com.bytecamp.experiencefeed;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {
    private ImageRepository repository;
    private boolean destroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ImageRepository.get(this);

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
        root.addView(createFeedColumns());
        setContentView(scrollView);
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
                badge.setText(result.source.label);
            }

            @Override
            public void onError(Throwable throwable) {
                if (destroyed) {
                    return;
                }
                badge.setText("加载失败");
                meta.setText(post.dateLabel + " · " + throwable.getMessage());
                Toast.makeText(MainActivity.this, "图片加载失败：" + post.title, Toast.LENGTH_SHORT).show();
            }
        });

        return card;
    }
}
