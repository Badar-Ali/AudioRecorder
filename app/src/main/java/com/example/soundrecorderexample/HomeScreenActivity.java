package com.example.soundrecorderexample;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.soundrecorderexample.database.MySQLiteDatabase;

import java.util.ArrayList;

public class HomeScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home_screen);

        MySQLiteDatabase.getInstance(getApplicationContext()).initApp();
        TextView recordNewWords = findViewById(R.id.recordNewWords);
        TextView learn = findViewById(R.id.learn);
//        TextView recentWords = findViewById(R.id.recent);
        TextView listOfWords = findViewById(R.id.word_list);

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
