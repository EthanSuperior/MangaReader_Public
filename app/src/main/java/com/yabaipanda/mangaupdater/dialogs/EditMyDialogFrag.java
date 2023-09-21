package com.yabaipanda.mangaupdater.dialogs;

import static java.lang.String.format;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.yabaipanda.mangaupdater.R;
import com.yabaipanda.mangaupdater.chapter.Chapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;
import com.yabaipanda.mangaupdater.chapter_ui.StylizedChapterViews;

import java.util.Locale;

public class EditMyDialogFrag extends MyDialogFrag {
    private ImageView iconImageView;
    private EditText titleEditText;
    private EditText sourceUrlEditText;
    private EditText prevUrlEditText;
    private EditText nextUrlEditText;
    private EditText selectorEditText;
    private EditText linkPosEditText;
    private Button saveButton;

    public static EditMyDialogFrag newInstance (int position) {
        EditMyDialogFrag frag = new EditMyDialogFrag();
        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);
        return frag;
    }

    //override onCreateDialog method to inflate the layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_chapter, container, false);


        Chapter w = getChapter();
        //Initialize variables
        ConstraintLayout mainLayout = view.findViewById(R.id.dialog_edit_layout);
        iconImageView = view.findViewById(R.id.dialog_edit_fav_icon);
        titleEditText = view.findViewById(R.id.dialog_edit_title_text);
        sourceUrlEditText = view.findViewById(R.id.dialog_edit_source_url);
        prevUrlEditText = view.findViewById(R.id.dialog_edit_prev_url);
        nextUrlEditText = view.findViewById(R.id.dialog_edit_next_url);
        selectorEditText = view.findViewById(R.id.dialog_edit_selector);
        linkPosEditText = view.findViewById(R.id.dialog_edit_link_pos);
        ImageButton refreshButton = view.findViewById(R.id.dialog_edit_refresh);
        ImageButton searchButton = view.findViewById(R.id.dialog_edit_find_selector);
        ImageButton snoozeButton = view.findViewById(R.id.dialog_edit_snooze);
        ImageButton deleteButton = view.findViewById(R.id.dialog_edit_delete);
        saveButton = view.findViewById(R.id.dialog_edit_save);
        ImageButton closeButton = view.findViewById(R.id.dialog_edit_close);

        CheckBox jsCheck = view.findViewById(R.id.dialog_edit_use_js);
        jsCheck.setChecked(w.isState(w.USE_JS));
        jsCheck.setOnCheckedChangeListener((v,b)-> w.flipStatus(w.USE_JS));

        ImageButton shareButton = view.findViewById(R.id.dialog_edit_share_save);
        shareButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(w.getTitle()+" - Save", w.saveData());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Story copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        //Set dialog background transparent
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        mainLayout.setMinWidth((int)(getActivity().getWindowManager().getCurrentWindowMetrics().getBounds().width() * .9f));
        iconImageView.setOnClickListener(StylizedChapterViews.statusUpdate(iconImageView, w));
        SpannableString titleStr = new SpannableString(w.getTitle());
        titleStr.setSpan(new LeadingMarginSpan.Standard(48, 0), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleEditText.setText(titleStr);
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Editable editable = titleEditText.getEditableText();
                if (editable.length() > 0 && i == 0 && (i1+i2)==1) {
                    LeadingMarginSpan[] leadingMarginSpans = editable.getSpans(0, 1, LeadingMarginSpan.class);
                    if (leadingMarginSpans.length == 1) return;
                    SpannableString spannableString = new SpannableString(editable);
                    spannableString.setSpan(new LeadingMarginSpan.Standard(48, 0), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    titleEditText.removeTextChangedListener(this);
                    titleEditText.setText(spannableString);
                    titleEditText.setSelection(i + i2);
                    titleEditText.addTextChangedListener(this);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        sourceUrlEditText.setText(w.getSourceURL());
        prevUrlEditText.setText(w.getPrevURL());
        nextUrlEditText.setText(w.getNextURL());
        selectorEditText.setText(w.getSelector());
        linkPosEditText.setText(format(Locale.US,"%d", w.getLinkPos()));
        snoozeButton.setOnClickListener(v -> {
            SnoozeDialogFragment dialogFragment = SnoozeDialogFragment.newInstance(position);
            dialogFragment.show(getChildFragmentManager(), w.getTitle());
        });
        refreshButton.setOnClickListener(v-> ChapterManager.UpdateChapter(position, true));
        searchButton.setOnClickListener(v -> {
            SelectorDialogFragment dialogFragment = SelectorDialogFragment.newInstance(position);
            dialogFragment.setCloseEvent(()-> {
                selectorEditText.setText(w.getSelector());
                sourceUrlEditText.setText(w.getSourceURL());
            });
            dialogFragment.show(getChildFragmentManager(), w.getTitle());
        });
        deleteButton.setOnClickListener(v -> {
            saveChanges = true;
            closer = ()->ChapterManager.RemoveChapter(position);
            dismiss();
        });
        closeButton.setOnClickListener(v ->dismiss());
        saveButton.setOnClickListener(v->{
            saveButton.setEnabled(false);
            int linkPos;
            try { linkPos = Integer.parseInt(linkPosEditText.getText().toString()); }
            catch (NumberFormatException e) { linkPos = 1; }
            ChapterManager.get(position).UpdateData(titleEditText.getText().toString(),
                    sourceUrlEditText.getText().toString(),
                    prevUrlEditText.getText().toString(),
                    nextUrlEditText.getText().toString(),
                    selectorEditText.getText().toString(),
                    linkPos);
            if (w.isState(w.USE_JS) && w.jsView == null) w.CreateWebView(requireContext());
            saveChanges = true;
            dismiss();
        });
        return view;
    }
}

