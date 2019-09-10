package com.example.soundrecorderexample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TranslatedListActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private String toData;
    private String fromData;
    private String srcLang;
    private String destLang;

    private ListView wordTranslationList;
    private TextView toLangText;
    private TextView fromLangText;
    private TextView nowords;
    StorageReference mStorageRef;
    DatabaseReference mDatabaseRef;
    String child;
    String fName;
    Context context;

	@Override
	public void onBackPressed() {
		Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
		startActivity(mainActivityIntent);
		finish();
	}

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
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
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

        wordTranslationList = findViewById(R.id.wordTranslations);
        toLangText = findViewById(R.id.toLang);
        fromLangText = findViewById(R.id.fromLang);
        nowords = findViewById(R.id.nowords);

        if(!toData.isEmpty() && !fromData.isEmpty()) {

            String[] toCommaSeparated = toData.split(",");
            String[] fromCommaSeparated = fromData.split(",");

            WordTranslationAdapter translationAdapter = new WordTranslationAdapter(Arrays.asList(fromCommaSeparated), Arrays.asList(toCommaSeparated));
            wordTranslationList.setAdapter(translationAdapter);
            toLangText.setText(destLang);
            fromLangText.setText(srcLang);

        }

        else {
            nowords.setVisibility(View.VISIBLE);
            toLangText.setVisibility(View.GONE);
            fromLangText.setVisibility(View.GONE);
            wordTranslationList.setVisibility(View.GONE);
        }

        StorageReference desertRef = mStorageRef.child(child);

        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(context,"File Deleted " + child,Toast.LENGTH_LONG).show();
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

        //Toast.makeText(context,"Firebase Data Deleted",Toast.LENGTH_LONG).show();



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
          //          Toast.makeText(context,"File Deleted from File Storage",Toast.LENGTH_LONG).show();
                }
            }

        }

    }

    class WordTranslationAdapter extends BaseAdapter {

    	List<String> srcWords;
    	List<String> destWords;

    	public WordTranslationAdapter(List<String> pSrcWords, List<String> pDestWords) {
    		srcWords = pSrcWords;
    		destWords = pDestWords;
	    }

	    @Override
	    public int getCount() {
		    return srcWords.size();
	    }

	    @Override
	    public Object getItem(int pI) {
		    return null;
	    }

	    @Override
	    public long getItemId(int pI) {
		    return 0;
	    }

	    @NonNull
	    @Override
	    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

	    	if(convertView == null) {
		    	convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.list_item, null);
		    }

	    	TextView src_text = convertView.findViewById(R.id.src_words);
	    	TextView dest_text = convertView.findViewById(R.id.dest_words);
	    	src_text.setText(srcWords.get(position));
	    	dest_text.setText(destWords.get(position));
	    	return convertView;
	    }
    }
}
