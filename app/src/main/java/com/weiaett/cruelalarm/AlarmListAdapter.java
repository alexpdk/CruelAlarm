package com.weiaett.cruelalarm;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.weiaett.cruelalarm.graphics.ColorCircleDrawable;
import com.weiaett.cruelalarm.models.Alarm;
import com.weiaett.cruelalarm.utils.DBHelper;
import com.weiaett.cruelalarm.utils.Utils;

import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.weiaett.cruelalarm.R.id.tvTime;

/**
 * Created by Weiss_A on 26.09.2016.
 * Adapter for alarm list
 */

class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {

    private List<Alarm> alarms;
    private Context context;
    private RecyclerView recyclerView;
    private int waitingForToneAlarmPosition = -1;
    private int expandedAlarmPosition = -1;

    AlarmListAdapter(List<Alarm> alarms, RecyclerView recyclerView) {
        this.alarms = alarms;
        this.recyclerView = recyclerView;
        Utils.sortAlarms(alarms);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_alarm, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.item = alarms.get(position);
        bindActiveDayView(holder);
        bindPalette(holder);
        holder.timeView.setText(holder.item.getTime());
        holder.toneView.setText(holder.item.getTone());
        holder.descriptionView.setText(holder.item.getDescription());
        holder.vibrationView.setChecked(holder.item.getHasVibration());
        holder.activateAlarmView.setColorFilter(holder.item.getIsActive() ?
                context.getResources().getColor(R.color.colorAccent) :
                context.getResources().getColor(R.color.colorNotActive));
        if (expandedAlarmPosition == position && holder.expandableViewPart.getVisibility() == View.GONE) {
            Utils.expand(holder.expandableViewPart);
        } else if (expandedAlarmPosition != position && holder.expandableViewPart.getVisibility() == View.VISIBLE) {
            holder.expandableViewPart.setVisibility(View.GONE);
        }
    }

