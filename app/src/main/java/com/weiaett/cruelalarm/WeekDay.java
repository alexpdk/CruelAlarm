package com.weiaett.cruelalarm;

/**
 * Created by Weiss_A on 26.09.2016.
 * Days of a week enumeration
 */

public enum WeekDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    @Override
    public String toString() {
        switch(this.ordinal()){
            case 0:
                return "Пн";
            case 1:
                return "Вт";
            case 2:
                return "Ср";
            case 3:
                return "Чт";
            case 4:
                return "Пт";
            case 5:
                return "Сб";
            case 6:
                return "Вс";
        }
        return super.toString();
    }
}