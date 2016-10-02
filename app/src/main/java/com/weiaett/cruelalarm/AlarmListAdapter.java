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
import android.util.SparseBooleanArray;
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
import com.weiaett.cruelalarm.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.weiaett.cruelalarm.R.id.tvTime;

/**
 * Created by Weiss_A on 26.09.2016.
 * Adapter for alarm list
 */

class AlarmListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_EMPTY_LIST_PLACEHOLDER = 0;
    private static final int VIEW_TYPE_LIST_VIEW = 1;
    private SparseBooleanArray selectedItems;
    private List<Alarm> alarms;
    private Context context;
    private RecyclerView recyclerView;
    private int waitingForToneAlarmPosition = -1;
    private int expandedAlarmPosition = -1;

    AlarmListAdapter(List<Alarm> alarms, RecyclerView recyclerView) {
        this.alarms = alarms;
        this.recyclerView = recyclerView;
        selectedItems = new SparseBooleanArray();
        Utils.sortAlarms(alarms);
    }

    @Override
    public int getItemViewType(int position) {
        return alarms.isEmpty() ? VIEW_TYPE_EMPTY_LIST_PLACEHOLDER : VIEW_TYPE_LIST_VIEW;
    }

    @Override
    public int getItemCount() {
        return alarms.isEmpty() ? 1 : alarms.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view;
        switch(viewType) {
            case VIEW_TYPE_EMPTY_LIST_PLACEHOLDER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.empty_alarm_list_placeholder, parent, false);
                return new EmptyViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_alarm, parent, false);
                return new ListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch(getItemViewType(position)) {
            case VIEW_TYPE_EMPTY_LIST_PLACEHOLDER:
                break;
            case VIEW_TYPE_LIST_VIEW:
                ((ListViewHolder) holder).item = alarms.get(position);
                ((ListViewHolder) holder).parentView.setBackground(isSelectionMode() ?
                        context.getResources().getDrawable(R.drawable.alarm_card_background_action_mode) :
                        context.getResources().getDrawable(R.drawable.alarm_card_background));
                ((ListViewHolder) holder).parentView.setActivated(selectedItems.get(position, false));
                bindActiveDayView((ListViewHolder) holder);
                bindPalette((ListViewHolder) holder);
                ((ListViewHolder) holder).clearAlarmView.setVisibility(isSelectionMode() ? View.INVISIBLE : View.VISIBLE);
                ((ListViewHolder) holder).expandAlarmView.setVisibility(isSelectionMode() ? View.INVISIBLE : View.VISIBLE);
                ((ListViewHolder) holder).clearAlarmView.setClickable(!isSelectionMode());
                ((ListViewHolder) holder).timeView.setText(((ListViewHolder) holder).item.getTime());
                ((ListViewHolder) holder).toneView.setText(((ListViewHolder) holder).item.getTone());
                ((ListViewHolder) holder).descriptionView.setText(((ListViewHolder) holder).item.getDescription());
                ((ListViewHolder) holder).vibrationView.setChecked(((ListViewHolder) holder).item.getHasVibration());
                ((ListViewHolder) holder).activateAlarmView.setColorFilter(((ListViewHolder) holder).item.getIsActive() ?
                        context.getResources().getColor(R.color.colorAccent) :
                        context.getResources().getColor(R.color.colorNotActive));
                if (expandedAlarmPosition == position && ((ListViewHolder) holder).expandableViewPart.getVisibility() == View.GONE) {
                    Utils.expand(((ListViewHolder) holder).expandableViewPart);
                } else if (expandedAlarmPosition != position && ((ListViewHolder) holder).expandableViewPart.getVisibility() == View.VISIBLE) {
                    ((ListViewHolder) holder).expandableViewPart.setVisibility(View.GONE);
                }
        }
    }

    private void bindActiveDayView(ListViewHolder holder) {
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

    private void bindPalette(ListViewHolder holder) {
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

    private void accentChangedAlarm(Alarm alarm) {
        Utils.sortAlarms(alarms);
        notifyDataSetChanged();
        int position = alarms.indexOf(alarm);
        expandedAlarmPosition = position;
        recyclerView.smoothScrollToPosition(position);
    }

    private void deleteAlarm(Alarm alarm) {
        alarm.deleteFromDatabase();
        notifyItemRemoved(alarms.indexOf(alarm));
        alarms.remove(alarm);
    }

    void deleteAlarm(int position) {
        deleteAlarm(alarms.get(position));
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
                ListViewHolder holder = (ListViewHolder) recyclerView.findViewHolderForAdapterPosition(waitingForToneAlarmPosition);
                holder.item.setTone(ringtoneTitle);
                holder.item.setToneUri(uri);
                holder.toneView.setText(ringtoneTitle);
                holder.item.updateInDatabase();
            }
        }
    }

    private boolean isSelectionMode() {
        return ((AlarmListActivity) context).isInActionMode();
    }

    void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        }
        else if (position >= 0) {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    void selectAll() {
        for (int i = 0; i < alarms.size(); i++) {
            selectedItems.put(i, true);
        }
        notifyDataSetChanged();
    }

    int getSelectedItemCount() {
        return selectedItems.size();
    }

    int getChildrenCount() {
        return alarms.size();
    }

    void performClickOnAlarmCard(int position) {
        toggleExpanding(((ListViewHolder)recyclerView.findViewHolderForAdapterPosition(position)));
    }

    private void toggleExpanding(ListViewHolder holder) {
        if (holder.expandableViewPart.getVisibility() == View.GONE) {
            holder.expandAlarmView.setVisibility(View.INVISIBLE);
            AlarmListAdapter.this.recyclerView.smoothScrollToPosition(holder.getLayoutPosition());
            notifyItemChanged(expandedAlarmPosition);
            expandedAlarmPosition = holder.getAdapterPosition();
            notifyItemChanged(expandedAlarmPosition);
        } else {
            expandedAlarmPosition = -1;
            Utils.collapse(holder.expandableViewPart);
            holder.expandAlarmView.setVisibility(View.VISIBLE);
        }
    }

    void closeExpanded() {
        if (expandedAlarmPosition != -1) {
            Utils.collapse(((ListViewHolder)recyclerView.
                    findViewHolderForAdapterPosition(expandedAlarmPosition)).expandableViewPart);
            expandedAlarmPosition = -1;
        }
    }

    List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    private class ListViewHolder extends RecyclerView.ViewHolder {
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

        ListViewHolder(View view) {
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
//            parentView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    toggleExpanding(ListViewHolder.this);
//                }
//            });
            clearAlarmView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteAlarm(item);
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
                            AlarmListAdapter.this.bindActiveDayView(ListViewHolder.this);
                        } else {
                            view.setBackground(new ColorCircleDrawable(context.getResources().
                                    getColor(R.color.colorAccent)));
                            item.addDay(weekDay);
                            AlarmListAdapter.this.bindActiveDayView(ListViewHolder.this);
                        }
                        item.updateInDatabase();
                    }
                });
            }
        }
    }

    private class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(View view) {
            super(view);
        }
    }
}
