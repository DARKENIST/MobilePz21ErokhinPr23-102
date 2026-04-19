package com.example.pr21greshnyakovpr_23102;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText editName, editYear;
    Button btnAdd, btnShowList;
    ListView userList;

    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    ArrayList<String> userArray = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Привязка элементов
        editName = findViewById(R.id.editName);
        editYear = findViewById(R.id.editYear);
        btnAdd = findViewById(R.id.btnAdd);
        btnShowList = findViewById(R.id.btnShowList);
        userList = findViewById(R.id.userList);

        databaseHelper = new DatabaseHelper(this);

        // Добавление пользователя через форму
        btnAdd.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String yearStr = editYear.getText().toString().trim();

            if (name.isEmpty() || yearStr.isEmpty()) {
                Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int year = Integer.parseInt(yearStr);

                SQLiteDatabase writableDb = databaseHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COLUMN_NAME, name);
                cv.put(DatabaseHelper.COLUMN_YEAR, year);

                long result = writableDb.insert(DatabaseHelper.TABLE, null, cv);

                if (result != -1) {
                    Toast.makeText(this, "Пользователь добавлен", Toast.LENGTH_SHORT).show();
                    editName.setText("");
                    editYear.setText("");
                    loadUsers();
                } else {
                    Toast.makeText(this, "Ошибка добавления", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Год должен быть числом!", Toast.LENGTH_SHORT).show();
            }
        });

        btnShowList.setOnClickListener(v -> loadUsers());

        // Клик по элементу списка → переход в UserActivity для редактирования/удаления
        userList.setOnItemClickListener((parent, view, position, id) -> {
            long userId = getUserIdByPosition(position);
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            intent.putExtra("id", userId);
            startActivity(intent);
        });

        // Загрузка списка при запуске
        loadUsers();
    }

    private void loadUsers() {
        userArray.clear();

        db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(1);   // COLUMN_NAME
                int year = cursor.getInt(2);         // COLUMN_YEAR
                userArray.add(name + " (" + year + " г.)");
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userArray);
        userList.setAdapter(adapter);
    }

    // Получаем настоящий _id по позиции в списке
    private long getUserIdByPosition(int position) {
        db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_ID +
                " FROM " + DatabaseHelper.TABLE, null);

        cursor.moveToPosition(position);
        long id = cursor.getLong(0);
        cursor.close();
        return id;
    }
}