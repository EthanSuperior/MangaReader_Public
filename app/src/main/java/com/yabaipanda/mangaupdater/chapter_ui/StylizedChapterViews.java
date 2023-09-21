package com.yabaipanda.mangaupdater.chapter_ui;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.yabaipanda.mangaupdater.R;
import com.yabaipanda.mangaupdater.chapter.Chapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;
import com.yabaipanda.mangaupdater.dialogs.EditMyDialogFrag;
import com.yabaipanda.mangaupdater.dialogs.SnoozeDialogFragment;

public class StylizedChapterViews {

    public static View.OnClickListener statusUpdate(ImageView button, Chapter w){
        SetStatusButtonImg(button, w);
        return v -> {
            PopupMenu popupMenu = new PopupMenu(button.getContext(), button);
            popupMenu.getMenuInflater().inflate(R.menu.status_popup_menu, popupMenu.getMenu());
            popupMenu.setForceShowIcon(true);
            Drawable likedIconDrawable = ContextCompat.getDrawable(button.getContext(),
                    (w.isState(w.LIKE)) ? R.drawable.baseline_star_24 : R.drawable.baseline_star_border_24);
            if (likedIconDrawable != null) {
                likedIconDrawable.setTint(Color.parseColor("#F3CE11"));
                popupMenu.getMenu().findItem(R.id.menu_item_liked).setIcon(likedIconDrawable);
            }
            Drawable lovedIconDrawable = ContextCompat.getDrawable(button.getContext(),
                    (w.isState(w.LOVE)) ? R.drawable.baseline_heart_24 : R.drawable.baseline_heart_border_24);
            if (lovedIconDrawable != null) {
                lovedIconDrawable.setTint(Color.RED);
                popupMenu.getMenu().findItem(R.id.menu_item_loved).setIcon(lovedIconDrawable);
            }
            Drawable possibleIconDrawable = ContextCompat.getDrawable(button.getContext(),
                    (w.isState(w.POSSIBLE)) ? R.drawable.baseline_bookmark_24 : R.drawable.baseline_bookmark_border_24);
            if (possibleIconDrawable != null) {
                possibleIconDrawable.setTint(Color.parseColor("#071630"));
                popupMenu.getMenu().findItem(R.id.menu_item_possible).setIcon(possibleIconDrawable);
            }
            Drawable completedIconDrawable = ContextCompat.getDrawable(button.getContext(),
                    (w.isState(w.COMPLETE)) ? R.drawable.baseline_check_circle_24 : R.drawable.baseline_check_circle_outline_24);
            if (completedIconDrawable != null) {
                completedIconDrawable.setTint(Color.parseColor("#499C54"));
                popupMenu.getMenu().findItem(R.id.menu_item_completed).setIcon(completedIconDrawable);
            }
            Drawable droppedIconDrawable = ContextCompat.getDrawable(button.getContext(),
                    (w.isState(w.HIATUS)) ? R.drawable.baseline_pause_circle_24 : R.drawable.baseline_pause_circle_outline_24);
            if (droppedIconDrawable != null) {
                droppedIconDrawable.setTint(Color.parseColor("#50D6EB"));
                popupMenu.getMenu().findItem(R.id.menu_item_dropped).setIcon(droppedIconDrawable);
            }
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.menu_item_liked) w.flipStatus(w.LIKE);
                else if (itemId == R.id.menu_item_loved) w.flipStatus(w.LOVE);
                else if (itemId == R.id.menu_item_possible) w.flipStatus(w.POSSIBLE);
                else if (itemId == R.id.menu_item_completed) w.flipStatus(w.COMPLETE);
                else if (itemId == R.id.menu_item_dropped) w.flipStatus(w.HIATUS);
                else return false;
                SetStatusButtonImg(button, w);
                return true;
            });
            popupMenu.show();
            SetStatusButtonImg(button, w);
        };
    }
    private static void SetStatusButtonImg(ImageView button, Chapter w){
        if (w.isState(w.COMPLETE)) button.setImageResource(R.drawable.baseline_check_circle_24);
        else if (w.isState(w.HIATUS)) button.setImageResource(R.drawable.baseline_pause_circle_24);
        else if (w.isState(w.LOVE)) button.setImageResource(R.drawable.baseline_heart_24);
        else if (w.isState(w.LIKE)) button.setImageResource(R.drawable.baseline_star_24);
        else if (w.isState(w.POSSIBLE)) button.setImageResource(R.drawable.baseline_bookmark_24);
        else button.setImageResource(R.drawable.outline_circle_24);

        if (w.isState(w.HIATUS)) button.setColorFilter(Color.parseColor("#50D6EB"));
        else if (w.isState(w.LOVE)) button.setColorFilter(Color.RED);
        else if (w.isState(w.LIKE)) button.setColorFilter(Color.parseColor("#F3CE11"));
        else if (w.isState(w.COMPLETE)) button.setColorFilter(Color.parseColor("#499C54"));
        else if (w.isState(w.POSSIBLE)) button.setColorFilter(Color.parseColor("#374680"));
        else button.setColorFilter(Color.GRAY);
    }

    public static class UpdateChapterView extends ChapterView {
        UpdateChapterView(@NonNull View itemView) {
            super(itemView, ChapterManager.Style.Update);
        }

        @Override
        protected void SetupBinded(Chapter w, int managerPos, FragmentManager fragMan) {
            linkTextview.setVisibility(View.VISIBLE);
            statusIcon.setOnClickListener(statusUpdate(statusIcon, w));
        }
    }

    public static class DatabaseChapterView extends ChapterView {
        DatabaseChapterView(@NonNull View itemView) {
            super(itemView, ChapterManager.Style.Edit);
        }

        @Override
        protected void SetupBinded(Chapter w, int managerPos, FragmentManager fragMan) {
            statusIcon.setOnClickListener(statusUpdate(statusIcon, w));
            View.OnClickListener listener = (v) -> {
                EditMyDialogFrag dialogFragment = EditMyDialogFrag.newInstance(managerPos);
                dialogFragment.setCloseEvent(() -> ChapterManager.Refresh(managerPos));
                dialogFragment.show(fragMan, w.getTitle());
            };
            mainView.setOnClickListener(listener);
            titleTextview.setOnClickListener(listener);
        }
    }

    public static class FailedUpdateChapterView extends ChapterView{
        FailedUpdateChapterView(@NonNull View itemView){
            super(itemView, ChapterManager.Style.Failed);
        }

        @Override
        protected void SetupBinded(Chapter w, int managerPos, FragmentManager fragMan) {
            if (w.getErrorCode() != 0) {
                linkTextview.setVisibility(View.VISIBLE);
                linkTextview.setText(w.getErrorCode() == -123 ? "Fix Me" :
                        w.getErrorCode() == -456 ? "Idx":
                        String.format("E:%s", w.getErrorCode()));
            }
            statusIcon.setVisibility(View.VISIBLE);
            w.setIcon(statusIcon);
            statusButton.setVisibility(View.VISIBLE);
            statusIcon.setImageResource(R.drawable.ic_baseline_warning_24);
            statusButton.setImageResource(R.drawable.ic_baseline_refresh_24);
            statusButton.setOnClickListener(v -> ChapterManager.UpdateChapter(managerPos, true));
        }
    }

    public static class SnoozedChapterView extends ChapterView {
        SnoozedChapterView(@NonNull View itemView){
            super(itemView, ChapterManager.Style.Snoozed);
        }

        @Override
        protected void SetupBinded(Chapter w, int managerPos, FragmentManager fragMan) {
            statusIcon.setVisibility(View.VISIBLE);
            w.setIcon(statusIcon);
            statusButton.setVisibility(View.VISIBLE);
            linkTextview.setVisibility(View.VISIBLE);
            linkTextview.setText(String.format("%s   ", w.getSnoozeInfoString()));
            statusButton.setImageResource(R.drawable.ic_baseline_edit_calendar_24);
            statusButton.setOnClickListener(v -> {
                SnoozeDialogFragment dialogFragment = SnoozeDialogFragment.newInstance(managerPos);
                dialogFragment.show(fragMan, w.getTitle());
                dialogFragment.setCloseEvent(() -> ChapterManager.Refresh(managerPos));
            });
        }
    }
}