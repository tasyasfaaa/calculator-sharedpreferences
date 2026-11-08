package com.example.calculator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends Activity {

    TextView txthasil, txtangka;
    String container = "";

    DatabaseHelper databaseHelper;
    PrefsManager prefsManager;

    // Menyimpan tampilan asli tombol (untuk dikembalikan saat tema gelap)
    private final HashMap<Button, ColorStateList> tintAsli = new HashMap<>();
    private final HashMap<Button, ColorStateList> textAsli = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txthasil = findViewById(R.id.txthasil);
        txtangka = findViewById(R.id.txtangka);

        databaseHelper = new DatabaseHelper(this);
        prefsManager = new PrefsManager(this);

        tampilanHasil();
        txthasil.setText("0");
    }

    // ================= onResume: BACA ULANG SHAREDPREFERENCES =================

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
    }

    // ================= TEMA & GETARAN (SharedPreferences) =================

    private boolean isDark() {
        return prefsManager.getTheme().equals("dark");
    }

    private int warnaUtama() {
        return isDark() ? 0xFFFFFFFF : 0xFF000000;
    }

    private int warnaSekunder() {
        return isDark() ? 0xFF909090 : 0xFF606060;
    }

    private void applyTheme() {
        boolean dark = isDark();

        View root = findViewById(R.id.root);
        root.setBackgroundColor(dark ? 0xFF000000 : 0xFFFFFFFF);
        terapkanKeTombol(root, dark);

        if (container.isEmpty()) {
            tampilanHasil();
        } else {
            tampilanInput();
        }
    }

    private void terapkanKeTombol(View v, boolean dark) {
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                terapkanKeTombol(g.getChildAt(i), dark);
            }
        } else if (v instanceof Button) {
            Button b = (Button) v;

            // simpan warna asli (backgroundTint dan teks) sekali saja
            if (!tintAsli.containsKey(b)) {
                tintAsli.put(b, b.getBackgroundTintList());
                textAsli.put(b, b.getTextColors());
            }

            ColorStateList asli = tintAsli.get(b);
            int warnaAsli = (asli != null) ? asli.getDefaultColor() : 0xFF1A1A1A;

            // tombol oranye (= dan DEL) tetap sama di kedua tema
            if (warnaAsli == 0xFFFF9800) return;

            if (dark) {
                b.setBackgroundTintList(asli);
                b.setTextColor(textAsli.get(b));
            } else {
                int terang = (warnaAsli == 0xFF424242) ? 0xFFBDBDBD : 0xFFE0E0E0;
                b.setBackgroundTintList(ColorStateList.valueOf(terang));
                b.setTextColor(0xFF000000);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void vibrateIfEnabled() {
        if (!prefsManager.isVibrationEnabled()) return;

        Vibrator vib;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager)
                    getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vib = (vm != null) ? vm.getDefaultVibrator() : null;
        } else {
            vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (vib != null && vib.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes attrs = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                vib.vibrate(VibrationEffect.createOneShot(80, 255), attrs);
            } else {
                vib.vibrate(80);
            }
        }

        // Cadangan: getaran sentuh bawaan sistem
        getWindow().getDecorView().performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    // ================= MEMBUKA PENGATURAN =================

    public void bukaPengaturan(View view) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    // ================= TAMPILAN INPUT =================

    private void tampilanInput() {
        txtangka.setTextSize(42);
        txtangka.setTextColor(warnaUtama());

        txthasil.setTextSize(28);
        txthasil.setTextColor(warnaSekunder());
    }

    // ================= TAMPILAN HASIL =================

    private void tampilanHasil() {
        txtangka.setTextSize(22);
        txtangka.setTextColor(warnaSekunder());

        txthasil.setTextSize(42);
        txthasil.setTextColor(warnaUtama());
    }

    // ================= FORMAT EKSPRESI =================

    private String formatEkspresi(String ekspresi) {
        return ekspresi.replace("*", "×")
                .replace("/", "÷");
    }

    // ================= PREVIEW HASIL =================

    private void perbaruiPreview() {

        if (container.isEmpty()) {
            txthasil.setText("0");
            return;
        }

        try {
            double hasil = eval(container);

            if (!Double.isInfinite(hasil)
                    && !Double.isNaN(hasil)) {

                String hasilText;

                if (hasil == (long) hasil) {
                    hasilText = String.valueOf((long) hasil);
                } else {
                    hasilText = String.valueOf(hasil);
                }

                txthasil.setText(hasilText);
            }

        } catch (Exception e) {
            txthasil.setText("");
        }
    }

    // ================= ANGKA =================

    public void nol(View view) {
        tambahAngka("0");
    }

    public void satu(View view) {
        tambahAngka("1");
    }

    public void dua(View view) {
        tambahAngka("2");
    }

    public void tiga(View view) {
        tambahAngka("3");
    }

    public void empat(View view) {
        tambahAngka("4");
    }

    public void lima(View view) {
        tambahAngka("5");
    }

    public void enam(View view) {
        tambahAngka("6");
    }

    public void tujuh(View view) {
        tambahAngka("7");
    }

    public void delapan(View view) {
        tambahAngka("8");
    }

    public void sembilan(View view) {
        tambahAngka("9");
    }

    private void tambahAngka(String angka) {
        vibrateIfEnabled();

        if (container.isEmpty()) {
            tampilanInput();
            txtangka.setText("");
            txthasil.setText("");
        }

        container += angka;

        txtangka.setText(formatEkspresi(container));

        perbaruiPreview();
    }

    // ================= CLEAR =================

    public void allClear(View view) {
        vibrateIfEnabled();

        container = "";

        txtangka.setText("");
        txthasil.setText("0");

        tampilanHasil();
    }

    // ================= PLUS MINUS =================

    public void plusMinus(View view) {
        vibrateIfEnabled();

        if (container.isEmpty()) return;

        int posisi = container.length() - 1;

        while (posisi >= 0 &&
                (Character.isDigit(container.charAt(posisi))
                        || container.charAt(posisi) == '.')) {
            posisi--;
        }

        String depan = container.substring(0, posisi + 1);
        String angka = container.substring(posisi + 1);

        if (angka.isEmpty()) return;

        boolean minusUnary = false;

        if (depan.endsWith("-")) {

            int sebelumMinus = depan.length() - 2;

            if (sebelumMinus < 0 ||
                    "+-*/%(".indexOf(depan.charAt(sebelumMinus)) >= 0) {
                minusUnary = true;
            }
        }

        if (minusUnary) {
            depan = depan.substring(0, depan.length() - 1);
        } else {
            depan += "-";
        }

        container = depan + angka;

        txtangka.setText(formatEkspresi(container));

        perbaruiPreview();
    }

    // ================= DESIMAL =================

    public void dot(View view) {
        vibrateIfEnabled();

        if (container.isEmpty()) {
            tampilanInput();
            txthasil.setText("");
        }

        int posisi = container.length() - 1;

        while (posisi >= 0 &&
                (Character.isDigit(container.charAt(posisi))
                        || container.charAt(posisi) == '.')) {
            posisi--;
        }

        String angkaTerakhir = container.substring(posisi + 1);

        if (!angkaTerakhir.contains(".")) {

            if (angkaTerakhir.isEmpty()) {
                container += "0.";
            } else {
                container += ".";
            }

            txtangka.setText(formatEkspresi(container));

            perbaruiPreview();
        }
    }

    // ================= DELETE =================

    public void delete(View view) {
        vibrateIfEnabled();

        if (!container.isEmpty()) {

            container = container.substring(
                    0, container.length() - 1
            );

            txtangka.setText(formatEkspresi(container));

            if (container.isEmpty()) {
                txthasil.setText("0");
                tampilanHasil();
            } else {
                perbaruiPreview();
            }
        }
    }

    // ================= OPERATOR =================

    public void tambah(View view) {
        tambahOperator("+");
    }

    public void kurang(View view) {
        tambahOperator("-");
    }

    public void kali(View view) {
        tambahOperator("*");
    }

    public void bagi(View view) {
        tambahOperator("/");
    }

    public void modulo(View view) {
        tambahOperator("%");
    }

    private void tambahOperator(String operator) {
        vibrateIfEnabled();

        if (container.isEmpty()) return;

        char terakhir = container.charAt(container.length() - 1);

        if (terakhir == '+' || terakhir == '-' ||
                terakhir == '*' || terakhir == '/' ||
                terakhir == '%') {

            container = container.substring(
                    0, container.length() - 1
            );
        }

        container += operator;

        txtangka.setText(formatEkspresi(container));

        perbaruiPreview();
    }

    // ================= SAMA DENGAN =================

    public void equals(View view) {
        vibrateIfEnabled();

        if (container.isEmpty()) return;

        String ekspresi = container;

        try {

            double hasil = eval(ekspresi);

            if (Double.isInfinite(hasil)
                    || Double.isNaN(hasil)) {

                txthasil.setText("Error");
                return;
            }

            String hasilText;

            if (hasil == (long) hasil) {
                hasilText = String.valueOf((long) hasil);
            } else {
                hasilText = String.valueOf(hasil);
            }

            // Tampilkan ekspresi dan hasil
            txtangka.setText(formatEkspresi(ekspresi));
            txthasil.setText(hasilText);

            // Ubah ukuran tulisan setelah menekan =
            tampilanHasil();

            // Simpan riwayat ke SQLite
            databaseHelper.addHistory(ekspresi, hasilText);

            // Kosongkan input untuk perhitungan berikutnya
            container = "";

        } catch (Exception e) {

            txthasil.setText("Error");
        }
    }

    // ================= MEMBUKA RIWAYAT =================

    public void bukaRiwayat(View view) {

        Intent intent = new Intent(
                MainActivity.this,
                HistoryActivity.class
        );

        startActivity(intent);
    }

    // ================= EVALUASI MATEMATIKA =================

    public static double eval(final String str) {

        return new Object() {

            int pos = -1;
            int ch;

            void nextChar() {
                ch = (++pos < str.length())
                        ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {

                while (ch == ' ') nextChar();

                if (ch == charToEat) {
                    nextChar();
                    return true;
                }

                return false;
            }

            double parse() {

                nextChar();

                double x = parseExpression();

                if (ch != -1) {
                    throw new RuntimeException(
                            "Unexpected: " + (char) ch
                    );
                }

                return x;
            }

            double parseExpression() {

                double x = parseTerm();

                for (;;) {

                    if (eat('+')) {
                        x += parseTerm();

                    } else if (eat('-')) {
                        x -= parseTerm();

                    } else {
                        return x;
                    }
                }
            }

            double parseTerm() {

                double x = parseFactor();

                for (;;) {

                    if (eat('*')) {
                        x *= parseFactor();

                    } else if (eat('/')) {
                        x /= parseFactor();

                    } else if (eat('%')) {
                        x %= parseFactor();

                    } else {
                        return x;
                    }
                }
            }

            double parseFactor() {

                if (eat('+')) return parseFactor();

                if (eat('-')) return -parseFactor();

                double x;

                int startPos = this.pos;

                if (eat('(')) {

                    x = parseExpression();

                    if (!eat(')')) {
                        throw new RuntimeException(
                                "Missing ')'"
                        );
                    }

                } else if ((ch >= '0' && ch <= '9') || ch == '.') {

                    while ((ch >= '0' && ch <= '9') || ch == '.') {
                        nextChar();
                    }

                    x = Double.parseDouble(
                            str.substring(startPos, this.pos)
                    );

                } else if (ch >= 'a' && ch <= 'z') {

                    while (ch >= 'a' && ch <= 'z') {
                        nextChar();
                    }

                    String func = str.substring(
                            startPos, this.pos
                    );

                    if (eat('(')) {

                        x = parseExpression();

                        if (!eat(')')) {
                            throw new RuntimeException(
                                    "Missing ')'"
                            );
                        }

                    } else {
                        x = parseFactor();
                    }

                    if (func.equals("sqrt")) {
                        x = Math.sqrt(x);

                    } else if (func.equals("sin")) {
                        x = Math.sin(Math.toRadians(x));

                    } else if (func.equals("cos")) {
                        x = Math.cos(Math.toRadians(x));

                    } else if (func.equals("tan")) {
                        x = Math.tan(Math.toRadians(x));

                    } else {
                        throw new RuntimeException(
                                "Unknown function: " + func
                        );
                    }

                } else {

                    throw new RuntimeException(
                            "Unexpected: " + (char) ch
                    );
                }

                if (eat('^')) {
                    x = Math.pow(x, parseFactor());
                }

                return x;
            }

        }.parse();
    }
}