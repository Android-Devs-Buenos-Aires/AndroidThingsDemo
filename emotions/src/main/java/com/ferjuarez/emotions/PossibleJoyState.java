package com.ferjuarez.emotions;

import android.content.Context;

/**
 * Created by ferjuarez on 5/18/17.
 */

public class PossibleJoyState extends EmotionState {
    @Override
    public String getStateDescription(Context context) {
        return context.getString(R.string.possible_joy_state_description);
    }

    @Override
    public String getLikelihood() {
        return "POSSIBLE";
    }

    @Override
    public String getEmojiIcon() {
        return "2";
    }

}
