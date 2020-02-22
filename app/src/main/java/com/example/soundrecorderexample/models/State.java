package com.example.soundrecorderexample.models;

import java.util.ArrayList;
import java.util.List;

public class State {

    public List<VocabCard> cards;

    public int current;
    public int explored;
    public boolean flipped;
    public boolean allDone;
    public long time;

    public State(List<VocabCard> cards, int current, int explored, boolean flipped, boolean allDone, long time) {
        this.cards = cards;
        this.current = current;
        this.explored = explored;
        this.flipped = flipped;
        this.allDone = allDone;
        this.time = time;
    }

}
