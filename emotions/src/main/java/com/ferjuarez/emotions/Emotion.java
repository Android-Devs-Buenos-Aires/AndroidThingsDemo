package com.ferjuarez.emotions;

/**
 * Created by ferjuarez on 5/18/17.
 */

public abstract class Emotion {

    private EmotionState mEmotionState;

    public Emotion(String likelihood) {
        this.mEmotionState = EmotionState.getEmotionState(likelihood);
    }

    public EmotionState getState(){
        return mEmotionState;
    }

    public void changeEmotionState(String likelihood){
        this.mEmotionState = EmotionState.getEmotionState(likelihood);
    }

}
