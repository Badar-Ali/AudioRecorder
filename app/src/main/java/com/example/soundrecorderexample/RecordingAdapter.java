package com.example.soundrecorderexample;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.support.v4.content.ContextCompat.startActivity;

public class RecordingAdapter  extends RecyclerView.Adapter<RecordingAdapter.ViewHolder>{

    private Context context;
    private ArrayList<Recording> recordingArrayList;
    private MediaPlayer mPlayer;
    private boolean isPlaying = false;
    private int last_index = -1;
    private StorageReference mStorageRef;
    DatabaseReference mDatabaseRef;
    int filePresent;

    public RecordingAdapter(Context context, ArrayList<Recording> recordingArrayList){
        this.context = context;
        this.recordingArrayList = recordingArrayList;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.recording_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        setUpData(holder,position);

    }

    private void setUpData(ViewHolder holder, int position) {

        Recording recording = recordingArrayList.get(position);
        holder.textViewName.setText(recording.getFileName());

        if( recording.isPlaying() ){
            holder.imageViewPlay.setImageResource(R.drawable.ic_media_pause);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            holder.seekBar.setVisibility(View.VISIBLE);
            holder.seekUpdation(holder);
        }else{
            holder.imageViewPlay.setImageResource(R.drawable.ic_media_play);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            holder.seekBar.setVisibility(View.GONE);
        }

        holder.manageSeekBar(holder);

    }

