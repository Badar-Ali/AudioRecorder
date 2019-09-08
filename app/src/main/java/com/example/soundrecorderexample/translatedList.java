package com.example.soundrecorderexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class translatedList extends AppCompatActivity {
    private Toolbar toolbar;
    private String toData;
    private String fromData;
    private String srcLang;
    private String destLang;

    private ListView toDataText;
    private ListView fromDataText;
    private TextView toLangText;
    private TextView fromLangText;
    private TextView separator;
    private TextView nowords;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translated_list);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Speech Translation");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
        toData = getIntent().getStringExtra("toData");
        fromData = getIntent().getStringExtra("fromData");
        srcLang = getIntent().getStringExtra("srcLanguage");
        destLang = getIntent().getStringExtra("destLanguage");

        toDataText = findViewById(R.id.toData);
        fromDataText = findViewById(R.id.fromData);
        toLangText = findViewById(R.id.toLang);
        fromLangText = findViewById(R.id.fromLang);
        separator = findViewById(R.id.separator);
        nowords = findViewById(R.id.nowords);


        if(!toData.isEmpty() && !fromData.isEmpty()) {

            String[] toCommaSeparated = toData.split(",");
            String[] fromCommaSeparated = fromData.split(",");

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.text_in_list, toCommaSeparated);
            toDataText.setAdapter(arrayAdapter);

            ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(this, R.layout.list_item, R.id.text_in_list, fromCommaSeparated);
            fromDataText.setAdapter(arrayAdapter2);

            toLangText.setText(destLang);
            fromLangText.setText(srcLang);
        }

        else {
            nowords.setVisibility(View.VISIBLE);
            toDataText.setVisibility(View.GONE);
            fromDataText.setVisibility(View.GONE);
            toLangText.setVisibility(View.GONE);
            fromLangText.setVisibility(View.GONE);
            separator.setVisibility(View.GONE);

        }

    }

}