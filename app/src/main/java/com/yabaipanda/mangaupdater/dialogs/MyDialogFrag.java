package com.yabaipanda.mangaupdater.dialogs;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.yabaipanda.mangaupdater.chapter.Chapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;

public class MyDialogFrag extends androidx.fragment.app.DialogFragment {
	protected Runnable closer;
	protected boolean saveChanges = true;
	protected String saveStr = null;
	int position;

	@Override
	public void onCreate (@Nullable Bundle savedInstanceState) { super.onCreate(savedInstanceState); }
	public void setCloseEvent (Runnable c) { closer = c; }
	@Override
	public void dismiss () {
		super.dismiss();
		if (!saveChanges && saveStr!=null) ChapterManager.get(position).loadData(saveStr);
		if (closer != null) closer.run();
	}
	public Chapter getChapter() {
		Bundle args = getArguments();
		if (args == null) return null;
		position = args.getInt("position");
		Chapter w = ChapterManager.get(position);
		saveStr = w.saveData();
		saveChanges = false;
		return w;
	}
}
