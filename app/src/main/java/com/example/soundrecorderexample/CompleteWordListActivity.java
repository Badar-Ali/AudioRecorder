package com.example.soundrecorderexample;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.example.soundrecorderexample.database.MySQLiteDatabase;
import com.example.soundrecorderexample.models.VocabCard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class CompleteWordListActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView wordTranslationList;
    private TextView noWords;
    StorageReference mStorageRef;
    DatabaseReference mDatabaseRef;
    Context context;
    private String transliteration;
    private View parent;
    private View editPanel;
    private EditText editWord;
    private TextView editPanelOkBtn;
    private TextView editPanelCancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_word_list);
        context = this;
        toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle("Speech Translation");
        setSupportActionBar(toolbar);
        wordTranslationList = findViewById(R.id.cardList);
        parent = findViewById(R.id.parent);
        editPanel = findViewById(R.id.editPanel);
        editWord = findViewById(R.id.wordEdit);
        editPanelOkBtn = findViewById(R.id.editPanelOkBtn);
        editPanelCancelBtn = findViewById(R.id.editPanelCancelBtn);
        editPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(mainActivityIntent);
//                finish();
                onBackPressed();
            }
        });
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        List<VocabCard> vocabCards = MySQLiteDatabase.getInstance(getApplicationContext()).getAllVocabCards();
        noWords = findViewById(R.id.nowords);
        TextView srcLang = findViewById(R.id.srcLang);
        TextView destLang = findViewById(R.id.destLang);


        if (vocabCards != null && !vocabCards.isEmpty()) {

            srcLang.setText(vocabCards.get(0).srcLang);
            destLang.setText(vocabCards.get(0).destLang);
            WordTranslationAdapter translationAdapter = new WordTranslationAdapter(vocabCards);
            Context context;
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
            wordTranslationList.setLayoutManager(linearLayoutManager);
            wordTranslationList.setAdapter(translationAdapter);


        } else {
            noWords.setVisibility(View.VISIBLE);
            wordTranslationList.setVisibility(View.GONE);
        }

    }


    public static int getContrastColor(int color) {
        int r = (color & 0x00ff0000)>>16;
        int g = (color & 0x0000ff00)>>8;
        int b = (color & 0x000000ff);
        double y = (299 * r + 587 * g + 114 * b) / 1000;
        return y >= 128 ? 0xff000000 : 0xffffffff;
    }

    public static int getContrastVersionForColor(int color) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color),
                hsv);
        if (hsv[2] < 0.5) {
            hsv[2] = 0.7f;
        } else {
            hsv[2] = 0.3f;
        }
        hsv[1] = hsv[1] * 0.2f;
        return Color.HSVToColor(hsv);
    }

    public static int getComplementaryColor(int colorToInvert) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(colorToInvert), Color.green(colorToInvert),
                Color.blue(colorToInvert), hsv);
        hsv[0] = (hsv[0] + 180) % 360;
        return Color.HSVToColor(hsv);
    }

    class ColorContrastPair {
        int foreground;
        int background;

        public ColorContrastPair(int background) {
            this.foreground = getContrastVersionForColor(background);
            this.background = background;
        }
    }



    @SuppressWarnings("WeakerAccess")
    class WordTranslationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        class WordTranslationHolder extends RecyclerView.ViewHolder {

            TextView word;
            TextView translation;
            TextView transliteration;
            EditText wordEdit;
            EditText translationEdit;
            EditText transliterationEdit;
            AppCompatTextView deleteBtn;
            AppCompatTextView editBtn;
            View root;
            CardView card;


            @SuppressWarnings("WeakerAccess")
            public WordTranslationHolder(@NonNull View itemView) {
                super(itemView);
                word = itemView.findViewById(R.id.word);
                wordEdit = itemView.findViewById(R.id.wordEdit);
                translation = itemView.findViewById(R.id.translation);
                translationEdit = itemView.findViewById(R.id.translationEdit);
                transliteration = itemView.findViewById(R.id.transliteration);
                transliterationEdit = itemView.findViewById(R.id.transliterationEdit);
                deleteBtn = itemView.findViewById(R.id.deleteBtn);
                editBtn = itemView.findViewById(R.id.editBtn);
                root = itemView;
                card = root.findViewById(R.id.card);

            }
        }

        class WordTranslationHeader extends RecyclerView.ViewHolder {

            TextView source;
            TextView destination;

            public WordTranslationHeader(@NonNull View itemView) {
                super(itemView);
                source = itemView.findViewById(R.id.srcLang);
                destination = itemView.findViewById(R.id.destLang);

            }
        }

        //        List<String> srcWords;
//        List<String> destWords;
//        List<String> romanWords;
        List<VocabCard> data;
        List<Boolean> flippedArray;
        private int HEADER_VH = 0;
        private int BODY_VH = 1;
