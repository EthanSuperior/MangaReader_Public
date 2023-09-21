package com.yabaipanda.mangaupdater.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yabaipanda.mangaupdater.R;
import com.yabaipanda.mangaupdater.chapter_ui.StylizedChapterLists.QueryableChapterList;
import com.yabaipanda.mangaupdater.dialogs.EditMyDialogFrag;
import com.yabaipanda.mangaupdater.dialogs.ImportDialogFragment;
import com.yabaipanda.mangaupdater.adapters.ChapterListAdapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;

public class FragmentEdits extends FragmentPage {

    public FragmentEdits() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edits, container, false);

        // Initialize the views
        recyclerView = view.findViewById(R.id.chapter_list);
        Button importExportButton = view.findViewById(R.id.btnExport);
        Button addChapterButton = view.findViewById(R.id.add_chapter_button);
        SearchView searchBar = view.findViewById(R.id.chapter_list_search_bar);
        addChapterButton.setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Add Chapter");

            // Set up the input
            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("Enter a recent Chapter URL");
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("Add", (dialog, which) -> {
                String text = input.getText().toString();
                int pos = ChapterManager.AddChapter(text, true);
                EditMyDialogFrag dialogFragment = EditMyDialogFrag.newInstance(pos);
                dialogFragment.show(getChildFragmentManager(), "Creating New Chapter");
                dialogFragment.setCloseEvent(() -> ChapterManager.UpdateChapter(pos, false));
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        importExportButton.setOnClickListener(v->{
            ImportDialogFragment dialogFragment = new ImportDialogFragment();
            //TODO: REPLACE WITH UPDATE_UNLOADED/FAILED
            dialogFragment.setCloseEvent(ChapterManager::RefreshAll);
            dialogFragment.show(getChildFragmentManager(), "URL Manager");
        });
        searchBar.setOnQueryTextFocusChangeListener((view1, b) -> searchBar.setIconified(true));
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                searchBar.setIconified(true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(searchBar.isIconified()) {
                    searchBar.setIconified(false);
                    searchBar.setQuery(newText, false);
                }
                ChapterListAdapter adapter = (ChapterListAdapter) recyclerView.getAdapter();
                if(adapter == null) return false;
                ((QueryableChapterList)adapter.list).setQuery(newText);
                ChapterManager.RefreshAll();
                return false;
            }
        });
        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new ChapterListAdapter(ChapterManager.Style.Edit, getParentFragmentManager()));
        return view;
    }
}
