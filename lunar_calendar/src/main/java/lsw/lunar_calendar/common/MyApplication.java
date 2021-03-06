package lsw.lunar_calendar.common;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by swli on 5/31/2016.
 */
public class MyApplication extends Application {

    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    public void setMonthFirstDayToSaturday(Boolean isSaturday) {
        sharedPreferencesEditor.putBoolean("TheFirstDayOfMonth", isSaturday);
        sharedPreferencesEditor.commit();
    }

    public boolean isSaturdayForMonthFirstDay() {
        boolean s = sharedPreferences.getBoolean("TheFirstDayOfMonth",false);
       return s;
    }
}