package com.yabaipanda.mangaupdater.chapter_ui;

import com.yabaipanda.mangaupdater.chapter.Chapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public abstract class ChapterList {
    protected final TreeSet<Integer> indexes;
    private ArrayList<Integer> accessible;
    public static ChapterList Factory(ChapterManager.Style style) {
        switch (style){
            case Edit: return new StylizedChapterLists.QueryableChapterList();
            case Snoozed: return new StylizedChapterLists.SnooozedChapterList();
            case Failed: return new StylizedChapterLists.FailedUpdateChapterList();
            default: return new StylizedChapterLists.UpdateChapterList();
        }
    }
    protected ChapterList(){
        indexes = new TreeSet<>(this::compare);
        reset();
    }
    protected abstract boolean filter(Chapter c);
    protected int compare(Integer idx1, Integer idx2){
        Chapter c1 = ChapterManager.get(idx1), c2 = ChapterManager.get(idx2);
        // To bot sort
        int drop_comp = Boolean.compare(c1.isState(c1.HIATUS), c2.isState(c2.HIATUS));
        int possible_comp = Boolean.compare(c1.isState(c1.POSSIBLE), c2.isState(c2.POSSIBLE));
        int done_comp = Boolean.compare(c1.isState(c1.COMPLETE), c2.isState(c2.COMPLETE));
        // To top sort
        int love_comp = Boolean.compare(c2.isState(c2.LOVE), c1.isState(c1.LOVE));
        int like_comp = Boolean.compare(c2.isState(c2.LIKE), c1.isState(c1.LIKE));
        return (drop_comp != 0) ? drop_comp : (possible_comp != 0) ? possible_comp : (done_comp != 0) ? done_comp : (love_comp != 0) ? love_comp : (like_comp != 0) ? like_comp : idx1.compareTo(idx2);
    }
    private void addAll(List<Chapter> list){
        for (int i = 0; i < list.size(); i++)
            if(filter(list.get(i))) indexes.add(i);
        accessible = new ArrayList<>(indexes);
    }
    public int add(Chapter c){
        return add(ChapterManager.allChapters.indexOf(c));
    }
    public int add(int idx){
        if (idx == -1) return -1;
        if (!filter(ChapterManager.get(idx)) || !indexes.add(idx)) return -1;
        accessible = new ArrayList<>(indexes);
        return accessible.indexOf(idx);
    }
    public int remove(int idx){
        if (idx == -1) return -1;
        int pos = accessible.indexOf(idx);
        if (pos == -1) return -1;
        accessible.remove(pos);
        indexes.remove(idx);
        return pos;
    }

    public int[] update(int idx){
        return new int[]{remove(idx), add(idx)};
    }
    public int size() {
        return indexes.size();
    }
    public boolean isEmpty() {
        return indexes.isEmpty();
    }

    public boolean contains(int i) {
        return indexes.contains(i);
    }
    public boolean contains(Chapter c){
        return contains(ChapterManager.allChapters.indexOf(c));
    }
    public void clear() {
        indexes.clear();
    }
    public Chapter get(int pos) {return ChapterManager.get(getManagerIdx(pos)); }
    public int getManagerIdx(int pos) { return accessible.get(pos); }

    public int getInnerIndex(int i){ return accessible.indexOf(i); }
    public void reset(){
        indexes.clear();
        addAll(ChapterManager.allChapters);
    }
}