    @Override
    public int getItemCount() {
        return recordingArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewPlay;
        SeekBar seekBar;
        TextView textViewName;
        Button deleteRecording;
        Button translateRecording;
        private String recordingUri;
        private int lastProgress = 0;
        private Handler mHandler = new Handler();
        ViewHolder holder;

        public ViewHolder(View itemView) {
            super(itemView);

            imageViewPlay = itemView.findViewById(R.id.imageViewPlay);
            seekBar = itemView.findViewById(R.id.seekBar);
            textViewName = itemView.findViewById(R.id.textViewRecordingname);
            deleteRecording = itemView.findViewById(R.id.deleteRecording);


            translateRecording = itemView.findViewById(R.id.translateRecording);

            translateRecording.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    /*Toast.makeText(context, "Audio Button Clicked",Toast.LENGTH_LONG).show();
                    Recording transRecording = recordingArrayList.get(getAdapterPosition());
                    try {
                        int i = getFile(recordingArrayList.get(getAdapterPosition()));
                    } catch (IOException e) {
                        Toast.makeText(context,"Exception: " + e,Toast.LENGTH_LONG ).show();
                    }
                    */
                    Recording transRecording = recordingArrayList.get(getAdapterPosition());
                    String fileName = transRecording.getUri();
                    String fName = transRecording.getFileName();


                    Intent i = new Intent(context, TranslateActivity.class);
                    i.putExtra("fileName", fileName);
                    i.putExtra("fName",fName);
                    context.startActivity(i);

                }
            });


            deleteRecording.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Recording delRecording = recordingArrayList.get(getAdapterPosition());
                    String fName = delRecording.getFileName();

                    DeleteRecording(fName);

                    deleteRecordingFromDatabase(fName);

                    String fileName = "Audios/" + delRecording.getFileName();

                    StorageReference audioDelRef = mStorageRef.child(fileName);

                    audioDelRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "File Deleted From Firebase Storage",Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                            //Toast.makeText(context, "File Deleted from Phone Storage ",Toast.LENGTH_LONG).show();

                        }
                    });


                    recordingArrayList.remove(getAdapterPosition());

                    notifyDataSetChanged();

                }
            });


            imageViewPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Recording recording = recordingArrayList.get(position);

                    //recordingUri = recording.getUri();
                    try {
                        filePresent = getFile(recordingArrayList.get(getAdapterPosition()));
                    } catch (IOException e) {
                        Toast.makeText(context,"Exception: " + e.toString(),Toast.LENGTH_LONG ).show();
                    }
                    //file download ki location manage karni hy

                    if (filePresent == 1) {
                        recordingUri = recording.getUri();
                        //HandlePlayer();
                        if (isPlaying) {
                            stopPlaying();
                            if (position == last_index) {
                                recording.setPlaying(false);
                                stopPlaying();
                                notifyItemChanged(position);
                            } else {
                                markAllPaused();
                                recording.setPlaying(true);
                                notifyItemChanged(position);
                                startPlaying(recording, position);
                                last_index = position;
                            }

                        } else {
                            if (recording.isPlaying()) {
                                recording.setPlaying(false);
                                stopPlaying();
                                Log.d("isPlayin", "True");
                            } else {
                                startPlaying(recording, position);
                                recording.setPlaying(true);
                                seekBar.setMax(mPlayer.getDuration());
                                Log.d("isPlayin", "False");
                            }
                            notifyItemChanged(position);
                            last_index = position;
                        }
                    }
                }

            });
        }

        public void HandlePlayer()
        {
            int position = getAdapterPosition();
            Recording recording = recordingArrayList.get(position);

            if (isPlaying) {
                stopPlaying();
                if (position == last_index) {
                    recording.setPlaying(false);
                    stopPlaying();
                    notifyItemChanged(position);
                } else {
                    markAllPaused();
                    recording.setPlaying(true);
                    notifyItemChanged(position);
                    startPlaying(recording, position);
                    last_index = position;
                }

            } else {
                if (recording.isPlaying()) {
                    recording.setPlaying(false);
                    stopPlaying();
                    Log.d("isPlayin", "True");
                } else {
                    startPlaying(recording, position);
                    recording.setPlaying(true);
                    seekBar.setMax(mPlayer.getDuration());
                    Log.d("isPlayin", "False");
                }
                notifyItemChanged(position);
                last_index = position;
            }
        }

        private int getFile(final Recording recording) throws IOException {
            String fName = recording.getFileName();

            int result = checkLocalStorageStatus(fName);

            if(result == 1)
            {
                recording.setLocalStorageStatus(true);
                Toast.makeText(context,"Recording Exists in phone",Toast.LENGTH_LONG).show();
            }
            else{

                Toast.makeText(context,"Please wait! Recording will be played from Server",Toast.LENGTH_LONG).show();
                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

                final String uploadfileName = "Audios/" + fName;

                final StorageReference audioRef = mStorageRef.child(uploadfileName);
                audioRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                      /*  MediaPlayer mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                        try {
                            mediaPlayer.setDataSource(String.valueOf(uri));
                        } catch (IOException e) {
                            Toast.makeText(context,"Exception: " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                        try {
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            Toast.makeText(context,"Exception: " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                        mediaPlayer.start();
                        */
                      recordingUri =uri.toString();
                      //Toast.makeText(context,"Recording Uri: " + recordingUri,Toast.LENGTH_LONG).show();
                      //Toast.makeText(context,"Firebase Uri: " + uri.toString(),Toast.LENGTH_LONG).show();

                        HandlePlayer();
                    }

                });
                //HandlePlayer();

                /*try {
                    MediaPlayer player = new MediaPlayer();
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setDataSource("https://firebasestorage.googleapis.com/v0/b/speechtranslate-40b4d.appspot.com/o/Audios%2F1565785685866.mp3?alt=media&token=f9ad820e-dff4-4726-a69a-51e4b1e70ad4");
                    player.prepare();
                    player.start();
                } catch (Exception e) {
                    Toast.makeText(context,"Exception: " + e.toString(), Toast.LENGTH_LONG).show();
                }*/
