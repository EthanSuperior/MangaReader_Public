package com.yabaipanda.mangaupdater.chapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.yabaipanda.mangaupdater.adapters.ChapterListAdapter;
import com.yabaipanda.mangaupdater.adapters.ViewPagerAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ChapterManager {
    public static List<Chapter> allChapters;
    public static void SaveChaptersToFile(Context context) {
        StringBuilder words = new StringBuilder();
        for (Chapter chapter : allChapters)
            words.append(chapter.saveData()).append('\n');
        Log.d("Close File","Writing " + allChapters.size() +" lines to the file.");
        File textSrc = new File(context.getFilesDir(), "SiteURLs.txt");
        try {
            FileWriter fw = new FileWriter(textSrc);
            fw.write(words.toString());
            fw.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public enum Style { Edit, Failed, Snoozed, Update }
    public static void GetChaptersFromFile(Context c){
        allChapters = new ArrayList<>();
        File textSrc = new File(c.getFilesDir(), "SiteURLs.txt");
        Log.d("Open File","Reading from "+textSrc.getAbsoluteFile());
        try {
            if (!textSrc.exists()) {
                boolean success = textSrc.createNewFile() && textSrc.setWritable(true) && textSrc.setReadable(true);
                if (!success) Log.d("File Creation","Error Changing Permissions");
            }
            BufferedReader br = new BufferedReader(new FileReader(textSrc));
            String prevURL;
            while ((prevURL = br.readLine()) != null) {
                if(prevURL.trim().isEmpty()) continue;
                AddChapter(prevURL, false);
            }
            br.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
    public static int AddChapter(String input, boolean refresh){
        int pos = allChapters.size();
        allChapters.add(new Chapter(input));
        if(refresh) {
            get(pos).GetUpdate();
            for (ChapterListAdapter a: ViewPagerAdapter.singleton.getCurrentFrag().getAdapters()){
                if (a == null) continue;
                int new_pos = a.list.add(pos);
                if (new_pos != -1) a.notifyItemInserted(new_pos);
            }
        }
        return pos;
    }
    public static void UpdateChapter(int pos, boolean async){
        CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(()->get(pos).GetUpdate());
        f.whenCompleteAsync((result,exception)->Refresh(pos));
        if (!async) f.join();
    }
    public static void RemoveChapter(int pos){
        Handler handler = new Handler(Looper.getMainLooper());
        for (ChapterListAdapter a: ViewPagerAdapter.singleton.getCurrentFrag().getAdapters()){
            if (a == null) continue;
            int new_pos = a.list.remove(pos);
            if (new_pos != -1) handler.post(()->a.notifyItemRemoved(new_pos));
        }
        allChapters.get(pos).jsView.destroy();
        allChapters.remove(pos);
    }
    public static void Refresh(int pos){
        Handler handler = new Handler(Looper.getMainLooper());
        for (ChapterListAdapter a: ViewPagerAdapter.singleton.getCurrentFrag().getAdapters()){
            if (a == null) continue;
            int[] new_pos = a.list.update(pos);
            Log.d("Switch", Arrays.toString(new_pos));
            if(new_pos[0] == new_pos[1]) {
                if (new_pos[0] != -1) handler.post(()->a.notifyItemChanged(new_pos[0]));
            } else {
                if (new_pos[0] != -1) handler.post(()->a.notifyItemRemoved(new_pos[0]));
                if (new_pos[1] != -1) handler.post(()->a.notifyItemInserted(new_pos[1]));
            }
        }
    }
    public static void RefreshAll(){
        Handler handler = new Handler(Looper.getMainLooper());
        for (ChapterListAdapter a: ViewPagerAdapter.singleton.getCurrentFrag().getAdapters()) {
            if (a == null) continue;
            a.list.reset();
            handler.post(a::notifyDataSetChanged);
        }
    }
    public static void UpdateAll(Predicate<Chapter> filter, BiConsumer<Boolean, Throwable> onEach, Runnable onComplete){
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < allChapters.size(); i++) {
            Chapter chapter = get(i);
            if (!filter.test(chapter)) continue;
            int finalI = i;
            CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(()->get(finalI).GetUpdate());
            f.whenComplete(onEach);
            futures.add(f);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(onComplete);
    }
    
    public static Chapter get(int idx){
        return allChapters.get(idx);
    }

    public static void MakeWebViews(Context ctx){
        for (Chapter c:allChapters) c.CreateWebView(ctx);
    }
}


