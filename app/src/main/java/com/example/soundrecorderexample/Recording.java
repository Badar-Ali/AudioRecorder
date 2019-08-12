package com.example.soundrecorderexample;

public class Recording {

    String Uri, fileName;
    boolean isPlaying = false;
    boolean localStorageStatus = false;


    public Recording(String uri, String fileName, boolean isPlaying,boolean localStorageStatus) {
        Uri = uri;
        this.fileName = fileName;
        this.isPlaying = isPlaying;
        this.localStorageStatus = localStorageStatus;
    }

    public String getUri() {
        return Uri;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing){
        this.isPlaying = playing;
    }

    public boolean isLocalStorageStatus() { return localStorageStatus; }

    public void setLocalStorageStatus(boolean localStorageStatus) { this.localStorageStatus = localStorageStatus; }

}
