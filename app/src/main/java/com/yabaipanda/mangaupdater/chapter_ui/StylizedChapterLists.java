package com.yabaipanda.mangaupdater.chapter_ui;

import android.util.Log;

import com.yabaipanda.mangaupdater.chapter.Chapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;

import java.util.Arrays;
import java.util.TreeSet;

public class StylizedChapterLists {
    public static class QueryableChapterList extends ChapterList {
        private String query;
        @Override
        protected boolean filter(Chapter c) {
            if(query == null || query.isEmpty()) return true;
            if("favorited".contains(query) && c.isState(c.LIKE)) return true;
            if("favorited".contains(query) && c.isState(c.LOVE)) return true;
            if("loved".contains(query) && c.isState(c.LOVE)) return true;
            if("liked".contains(query) && c.isState(c.LIKE)) return true;
            if("done".contains(query) && c.isState(c.COMPLETE)) return true;
            if("completed".contains(query) && c.isState(c.COMPLETE)) return true;
            if("dropped".contains(query) && c.isState(c.HIATUS)) return true;
            if("hiatus".contains(query) && c.isState(c.HIATUS)) return true;
            if("snoozed".contains(query) && c.isSnoozed()) return true;
            String[] urlQuery = query.split("url ",2);
            if (urlQuery.length > 1) {
                if (c.getSourceURL().toLowerCase().contains(urlQuery[1])) return true;
                if (c.getNextURL().toLowerCase().contains(urlQuery[1])) return true;
                if (c.getPrevURL().toLowerCase().contains(urlQuery[1])) return true;
            }
            return c.getTitle().toLowerCase().contains(query);
        }

        public void setQuery(String query) {
            this.query = query.toLowerCase();
        }
    }

    public static class UpdateChapterList extends ChapterList {
        private Boolean showNextRead = null;
        @Override
        protected boolean filter(Chapter c) {
            if (showNextRead == null) showNextRead = ChapterManager.allChapters.stream().noneMatch(Chapter::displayUpdate);
            if (!showNextRead) return c.displayUpdate() || (c.isState(c.HIATUS) && c.freshUnsnoozed());
            else return c.isState(c.POSSIBLE) || c.isState(c.HIATUS);
        }
        @Override
        protected int compare(Integer idx1, Integer idx2) {
            Chapter c1 = ChapterManager.get(idx1), c2 = ChapterManager.get(idx2);
            int new_up_comp = Boolean.compare(c2.hasNewUpdate(), c1.hasNewUpdate());
            int fresh_up_comp = Boolean.compare(c2.freshUnsnoozed(), c1.freshUnsnoozed());
            return (new_up_comp != 0) ? new_up_comp : (fresh_up_comp != 0) ? fresh_up_comp :
                    super.compare(idx1, idx2);
        }
        @Override
        public int[] update (int idx) {
            showNextRead = ChapterManager.allChapters.stream().noneMatch(Chapter::displayUpdate);
            return super.update(idx);
        }
        @Override
        public void reset () {
            showNextRead = ChapterManager.allChapters.stream().noneMatch(Chapter::displayUpdate);
            super.reset();
        }
    }

    public static class SnooozedChapterList extends ChapterList {
        @Override
        protected boolean filter(Chapter c) {
            return c.isSnoozed();
        }
    }

    public static class FailedUpdateChapterList extends ChapterList{
        @Override
        protected boolean filter(Chapter c) {
            return c.isUnloaded();
        }
    }
}
