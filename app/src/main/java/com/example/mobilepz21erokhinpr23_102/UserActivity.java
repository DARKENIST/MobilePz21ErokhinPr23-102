package com.example.mobilepz21erokhinpr23_102;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UserActivity extends AppCompatActivity {

    EditText nameBox, yearBox;
    Button delButton, saveButton;

    DatabaseHelper sqlHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    long userId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        nameBox = findViewById(R.id.name);
        yearBox = findViewById(R.id.year);
        delButton = findViewById(R.id.deleteButton);
        saveButton = findViewById(R.id.saveButton);

        sqlHelper = new DatabaseHelper(this);
        db = sqlHelper.getWritableDatabase();

        // Получаем id, если передан из MainActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getLong("id");
        }

        if (userId > 0) {
            // Режим редактирования
            userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE +
                            " where " + DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(userId)});

            if (userCursor.moveToFirst()) {
                nameBox.setText(userCursor.getString(1));
                yearBox.setText(String.valueOf(userCursor.getInt(2)));
            }
            userCursor.close();
        } else {
            // Режим добавления — скрываем кнопку удаления
            delButton.setVisibility(View.GONE);
        }
    }

    // Сохранение (добавление или обновление)
    public void save(View view) {
        String name = nameBox.getText().toString().trim();
        String yearStr = yearBox.getText().toString().trim();

        if (name.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COLUMN_NAME, name);
            cv.put(DatabaseHelper.COLUMN_YEAR, year);

            if (userId > 0) {
                db.update(DatabaseHelper.TABLE, cv,
                        DatabaseHelper.COLUMN_ID + "=" + userId, null);
            } else {
                db.insert(DatabaseHelper.TABLE, null, cv);
            }
            goHome();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Год должен быть числом!", Toast.LENGTH_SHORT).show();
        }
    }

    // Удаление
    public void delete(View view) {
        db.delete(DatabaseHelper.TABLE,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)});
        goHome();
    }

    private void goHome() {
        db.close();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); // Добавим finish(), чтобы убрать UserActivity из стека
    }
}
