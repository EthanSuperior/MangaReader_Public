package com.yabaipanda.mangaupdater.dialogs;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.yabaipanda.mangaupdater.R;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

public class ImportDialogFragment extends MyDialogFrag {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_import_export, container, false);

        ConstraintLayout mainLayout = view.findViewById(R.id.dialog_import_layout);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        mainLayout.setMinWidth((int)(getActivity().getWindowManager().getCurrentWindowMetrics().getBounds().width() * .9f));

        Button importButton = view.findViewById(R.id.dialog_import_import_url);
        Button exportButton = view.findViewById(R.id.dialog_import_export_url);
        ImageButton clearButton = view.findViewById(R.id.dialog_import_clear_urls);
        ImageButton closeButton = view.findViewById(R.id.dialog_import_close);

        importButton.setOnClickListener(v -> runImport(getContext()));

        exportButton.setOnClickListener(v -> {
            String urls = readUrlsFromFile();
            if (urls != null) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("SiteURLs", urls);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "URLs copied to clipboard", Toast.LENGTH_SHORT).show();
            } else Toast.makeText(getContext(), "Error reading URLs file", Toast.LENGTH_SHORT).show();
        });

        clearButton.setOnClickListener(v -> clearUrls(getContext()));

        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }

    public void runImport(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Import Chapters");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setLines(5);
        input.setMaxLines(Integer.MAX_VALUE);
        input.setVerticalScrollBarEnabled(true);
        input.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        input.setMinHeight((int)(getActivity().getWindowManager().getCurrentWindowMetrics().getBounds().height() * .5f));
        input.setText(getClipboard(context));
        input.setHint("Enter URLS to import, each on a new line");
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("Add", (dialog, which) -> dialog.dismiss());
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            input.setText("");
            dialog.cancel();
        });
        builder.setOnDismissListener((i)->{
            BufferedReader br = new BufferedReader(new StringReader(input.getText().toString()));
            String prevURL;
            try {
                while ((prevURL = br.readLine()) != null) {
                    if (prevURL.trim().isEmpty()) continue;
                    ChapterManager.AddChapter(prevURL, false);
                }
                br.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ChapterManager.SaveChaptersToFile(getContext());
        });
        builder.show();
    }

    private CharSequence getClipboard(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (!clipboard.hasPrimaryClip()) return "";

        // Get the text content of the clipboard
        ClipData clipData = clipboard.getPrimaryClip();
        ClipData.Item item = clipData.getItemAt(0);
        return item.getText();
    }

    private String readUrlsFromFile() {
        File urlsFile = new File(getContext().getFilesDir(), "SiteURLs.txt");
        if (!urlsFile.exists()) return null;
        String urls = null;
        try {
            java.util.Scanner scanner = new java.util.Scanner(urlsFile);
            if (scanner.hasNext()) urls = scanner.useDelimiter("\\A").next();
            scanner.close();
            return urls;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void clearUrls(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to clear your database?");
        builder.setPositiveButton("Yes", (dialog, which) -> ChapterManager.allChapters.clear());
        builder.setNegativeButton("No", null);
        builder.show();
    }

}