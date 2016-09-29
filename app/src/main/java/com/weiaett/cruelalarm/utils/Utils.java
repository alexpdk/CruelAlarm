package com.weiaett.cruelalarm.utils;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

import com.weiaett.cruelalarm.models.Alarm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Weiss_A on 28.09.2016.
 * Useful stuff
 */

public class Utils {

    public static String getFormattedTime(Calendar calendar) {
        String format = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        return sdf.format(calendar.getTime().getTime());
    }

    public static void sortAlarms(List<Alarm> alarms) {
        Collections.sort(alarms, new Comparator<Alarm>() {
            @Override
            public int compare(Alarm first, Alarm second) {
                return first.getTime().compareTo(second.getTime());
            }
        });
    }
//
//    public static void sortAlarms(List<Alarm> alarms) {
//        Collections.sort(alarms, new Comparator<Alarm>() {
//            @Override
//            public int compare(Alarm first, Alarm second) {
//                return Utils.getFormattedTime(first.getTime()).
//                        compareTo(Utils.getFormattedTime(second.getTime()));
//            }
//        });
//    }

    public static void expand(final View view) {
        view.setVisibility(View.VISIBLE);
        int finalHeight = 300; // TODO: get real height
        ValueAnimator animator = slideAnimator(view, 0, finalHeight);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.start();
    }

    public static void collapse(final View view) {
        int finalHeight = view.getHeight();
        ValueAnimator animator = slideAnimator(view, finalHeight, 0);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.start();
    }

    private static ValueAnimator slideAnimator(final View view, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
}
