package com.yabaipanda.mangaupdater.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.yabaipanda.mangaupdater.R;
import com.yabaipanda.mangaupdater.chapter.Chapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;

public class MarkAllDialogFragment extends MyDialogFrag {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_mark_all, container, false);

        ConstraintLayout mainLayout = view.findViewById(R.id.dialog_mark_all_layout);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        mainLayout.setMinWidth((int)(getActivity().getWindowManager().getCurrentWindowMetrics().getBounds().width() * .8f));

        Button markAllButton = view.findViewById(R.id.dialog_mark_all_mark_all);
        Button markDupsButton = view.findViewById(R.id.dialog_mark_all_mark_duplicates);
        ImageButton closeButton = view.findViewById(R.id.dialog_mark_all_close);

        markAllButton.setOnClickListener(v -> {
            for (Chapter w :ChapterManager.allChapters)
                if (w.isUpdated() && !w.isSnoozed()) w.markAsRead();
            dismiss();
        });
        markDupsButton.setOnClickListener(v -> {
            for (Chapter w :ChapterManager.allChapters)
                if (w.isUpdated() && !w.isSnoozed() && w.isDup()) w.markAsRead();
            dismiss();
        });
        closeButton.setOnClickListener(v -> dismiss());
        return view;
    }
}