package com.ferjuarez.emotions;

import android.content.Context;

/**
 * Created by ferjuarez on 5/18/17.
 */

public class VeryJoyState extends EmotionState {
    @Override
    public String getStateDescription(Context context) {
        return context.getString(R.string.very_joy_state_description);
    }

    @Override
    public String getLikelihood() {
        return "VERY_LIKELY";
    }

    @Override
    public String getEmojiIcon() {
        return "1";
    }
}
