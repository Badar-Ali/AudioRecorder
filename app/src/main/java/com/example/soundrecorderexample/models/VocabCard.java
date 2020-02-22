package com.example.soundrecorderexample.models;

import android.os.Parcel;
import android.os.Parcelable;

public class VocabCard implements Parcelable {
    private String id;
    public String srcLang;
    public String destLang;
    public String fileName;
    public String word;
    public String translation;
    public String transliteration;
    public int rank;
    public int position;

    public VocabCard(String id, int position, String srcLang, String destLang, String word, String translation, String transliteration, int rank, String fileName) {
        this.id = id;
        this.srcLang = srcLang;
        this.destLang = destLang;
        this.word = word;
        this.translation = translation;
        this.transliteration = transliteration;
        this.rank = rank;
        this.fileName = fileName;
        this.position = position;
    }

    public VocabCard(Parcel in) {
        id = in.readString();
        srcLang = in.readString();
        destLang = in.readString();
        fileName = in.readString();
        word = in.readString();
        translation = in.readString();
        transliteration = in.readString();
        rank = in.readInt();
        position = in.readInt();
    }

    public static final Creator<VocabCard> CREATOR = new Creator<VocabCard>() {
        @Override
        public VocabCard createFromParcel(Parcel in) {
            return new VocabCard(in);
        }

        @Override
        public VocabCard[] newArray(int size) {
            return new VocabCard[size];
        }
    };

    public String getId() {

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(id);
        dest.writeInt(position);
        dest.writeString(srcLang);
        dest.writeString(destLang);
        dest.writeString(fileName);
        dest.writeString(word);
        dest.writeString(translation);
        dest.writeString(transliteration);
        dest.writeInt(rank);
    }
}
