package com.yabaipanda.mangaupdater.fragments;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.yabaipanda.mangaupdater.adapters.ChapterListAdapter;

import java.util.Arrays;

public class FragmentPage extends Fragment {
    public RecyclerView recyclerView;
    public RecyclerView[] getViews(){
        return new RecyclerView[]{recyclerView};
    }

    public ChapterListAdapter[] getAdapters(){
        return  Arrays.stream(getViews())
                .map(r -> r != null ? (ChapterListAdapter) r.getAdapter() : null)
                .toArray(ChapterListAdapter[]::new);
    }
}
