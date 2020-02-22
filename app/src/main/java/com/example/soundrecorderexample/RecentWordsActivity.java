package com.example.soundrecorderexample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.example.soundrecorderexample.database.MySQLiteDatabase;
import com.example.soundrecorderexample.models.VocabCard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RecentWordsActivity extends AppCompatActivity {
    private Button previous;
    private Button next;
    private CardView cardView;
    private TextView text;
    private AppCompatTextView deleteWord;
    private TextView lang;
    private TextView transliterationView;
    boolean flipped = false;
    int current = 0;
    int explored = 0;
    Context context;
    private int nextCardAnimationDuration = 500;
    private Interpolator nextCardAnimationInterpolator = new FastOutSlowInInterpolator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);
        context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Speech Translation");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        cardView = findViewById(R.id.card);
        deleteWord = cardView.findViewById(R.id.deleteBtn);
        TextView noWords = findViewById(R.id.nowords);

        final List<VocabCard> vocabCardList = MySQLiteDatabase.getInstance(context).getRecentVocabCards();

        if (vocabCardList != null && !vocabCardList.isEmpty()) {

            if(vocabCardList.size() == 1) {
                next.setVisibility(View.INVISIBLE);
            }

            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    next.setEnabled(false);
                    gotoNext(vocabCardList);
                }
            });
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

            lang.setText(vocabCardList.get(0).srcLang);
            text.setText(vocabCardList.get(0).word);

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
                                text.setTextColor(0xffffffff);
                                deleteWord.setTextColor(0xffffffff);
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

        } else {
            noWords.setVisibility(View.VISIBLE);
            cardView.setVisibility(View.GONE);
            next.setVisibility(View.GONE);
            previous.setVisibility(View.GONE);

        }

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
                next.setVisibility(View.VISIBLE);
                next.setEnabled(true);
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
                        previous.setEnabled(true);
                    }

                });
            }

        });
        scaleDown.start();
    }

    private void gotoNext(final List<VocabCard> data) {
        if(current == data.size()-1) {
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
                if(current >= explored) {
                    next.setEnabled(false);
                    next.setVisibility(View.INVISIBLE);
                }
                else {
                    next.setEnabled(true);
                    next.setVisibility(View.VISIBLE);
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
                        next.setEnabled(true);
                    }

                });
            }

        });
        scaleDown.start();

    }

}
