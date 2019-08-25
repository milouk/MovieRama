package com.workable.movierama.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.workable.movierama.fragments.MovieFragment;
import com.workable.movierama.fragments.TvShowFragment;

public class PageAdapter extends FragmentPagerAdapter {

    private int numOfTabs;

    public PageAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        this.numOfTabs = behavior;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MovieFragment();
            case 1:
                return new TvShowFragment();
            default:
                final Fragment o = null;
                return o;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
