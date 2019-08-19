package com.example.soundrecorderexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class translatedList extends AppCompatActivity {
    private String toData;
    private String fromData;
    private String srcLang;
    private String destLang;

    private ListView toDataText;
    private ListView fromDataText;
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

        String[] toCommaSeparated = toData.split(",");
        String[] fromCommaSeparated = fromData.split(",");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.text_in_list ,toCommaSeparated);
        toDataText.setAdapter(arrayAdapter);

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(this, R.layout.list_item, R.id.text_in_list ,fromCommaSeparated);
        fromDataText.setAdapter(arrayAdapter2);

//        for (String a : toCommaSeparated) {
//            //Toast.makeText(this,"Values: " )
//        }

//        toDataText.setText(toData);
//        fromDataText.setText(fromData);
        toLangText.setText(destLang);
        fromLangText.setText(srcLang);


    }
}