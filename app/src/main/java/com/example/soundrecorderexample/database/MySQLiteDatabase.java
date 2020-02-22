package com.example.soundrecorderexample.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.soundrecorderexample.models.State;
import com.example.soundrecorderexample.models.VocabCard;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class MySQLiteDatabase extends SQLiteOpenHelper {

    private static final String VOCABULARY_TABLE;
    private static final String RECENT_VOCABULARY_TABLE;
    private static final String DATA_TO_UPLOAD;
    private static final String FLAG_TABLE;
    private static final String FIRST_RUN_FLAG;
    private static final String CURRENT_CARD_FLAG = "CURRENT_CARD_FLAG";
    private static final String EXPLORED_CARD_FLAG = "EXPLORED_CARD_FLAG";
    private static final String ALL_DONE_CARD_FLAG = "ALL_DONE_CARD_FLAG";
    private static final String TIME_CARD_FLAG = "TIME_CARD_FLAG";
    private static final String NEW_CARD_AVAILABLE_DATE = "NEW_CARDS_AVAILABLE_DATE";
    private static final String FLIPPED_CARD_FLAG = "FLIPPED_CARD_FLAG";
    private static final String WORD_COLUMN;
    private static final String SRC_LANG_COLUMN;
    private static final String DEST_LANG_COLUMN;
    private static final String TRANSLITERATION_COLUMN;
    private static final String TRANSLATION_COLUMN;
    private static final String TIME_WHEN_TO_SHOW_COLUMN;
    private static final String ID;
    private static final String RANK_COLUMN;
    private static final String FILE_NAME_COLUMN;
    private static final String POSITION;
    private static final String STATE_TABLE = "STATE_TABLE";

    static {
        POSITION = "POSITION";
        RANK_COLUMN = "RANK_COLUMN";
        ID = "ID";
        TIME_WHEN_TO_SHOW_COLUMN = "TIME_WHEN_TO_SHOW";
        VOCABULARY_TABLE = "VOCABULARY";
        RECENT_VOCABULARY_TABLE = "RECENT_VOCABULARY";
        DATA_TO_UPLOAD = "DATA_TO_UPLOAD";
        FLAG_TABLE = "FLAG_TABLE";
        FIRST_RUN_FLAG = "FIRST_RUN_FLAG";
        WORD_COLUMN = "WORDS";
        SRC_LANG_COLUMN = "SOURCE_LANGUAGE";
        DEST_LANG_COLUMN = "DEST_LANGUAGE_COLUMN";
        TRANSLITERATION_COLUMN = "TRANSLITERATION";
        TRANSLATION_COLUMN = "TRANSLATION";
        FILE_NAME_COLUMN = "FILE_NAME_COLUMN";
    }

    private static final String[] COLS = new String[]{ID, POSITION, WORD_COLUMN, TRANSLATION_COLUMN, SRC_LANG_COLUMN, DEST_LANG_COLUMN, TRANSLITERATION_COLUMN, TIME_WHEN_TO_SHOW_COLUMN, RANK_COLUMN, FILE_NAME_COLUMN};
    private static final String[] COL_TYPES;

    static {
        COL_TYPES = new String[]{" TEXT", " INTEGER", " TEXT", " TEXT", " TEXT", " TEXT", " TEXT", " INTEGER", " INTEGER", " TEXT"};
    }

    private static MySQLiteDatabase database = null;
    private static final String CURRENT_LEARNING_SESSION = "CURRENT_LEARNING_SESSION";
    public static MySQLiteDatabase getInstance(Context context) {
        if (database == null) {
            database = new MySQLiteDatabase(context);
        }
        return database;
    }
    private MySQLiteDatabase(Context context) {
        super(context, "new_dictionary", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + STATE_TABLE + "(" +
                COLS[0] + COL_TYPES[0] + " PRIMARY KEY," +
                COLS[1] + COL_TYPES[1] + " , "+
                COLS[2] + COL_TYPES[2] + " , " +
                COLS[3] + COL_TYPES[3] + " , " +
                COLS[4] + COL_TYPES[4] + " , " +
                COLS[5] + COL_TYPES[5] + " , " +
                COLS[6] + COL_TYPES[6] + " , " +
                COLS[7] + COL_TYPES[7] + " , " +
                COLS[8] + COL_TYPES[8] + " , " +
                COLS[9] + COL_TYPES[9] + " " +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + VOCABULARY_TABLE + "(" +
                COLS[0] + COL_TYPES[0] + " PRIMARY KEY," +
                COLS[1] + COL_TYPES[1] + " , "+
                COLS[2] + COL_TYPES[2] + " , " +
                COLS[3] + COL_TYPES[3] + " , " +
                COLS[4] + COL_TYPES[4] + " , " +
                COLS[5] + COL_TYPES[5] + " , " +
                COLS[6] + COL_TYPES[6] + " , " +
                COLS[7] + COL_TYPES[7] + " , " +
                COLS[8] + COL_TYPES[8] + " , " +
                COLS[9] + COL_TYPES[9] + " " +
                ");");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + RECENT_VOCABULARY_TABLE + "(" +
                COLS[0] + COL_TYPES[0] + " PRIMARY KEY," +
                COLS[1] + COL_TYPES[1] + " , " +
                COLS[2] + COL_TYPES[2] + " , " +
                COLS[3] + COL_TYPES[3] + " , " +
                COLS[4] + COL_TYPES[4] + " , " +
                COLS[5] + COL_TYPES[5] + " , " +
                COLS[6] + COL_TYPES[6] + " , " +
                COLS[7] + COL_TYPES[7] + " , " +
                COLS[8] + COL_TYPES[8] + " , " +
                COLS[9] + COL_TYPES[9] + " " +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + CURRENT_LEARNING_SESSION + "(" +
                COLS[0] + COL_TYPES[0] + " PRIMARY KEY," +
                COLS[1] + COL_TYPES[1] + " , " +
                COLS[2] + COL_TYPES[2] + " , " +
                COLS[3] + COL_TYPES[3] + " , " +
                COLS[4] + COL_TYPES[4] + " , " +
                COLS[5] + COL_TYPES[5] + " , " +
                COLS[6] + COL_TYPES[6] + " , " +
                COLS[7] + COL_TYPES[7] + " , " +
                COLS[8] + COL_TYPES[8] + " , " +
                COLS[9] + COL_TYPES[9] + " " +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + DATA_TO_UPLOAD + "(" +
                COLS[0] + COL_TYPES[0] + " PRIMARY KEY," +
                COLS[1] + COL_TYPES[1]+ " , " +
                COLS[2] + COL_TYPES[2] + " , " +
                COLS[3] + COL_TYPES[3]+ " , " +
                COLS[4] + COL_TYPES[4] + " , " +
                COLS[5] + COL_TYPES[5] + " , " +
                COLS[6] + COL_TYPES[6] + " , " +
                COLS[7] + COL_TYPES[7] + " , " +
                COLS[8] + COL_TYPES[8] + " , " +
                COLS[9] + COL_TYPES[9] + " " +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + FLAG_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY, " +
                FIRST_RUN_FLAG + " INTEGER, " +
                CURRENT_CARD_FLAG + " INTEGER, " +
                EXPLORED_CARD_FLAG + " INTEGER, " +
                ALL_DONE_CARD_FLAG+ " INTEGER, " +
                TIME_CARD_FLAG+ " INTEGER, " +
                NEW_CARD_AVAILABLE_DATE+ " INTEGER, " +
                FLIPPED_CARD_FLAG + " INTEGER " +
                ");");

        ContentValues values = new ContentValues();
        values.put(ID, 1);
        values.put(FIRST_RUN_FLAG, 1);
        values.put(CURRENT_CARD_FLAG, 0);
        values.put(EXPLORED_CARD_FLAG, 0);
        values.put(ALL_DONE_CARD_FLAG, 1);
        long time = new Date().getTime();
        values.put(TIME_CARD_FLAG, time);
        values.put(NEW_CARD_AVAILABLE_DATE, time);
        values.put(FLIPPED_CARD_FLAG, 0);
        db.insert(FLAG_TABLE, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(
                "DROP TABLE IF EXISTS " + VOCABULARY_TABLE
        );
        db.execSQL(
                "DROP TABLE IF EXISTS " + RECENT_VOCABULARY_TABLE
        );
        db.execSQL(
                "DROP TABLE IF EXISTS " + DATA_TO_UPLOAD
        );
        db.execSQL(
                "DROP TABLE IF EXISTS " + FLAG_TABLE
        );
        db.execSQL(
                "DROP TABLE IF EXISTS " + CURRENT_LEARNING_SESSION
        );

        db.execSQL(
                "DROP TABLE IF EXISTS " + STATE_TABLE
        );


        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void insertWord(VocabCard card) {
        Date date = new Date();
        long time = date.getTime();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, card.getId());
        values.put(POSITION, card.position);
        values.put(WORD_COLUMN, card.word);
        values.put(RANK_COLUMN, card.rank);
        values.put(TRANSLATION_COLUMN, card.translation);
        values.put(SRC_LANG_COLUMN, card.srcLang);
        values.put(DEST_LANG_COLUMN, card.destLang);
        values.put(TRANSLITERATION_COLUMN, card.transliteration);
        values.put(TIME_WHEN_TO_SHOW_COLUMN, time);
        values.put(FILE_NAME_COLUMN, card.fileName);
        try {
            db.insert(VOCABULARY_TABLE, null, values);
        } catch (Exception ignored) {

        }
    }

    private void insertIntoRecentWord(VocabCard card) {
        Date date = new Date();
        long time = date.getTime();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, card.getId());
        values.put(WORD_COLUMN, card.word);
        values.put(TRANSLATION_COLUMN, card.translation);
        values.put(SRC_LANG_COLUMN, card.word);
        values.put(RANK_COLUMN, card.rank);
        values.put(DEST_LANG_COLUMN, card.destLang);
        values.put(TRANSLITERATION_COLUMN, card.transliteration);
        values.put(TIME_WHEN_TO_SHOW_COLUMN, time);
        values.put(FILE_NAME_COLUMN, card.fileName);
        db.insert(RECENT_VOCABULARY_TABLE, null, values);
    }

    public void insertWordList(@NonNull List<VocabCard> words) {

        SQLiteDatabase db = getWritableDatabase();
        db.delete(VOCABULARY_TABLE, "1", null);

        db.delete(RECENT_VOCABULARY_TABLE, "1", new String[]{});

        for (int i = 0; i < words.size(); i++) {
            insertWord(words.get(i));
            insertIntoRecentWord(words.get(i));
        }
    }

    public List<VocabCard> getAllVocabCards() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + VOCABULARY_TABLE, null);
        List<VocabCard> vocabCards = new ArrayList<>();
        while (cursor.moveToNext()) {
            VocabCard vocabCard = new VocabCard(cursor.getString(0), cursor.getInt(1), cursor.getString(4), cursor.getString(5), cursor.getString(2), cursor.getString(3), cursor.getString(6), cursor.getInt(8), cursor.getString(9));
            vocabCards.add(vocabCard);
        }
        cursor.close();
        return vocabCards;
    }

    public List<VocabCard> getRecentVocabCards() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + RECENT_VOCABULARY_TABLE, null);
        List<VocabCard> vocabCards = new ArrayList<>();
        while (cursor.moveToNext()) {
            VocabCard vocabCard = new VocabCard(cursor.getString(0), cursor.getInt(1), cursor.getString(4), cursor.getString(5), cursor.getString(2), cursor.getString(3), cursor.getString(6), cursor.getInt(8), cursor.getString(9));
            vocabCards.add(vocabCard);
        }
        cursor.close();
        return vocabCards;
    }

    public void initApp() {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + FLAG_TABLE, null);

        cursor.moveToFirst();
        int firstRun = cursor.getInt(0);

        if (firstRun == 1) {
            database.close();
            cursor.close();
//            syncServerData();
        } else {
            database.close();
            cursor.close();
        }

    }

    private void syncServerData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child("Translations").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot recording : dataSnapshot.getChildren()) {
                            List<VocabCard> vocabCards = new ArrayList<>();
                            String srcLang = recording.child("srcLang").getValue(String.class);
                            String destLang = recording.child("destLang").getValue(String.class);
                            assert srcLang != null;
                            assert destLang != null;
//                            words = Objects.requireNonNull(recording.child(srcLang.toLowerCase()).getValue(String.class)).split(";");
//                            translations = Objects.requireNonNull(recording.child(destLang.toLowerCase()).getValue(String.class)).split(";");
//                            transliterations = Objects.requireNonNull(recording.child("InRomanLetters").getValue(String.class)).split(";");
                            Iterable<DataSnapshot> wordsList = recording.child("words").getChildren();

                            for(DataSnapshot wordNode : wordsList) {
                                int position = Integer.parseInt(Objects.requireNonNull(wordNode.getKey()));
                                String id = "" + new Date().getTime() + position;
                                String word = wordNode.child("word").getValue(String.class);
                                String translation = wordNode.child("translation").getValue(String.class);
                                String roman = wordNode.child("roman").getValue(String.class);
                                int rank = ((Long)Objects.requireNonNull(wordNode.child("rank").getValue())).intValue();
                                String fileName = wordNode.child("fname").getValue(String.class);
                                VocabCard vocabCard = new VocabCard(id, position, srcLang, destLang, word, translation, roman, rank, fileName);
                                vocabCards.add(vocabCard);
                            }

                            insertWordList(vocabCards);
                            break;
                        }
                        SQLiteDatabase database = getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(FIRST_RUN_FLAG, 0);
                        database.update(FLAG_TABLE, values, ID + " = ?", new String[]{"1"});
                        database.close();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }

                });
            }
        });

        thread.start();
    }

    public boolean deleteWord(String id) {
        SQLiteDatabase database = getWritableDatabase();
        int count = database.delete(VOCABULARY_TABLE, ID + " = ?", new String[]{id});
        count += database.delete(RECENT_VOCABULARY_TABLE, ID +  "= ?", new String[]{id});
        return count != 0;
    }

    public boolean addTimeStamp(String id, long milliSinceEpoch) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues(1);
        values.put(TIME_WHEN_TO_SHOW_COLUMN, milliSinceEpoch);
        int count = database.update(VOCABULARY_TABLE, values, ID +" = ?", new String[]{id});
        Cursor cursor = database.rawQuery("SELECT * FROM " + VOCABULARY_TABLE, null);
        cursor.moveToFirst();
        Log.d("cursor", cursor.getString(0));
        Log.d("cursor", cursor.getString(6));
        cursor.close();
        return count > 0;
    }

    private List<VocabCard> parseTable(int limit, Cursor cursor) {
        int i = 0;
        List<VocabCard> vocabCards = new ArrayList<>();
        while(cursor.moveToNext() && i < limit) {
            List<String> cols = Arrays.asList(COLS);
            String id = cursor.getString(cols.indexOf(ID));
            int position = cursor.getInt(cols.indexOf(POSITION));
            String word = cursor.getString(cols.indexOf(WORD_COLUMN));
            String translation = cursor.getString(cols.indexOf(TRANSLATION_COLUMN));
            String transliteration = cursor.getString(cols.indexOf(TRANSLITERATION_COLUMN));
            long timeStamp = cursor.getLong(cols.indexOf(TIME_WHEN_TO_SHOW_COLUMN));
            String fname = cursor.getString(cols.indexOf(FILE_NAME_COLUMN));
            String srcLang = cursor.getString(cols.indexOf(SRC_LANG_COLUMN));
            String destLang = cursor.getString(cols.indexOf(DEST_LANG_COLUMN));
            int rank = cursor.getInt(cols.indexOf(RANK_COLUMN));
            VocabCard vocabCard = new VocabCard(id, position, srcLang, destLang, word, translation, transliteration, rank, fname);
            vocabCards.add(vocabCard);
            i++;
            ContentValues values = new ContentValues();
            values.put(ID, id);
            values.put(WORD_COLUMN, word);
            values.put(TRANSLATION_COLUMN, translation);
            values.put(TRANSLITERATION_COLUMN, transliteration);
            values.put(SRC_LANG_COLUMN, srcLang);
            values.put(DEST_LANG_COLUMN, destLang);
            values.put(FILE_NAME_COLUMN, fname);
            values.put(TIME_WHEN_TO_SHOW_COLUMN, timeStamp);
            values.put(RANK_COLUMN, rank);
            SQLiteDatabase database = getWritableDatabase();
            database.insertWithOnConflict(CURRENT_LEARNING_SESSION, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
        return vocabCards;
    }

    public List<VocabCard> getNewCards(int limit) {
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + VOCABULARY_TABLE + " WHERE " +
                TIME_WHEN_TO_SHOW_COLUMN + " IS NULL OR " + TIME_WHEN_TO_SHOW_COLUMN + " <= ?" +
                        " ORDER BY " + RANK_COLUMN + " DESC, " + TIME_WHEN_TO_SHOW_COLUMN + " DESC;"
                , new String[]{String.valueOf((new Date()).getTime())});

        List<VocabCard> cards;
        if(cursor.getCount() > 0) {
            cards = parseTable(limit, cursor);
//            while (cursor.moveToNext() && index < limit) {
//                VocabCard vocabCard = new VocabCard(cursor.getString(0), cursor.getString(3), cursor.getString(4), cursor.getString(1), cursor.getString(2), cursor.getString(5), cursor.getLong(7), cursor.getString(8));
//                cards.add(vocabCard);
//                index++;
//            }
            cursor.close();
        }
        else {
            cursor.close();
            cursor = database.rawQuery("SELECT * FROM " + VOCABULARY_TABLE + " ORDER BY " + RANK_COLUMN + " DESC, " + TIME_WHEN_TO_SHOW_COLUMN + " ASC;", null);
            cards = parseTable(limit, cursor);
//            while(cursor.moveToNext() && index < limit) {
//                VocabCard vocabCard = new VocabCard(cursor.getString(0), cursor.getString(3), cursor.getString(4), cursor.getString(1), cursor.getString(2), cursor.getString(5), cursor.getLong(7), cursor.getString(8));
//                cards.add(vocabCard);
//                index++;
//            }
        }

        ContentValues values = new ContentValues();
        Calendar today = new GregorianCalendar(TimeZone.getDefault());
        Calendar tommorrow = new GregorianCalendar(TimeZone.getDefault());
        tommorrow.add(GregorianCalendar.DATE, 1);
        tommorrow.set(GregorianCalendar.HOUR, 0);
        tommorrow.set(GregorianCalendar.MINUTE, 0);
        tommorrow.set(GregorianCalendar.SECOND, 0);
        long tommorrowTimeInMillis = tommorrow.getTimeInMillis();
        long todayTimeInMillis = today.getTimeInMillis();

        values.put(NEW_CARD_AVAILABLE_DATE, tommorrowTimeInMillis - todayTimeInMillis);
        database = getWritableDatabase();
        database.update(FLAG_TABLE, values, ID + " = ?", new String[]{"1"});
        return cards;




    }

    private void insertWordInState(VocabCard card) {
        Date date = new Date();
        long time = date.getTime();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, card.getId());
        values.put(WORD_COLUMN, card.word);
        values.put(RANK_COLUMN, card.rank);
        values.put(TRANSLATION_COLUMN, card.translation);
        values.put(SRC_LANG_COLUMN, card.srcLang);
        values.put(DEST_LANG_COLUMN, card.destLang);
        values.put(TRANSLITERATION_COLUMN, card.transliteration);
        values.put(TIME_WHEN_TO_SHOW_COLUMN, time);
        values.put(FILE_NAME_COLUMN, card.fileName);
        try {
            db.insert(STATE_TABLE, null, values);
        } catch (Exception ignored) {

        }
    }

    public List<VocabCard> getStateVocabCards() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + STATE_TABLE, null);
        List<VocabCard> vocabCards = new ArrayList<>();
        while (cursor.moveToNext()) {
            VocabCard vocabCard = new VocabCard(cursor.getString(0), cursor.getInt(1), cursor.getString(4), cursor.getString(5), cursor.getString(2), cursor.getString(3), cursor.getString(6), cursor.getInt(8), cursor.getString(9));
            vocabCards.add(vocabCard);
        }
        cursor.close();
        return vocabCards;
    }

    public void saveState(List<VocabCard> cards, int current, int explored, boolean flipped, boolean allDone) {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(STATE_TABLE, "1", null);
        ContentValues values = new ContentValues();
        values.put(CURRENT_CARD_FLAG, current);
        values.put(EXPLORED_CARD_FLAG, explored);
        values.put(ALL_DONE_CARD_FLAG, allDone);
        values.put(FLIPPED_CARD_FLAG, flipped);
        long time = new Date().getTime();
        values.put(TIME_CARD_FLAG, time);
        database.update(FLAG_TABLE, values, ID + " = ?", new String[]{"1"});
        for(int i = 0; i < cards.size(); i++) {
            insertWordInState(cards.get(i));
        }
    }

    public State getState(){
        SQLiteDatabase database = getReadableDatabase();
        List<VocabCard> cards = getStateVocabCards();
        Cursor cursor = database.rawQuery("SELECT * FROM " + FLAG_TABLE, null);
        if(cursor == null || cursor.getCount() == 0) {
            if(cursor != null) cursor.close();
            return null;
        }
        cursor.moveToFirst();
        State state = new State(cards, cursor.getInt(2), cursor.getInt(3), cursor.getInt(7) != 0, cursor.getInt(4) != 0, cursor.getLong(5));
        cursor.close();
        return state;
    }

    public long getNewWordsShowTime() {

        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + FLAG_TABLE, null);
        cursor.moveToFirst();
        long time = cursor.getLong(6);
        cursor.close();
        return time;
    }

    public boolean editWordInVocabCard(String id, String word) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WORD_COLUMN, word);

        if(db.update(VOCABULARY_TABLE, values,ID + " = ?", new String[]{id}) > 0){
            return true;
        }
        else {
            return false;
        }
    }

}