/*
                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

                final String uploadfileName = "Audios/" + fName;

                final StorageReference audioRef = mStorageRef.child(uploadfileName);

                MediaRecorder mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                File root = android.os.Environment.getExternalStorageDirectory();
                File localFile = new File(root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios");
                if (!localFile.exists()) {
                    localFile.mkdirs();
                }
                String fileName =  root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios/" + fName;
                Log.d("filename",fileName);
                mRecorder.setOutputFile(fileName);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


                File root = android.os.Environment.getExternalStorageDirectory();

                String path =root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios/";
                //File localFile = File.createTempFile("images", "jpg");
                File audio = new File(path);
                File localFile = File.createTempFile(fName,"",audio);
                localFile.createNewFile();

                final String uploadfileName = "Audios/" + fName;

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/speechtranslate-40b4d.appspot.com/o/Audios%2F1565785685866.mp3?alt=media&token=f9ad820e-dff4-4726-a69a-51e4b1e70ad4");
                StorageReference  audioRef = storageRef.child(uploadfileName);

                File root = android.os.Environment.getExternalStorageDirectory();
                String path =root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios/";

                File rootPath = new File(path, fName);
                    if(!rootPath.exists()) {
                        rootPath.mkdirs();
                    }

                    final File localFile = new File(rootPath,fName);


                audioRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created
                        recording.setLocalStorageStatus(true);
                        Toast.makeText(context,"File Downloaded", Toast.LENGTH_LONG).show();


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(context,"Exception: " + exception.toString(), Toast.LENGTH_LONG).show();
                    }
                });
*/            }

            return result;
        }

        public void manageSeekBar(ViewHolder holder){
            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if( mPlayer!=null && fromUser ){
                        mPlayer.seekTo(progress);
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

        private void markAllPaused() {
            for( int i=0; i < recordingArrayList.size(); i++ ){
                recordingArrayList.get(i).setPlaying(false);
                recordingArrayList.set(i,recordingArrayList.get(i));
            }
            notifyDataSetChanged();
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                seekUpdation(holder);
            }
        };

        private void seekUpdation(ViewHolder holder) {
            this.holder = holder;
            if(mPlayer != null){
                int mCurrentPosition = mPlayer.getCurrentPosition() ;
                holder.seekBar.setMax(mPlayer.getDuration());
                holder.seekBar.setProgress(mCurrentPosition);
                lastProgress = mCurrentPosition;
            }
            mHandler.postDelayed(runnable, 100);
        }

        private void stopPlaying() {
            try{
                mPlayer.release();
            }catch (Exception e){
                e.printStackTrace();
            }
            mPlayer = null;
            isPlaying = false;
        }

        private void startPlaying(final Recording audio, final int position) {
            mPlayer = new MediaPlayer();
            try {
                //3Toast.makeText(context,"Recording Uri: " + recordingUri,Toast.LENGTH_LONG).show();
                mPlayer.setDataSource(recordingUri);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Toast.makeText(context,"Exception: " + e.toString(),Toast.LENGTH_LONG).show();
            }
            //showing the pause button
            seekBar.setMax(mPlayer.getDuration());
            isPlaying = true;

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    audio.setPlaying(false);
                    notifyItemChanged(position);
                }
            });



        }

    }

    private int checkLocalStorageStatus(String fName) {

        File root = android.os.Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        if( files!=null ){

            for (int i = 0; i < files.length; i++) {

                Log.d("Files", "FileName:" + files[i].getName());
                String fileName = files[i].getName();
                String recordingUri = root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios/" + fileName;

                if(fileName.equals(fName))
                {
                    return 1;
                }
            }

        }

        return 0;
    }

    private void deleteRecordingFromDatabase(String fName) {
        String audioName = fName.replace(".",",");

        DatabaseReference audiosRef = mDatabaseRef.child("Audio").child(audioName);
        audiosRef.setValue(null);
    }

    private void DeleteRecording(String fName) {

        File root = android.os.Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        if( files!=null ){

            for (int i = 0; i < files.length; i++) {

                Log.d("Files", "FileName:" + files[i].getName());
                String fileName = files[i].getName();
                String recordingUri = root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios/" + fileName;

                if(fileName.equals(fName))
                {
                    files[i].delete();
                }
            }

        }

    }
}