    private void bindActiveDayView(ViewHolder holder) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (!holder.item.getDays().isEmpty()) {
            if (holder.item.getDays().size() == 7) {
                holder.activeDayView.setText("Каждый день");
            } else {
                for (WeekDay day : WeekDay.values()) {
                    if (holder.item.getDays().contains(day)) {
                        SpannableString accentSpannable = new SpannableString(day.toString());
                        accentSpannable.setSpan(new ForegroundColorSpan(context.getResources().
                                getColor(R.color.colorAccent)), 0, day.toString().length(), 0);
                        builder.append(accentSpannable);
                    } else {
                        SpannableString simpleSpannable = new SpannableString(day.toString());
                        simpleSpannable.setSpan(new ForegroundColorSpan(context.getResources().
                                getColor(R.color.colorNotActive)), 0, day.toString().length(), 0);
                        builder.append(simpleSpannable);
                    }
                    SpannableString space = new SpannableString(" ");
                    builder.append(space);
                }
                holder.activeDayView.setText(builder, TextView.BufferType.SPANNABLE);
            }
        }
        else {
            if (holder.item.getTime().
                    compareTo(Utils.getFormattedTime(Calendar.getInstance())) > 0) {
                holder.activeDayView.setText("Сегодня");
            }
            else {
                holder.activeDayView.setText("Завтра");
            }
        }
    }

    private void bindPalette(ViewHolder holder) {
        for (WeekDay day : WeekDay.values()) {
            TextView tvWeekday = (TextView) holder.paletteView.getChildAt(day.ordinal());
            if (holder.item.getDays().contains(day)) {
                tvWeekday.setBackground(new ColorCircleDrawable(context.getResources().
                        getColor(R.color.colorAccent)));
            } else {
                tvWeekday.setBackground(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    private void accentChangedAlarm(Alarm alarm) {
        Utils.sortAlarms(alarms);
        notifyDataSetChanged();
        int position = alarms.indexOf(alarm);
        expandedAlarmPosition = position;
        recyclerView.smoothScrollToPosition(position);
    }

    void addAlarm(Alarm alarm) {
        alarm.addToDatabase();
        alarms.add(alarm);
        accentChangedAlarm(alarm);
    }

    void onActivityResult(int requestCode, int resultCode, Intent ringtoneIntent) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            final Uri uri = ringtoneIntent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            final String ringtoneTitle = RingtoneManager.getRingtone(context, uri).getTitle(context);
            if (waitingForToneAlarmPosition != -1) {
                ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(waitingForToneAlarmPosition);
                holder.item.setTone(ringtoneTitle);
                holder.item.setToneUri(uri);
                holder.toneView.setText(ringtoneTitle);
                holder.item.updateInDatabase();
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        Alarm item;
        final View parentView;
        final View expandableViewPart;
        final LinearLayout paletteView;
        final ImageButton clearAlarmView;
        final ImageView expandAlarmView;
        final ImageView activateAlarmView;
        final TextView timeView;
        final TextView activeDayView;
        final TextView toneView;
        final Switch vibrationView;
        final TextView photoView;
        final EditText descriptionView;

        ViewHolder(View view) {
            super(view);
            this.parentView = view;
            expandableViewPart = view.findViewById(R.id.incExpanded);
            paletteView = (LinearLayout) view.findViewById(R.id.incDayPalette);
            clearAlarmView = (ImageButton) view.findViewById(R.id.btnClear);
            expandAlarmView = (ImageView) view.findViewById(R.id.imgChevron);
            activateAlarmView = (ImageView) view.findViewById(R.id.imgAlarm);
            timeView = (TextView) view.findViewById(tvTime);
            activeDayView = (TextView) view.findViewById(R.id.tvDays);
            toneView = (TextView) expandableViewPart.findViewById(R.id.tvTone);
            vibrationView = (Switch) expandableViewPart.findViewById(R.id.switchVibration);
            photoView = (TextView) expandableViewPart.findViewById(R.id.tvPhotoPicker);
            descriptionView = (EditText) expandableViewPart.findViewById(R.id.etDescription);
            setupListeners();
            setupPalette();
        }

        void setupListeners() {
            parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (expandableViewPart.getVisibility() == View.GONE) {
                        expandAlarmView.setVisibility(View.INVISIBLE);
                        AlarmListAdapter.this.recyclerView.smoothScrollToPosition(getLayoutPosition());
                        notifyItemChanged(expandedAlarmPosition);
                        expandedAlarmPosition = getAdapterPosition();
                        notifyItemChanged(expandedAlarmPosition);
                    } else {
                        expandedAlarmPosition = -1;
                        Utils.collapse(expandableViewPart);
                        expandAlarmView.setVisibility(View.VISIBLE);
                    }
                }
            });

            clearAlarmView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    item.deleteFromDatabase();
                    alarms.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                }
            });

            activateAlarmView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activateAlarmView.setColorFilter(!item.getIsActive() ?
                            context.getResources().getColor(R.color.colorAccent) :
                            context.getResources().getColor(R.color.colorNotActive));
                    item.setIsActive(!item.getIsActive());
                    item.updateInDatabase();
                }
            });

            descriptionView.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (!item.getDescription().equals(s.toString())) {
                        item.setDescription(s.toString());
                        item.updateInDatabase();
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });

            timeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final Calendar calendar = Calendar.getInstance();
                    final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                            new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    calendar.set(Calendar.MINUTE, selectedMinute);
                                    item.setTime(Utils.getFormattedTime(calendar));
                                    timeView.setText(Utils.getFormattedTime(calendar));
                                    accentChangedAlarm(item);
                                    item.updateInDatabase();
                                }
                            }, hour, minute, true);
                    timePickerDialog.show();
                }
            });

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: call photo picker dialog
                }
            });

            toneView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Intent ringtoneIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                    ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                    ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                    ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, item.getToneUri());
                    ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, item.getToneUri());
                    ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Выбор мелодии");
                    ((Activity) context).startActivityForResult(ringtoneIntent, 0);
                    waitingForToneAlarmPosition = getAdapterPosition();
                }
            });

            vibrationView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (item.getHasVibration() != isChecked) {
                        item.setHasVibration(isChecked);
                        item.updateInDatabase();
                    }
                }
            });
        }

        void setupPalette() {
            for(int i = 0; i < paletteView.getChildCount(); i++) {
                final WeekDay weekDay = WeekDay.values()[i];
                paletteView.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (item.getDays().contains(weekDay)) {
                            view.setBackground(null);
                            item.removeDay(weekDay);
                            AlarmListAdapter.this.bindActiveDayView(ViewHolder.this);
                        } else {
                            view.setBackground(new ColorCircleDrawable(context.getResources().
                                    getColor(R.color.colorAccent)));
                            item.addDay(weekDay);
                            AlarmListAdapter.this.bindActiveDayView(ViewHolder.this);
                        }
                        item.updateInDatabase();
                    }
                });
            }
        }
    }
}
