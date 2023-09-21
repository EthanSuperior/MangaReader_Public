package com.yabaipanda.mangaupdater.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yabaipanda.mangaupdater.R;
import com.yabaipanda.mangaupdater.adapters.ChapterListAdapter;
import com.yabaipanda.mangaupdater.chapter.Chapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;

public class FragmentNotices extends FragmentPage {
    private RecyclerView snoozeRecycler;
    private TextView failedTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notices, container, false);

        // Initialize the views
        recyclerView = view.findViewById(R.id.failed_updates_list);
        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new ChapterListAdapter(ChapterManager.Style.Failed, getParentFragmentManager()));
        snoozeRecycler = view.findViewById(R.id.snooze_updates_list);
        failedTitle = view.findViewById(R.id.failed_update_title);

        ImageButton btnRetry = view.findViewById(R.id.refresh_failed_btn);
        btnRetry.setOnClickListener(v-> {
            failedTitle.setText("Checking");
            ChapterManager.UpdateAll(Chapter::isUnloaded, (r, ex) -> {},
                    () -> {
                failedTitle.setText("Failed Chapters");
                ChapterManager.RefreshAll();
            });
        });

        // Set up the RecyclerView
        snoozeRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        snoozeRecycler.setAdapter(new ChapterListAdapter(ChapterManager.Style.Snoozed, getParentFragmentManager()));
        return view;
    }

    @Override
    public RecyclerView[] getViews() {
        return new RecyclerView[] {recyclerView, snoozeRecycler};
    }
}
