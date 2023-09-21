package com.yabaipanda.mangaupdater.dialogs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.yabaipanda.mangaupdater.R;
import com.yabaipanda.mangaupdater.chapter.Chapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class SelectorDialogFragment extends MyDialogFrag {
    private Button saveButton;
    private EditText selector;
    private EditText sourceURL;
    private EditText firstURL;
    private EditText secondURL;

    public static SelectorDialogFragment newInstance (int position) {
        SelectorDialogFragment frag = new SelectorDialogFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);
        return frag;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_selector, container, false);

        Chapter w = getChapter();

        ConstraintLayout mainLayout = view.findViewById(R.id.dialog_selector_layout);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        mainLayout.setMinWidth((int)(getActivity().getWindowManager().getCurrentWindowMetrics().getBounds().width() * .8f));

        TextView titleTextView = view.findViewById(R.id.dialog_selector_title_text);
        saveButton = view.findViewById(R.id.dialog_selector_save);
        ImageButton closeButton = view.findViewById(R.id.dialog_selector_close);
        selector = view.findViewById(R.id.dialog_selector_preview_text_view);
        sourceURL = view.findViewById(R.id.dialog_selector_source_url_edit_text);
        firstURL = view.findViewById(R.id.dialog_selector_first_url_edit_text);
        ImageButton clearFirst = view.findViewById(R.id.dialog_selector_first_url_clear_button);
        secondURL = view.findViewById(R.id.dialog_selector_second_url_edit_text);
        ImageButton clearSecond = view.findViewById(R.id.dialog_selector_second_url_close_button);
        ImageButton searchButton = view.findViewById(R.id.dialog_selector_search_button);
        selector.setText(w.getSelector());
        ImageButton previewButton = view.findViewById(R.id.dialog_selector_preview);
        previewButton.setOnClickListener(v->{
            // Inflate custom layout for the dialog
            View dialogView = getLayoutInflater().inflate(R.layout.popout_preview_page, null);

            // Initialize WebView in the dialog layout
            WebView webViewInDialog = dialogView.findViewById(R.id.popup_preview_web_view);
            webViewInDialog.setVisibility(View.VISIBLE);
            if (w.isState(w.USE_JS)) {
                ((ViewGroup) webViewInDialog.getParent()).addView(w.jsView);
                webViewInDialog.setVisibility(View.INVISIBLE);
            } else webViewInDialog.loadUrl(w.getSourceURL());
            // Build and show the dialog
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
            dialogBuilder.setView(dialogView);
            dialogBuilder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
            dialogBuilder.setOnDismissListener((dialogInterface)-> {
                if (w.isState(w.USE_JS)) ((ViewGroup) webViewInDialog.getParent()).removeView(w.jsView);
            });
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        });

        titleTextView.setText(w.getTitle());
        sourceURL.setText(w.getSourceURL());
        firstURL.setText(w.getPrevURL());
        secondURL.setText(w.getPrevURL());

        clearFirst.setOnClickListener(v -> firstURL.setText(""));
        clearSecond.setOnClickListener(v -> secondURL.setText(""));

        closeButton.setOnClickListener(v -> dismiss());

        secondURL.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) { searchButton.callOnClick(); }
        });
        searchButton.setOnClickListener(v-> w.findSelectorAsync(sourceURL.getText().toString(),
                firstURL.getText().toString(),
                secondURL.getText().toString())
                .thenAccept((s -> selector.setText(s))));
        saveButton.setOnClickListener(v-> {
            saveButton.setEnabled(false);
            w.setSelector(selector.getText().toString(), sourceURL.getText().toString());
            saveChanges = true;
            dismiss();
        });
        return view;
    }
}