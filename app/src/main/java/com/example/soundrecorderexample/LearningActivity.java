package com.example.soundrecorderexample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.soundrecorderexample.database.MySQLiteDatabase;
import com.example.soundrecorderexample.models.State;
import com.example.soundrecorderexample.models.VocabCard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LearningActivity extends AppCompatActivity {

    int limit = 2;

    private Button previous;
    private Button next;
    private CardView cardView;
    private TextView text;
    private TextView lang;
    private TextView transliterationView, easy, medium, hard;
    boolean flipped = false;
    View allDoneDialog;

    private int nextCardAnimationDuration = 500;
    private Interpolator nextCardAnimationInterpolator = new BounceInterpolator();
    List<VocabCard> vocabCardList;
    int current = 0;
    int explored = 0;
    Context context;
    private AppCompatTextView deleteWord;
    boolean allDone = false;

    @Override
    protected void onDestroy() {
        MySQLiteDatabase.getInstance(getApplicationContext()).saveState(vocabCardList, current, explored, flipped, allDone);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelableArrayList("vocabCardList", (ArrayList<? extends Parcelable>) vocabCardList);
        outState.putInt("current", current);
        outState.putInt("explored", explored);
        outState.putLong("time", new Date().getTime());
        outState.putBoolean("allDone", allDone);
        outState.putBoolean("flipped", flipped);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);
        context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        allDoneDialog = findViewById(R.id.allDoneDialog);
        toolbar.setTitle("Speech Translation");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        long time = -1;
        State state;
        if(savedInstanceState != null) {
            allDone = savedInstanceState.getBoolean("allDone");
            current = savedInstanceState.getInt("current");
            explored = savedInstanceState.getInt("explored");
            vocabCardList = savedInstanceState.getParcelableArrayList("vocabCardList");
            flipped = savedInstanceState.getBoolean("flipped");
            time = savedInstanceState.getLong("time");
        }
        else {
            state = MySQLiteDatabase.getInstance(getApplicationContext()).getState();
            if(state!=null && !state.cards.isEmpty()) {
                vocabCardList = state.cards;
                explored = state.explored;
                flipped = state.flipped;
                current = state.current;
                allDone = state.allDone;
                time = state.time;
            }
        }

        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        cardView = findViewById(R.id.card);
        TextView noWords = findViewById(R.id.nowords);
        easy = findViewById(R.id.easy);
        medium = findViewById(R.id.normal);
        hard = findViewById(R.id.hard);

//        vocabCardList = savedInstanceState.getParcelableArrayList("vocabCardList");

        long ctime = new Date().getTime();
        long showtime = MySQLiteDatabase.getInstance(getApplicationContext()).getNewWordsShowTime();
        if(time == -1 || (allDone  && (((ctime - time) <= 600000) || ctime >= showtime))) {
            vocabCardList = MySQLiteDatabase.getInstance(context).getNewCards(limit);
            current = 0;
            flipped = false;
            explored = 0;
            allDone = false;
        }

        if (vocabCardList != null && !vocabCardList.isEmpty()) {

            easy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id = vocabCardList.get(current).getId();
                    MySQLiteDatabase.getInstance(getApplicationContext()).addTimeStamp(id, new Date().getTime() + 345600000);
                    gotoNext(vocabCardList);
                }
            });
            medium.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id = vocabCardList.get(current).getId();
                    MySQLiteDatabase.getInstance(getApplicationContext()).addTimeStamp(id, new Date().getTime() + 10 * 60 * 1000);
                    gotoNext(vocabCardList);
                }
            });
            hard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id = vocabCardList.get(current).getId();
                    MySQLiteDatabase.getInstance(getApplicationContext()).addTimeStamp(id, new Date().getTime() + 60*1000);
                    gotoNext(vocabCardList);
                }
            });

            if(vocabCardList.size() == 1) {
                next.setVisibility(View.INVISIBLE);
            }

