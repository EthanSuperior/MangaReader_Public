package com.yabaipanda.mangaupdater.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.yabaipanda.mangaupdater.R;
import com.yabaipanda.mangaupdater.chapter.Chapter;
import com.yabaipanda.mangaupdater.dialogs.MarkAllDialogFragment;
import com.yabaipanda.mangaupdater.adapters.ChapterListAdapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;

import java.util.concurrent.CompletableFuture;

public class FragmentUpdates extends FragmentPage {
    Button buttonUpdate;
    Button hardRefresh;
    TextView title;
    TextView progress;
    static boolean canEdit = true;
    static int currentProgress = 0;
    static int success = 0;
    SwipeRefreshLayout pullToRefresh;
    public static final int MAX_ATTEMPTS = 2;

    @SuppressLint("JavascriptInterface")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_updates, container, false);
        pullToRefresh = view.findViewById(R.id.layoutRefresh);
        title = view.findViewById(R.id.title);
        progress = view.findViewById(R.id.progress);
        buttonUpdate = view.findViewById(R.id.btnAllUpdate);
        hardRefresh = view.findViewById(R.id.refresh_display_btn);
        pullToRefresh.setOnRefreshListener(this::FetchSites);
        pullToRefresh.setNestedScrollingEnabled(true);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new ChapterListAdapter(ChapterManager.Style.Update, getParentFragmentManager()));
        buttonUpdate.setOnClickListener(v -> {
            if (!canEdit) return;
            MarkAllDialogFragment dialogFragment = new MarkAllDialogFragment();
            dialogFragment.setCloseEvent(ChapterManager::RefreshAll);
            dialogFragment.show(getChildFragmentManager(), "Mark All");
        });
        hardRefresh.setOnClickListener(v -> FetchSites());
        title.setOnClickListener(v -> FetchSites());
        ChapterManager.RefreshAll();
        CompletableFuture.runAsync(this::FetchSites);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!canEdit) {
            pullToRefresh.setRefreshing(false);
            pullToRefresh.setRefreshing(true);
        }
    }
    public void FetchSites() {
        if (!canEdit) {
            pullToRefresh.setRefreshing(false);
            return;
        }
        title.setText(GetNextSearch());
        int maxSize = ChapterManager.allChapters.size();
        canEdit = false;
        pullToRefresh.setRefreshing(true);
        success = 0;
        progress.setText(String.format("%s/%s",0, maxSize));
        updateAll(MAX_ATTEMPTS);
    }
    private void updateAll(int attempt) {
        int maxSize = ChapterManager.allChapters.size();
        ChapterManager.UpdateAll(
                (c) -> attempt == MAX_ATTEMPTS  || c.isUnloaded(), (r, ex) -> {
                    title.setText(GetNextSearch());
                    if(attempt == 1 || r) currentProgress++;
                    progress.setText(String.format("%s/%s", currentProgress, maxSize));
                    if (r) success++;
                },
                (attempt == 1) ? () -> {
                    Log.d("Refreshing", "RequestEnd");
                    new Handler(Looper.getMainLooper()).post(this::RefreshUpdateView);
                } : () -> updateAll(attempt - 1)
        );
    }
    private void RefreshUpdateView() {
        Log.d("Refreshing", "EndClosed");
        currentProgress = 0;
        canEdit = true;
        pullToRefresh.setRefreshing(false);
        title.setText(R.string.updates);
        ChapterListAdapter a = (ChapterListAdapter) recyclerView.getAdapter();
        progress.setText(String.format("%s/%s",(a!=null)?a.getItemCount():success, ChapterManager.allChapters.size()));
        recyclerView.scrollToPosition(0);
        ChapterManager.RefreshAll();
    }

    private String GetNextSearch() {
        if (canEdit) return "Updates";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentProgress %4; i++) sb.append(".");
        return "Searching" + sb;
    }
}