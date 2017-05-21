package com.ferjuarez.emotions;

import android.content.Context;

/**
 * Created by ferjuarez on 5/18/17.
 */

public abstract class EmotionState {

    public static EmotionState getEmotionState(String likelihood){
        if(likelihood != null){
            switch (likelihood) {
                case "UNDEFINED":
                    return new UndefinedState();
                case "UNKNOWN":
                    return new UnknownState();
                case "VERY_UNLIKELY":
                    return new VerySadState();
                case "UNLIKELY":
                    return new SadState();
                case "POSSIBLE":
                    return new PossibleJoyState();
                case "LIKELY":
                    return new JoyState();
                case "VERY_LIKELY":
                    return new VeryJoyState();
            }
            return new UndefinedState();
        } else return new UndefinedState();
    }

    public abstract String getStateDescription(Context context);
    public abstract String getLikelihood();
    public abstract String getEmojiIcon();
}
