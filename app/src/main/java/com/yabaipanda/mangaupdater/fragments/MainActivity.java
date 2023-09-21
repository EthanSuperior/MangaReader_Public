package com.yabaipanda.mangaupdater.fragments;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.yabaipanda.mangaupdater.R;
import com.yabaipanda.mangaupdater.adapters.ViewPagerAdapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.view_pager);
        setupViewPager();
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        ChapterManager.GetChaptersFromFile(getBaseContext());
        ChapterManager.MakeWebViews(getBaseContext());
        setupBottomNavigationView();
    }
    private void setupViewPager() {
        viewPager.setAdapter(new ViewPagerAdapter(this));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                ViewPagerAdapter.singleton.currentItem = position;
                ChapterManager.RefreshAll();
                Log.d("Page Change", String.valueOf(ViewPagerAdapter.singleton.currentItem));
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        viewPager.setCurrentItem(1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChapterManager.SaveChaptersToFile(this);
    }

    private void setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_web_chapters) viewPager.setCurrentItem(1);
            else if (item.getItemId() == R.id.navigation_failed_chapters) viewPager.setCurrentItem(2);
            else ViewPagerAdapter.singleton.currentItem = 0;
            return true;
        });
        bottomNavigationView.setVisibility(View.INVISIBLE);
    }

}