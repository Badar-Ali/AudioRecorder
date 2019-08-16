package com.example.soundrecorderexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class translatedList extends AppCompatActivity {
    private String toData;
    private String fromData;
    private String srcLang;
    private String destLang;

    private TextView toDataText;
    private TextView fromDataText;
    private TextView toLangText;
    private TextView fromLangText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translated_list);

        toData = getIntent().getStringExtra("toData");
        fromData = getIntent().getStringExtra("fromData");
        srcLang = getIntent().getStringExtra("srcLanguage");
        destLang = getIntent().getStringExtra("destLanguage");

        toDataText = findViewById(R.id.toData);
        fromDataText = findViewById(R.id.fromData);
        toLangText = findViewById(R.id.toLang);
        fromLangText = findViewById(R.id.fromLang);

        toDataText.setText(toData);
        fromDataText.setText(fromData);
        toLangText.setText(destLang);
        fromLangText.setText(srcLang);


    }
}