//            next.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    next.setEnabled(false);
//                    gotoNext(vocabCardList);
//                }
//            });
            previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    previous.setEnabled(false);
                    gotoPrevious(vocabCardList);
                }
            });

            lang = cardView.findViewById(R.id.lang);
            text = cardView.findViewById(R.id.text);
            transliterationView = cardView.findViewById(R.id.transliteration);
            deleteWord = cardView.findViewById(R.id.deleteBtn);
//            lang.setText(vocabCardList.get(current).srcLang);
//            text.setText(vocabCardList.get(current).word);

            if(explored > current) {
                if(current < vocabCardList.size()-1) {
                    next.setVisibility(View.VISIBLE);
                }
            }

            if(current > 0) {
                previous.setVisibility(View.VISIBLE);
            }

            if (!flipped) {
                lang.setTextColor(0xff000000);
                text.setTextColor(0xff000000);
                deleteWord.setTextColor(0xff000000);
                transliterationView.setTextColor(0xff000000);
                lang.setText(vocabCardList.get(current).srcLang);
                text.setText(vocabCardList.get(current).word);
                transliterationView.setText("");
                cardView.setBackgroundColor(0xffffffff);
            }
            else {
                cardView.setBackgroundColor(0xff000000);
                lang.setTextColor(0xffffffff);
                deleteWord.setTextColor(0xffffffff);
                text.setTextColor(0xffffffff);
                transliterationView.setTextColor(0xffffffff);
                lang.setText(vocabCardList.get(current).destLang);
                text.setText(vocabCardList.get(current).translation);
                transliterationView.setText(vocabCardList.get(current).transliteration);
            }
            deleteWord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getApplicationContext())
                            .setTitle("Delete ?")
                            .setMessage("Do you really want to delete this word?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());
                                    progressDialog.setTitle("Deleting");
                                    progressDialog.setCancelable(true);
                                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                        }
                                    });
                                    progressDialog.show();

                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                                    reference.child("Translations").child(vocabCardList.get(current).fileName).child(vocabCardList.get(current).getId()).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                MySQLiteDatabase.getInstance(context).deleteWord(vocabCardList.get(current).getId());
                                                vocabCardList.remove(current);
                                                gotoPrevious(vocabCardList);
                                            }
                                        }
                                    });
                                }})
                            .setNegativeButton(android.R.string.no, null).show();
                }
            });

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ObjectAnimator oa1 = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0f);
                    final ObjectAnimator oa2 = ObjectAnimator.ofFloat(v, "scaleY", 0f, 1f);
                    oa1.setDuration(300);
                    oa2.setDuration(300);
                    oa1.setInterpolator(new DecelerateInterpolator());
                    oa2.setInterpolator(new AccelerateDecelerateInterpolator());
                    oa1.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            flipped = !flipped;
                            if(explored == current) {
                                if(current < vocabCardList.size()-1) {
                                    next.setEnabled(true);
                                    next.setVisibility(View.VISIBLE);
                                    explored++;
                                }
                            }
                            if (!flipped) {
                                lang.setTextColor(0xff000000);
                                text.setTextColor(0xff000000);
                                deleteWord.setTextColor(0xff000000);
                                transliterationView.setTextColor(0xff000000);
                                lang.setText(vocabCardList.get(current).srcLang);
                                text.setText(vocabCardList.get(current).word);
                                transliterationView.setText("");
                                cardView.setBackgroundColor(0xffffffff);

                            } else {

                                cardView.setBackgroundColor(0xff000000);
                                lang.setTextColor(0xffffffff);
                                deleteWord.setTextColor(0xffffffff);
                                text.setTextColor(0xffffffff);
                                transliterationView.setTextColor(0xffffffff);
                                lang.setText(vocabCardList.get(current).destLang);
                                text.setText(vocabCardList.get(current).translation);
                                transliterationView.setText(vocabCardList.get(current).transliteration);
                            }

                            oa2.start();
                        }
                    });
                    oa1.start();

                }
            });
        }
        else {
            noWords.setVisibility(View.VISIBLE);
            cardView.setVisibility(View.GONE);
            next.setVisibility(View.GONE);
            previous.setVisibility(View.GONE);
            easy.setVisibility(View.GONE);
            medium.setVisibility(View.GONE);
            hard.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void gotoPrevious(final List<VocabCard> data) {
        if(current == 0) {
            return;
        }
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 0.001f);
        animatorX.setDuration(nextCardAnimationDuration);
        animatorX.setInterpolator(nextCardAnimationInterpolator);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 0.001f);
        animatorY.setDuration(nextCardAnimationDuration);
        animatorY.setInterpolator(nextCardAnimationInterpolator);
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(animatorX, animatorY);

        scaleDown.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                --current;
                if(current == 0) {
                    previous.setVisibility(View.INVISIBLE);
                }
