
package com.example.calculator;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class HistoryActivity extends Activity {

    private DatabaseHelper databaseHelper;
    private ListView listHistory;
    private TextView txtKosong;
    private ArrayAdapter<String> adapter;

    private final ArrayList<String> dataHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        databaseHelper = new DatabaseHelper(this);

        listHistory = findViewById(R.id.listHistory);
        txtKosong = findViewById(R.id.txtKosong);

        Button btnHapus = findViewById(R.id.btnHapus);

        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                dataHistory
        ) {
            @Override
            public View getView(int position, View convertView,
                                android.view.ViewGroup parent) {

                TextView item = (TextView) super.getView(
                        position, convertView, parent
                );

                item.setTextColor(Color.WHITE);
                item.setTextSize(18);
                item.setPadding(20, 25, 20, 25);

                return item;
            }
        };

        listHistory.setAdapter(adapter);

        btnHapus.setOnClickListener(v -> {
            databaseHelper.deleteAllHistory();
            tampilkanHistory();

            Toast.makeText(
                    this,
                    "Riwayat berhasil dihapus",
                    Toast.LENGTH_SHORT
            ).show();
        });

        tampilkanHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null) {
            tampilkanHistory();
        }
    }

    private void tampilkanHistory() {

        dataHistory.clear();

        Cursor cursor = databaseHelper.getAllHistory();

        try {
            if (cursor.moveToFirst()) {
                do {
                    String ekspresi = cursor.getString(
                            cursor.getColumnIndexOrThrow("expression")
                    );

                    String hasil = cursor.getString(
                            cursor.getColumnIndexOrThrow("result")
                    );

                    dataHistory.add(
                            ekspresi.replace("*", "×") + " = " + hasil
                    );

                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        adapter.notifyDataSetChanged();

        txtKosong.setVisibility(
                dataHistory.isEmpty() ? View.VISIBLE : View.GONE
        );

        listHistory.setVisibility(
                dataHistory.isEmpty() ? View.GONE : View.VISIBLE
        );
    }
}
