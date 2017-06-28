package com.weatheradviceapp.models;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.DateUtils;

import com.weatheradviceapp.WeatherApplication;

import java.util.Calendar;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

public class UserCalendar extends RealmObject {
    long calID = 0;

    String displayName;

    private RealmList<UserCalendarEvent> events;

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

    public RealmList<UserCalendarEvent> getEvents() {
        return events;
    }

    public void setEvents(RealmList<UserCalendarEvent> events) {
        this.events = events;
    }

    public static final String[] CALENDER_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 1
    };

    public static final int CALENDAR_PROJECTION_ID_INDEX = 0;
    public static final int CALENDAR_PROJECTION_DISPLAY_NAME_INDEX = 1;

    public static final String[] INSTANCE_PROJECTION = new String[] {
            CalendarContract.Instances._ID,      // 0
            CalendarContract.Instances.EVENT_ID,      // 1
            CalendarContract.Instances.TITLE,          // 2
            CalendarContract.Instances.BEGIN,         // 3
            CalendarContract.Instances.END,         // 4
    };

    public static final int INSTANCE_PROJECTION_ID_INDEX = 0;
    public static final int INSTANCE_PROJECTION_EVENT_ID_INDEX = 1;
    public static final int INSTANCE_PROJECTION_TITLE_INDEX = 2;
    public static final int INSTANCE_PROJECTION_BEGIN_INDEX = 3;
    public static final int INSTANCE_PROJECTION_END_INDEX = 4;

    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Events._ID,      // 0
            CalendarContract.Events.EVENT_LOCATION,          // 1
    };

    public static final int EVENT_PROJECTION_ID_INDEX = 0;
    public static final int EVENT_PROJECTION_EVENT_LOCATION_INDEX = 1;

    public static Cursor getUserCalendars() {
        ContentResolver cr = WeatherApplication.getContext().getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "";
        String[] selectionArgs = new String[] {};
        return cr.query(uri, CALENDER_PROJECTION, selection, selectionArgs, null);
    }

    public Cursor getInstanceCursor() {
        ContentResolver cr = WeatherApplication.getContext().getContentResolver();
        Uri.Builder builder = Uri.parse(CalendarContract.Instances.CONTENT_URI.toString()).buildUpon();
        long now = new Date().getTime();
        ContentUris.appendId(builder, now);
        ContentUris.appendId(builder, now + DateUtils.WEEK_IN_MILLIS);

        String selection = CalendarContract.Instances.CALENDAR_ID + " = ?";
        String[] selectionArgs = new String[] {
                String.valueOf(this.getCalID())
        };

        return cr.query(builder.build(), INSTANCE_PROJECTION, selection, selectionArgs, null);
    }

    public static Cursor getEvent(long event_id) {
        ContentResolver cr = WeatherApplication.getContext().getContentResolver();
        Uri.Builder builder = Uri.parse(CalendarContract.Events.CONTENT_URI.toString()).buildUpon();
        String selection = CalendarContract.Events._ID + " = ?";
        String[] selectionArgs = new String[] {
                String.valueOf(event_id)
        };

        return cr.query(builder.build(), EVENT_PROJECTION, selection, selectionArgs, null);
    }
}