//                next.setVisibility(View.VISIBLE);
//                next.setEnabled(true);
                flipped = false;
                cardView.setBackgroundColor(0xffffffff);
                lang.setTextColor(0xff000000);
                deleteWord.setTextColor(0xff000000);
                text.setTextColor(0xff000000);
                transliterationView.setTextColor(0xff000000);
                lang.setText(data.get(current).srcLang);
                text.setText(data.get(current).word);
                transliterationView.setText("");

                ObjectAnimator animatorX = ObjectAnimator.ofFloat(cardView, "scaleX", 0.001f, 1f);
                animatorX.setDuration(nextCardAnimationDuration);
                animatorX.setInterpolator(nextCardAnimationInterpolator);
                ObjectAnimator animatorY = ObjectAnimator.ofFloat(cardView, "scaleY", 0.001f, 1f);
                animatorY.setDuration(nextCardAnimationDuration);
                animatorY.setInterpolator(nextCardAnimationInterpolator);
                AnimatorSet scaleUp = new AnimatorSet();
                scaleUp.playTogether(animatorX, animatorY);
                scaleUp.start();
                scaleUp.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        previous.setEnabled(true);
                    }

                });
            }

        });
        scaleDown.start();
    }

    private void gotoNext(final List<VocabCard> data) {

        if(current == data.size()-1) {
            allDone = true;
            ObjectAnimator animator = ObjectAnimator.ofFloat(allDoneDialog, "alpha", 0,1);
            animator.setDuration(200);
            animator.setInterpolator(new LinearInterpolator());
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    cardView.setVisibility(View.GONE);
                    next.setVisibility(View.GONE);
                    previous.setVisibility(View.GONE);
                    easy.setVisibility(View.GONE);
                    medium.setVisibility(View.GONE);
                    hard.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    }, 400);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
            return;
        }
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 0.001f);
        animatorX.setDuration(nextCardAnimationDuration);
        animatorX.setInterpolator(nextCardAnimationInterpolator);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 0.001f);
        animatorY.setDuration(nextCardAnimationDuration);
        animatorY.setInterpolator(nextCardAnimationInterpolator);
        final AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(animatorX, animatorY);

        scaleDown.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                ++current;

                if(current == data.size()-1){
                    next.setVisibility(View.INVISIBLE);
                    next.setEnabled(false);
                }

                previous.setVisibility(View.VISIBLE);
                previous.setEnabled(true);
                flipped = false;
                cardView.setBackgroundColor(0xffffffff);
                lang.setTextColor(0xff000000);
                text.setTextColor(0xff000000);
                deleteWord.setTextColor(0xff000000);
                transliterationView.setTextColor(0xff000000);
                lang.setText(data.get(current).srcLang);
                text.setText(data.get(current).word);
                transliterationView.setText("");

                ObjectAnimator animatorX = ObjectAnimator.ofFloat(cardView, "scaleX", 0.001f, 1f);
                animatorX.setDuration(nextCardAnimationDuration);
                animatorX.setInterpolator(nextCardAnimationInterpolator);
                ObjectAnimator animatorY = ObjectAnimator.ofFloat(cardView, "scaleY", 0.001f, 1f);
                animatorY.setDuration(nextCardAnimationDuration);
                animatorY.setInterpolator(nextCardAnimationInterpolator);
                AnimatorSet scaleUp = new AnimatorSet();
                scaleUp.playTogether(animatorX, animatorY);
                scaleUp.start();
                scaleUp.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }

                });
            }

        });
        scaleDown.start();

    }

}
