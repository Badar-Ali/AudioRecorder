package com.example.soundrecorderexample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Toolbar toolbar;
    private Chronometer chronometer;
    private ImageView imageViewRecord, imageViewPlay, imageViewStop, imageViewCancel;
    private SeekBar seekBar;
    private Spinner sourceSpinner, destSpinner;
    private LinearLayout linearLayoutRecorder, linearLayoutPlay;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private String fileName = null;
    private String fName = null;
    private int lastProgress = 0;
    private Handler mHandler = new Handler();
    private int RECORD_AUDIO_REQUEST_CODE = 123;
    private boolean isPlaying = false;
    private Button trasnlateBtn;
    private Context context;
    private StorageReference mStorageRef;
    private int counter = 0;
    private ProgressDialog progressDialog;
    String downloadLink;
    DatabaseReference mDatabaseRef;
    int DataChangeCounter = 0;
    long timePassed = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToRecordAudio();
        }
        context = this;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        lastProgress = 0;
        initViews();

    }

    private void initViews() {

        // setting up the toolbar
         toolbar = (Toolbar) findViewById(R.id.toolbar);
         toolbar.setTitle("Speech Translation");
         setSupportActionBar(toolbar);

        linearLayoutRecorder = (LinearLayout) findViewById(R.id.linearLayoutRecorder);
        chronometer = (Chronometer) findViewById(R.id.chronometerTimer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        imageViewRecord = (ImageView) findViewById(R.id.imageViewRecord);
        imageViewStop = (ImageView) findViewById(R.id.imageViewStop);
        imageViewPlay = (ImageView) findViewById(R.id.imageViewPlay);
        imageViewCancel = (ImageView) findViewById(R.id.imageViewCancel);
        linearLayoutPlay = (LinearLayout) findViewById(R.id.linearLayoutPlay);
        linearLayoutPlay.setVisibility(View.GONE);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        sourceSpinner = findViewById(R.id.sourceSpinner);
        destSpinner = findViewById(R.id.destSpinner);
        trasnlateBtn = findViewById(R.id.translateBtn);


        String[] srclanguages = new String[]{"Select Source Language", "English", "German", "Russian", "Italian", "Spanish","Japanese","Swedish"};
        String[] destlanguages = new String[]{"Select Destination Language", "English", "German", "Russian", "Italian", "Spanish","Japanese","Swedish"};

        ArrayAdapter<String> srcAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, srclanguages);
        final ArrayAdapter<String> destAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, destlanguages);
        sourceSpinner.setAdapter(srcAdapter);
        destSpinner.setAdapter(destAdapter);

        imageViewRecord.setOnClickListener(this);
        imageViewStop.setOnClickListener(this);
        imageViewPlay.setOnClickListener(this);
        imageViewCancel.setOnClickListener(this);

        trasnlateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "Translate Button Clicked",Toast.LENGTH_LONG).show();
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("Please Wait!");
                progressDialog.setMessage("Wait");
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();

                int srcPos = sourceSpinner.getSelectedItemPosition();
                int destPos = destSpinner.getSelectedItemPosition();

                if (fileName != null && counter == 1 && srcPos > 0 && destPos > 0) {
                    final String uploadfileName = "Audios/" + fName;

                    final Uri file = Uri.fromFile(new File(fileName));

                    final StorageReference audioRef = mStorageRef.child(uploadfileName);


                    audioRef.putFile(file)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                    //Toast.makeText(context, "File Uploaded", Toast.LENGTH_SHORT).show();

                                    //Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri pUri) {
                                            downloadLink = String.valueOf("gs://speechtranslate-40b4d.appspot.com/" + pUri.getLastPathSegment());
                                            //Toast.makeText(context, "File Link: " + downloadLink, Toast.LENGTH_SHORT).show();

                                            DatabaseReference audiosRef = mDatabaseRef.child("Translate");

                                            String srcLanguage = sourceSpinner.getSelectedItem().toString();
                                            String destLanguage = destSpinner.getSelectedItem().toString();
                                            String btnPressed = "" + counter;

                                            //Toast.makeText(context, "Real time Database" , Toast.LENGTH_LONG).show();
                                            //Toast.makeText(context, "Selected Source Language: " + srcLanguage, Toast.LENGTH_LONG).show();
                                            //Toast.makeText(context, "Selected Destination Language: " + destLanguage, Toast.LENGTH_LONG).show();

                                            audiosRef.child("from").setValue(srcLanguage);
                                            audiosRef.child("to").setValue(destLanguage);
                                            audiosRef.child("uri").setValue(downloadLink);
                                            audiosRef.child("pressed").setValue(1);
                                            audiosRef.child("fname").setValue(fName);


                                            writeFileToDatabase(fName);
                                            counter = 0;
                                        }
                                    });
                                    //databaseCounter = 1;
                                    //counter = 0;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    // ...
                                    Toast.makeText(context, "File Unable to Upload!!!!!", Toast.LENGTH_LONG).show();

                                }
                            });

                } else {

                    Toast.makeText(context, " Please Record Audio Before Translating and Select Desired Source and Destination Language", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();

                }
            }
        });

        sourceSpinner.setOnItemSelectedListener(this);
        destSpinner.setOnItemSelectedListener(this);

    }

    private void writeFileToDatabase(String filName) {


        if (fName != null && !fName.isEmpty() && filName != null && !filName.isEmpty()) {
            try {
                String audioFile = fName.replace(".", ",");
                DatabaseReference audiosRef = mDatabaseRef.child("Audio").child(audioFile);

                audiosRef.setValue(filName);
                /*String from = (String) sourceSpinner.getSelectedItem();
                String to = (String) destSpinner.getSelectedItem();

                DatabaseReference audiosRef2 = mDatabaseRef.child("SrcLangInfo").child(filName);
                audiosRef2.child("to").setValue(to);
                audiosRef2.child("from").setValue(from);
*/
                //DatabaseReference audiosRef3 = mDatabaseRef.child("ScrLangInfo").child(audioFile).child("to");
                //audiosRef2.setValue(to);
                //audiosRef2.child("from").setValue(from);
                translateAudio();

                //Toast.makeText(context,"Written to Database",Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(context, "Exception: " + e, Toast.LENGTH_LONG).show();
            }
        }

    }

    private String TrimFileName(String fileName) {
        String ext = ".mp3";
        return fileName.substring(0, fileName.length() - ext.length());
    }

    private void translateAudio() {
        String filename = fName;
        String filName = TrimFileName(filename);

        //Toast.makeText(context, "FName: " + fName, Toast.LENGTH_LONG).show();
        //Toast.makeText(context, "Trimmed File Name: " + filName, Toast.LENGTH_LONG).show();


        final int srcPos = sourceSpinner.getSelectedItemPosition();
        final int destPos = sourceSpinner.getSelectedItemPosition();

        final String srcLanguage = sourceSpinner.getSelectedItem().toString();
        final String destLanguage = destSpinner.getSelectedItem().toString();

        if (srcPos > 0 && destPos > 0) {

            final DatabaseReference translationRef = FirebaseDatabase.getInstance().getReference().child("Translations").child(filName);
            translationRef.child("waiting").setValue(1);


            translationRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    DataChangeCounter++;
                    Log.i("AAA", "" + DataChangeCounter);
                    if (DataChangeCounter == 2 || DataChangeCounter == 3) {
                        DataChangeCounter = 0;
                        //Toast.makeText(context, "On Data Change Called." + DataChangeCounter, Toast.LENGTH_LONG).show();
                        if (dataSnapshot.hasChildren()) {

                            translationRef.removeEventListener(this);
                            dataSnapshot.child("waiting").getRef().setValue(0);

                            String fromData = dataSnapshot.child(srcLanguage).getValue(String.class);
                            String toData = dataSnapshot.child(destLanguage).getValue(String.class);


                            //Toast.makeText(context, "To Data: " + toData, Toast.LENGTH_LONG).show();
                            //Toast.makeText(context, "From Data: " + fromData, Toast.LENGTH_LONG).show();

                            if (toData != null && fromData != null && !toData.equals("") && !fromData.equals("")) {
//                                String[] toCommaSeparated = toData.split(",");
//                                String[] fromCommaSeparated = fromData.split(",");

                                Intent i = new Intent(context, translatedList.class);
//                                Toast.makeText(context,"to comma sperated: " + toCommaSeparated.toString(), Toast.LENGTH_LONG)
                                i.putExtra("toData", toData);
                                i.putExtra("fromData", fromData);
                                i.putExtra("srcLanguage", srcLanguage);
                                i.putExtra("destLanguage", destLanguage);
                                progressDialog.dismiss();

                                context.startActivity(i);

                            } else {

                                Intent i = new Intent(context, translatedList.class);
                                i.putExtra("toData", "No,translated,words,found,in,audio");
                                i.putExtra("fromData", "No,words,found,in,audio");
                                i.putExtra("srcLanguage", srcLanguage);
                                i.putExtra("destLanguage", destLanguage);
                                progressDialog.dismiss();

                                context.startActivity(i);

                            }
                            //Toast.makeText(context,"To Data: "+ toCommaSeparated,Toast.LENGTH_LONG).show();
                            //Toast.makeText(context,"From Data: "+ fromCommaSeparated,Toast.LENGTH_LONG).show();

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    private void gotoRecodingListActivity() {
        Intent intent = new Intent(this, RecordingListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {

        if (view == imageViewRecord) {
            prepareforRecording();
            startRecording();
        } else if (view == imageViewStop) {
            prepareforStop();
            stopRecording();
        } else if (view == imageViewCancel) {
            prepareforCancel();
            cancelRecording();
        } else if (view == imageViewPlay) {
            if (!isPlaying && fileName != null) {
                isPlaying = true;
                startPlaying();
            } else {
                isPlaying = false;
                stopPlaying();
            }
        }

    }

    private void prepareforStop() {
        TransitionManager.beginDelayedTransition(linearLayoutRecorder);
        imageViewRecord.setVisibility(View.VISIBLE);
        imageViewStop.setVisibility(View.GONE);
        imageViewCancel.setVisibility(View.GONE);
        linearLayoutPlay.setVisibility(View.VISIBLE);
        counter = 1;
    }

    private void prepareforCancel() {
        TransitionManager.beginDelayedTransition(linearLayoutRecorder);
        imageViewRecord.setVisibility(View.VISIBLE);
        imageViewStop.setVisibility(View.GONE);
        imageViewCancel.setVisibility(View.GONE);
        linearLayoutPlay.setVisibility(View.GONE);
        counter = 0;
    }


    private void prepareforRecording() {
        TransitionManager.beginDelayedTransition(linearLayoutRecorder);
        imageViewRecord.setVisibility(View.GONE);
        imageViewStop.setVisibility(View.VISIBLE);
        imageViewCancel.setVisibility(View.VISIBLE);
        linearLayoutPlay.setVisibility(View.GONE);
        counter = 0;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios");
        if (!file.exists()) {
            file.mkdirs();
        }
        fName = String.valueOf(System.currentTimeMillis() + ".mp3");
        fileName = root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios/" + fName;
        Log.d("filename", fileName);
        mRecorder.setOutputFile(fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        lastProgress = 0;
        seekBar.setProgress(0);
        stopPlaying();
        // making the imageview a stop button
        //starting the chronometer
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

    }


    private void stopRecording() {

        try {
            mRecorder.stop();
            mRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecorder = null;
        //starting the chronometer
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        //showing the play button
        Toast.makeText(this, "Recording saved successfully.", Toast.LENGTH_SHORT).show();
    }

    private void cancelRecording() {

        try {
            mRecorder.reset();

        } catch (Exception e) {
            e.printStackTrace();
        }

        DeleteRecording(fName);
        lastProgress = 0;
        seekBar.setProgress(0);
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());


    }

    private void stopPlaying() {
        try {
            mPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPlayer = null;
        //showing the play button
        imageViewPlay.setImageResource(R.drawable.ic_media_play);

        chronometer.stop();
        //chronometer.setBase(SystemClock.elapsedRealtime());
        //lastProgress = 0;
        //seekBar.setProgress(lastProgress);


    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("LOG_TAG", "prepare() failed");
        }
        //making the imageview pause button
        imageViewPlay.setImageResource(R.drawable.ic_media_pause);



        seekBar.setProgress(lastProgress);
        mPlayer.seekTo(lastProgress);
        seekBar.setMax(mPlayer.getDuration());
        seekUpdation();


        int stoppedMilliseconds = 0;

        String chronoText = chronometer.getText().toString();
        String array[] = chronoText.split(":");
        if (array.length == 2) {
            stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 1000
                    + Integer.parseInt(array[1]) * 1000;
        } else if (array.length == 3) {
            stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 60 * 1000
                    + Integer.parseInt(array[1]) * 60 * 1000
                    + Integer.parseInt(array[2]) * 1000;
        }

        chronometer.setBase(SystemClock.elapsedRealtime() - stoppedMilliseconds);
        chronometer.start();

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imageViewPlay.setImageResource(R.drawable.ic_media_play);
                isPlaying = false;
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                lastProgress = 0;
                seekBar.setProgress(lastProgress);
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mPlayer != null && fromUser) {
                    mPlayer.seekTo(progress);
                    chronometer.setBase(SystemClock.elapsedRealtime() - mPlayer.getCurrentPosition());
                    lastProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };

    private void seekUpdation() {
        if (mPlayer != null) {
            int mCurrentPosition = mPlayer.getCurrentPosition();
            seekBar.setProgress(mCurrentPosition);
            lastProgress = mCurrentPosition;
        }
        mHandler.postDelayed(runnable, 100);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToRecordAudio() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RECORD_AUDIO_REQUEST_CODE);

        }
    }

    // Callback with the request from calling requestPermissions(...)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length == 3 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                //Toast.makeText(this, "Record Audio permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }

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
                }
            }

        }

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) {
            Toast.makeText(this, "Selected Language: " + sourceSpinner.getItemAtPosition(position), Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
