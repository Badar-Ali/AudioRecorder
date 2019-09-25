package com.example.soundrecorderexample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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
    private ImageView imageViewRecord, imageViewPlay, imageViewStop, imageViewCancel, imageViewPause;
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
    String uploadfileName;
    View parent;
    BroadcastReceiver receiver;
    PowerManager.WakeLock wakeLock;

    public void playSound(Context context) throws IllegalArgumentException,
            SecurityException,
            IllegalStateException,
            IOException {

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(context, soundUri);
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            // Uncomment the following line if you aim to play it repeatedly
            // mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parent = findViewById(R.id.relative_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToRecordAudio();
        }
        context = this;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, ":tag");
                wl.acquire(5*1000 /*5 sec*/);
                try {
                    playSound(getApplicationContext());
                } catch (IOException e) {
                    e.printStackTrace();
                }
//
                if(intent.getAction() != null) {
                    if (intent.getAction().equals(RecordingService.ACTION_MAX_TIME_REACHED)) {
                        prepareforStop();
                        stopRecording();
                    } else if (intent.getAction().equals(RecordingService.ACTION_MAX_FILE_SIZE_REACHED)) {
                        prepareforStop();
                        stopRecording();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(RecordingService.ACTION_MAX_TIME_REACHED);
        filter.addAction(RecordingService.ACTION_MAX_FILE_SIZE_REACHED);
        registerReceiver(receiver, filter);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        lastProgress = 0;
        initViews();

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

    private void initViews() {

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
//        imageViewPause = findViewById(R.id.imageViewPause);
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
        imageViewCancel.setOnClickListener(this);

        trasnlateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("Please Wait!");
                progressDialog.setMessage("Recognizing and Translating Audio");
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();

                int srcPos = sourceSpinner.getSelectedItemPosition();
                int destPos = destSpinner.getSelectedItemPosition();

                if (fileName != null && counter == 1 && srcPos > 0 && destPos > 0) {
                    uploadfileName = "Audios/" + fName;

                    final Uri file = Uri.fromFile(new File(fileName));

                    final StorageReference audioRef = mStorageRef.child(uploadfileName);


                    audioRef.putFile(file)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri pUri) {
                                            downloadLink = String.valueOf("gs://speechtranslate-40b4d.appspot.com/" + pUri.getLastPathSegment());
                                            DatabaseReference audiosRef = mDatabaseRef.child("Translate");
                                            String srcLanguage = sourceSpinner.getSelectedItem().toString();
                                            String destLanguage = destSpinner.getSelectedItem().toString();
                                            String btnPressed = "" + counter;
                                            audiosRef.child("from").setValue(srcLanguage);
                                            audiosRef.child("to").setValue(destLanguage);
                                            audiosRef.child("uri").setValue(downloadLink);
                                            audiosRef.child("pressed").setValue(1);
                                            audiosRef.child("fname").setValue(fName);


                                            writeFileToDatabase(fName);
                                            counter = 0;
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(context, "File Unable to Upload!!!!!", Toast.LENGTH_LONG).show();
                                }
                            });

                } else {

                    if(srcPos <= 0) {
	                    Snackbar lSnackbar = Snackbar.make(parent, Html.fromHtml("<font color=\"#ffffff\">Please Select Source Language."),Snackbar.LENGTH_SHORT);
	                    View sbView = lSnackbar.getView();
                        sbView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_light));
                        lSnackbar.show();
                    } else if(destPos <= 0) {
                        Snackbar lSnackbar = Snackbar.make(parent, Html.fromHtml("<font color=\"#ffffff\">Please select language in which to translate."),Snackbar.LENGTH_SHORT);
                        View sbView = lSnackbar.getView();
                        sbView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_light));
                        lSnackbar.show();
                    }
                    else {
                        Snackbar lSnackbar = Snackbar.make(parent, Html.fromHtml("<font color=\"#ffffff\">Please Record Audio First."),Snackbar.LENGTH_SHORT);
                        View sbView = lSnackbar.getView();
                        sbView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_light));
                        lSnackbar.show();
                    }
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
                translateAudio();

            } catch (Exception e) {
                        e.printStackTrace();
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

                        if (dataSnapshot.hasChildren()) {

                            translationRef.removeEventListener(this);
                            dataSnapshot.child("waiting").getRef().setValue(0);

                            String fromData = dataSnapshot.child(srcLanguage).getValue(String.class);
                            String toData = dataSnapshot.child(destLanguage).getValue(String.class);

                                Intent i = new Intent(context, TranslatedListActivity.class);
                                i.putExtra("toData", toData);
                                i.putExtra("fromData", fromData);
                                i.putExtra("srcLanguage", srcLanguage);
                                i.putExtra("destLanguage", destLanguage);
                                i.putExtra("child",uploadfileName);
                                i.putExtra("fName",fName);
                                progressDialog.dismiss();

                                context.startActivity(i);
                                finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
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
//        else if(view == imageViewPause) {
//
//        }

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
//        File path = Environment.getDataDirectory();
        startService(RecordingService.actionStartRecording);
        lastProgress = 0;
        seekBar.setProgress(0);
        stopPlaying();
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

    }

    RecordingService recordingService;
    boolean mBound = false;
    public static String NotificatoinChannelID = "MediaRecorderChannel";
    public static String NotificationID = "RecorderNotification";
    ServiceConnection serviceConnection;

    private void startService(String action) {
        Intent service = new Intent(getApplicationContext(), RecordingService.class);
        service.setAction(action);
        startService(service);

        serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                recordingService = ((RecordingService.MyBinder) iBinder).getService();
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                recordingService = null;
                mBound = false;
            }
        };

        bindService(service, serviceConnection, BIND_AUTO_CREATE);

    }


    private void stopRecording() {

        if(recordingService!=null) {
            recordingService.handleActionStopRecording();
            if(mBound) {
                fileName = RecordingService.fileName;
                fName = RecordingService.fName;
                unbindService(serviceConnection);
            }
        }

        mRecorder = null;
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
    }

    private void cancelRecording() {

        if(recordingService!=null) {
            recordingService.handleActionCancelRecording();
            if(mBound) {
                unbindService(serviceConnection);
            }
        }
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
        isPlaying = false;
        imageViewPlay.setImageResource(R.drawable.ic_media_play);
        chronometer.stop();
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
//            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mediaPlayer) {
//                    stopPlaying();
//                    lastProgress = 0;
//                    seekBar.setProgress(0);
//                }
//            });
            mPlayer.start();
        } catch (IOException e) {
            Log.e("LOG_TAG", "prepare() failed");
        }
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
                mPlayer.seekTo(0);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RECORD_AUDIO_REQUEST_CODE);

        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length == 3 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            imageViewPlay.setImageResource(R.drawable.ic_media_play);
            if(isPlaying) {
                chronometer.stop();
            }
            isPlaying = false;
//            chronometer.stop();
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(recordingService!=null){
            recordingService.handleActionCancelRecording();
        }
        if(receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}
