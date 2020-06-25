package com.example.soundrecorderexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.soundrecorderexample.database.MySQLiteDatabase;

public class HomeScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        ActionBar appbar = getSupportActionBar();
        if (appbar != null) {
            appbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            appbar.setCustomView(R.layout.appbar_layout_home);
        }
        MySQLiteDatabase.getInstance(getApplicationContext()).initApp();
        View recordNewWords = findViewById(R.id.recordNewWords);
        View learn = findViewById(R.id.learn);
        View listOfWords = findViewById(R.id.word_list);

        recordNewWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        learn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LearningActivity.class);
                startActivity(intent);
            }
        });

//        recentWords.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), RecentWordsActivity.class);
//                startActivity(intent);
//            }
//        });

        listOfWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CompleteWordListActivity.class);
                startActivity(intent);
            }
        });

    }
}
