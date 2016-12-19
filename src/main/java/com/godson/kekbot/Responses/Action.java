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
    MENTION_PURGE_FAIL(0),
    //Oh, you want me to ban nobody? Fair enough.
    BAN_EMPTY(0),
    //{} got banned.
    BAN_SUCCESS(1),
    //Oh, no one's getting kicked? Alright...
    KICK_EMPTY(0),
    //{} got kicked.
    KICK_SUCCESS(1),
    //You don't have the {} permission!
    NOPERM_USER(1),
    //I don't seem to have the {} permission!
    NOPERM_BOT(1),
    //I can't find the Living Meme role!
    MEME_NOT_FOUND(1),
    //I don't have the Living Meme role!
    MEME_NOT_APPLIED(1),
    //I'm not even playing any music!
    MUSIC_NOT_PLAYING(0),
    //You have to be in {} to run music commands.
    MUSIC_NOT_IN_CHANNEL(1),
    //Fine, I didn't wanna play music anyway...
    MUSIC_EMPTY_CHANNEL(0),
    //You have to be in a voice channel to run this command!
    GET_IN_VOICE_CHANNEL(0),
    //{} has been given the role.
    ROLE_ADDED(1),
    //{} no longer has that role.
    ROLE_TAKEN(1);

    Action(int blanks) {
        this.blanks = blanks;
    }

    private int blanks;

    public int getBlanksNeeded() {
        return blanks;
    }
}
