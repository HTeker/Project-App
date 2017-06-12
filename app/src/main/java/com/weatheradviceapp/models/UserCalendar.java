package com.weatheradviceapp.models;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import com.weatheradviceapp.WeatherApplication;

import io.realm.RealmObject;

public class UserCalendar extends RealmObject {
    long calID = 0;

    String displayName;

    public long getCalID() {
        return calID;
    }

    public void setCalID(long calID) {
        this.calID = calID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public static final String[] CALENDER_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 1
    };

    public static final int CALENDAR_PROJECTION_ID_INDEX = 0;
    public static final int CALENDAR_PROJECTION_DISPLAY_NAME_INDEX = 1;

    public static Cursor getUserCalendars() {
        Cursor cur = null;
        ContentResolver cr = WeatherApplication.getContext().getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "";
        String[] selectionArgs = new String[] {};
        cur = cr.query(uri, CALENDER_PROJECTION, selection, selectionArgs, null);
        return cur;
    }
}
