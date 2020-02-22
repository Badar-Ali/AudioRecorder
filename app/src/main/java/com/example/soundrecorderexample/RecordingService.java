package com.example.soundrecorderexample;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


@SuppressWarnings("FieldCanBeLocal")
public class RecordingService extends Service {

    public static final String ACTION_MAX_TIME_REACHED = "com.speechtranslation.max_time_reached";
    public static final String ACTION_MAX_FILE_SIZE_REACHED = "com.speechtranslation.max_file_size_reached";
    private static MediaRecorder mRecorder;
    public static String fName;
    public static String fileName;
    private static String CHANNEL_ID = "RECORDING_SERVICE_CHANNEL";
    private static int NOTIFICATION_ID = 1;
    final static String actionStartRecording = "Start Recording";
    //    final static String actionStopRecording = "Stop Recording";
//    final static String actionCancelRecording = "Cancel Recording";
    private final IBinder myBinder = new MyBinder();
    public static int REQUEST_CODE = 0;

    public RecordingService() {
        super();
    }

    @SuppressWarnings("WeakerAccess")
    public class MyBinder extends Binder {
        RecordingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return RecordingService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        createNotificationChannel();
//        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_recording);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_mic_white_36dp)
                .setContentTitle("Recording Audio")
                .setSubText("Recording Audio in Background")
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mRecorder = new MediaRecorder();
        handleActionStartRecording();
        return myBinder;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        if (intent != null) {
//            final String action = intent.getAction();
//            if(actionStartRecording.equals(action)) {
//                handleActionStartRecording();
//            }
//            else if(actionStopRecording.equals(action)) {
//                handleActionStopRecording();
//            }
//            else if(actionCancelRecording.equals(action)) {
//                handleActionCancelRecording();
//            }
//            else {
//                throw new UnsupportedOperationException("Action not Available");
//            }
//        }
//    }

    public void handleActionStartRecording() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        Objects.requireNonNull(activityManager).getMemoryInfo(mi);
        long availableBytes = mi.availMem;
        long reserveMBs = 20 * 1024 * 1024;

//        StatFs statFs = new StatFs(path.getPath());
//        long availableBytes = statFs.getAvailableBytes();
        if (availableBytes <= reserveMBs) {
            Toast.makeText(getApplicationContext(), "Low Disk Space", Toast.LENGTH_SHORT).show();
            return;
        }

        mRecorder.setMaxFileSize(availableBytes - reserveMBs);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        File root = getExternalFilesDir(null);
        File file = new File(Objects.requireNonNull(root).getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios");

        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
        }

        fName = System.currentTimeMillis() + ".mp3";
        fileName = root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios/" + fName;
        Log.d("filename", fileName);
        mRecorder.setOutputFile(fileName);
        mRecorder.setMaxDuration(30 * 60 * 1000 /*5 seconds*/);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    Intent intent = new Intent(ACTION_MAX_TIME_REACHED);
                    sendBroadcast(intent);
//                    handleActionStopRecording();
                    Toast.makeText(getApplicationContext(), "Max Duration Reached", Toast.LENGTH_SHORT).show();
                } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
//                    handleActionStopRecording();
                    Intent intent = new Intent(ACTION_MAX_FILE_SIZE_REACHED);
                    sendBroadcast(intent);
                    Toast.makeText(getApplicationContext(), "Device Memory Full", Toast.LENGTH_SHORT).show();

                }
            }
        });

        try {
            mRecorder.prepare();
            mRecorder.start();
//            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AudioRecorder:/backgroundRecording");
//            wl.acquire(30*60*1001);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void handleActionStopRecording() {
        mRecorder.stop();
        mRecorder.release();
        stopForeground(true);
        stopSelf();
    }

    public void handleActionCancelRecording() {
        try {
            mRecorder.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DeleteRecording(fName);
        stopForeground(true);
        stopSelf();
    }

    private void DeleteRecording(String fName) {

        File root = getExternalFilesDir(null);
        assert root != null;
        String path = root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
//        Log.d("Files", "Size: " + files.length);
        if (files != null) {

            for (File file : files) {

                Log.d("Files", "FileName:" + file.getName());
                String fileName = file.getName();
//                String recordingUri = root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios/" + fileName;

                if (fileName.equals(fName)) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }

        }

    }

}
