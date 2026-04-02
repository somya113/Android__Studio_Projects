package com.ques_1_ass;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Settings screen: toggle between light and dark theme.
 * <p>
 * The choice is stored in {@link SharedPreferences} under {@link MainActivity#KEY_THEME_DARK},
 * so it survives app restarts. {@link AppCompatDelegate#setDefaultNightMode(int)} switches
 * Material3 DayNight colors; {@link #recreate()} refreshes this activity so the new theme applies
 * immediately.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch themeSwitch = findViewById(R.id.switchTheme);
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(MainActivity.KEY_THEME_DARK, false);
        themeSwitch.setChecked(isDark);

        themeSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean checked) -> {
            prefs.edit().putBoolean(MainActivity.KEY_THEME_DARK, checked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            recreate();
        });
    }

    /** Same logic as {@link MainActivity#applySavedTheme()} so this screen opens in the right mode. */
    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(MainActivity.KEY_THEME_DARK, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