//        List<ColorContrastPair> colors;

        public WordTranslationAdapter(List<VocabCard> vocabCards) {
            data = vocabCards;
            flippedArray = new ArrayList<>();
//            colors = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < vocabCards.size(); i++) {
                flippedArray.add(false);
//                int a = 0xff000000;
//                int r = random.nextInt() & 0x00ff0000;
//                int g = random.nextInt() & 0x0000ff00;
//                int b = random.nextInt() & 0x000000ff;
//                int color = a | r | g | b;
//                colors.add(new ColorContrastPair(color));
            }
        }

        private void setTextViewDrawableColor(TextView textView, int color) {
            for (Drawable drawable : textView.getCompoundDrawables()) {
                if (drawable != null) {
                    drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(textView.getContext(), color), PorterDuff.Mode.SRC_IN));
                }
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new WordTranslationHolder(view);
//                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_list_item, parent, false);
//                return new WordTranslationHeader(view);
//            }
        }


        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int index) {

            final AppCompatTextView deleteWord;

            if(viewHolder instanceof WordTranslationHolder) {
                final int position = index;
                final WordTranslationHolder holder = (WordTranslationHolder) viewHolder;
                deleteWord = holder.deleteBtn;
                final TextView word= holder.word;
                final TextView translation = holder.translation;
                final TextView transliteration = holder.transliteration;

                word.setText(data.get(position).word);
//                holder.wordEdit.setText(data.get(position).word);
                translation.setText(data.get(position).translation);
                transliteration.setText(data.get(position).transliteration);
//                holder.translationEdit.setText(data.get(position).translation);
//                holder.transliterationEdit.setText(data.get(position).transliteration);

                holder.editBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
//                        v.setEnabled(false);

                        editPanel.setVisibility(View.VISIBLE);

                        editWord.setText(word.getText());


                        editPanelOkBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                editPanel.setVisibility(View.GONE);
                                if(editWord.getText().toString().isEmpty() || word.getText().toString().equals(editWord.getText().toString())){
                                    return;
                                }
                                final ProgressDialog progressDialog = new ProgressDialog(CompleteWordListActivity.this);
                                progressDialog.setTitle("Applying Changes");
                                progressDialog.setMessage("Please Wait....");
                                progressDialog.show();
                                FirebaseDatabase.getInstance().getReference().child("Translations")
                                        .child(data.get(position).fileName.split("\\.")[0])
                                        .child("words").child(data.get(position).position+"")
                                        .child("word").setValue(editWord.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    data.get(position).word = editWord.getText().toString();
                                                    MySQLiteDatabase.getInstance(getApplicationContext())
                                                            .editWordInVocabCard(data.get(position).getId()
                                                                    , editWord.getText().toString());
                                                    notifyDataSetChanged();
                                                }
                                                else{
                                                    Snackbar snackbar = Snackbar.make(parent, "Enable to change the word! Try again.", Snackbar.LENGTH_LONG);
                                                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                                                    ((TextView)snackbar.getView().findViewById(R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.colorWhite));
                                                }
                                                progressDialog.dismiss();
                                            }
                                        });
                            }
                        });

                        editPanelCancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                editPanel.setVisibility(View.GONE);
                            }
                        });

