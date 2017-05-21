package com.ferjuarez.emotions;

import android.content.Context;

/**
 * Created by ferjuarez on 5/18/17.
 */

public class JoyState extends EmotionState {
    @Override
    public String getStateDescription(Context context) {
        return context.getString(R.string.joy_state_description);
    }

    @Override
    public String getLikelihood() {
        return "LIKELY";
    }

    @Override
    public String getEmojiIcon() {
        return "1";
    }
}
