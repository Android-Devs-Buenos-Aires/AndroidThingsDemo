package com.ferjuarez.emotions;

import android.content.Context;

/**
 * Created by ferjuarez on 5/18/17.
 */

public class SadState extends EmotionState {
    @Override
    public String getStateDescription(Context context) {
        return context.getString(R.string.sad_state_description);
    }

    @Override
    public String getLikelihood() {
        return "UNLIKELY";
    }

    @Override
    public String getEmojiIcon() {
        return "3";
    }

}
