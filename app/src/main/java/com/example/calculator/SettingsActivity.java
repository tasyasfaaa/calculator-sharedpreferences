package com.example.calculator;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;

public class SettingsActivity extends Activity {
    LinearLayout root;
    TextView tvTitle, tvThemeLabel, tvInfo;
    RadioGroup rgTheme;
    RadioButton rbLight, rbDark;
    Switch swVibration;
    Button btnBack;
    PrefsManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new PrefsManager(this);
        root = findViewById(R.id.settingsRoot);
        tvTitle = findViewById(R.id.tvTitle);
        tvThemeLabel = findViewById(R.id.tvThemeLabel);
        tvInfo = findViewById(R.id.tvInfo);
        rgTheme = findViewById(R.id.rgTheme);
        rbLight = findViewById(R.id.rbLight);
        rbDark = findViewById(R.id.rbDark);
        swVibration = findViewById(R.id.swVibration);
        btnBack = findViewById(R.id.btnBack);

        if (prefs.getTheme().equals("dark")) rbDark.setChecked(true);
        else rbLight.setChecked(true);
        swVibration.setChecked(prefs.isVibrationEnabled());
        applyTheme();

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            prefs.setTheme(checkedId == R.id.rbDark ? "dark" : "light");
            applyTheme();
        });

        swVibration.setOnCheckedChangeListener((b, checked) ->
                prefs.setVibration(checked));

        btnBack.setOnClickListener(v -> finish());
    }

    private void applyTheme() {
        boolean dark = prefs.getTheme().equals("dark");
        int bg = dark ? Color.BLACK : Color.WHITE;
        int fg = dark ? Color.WHITE : Color.BLACK;
        root.setBackgroundColor(bg);
        tvTitle.setTextColor(fg);
        tvThemeLabel.setTextColor(fg);
        tvInfo.setTextColor(fg);
        rbLight.setTextColor(fg);
        rbDark.setTextColor(fg);
        swVibration.setTextColor(fg);
    }
}