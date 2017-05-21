package com.ferjuarez.emotions;

import android.content.Context;

/**
 * Created by ferjuarez on 5/18/17.
 */

public class UndefinedState extends EmotionState {
    @Override
    public String getStateDescription(Context context) {
        return context.getString(R.string.undefined_state_description);
    }

    @Override
    public String getLikelihood() {
        return "UNDEFINED";
    }

    @Override
    public String getEmojiIcon() {
        return null;
    }
}
