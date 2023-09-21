package com.yabaipanda.mangaupdater.dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;
import android.app.DatePickerDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.yabaipanda.mangaupdater.R;
import com.yabaipanda.mangaupdater.chapter.Chapter;
import com.yabaipanda.mangaupdater.chapter.ChapterManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SnoozeDialogFragment extends MyDialogFrag {
    private EditText delayEditText;
    private Date date;
    private TextView dateTextView;
    public static SnoozeDialogFragment newInstance (int position) {
        SnoozeDialogFragment frag = new SnoozeDialogFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);
        return frag;
    }

    //override onCreateDialog method to inflate the layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_snooze, container, false);

        Chapter w = getChapter();

        //Initialize variables
        ConstraintLayout mainLayout = view.findViewById(R.id.dialog_snooze_layout);
        ImageView iconImageView = view.findViewById(R.id.dialog_snooze_icon);
        TextView titleEditText = view.findViewById(R.id.dialog_snooze_title_text);
        delayEditText = view.findViewById(R.id.dialog_snooze_delay_edit_text);
        ImageButton clearDelayButton = view.findViewById(R.id.dialog_snooze_clear_delay);
        dateTextView = view.findViewById(R.id.dialog_snooze_date_text);
        Button dateEditButton = view.findViewById(R.id.dialog_snooze_date_button);
        ImageButton clearDateButton = view.findViewById(R.id.dialog_snooze_clear_date);
        Button saveButton = view.findViewById(R.id.dialog_snooze_save);
        ImageButton closeButton = view.findViewById(R.id.dialog_snooze_close_button);

        //Set dialog background transparent
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        mainLayout.setMinWidth((int)(getActivity().getWindowManager().getCurrentWindowMetrics().getBounds().width() * .8f));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy",  Locale.US);
        final Calendar c = Calendar.getInstance();

        date = w.getSnoozeDate();
        w.setIcon(iconImageView);
        titleEditText.setText(w.getTitle());
        delayEditText.setText(String.valueOf(w.getSnoozeDelay()!=0?w.getSnoozeDelay():w.getChapDiff()));
        clearDelayButton.setOnClickListener(v -> delayEditText.setText(""));
        dateTextView.setText(dateFormat.format(date!=null?date:c.getTime()));
        dateEditButton.setOnClickListener(v -> {
            // Create a Calendar object and initialize it with the current date
            if (date != null) c.setTime(date);
            // Create a DatePickerDialog and set its initial date to the current date
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (dView, year, monthOfYear, dayOfMonth) -> {
                        // Update the date variable with the selected date
                        c.set(year, monthOfYear, dayOfMonth);
                        Log.d("Clock", date != null ? dateFormat.format(date) : "");
                        date = c.getTime();
                        dateTextView.setText(dateFormat.format(date));
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));
            // Show the dialog
            datePickerDialog.show();
        });
        clearDateButton.setOnClickListener(v -> {
            dateTextView.setText(dateFormat.format(Calendar.getInstance().getTime()));
            date = null;
        });
        closeButton.setOnClickListener(v -> dismiss());
        saveButton.setOnClickListener(v->{
            saveButton.setEnabled(false);
            int delay;
            try { delay = Integer.parseInt(delayEditText.getText().toString()); }
            catch (NumberFormatException e) { delay = 0; }
            w.UpdateDate(delay, date);
            saveChanges = true;
            dismiss();
        });

        return view;
    }
}

