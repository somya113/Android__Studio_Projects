package com.ques_1_ass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Main screen: currency conversion among INR, USD, JPY, and EUR.
 * <p>
 * Flow (similar to a simple Google-style converter):
 * <ul>
 *     <li>User enters an amount.</li>
 *     <li>User picks “from” and “to” currencies from spinners.</li>
 *     <li>Tapping Convert shows the converted amount.</li>
 *     <li>The gear icon opens {@link SettingsActivity} for light/dark theme.</li>
 * </ul>
 * <p>
 * Exchange rates are stored in {@link #usdRates}: each value is “how many units of that
 * currency equal 1 USD” (example: INR 83.20 means 1 USD = 83.20 INR). Conversion converts
 * via USD: amountUsd = amount / fromRate; result = amountUsd * toRate.
 * <p>
 * Theme is read from {@link SharedPreferences} before {@code setContentView} so the correct
 * day/night resources load on startup.
 */
public class MainActivity extends AppCompatActivity {

    /** SharedPreferences file name (same as {@link SettingsActivity}). */
    public static final String PREFS_NAME = "app_prefs";

    /** Key: {@code true} = dark theme, {@code false} = light theme. */
    public static final String KEY_THEME_DARK = "theme_dark";

    /**
     * Maps currency code to “units per 1 USD” (fixed demo rates; not live from the internet).
     */
    private final Map<String, Double> usdRates = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupRates();
        setupCurrencySpinners();
        setupActions();
    }

    /**
     * Applies light or dark mode from saved preferences so the whole activity uses
     * the correct {@code values} / {@code values-night} theme resources.
     */
    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_THEME_DARK, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    /** Fills {@link #usdRates} for the four assignment currencies. */
    private void setupRates() {
        usdRates.put("USD", 1.0);
        usdRates.put("INR", 83.20);
        usdRates.put("JPY", 151.50);
        usdRates.put("EUR", 0.92);
    }

    /**
     * Binds the same four codes to both spinners with default selections (USD → INR) so
     * the first launch shows a sensible pair.
     */
    private void setupCurrencySpinners() {
        String[] currencies = {"INR", "USD", "JPY", "EUR"};
        Spinner fromSpinner = findViewById(R.id.spinnerFromCurrency);
        Spinner toSpinner = findViewById(R.id.spinnerToCurrency);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);
        fromSpinner.setSelection(1);
        toSpinner.setSelection(0);
    }

    /**
     * Wires Convert (validate input, compute, show result) and Settings navigation.
     */
    private void setupActions() {
        EditText etAmount = findViewById(R.id.etAmount);
        Spinner fromSpinner = findViewById(R.id.spinnerFromCurrency);
        Spinner toSpinner = findViewById(R.id.spinnerToCurrency);
        TextView tvResult = findViewById(R.id.tvResult);
        Button btnConvert = findViewById(R.id.btnConvert);
        ImageButton btnSettings = findViewById(R.id.btnSettings);

        btnConvert.setOnClickListener(v -> {
            String amountText = etAmount.getText().toString().trim();
            if (amountText.isEmpty()) {
                Toast.makeText(this, R.string.enter_amount, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);
                String fromCurrency = fromSpinner.getSelectedItem().toString();
                String toCurrency = toSpinner.getSelectedItem().toString();

                double converted = convertCurrency(amount, fromCurrency, toCurrency);
                String result = String.format(
                        Locale.US,
                        "%.2f %s = %.2f %s",
                        amount,
                        fromCurrency,
                        converted,
                        toCurrency
                );
                tvResult.setText(result);
            } catch (NumberFormatException ex) {
                Toast.makeText(this, R.string.invalid_amount, Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException ex) {
                Toast.makeText(this, R.string.invalid_currency_selection, Toast.LENGTH_SHORT).show();
            }
        });

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    /**
     * Converts {@code amount} from {@code fromCurrency} to {@code toCurrency} using USD as bridge.
     *
     * @throws IllegalArgumentException if a code is missing from {@link #usdRates} (should not
     *                                  happen with current spinners; avoids null unboxing warnings).
     */
    private double convertCurrency(double amount, String fromCurrency, String toCurrency) {
        Double fromRateValue = usdRates.get(fromCurrency);
        Double toRateValue = usdRates.get(toCurrency);
        if (fromRateValue == null || toRateValue == null) {
            throw new IllegalArgumentException("Unsupported currency selected");
        }
        double fromRate = fromRateValue;
        double toRate = toRateValue;
        double amountInUsd = amount / fromRate;
        return amountInUsd * toRate;
    }
}
