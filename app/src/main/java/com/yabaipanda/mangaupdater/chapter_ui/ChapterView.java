package com.yabaipanda.mangaupdater.chapter_ui;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yabaipanda.mangaupdater.R;
import com.yabaipanda.mangaupdater.chapter.Chapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;
import com.yabaipanda.mangaupdater.dialogs.EditMyDialogFrag;

public abstract class ChapterView extends RecyclerView.ViewHolder {
    protected final ChapterManager.Style style;
    public final ConstraintLayout mainView;
    protected final ImageView statusIcon;
    protected final TextView titleTextview;
    protected final TextView linkTextview;
    public final ImageButton statusButton;

    public static ChapterView Factory(@NonNull View itemView, ChapterManager.Style style){
        switch (style){
            case Edit: return new StylizedChapterViews.DatabaseChapterView(itemView);
            case Snoozed: return new StylizedChapterViews.SnoozedChapterView(itemView);
            case Failed: return new StylizedChapterViews.FailedUpdateChapterView(itemView);
            default: return new StylizedChapterViews.UpdateChapterView(itemView);
        }
    }
    private static final int BG_COLOR_LIGHT = Color.parseColor("#FFF8F8F8");
    private static final int BG_COLOR_DARK = Color.parseColor("#FF212121");
    protected ChapterView(@NonNull View itemView, ChapterManager.Style style) {
        super(itemView);
        this.style = style;
        mainView = itemView.findViewById(R.id.filter_item);
        statusIcon = itemView.findViewById(R.id.status_icon);
        titleTextview = itemView.findViewById(R.id.title_textview);
        linkTextview = itemView.findViewById(R.id.link_textview);
        statusButton = itemView.findViewById(R.id.status_button);
        boolean isDarkModeEnabled = (itemView.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        itemView.setBackground(new ColorDrawable(isDarkModeEnabled ? BG_COLOR_DARK : BG_COLOR_LIGHT));
    }

    public void bind(Chapter w, int managerPos, FragmentManager fragMan) {
        // Fill values
        titleTextview.setText(w.getTitleLink(itemView, statusButton.getDrawable().getIntrinsicWidth() + 8));
        titleTextview.setMovementMethod(LinkMovementMethod.getInstance());
        linkTextview.setText(w.getLinks(itemView));
        linkTextview.setMovementMethod(LinkMovementMethod.getInstance());
//        //Toggle Default visibilities
        linkTextview.setVisibility(View.GONE);
        statusButton.setVisibility(View.GONE);
        View.OnLongClickListener listener = (v) -> {
            EditMyDialogFrag dialogFragment = EditMyDialogFrag.newInstance(managerPos);
            dialogFragment.setCloseEvent(() -> ChapterManager.Refresh(managerPos));
            dialogFragment.show(fragMan, w.getTitle());
            return true;
        };
        mainView.setOnLongClickListener(listener);
        titleTextview.setOnLongClickListener(listener);
        //Update based on style
        SetupBinded(w, managerPos, fragMan);
    }

    protected abstract void SetupBinded(Chapter w, int managerPos, FragmentManager fragMan);
}
