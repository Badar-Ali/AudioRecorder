package com.example.soundrecorderexample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

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
    StorageReference mStorageRef;
    DatabaseReference mDatabaseRef;
    String child;
    String fName;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translated_list);

        context = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Speech Translation");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        toData = getIntent().getStringExtra("toData");
        fromData = getIntent().getStringExtra("fromData");
        srcLang = getIntent().getStringExtra("srcLanguage");
        destLang = getIntent().getStringExtra("destLanguage");
        child = getIntent().getStringExtra("child");
        fName = getIntent().getStringExtra("fName");

        Toast.makeText(this,"Child path:" + child,Toast.LENGTH_LONG).show();


        toDataText = findViewById(R.id.toData);
        fromDataText = findViewById(R.id.fromData);
        toLangText = findViewById(R.id.toLang);
        fromLangText = findViewById(R.id.fromLang);
        separator = findViewById(R.id.borderLine);
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

        StorageReference desertRef = mStorageRef.child(child);

        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context,"Child Deleted " + child,Toast.LENGTH_LONG).show();
                DeleteFirebase();
                DeleteRecording(fName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });

    }

    private String TrimFileName(String fileName) {
        String ext = ".mp3";
        return fileName.substring(0, fileName.length() - ext.length());
    }

    private void DeleteFirebase()
    {
        String audioName = fName.replace(".",",");

        DatabaseReference audiosRef = mDatabaseRef.child("Audio").child(audioName);
        audiosRef.setValue(null);

        String filename = fName;
        String filName = TrimFileName(filename);

        final DatabaseReference translationRef = FirebaseDatabase.getInstance().getReference().child("Translations").child(filName);
        translationRef.setValue(null);

        Toast.makeText(context,"Firebase Data Deleted",Toast.LENGTH_LONG).show();



        }

    private void DeleteRecording(String fName) {


        File root = android.os.Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);
        if (files != null) {

            for (int i = 0; i < files.length; i++) {

                Log.d("Files", "FileName:" + files[i].getName());
                String fileName = files[i].getName();
                String recordingUri = root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios/" + fileName;

                if (fileName.equals(fName)) {
                    files[i].delete();
                    Toast.makeText(context,"File Deleted from File Storage",Toast.LENGTH_LONG).show();
                }
            }

        }

    }


}