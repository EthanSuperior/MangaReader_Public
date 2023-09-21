package com.yabaipanda.mangaupdater.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.yabaipanda.mangaupdater.fragments.FragmentEdits;
import com.yabaipanda.mangaupdater.fragments.FragmentNotices;
import com.yabaipanda.mangaupdater.fragments.FragmentPage;
import com.yabaipanda.mangaupdater.fragments.FragmentUpdates;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public static ViewPagerAdapter singleton;
    private final List<FragmentPage> fragmentList = new ArrayList<>();
    public int currentItem = 1;
    public ViewPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragmentList.clear();
        fragmentList.add(new FragmentEdits());
        fragmentList.add(new FragmentUpdates());
        fragmentList.add(new FragmentNotices());
        singleton = this;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }
    public FragmentPage getCurrentFrag(){
        return fragmentList.get(currentItem);
    }
    @Override
    public int getItemCount() {
        return 3;
    }
}
