package com.yabaipanda.mangaupdater.adapters;


import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yabaipanda.mangaupdater.R;
import com.yabaipanda.mangaupdater.chapter.Chapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;
import com.yabaipanda.mangaupdater.chapter_ui.ChapterView;
import com.yabaipanda.mangaupdater.chapter_ui.ChapterList;
import com.yabaipanda.mangaupdater.chapter_ui.ChapterHighlighter;

public class ChapterListAdapter extends RecyclerView.Adapter<ChapterView> {
    private final ChapterManager.Style style;
    private final FragmentManager fragMan;
    public final ChapterList list;

    public ChapterListAdapter(ChapterManager.Style style, FragmentManager fragMan){
        this.style = style;
        this.fragMan = fragMan;
        this.list = ChapterList.Factory(style);
    }

    @NonNull
    @Override
    public ChapterView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_item, parent, false);
        return ChapterView.Factory(view, style);
    }

    private static final int BG_COLOR_LIGHT = Color.parseColor("#FFF8F8F8");
    private static final int BG_COLOR_DARK = Color.parseColor("#FF212121");
    @Override
    public void onBindViewHolder(@NonNull ChapterView holder, int position) {
        Chapter currentChapter;
        try { currentChapter = list.get(holder.getAdapterPosition()); }
        catch (IndexOutOfBoundsException e) {
            holder.itemView.setVisibility(View.GONE);
            ChapterManager.RefreshAll();
            return;
        }
        if (currentChapter == null) return;
        holder.bind(currentChapter, list.getManagerIdx(position), fragMan);
        if(style != ChapterManager.Style.Update) return;
        if (currentChapter.hasNewUpdate())
            holder.itemView.startAnimation(new ChapterHighlighter(holder.itemView, 1000, "#FFD3FBFD", "#7FCFCBA3"));
        else if (currentChapter.freshUnsnoozed())
            holder.itemView.startAnimation(new ChapterHighlighter(holder.itemView, 3000, "#FFFDFBD3", "#7FA3CBCF"));
        else {
            boolean isDarkModeEnabled = (holder.itemView.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            holder.itemView.clearAnimation();
            holder.itemView.setBackground(new ColorDrawable(isDarkModeEnabled ? BG_COLOR_DARK : BG_COLOR_LIGHT));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}