//
//                        if(holder.editBtn.getText().toString().equalsIgnoreCase("Edit")) {
//                            LinearLayoutManager manager = (LinearLayoutManager) wordTranslationList.getLayoutManager();
//
//                            assert manager != null;
//                            int firstIndex = manager.findFirstVisibleItemPosition();
//                            int lastIndex = manager.findLastVisibleItemPosition();
//                            int upDifference = position - firstIndex;
//                            int downDifference = lastIndex - position;
//                            int upDownDelta = (Math.abs(upDifference-downDifference))/2;
//
//
//                            if(upDifference > downDifference) {
//                                int scrollTo = lastIndex + upDownDelta > data.size() ? data.size() : lastIndex + upDownDelta;
//                                Objects.requireNonNull(wordTranslationList.getLayoutManager()).smoothScrollToPosition(wordTranslationList, null, scrollTo);
//                            }
//                            else {
//                                int scrollTo = firstIndex - upDownDelta <= 0 ? 0 : firstIndex - upDownDelta;
//                                Objects.requireNonNull(wordTranslationList.getLayoutManager()).smoothScrollToPosition(wordTranslationList, null, scrollTo);
//                            }
//
//                            Context context;
//                            LinearLayoutManager manager1 = new LinearLayoutManager(getApplicationContext()){
//                                @Override
//                                public boolean canScrollVertically() {
//                                    return false;
//                                }
//                            };
//                            manager1.setOrientation(RecyclerView.VERTICAL);
//                            wordTranslationList.setLayoutManager(manager1);
////                            Objects.requireNonNull(wordTranslationList.getLayoutManager()).smoothScrollToPosition(wordTranslationList, null, position);
//                            holder.card.setTag(R.id.cardElevationTag, holder.card.getCardElevation());
//                            holder.card.setCardElevation(16);
//                            RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) holder.card.getLayoutParams();
//                            holder.card.setTag(R.id.cardHeightTag, param.height);
//                            param.height = param.height + 400;
//                            deleteWord.setText("Cancel");
//                            deleteWord.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel), null, null, null);
//                            holder.editBtn.setText("Save");
//                            holder.editBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_save_black_24dp), null, null, null);
//                            word.setVisibility(View.GONE);
//                            holder.wordEdit.setVisibility(View.VISIBLE);
//                            holder.wordEdit.setTag(word.getText());
//                            translation.setVisibility(View.GONE);
//                            holder.translationEdit.setVisibility(View.VISIBLE);
//                            holder.translationEdit.setTag(translation.getText());
//                            transliteration.setVisibility(View.GONE);
//                            holder.transliterationEdit.setTag(transliteration.getText());
//                            holder.transliterationEdit.setVisibility(View.VISIBLE);
//                            v.setEnabled(true);
//                        }
//                        else {
//                            final ProgressDialog progressDialog = new ProgressDialog(CompleteWordListActivity.this);
//                            progressDialog.setTitle("Deleting");
//                            progressDialog.setMessage("Please Wait...");
//                            progressDialog.setCancelable(true);
//                            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                                @Override
//                                public void onCancel(DialogInterface dialog) {
//                                }
//                            });
//                            progressDialog.show();
//                            final VocabCard card = data.get(position);
//
//                            HashMap<String, Object> map = new HashMap<>();
//                            map.put("fname", card.fileName);
//                            map.put("roman", card.transliteration);
//                            map.put("rank", card.rank);
//                            map.put("word", card.word);
//                            map.put("translation", card.translation);
//
//                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Translations")
//                                    .child(data.get(position).fileName.split("\\.")[0])
//                                    .child("words").child(data.get(position).position + "");
//
//                            ref.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
//                                    manager.setOrientation(RecyclerView.VERTICAL);
//
//                                    wordTranslationList.setLayoutManager(manager);
//                                    holder.card.setCardElevation((Float) holder.card.getTag(R.id.cardElevationTag));
//                                    holder.card.getLayoutParams().height = (int) holder.card.getTag(R.id.cardHeightTag);
//                                    deleteWord.setText("Remove");
//                                    deleteWord.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_delete_black_24dp), null, null, null);
//                                    holder.editBtn.setText("Edit");
//                                    holder.editBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_edit_black_24dp), null, null, null);
//                                    progressDialog.dismiss();
//                                    if(task.isSuccessful()) {
//                                        word.setVisibility(View.VISIBLE);
//                                        word.setText(holder.wordEdit.getText());
//                                        holder.wordEdit.setVisibility(View.GONE);
//                                        translation.setVisibility(View.VISIBLE);
//                                        translation.setText(holder.translationEdit.getText());
//                                        holder.translationEdit.setVisibility(View.GONE);
//                                        transliteration.setVisibility(View.VISIBLE);
//                                        transliteration.setText(holder.translationEdit.getText());
//                                        holder.transliterationEdit.setVisibility(View.GONE);
//                                        v.setEnabled(true);
//                                    }
//                                    else {
//                                        word.setVisibility(View.VISIBLE);
//                                        word.setText(holder.wordEdit.getTag().toString());
//                                        holder.wordEdit.setVisibility(View.GONE);
//                                        translation.setVisibility(View.VISIBLE);
//                                        translation.setText(holder.translationEdit.getTag().toString());
//                                        holder.translationEdit.setVisibility(View.GONE);
//                                        transliteration.setVisibility(View.VISIBLE);
//                                        transliteration.setText(holder.translationEdit.getTag().toString());
//                                        holder.transliterationEdit.setVisibility(View.GONE);
//                                        Snackbar error = Snackbar.make(parent, "An Error Occurred! Please, Try Again", Snackbar.LENGTH_LONG);
//                                        View view = error.getView();
//                                        TextView textView = view.findViewById(R.id.snackbar_text);
//                                        textView.setTextColor(0xffffffff);
//                                        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
//                                        error.show();
//                                        v.setEnabled(true);
//                                    }
//                                }
//                            });
//
//
//                        }
                    }
                });

                deleteWord.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(deleteWord.getText().toString().equalsIgnoreCase("Cancel")){
                            LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
                            manager.setOrientation(RecyclerView.VERTICAL);
                            wordTranslationList.setLayoutManager(manager);

                            holder.card.setCardElevation((Float) holder.card.getTag(R.id.cardElevationTag));
                            holder.card.getLayoutParams().height = (int) holder.card.getTag(R.id.cardHeightTag);
                            deleteWord.setText("Remove");
                            deleteWord.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_delete_black_24dp), null, null, null);
                            holder.editBtn.setText("Edit");
                            holder.editBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_edit_black_24dp), null, null, null);
                            word.setVisibility(View.VISIBLE);
                            word.setText(holder.wordEdit.getTag().toString());
                            holder.wordEdit.setVisibility(View.GONE);
                            translation.setVisibility(View.VISIBLE);
                            translation.setText(holder.translationEdit.getTag().toString());
                            holder.translationEdit.setVisibility(View.GONE);
                            transliteration.setVisibility(View.VISIBLE);
                            transliteration.setText(holder.translationEdit.getTag().toString());
                            holder.transliterationEdit.setVisibility(View.GONE);
                            return;
                        }
                        new AlertDialog.Builder(CompleteWordListActivity.this)
                                .setTitle("Delete ?")
                                .setMessage("Do you really want to delete this word?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        final ProgressDialog progressDialog = new ProgressDialog(CompleteWordListActivity.this);
                                        progressDialog.setTitle("Deleting");
                                        progressDialog.setMessage("Please Wait...");
                                        progressDialog.setCancelable(true);
                                        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                            }
                                        });
                                        progressDialog.show();

                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                                        reference.child("Translations").child(data.get(position).fileName.split("\\.")[0]).child("words").child(data.get(position).position + "").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressDialog.dismiss();
                                                if(task.isSuccessful()) {
                                                    MySQLiteDatabase.getInstance(context).deleteWord(data.get(position).getId());
                                                    data.remove(position);
                                                    notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                });
            }
