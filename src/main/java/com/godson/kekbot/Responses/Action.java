package com.godson.kekbot.Responses;

public enum Action {
    //Successfully purged {} messages.
    PURGE_SUCCESS(1),
    //{} isn't a a number...
    NOT_A_NUMBER(1),
    //Sorry, that's too high of a number.
    PURGE_TOOHIGH(0),
    //Sorry, that's too low of a number.
    PURGE_TOOLOW(0),
    //Successfully purged {} messages containing the phrase {}.
    KEYPHRASE_PURGE_SUCCESS(2),
    //Couldn't find any messages containing that phrase...
    KEYPHRASE_PURGE_FAIL(0),
    //Successfully purged {} messages written by: {}
    MENTION_PURGE_SUCCESS(2),
    //Couldn't find any messages written by that user.
    MENTION_PURGE_FAIL(0);


    Action(int blanks) {
        this.blanks = blanks;
    }

    private int blanks;

    public int getBlanksNeeded() {
        return blanks;
    }
}