//            else if(viewHolder instanceof WordTranslationHeader) {
//                WordTranslationHeader holder = (WordTranslationHeader) viewHolder;
//                holder.source.setText(data.get(0).srcLang);
//                holder.destination.setText(data.get(0).destLang);
//            }

//            card.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(final View v) {
//
//                    final ObjectAnimator oa1 = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0f);
//                    final ObjectAnimator oa2 = ObjectAnimator.ofFloat(v, "scaleY", 0f, 1f);
//                    oa1.setDuration(300);
//                    oa2.setDuration(300);
//                    oa1.setInterpolator(new DecelerateInterpolator());
//                    oa2.setInterpolator(new AccelerateDecelerateInterpolator());
//                    oa1.addListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            super.onAnimationEnd(animation);
//                            TextView lang = holder.lang;
//                            TextView text = holder.text;
//                            TextView transliteration = holder.transliteration;
//                            View cardBackground = holder.cardBackground;
////                            ColorContrastPair pair = colors.get(position);
//                            flippedArray.set(position, !flippedArray.get(position));
//                            if (!flippedArray.get(position)) {
////                                setTextViewDrawableColor(deleteWord, 0xff000000);
//                                deleteWord.setTextColor(0xff000000);
//                                lang.setTextColor(0xff000000);
//                                text.setTextColor(0xff000000);
//                                transliteration.setTextColor(0xff000000);
//                                lang.setText(data.get(position).srcLang);
//                                text.setText(data.get(position).word);
//                                cardBackground.setBackgroundColor(0xffffffff);
//                                transliteration.setText("");
//                            } else {
//                                transliteration.setText(data.get(position).transliteration);
//                                cardBackground.setBackgroundColor(0xff000000);
////                                setTextViewDrawableColor(deleteWord, 0xffffffff);
//                                deleteWord.setTextColor(0xffffffff);
//                                lang.setTextColor(0xffffffff);
//                                text.setTextColor(0xffffffff);
//                                transliteration.setTextColor(0xffffffff);
//                                lang.setText(data.get(position).destLang);
//                                text.setText(data.get(position).translation);
//                            }
//
//                            oa2.start();
//                        }
//                    });
//                    oa1.start();
//
////                    v.animate().withLayer()
////                            .rotationY(90)
////                            .setDuration(300)
////                            .withEndAction(
////                                    new Runnable() {
////                                        @Override
////                                        public void run() {
////                                            TextView lang = holder.lang;
////                                            TextView text = holder.text;
////                                            TextView transliteration = holder.transliteration;
////                                            flippedArray.set(position, !flippedArray.get(position));
////                                            if (flippedArray.get(position)) {
////                                                lang.setText(destLang);
////                                                text.setText(destWords.get(position));
////                                                transliteration.setText(romanWords.get(position));
////                                            } else {
////                                                lang.setText(srcLang);
////                                                text.setText(srcWords.get(position));
////                                                transliteration.setText("");
////                                            }
////                                            // second quarter turn
////                                            v.setRotationY(90);
////                                            v.animate().withLayer()
////                                                    .rotationY(0)
////                                                    .setDuration(300)
////                                                    .start();
////                                        }
////                                    }
////                            ).start();
//                }
//            });


        }

        @Override
        public long getItemId(int pI) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
