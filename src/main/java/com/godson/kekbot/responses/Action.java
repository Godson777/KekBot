package com.godson.kekbot.responses;

public enum Action {
    //Successfully purged {} messages.
    PURGE_SUCCESS(1, "action.purgesuccess.default"),
    //{} isn't a valid number...
    NOT_A_NUMBER(1, "action.notanumber.default"),
    //Sorry, you can't purge over 100 messages.
    PURGE_TOOHIGH(0, "action.purgetoohigh.default"),
    //Sorry, that's too low of a number.
    PURGE_TOOLOW(0, "action.purgetoolow.default"),
    //Successfully purged {} messages containing the phrase {}.
    KEYPHRASE_PURGE_SUCCESS(2, "action.keyphrasepurgesuccess.default"),
    //Couldn't find any messages containing that phrase...
    KEYPHRASE_PURGE_FAIL(0, "action.keyphrasepurgefail.default"),
    //Successfully purged {} messages written by: {}
    MENTION_PURGE_SUCCESS(2, "action.mentionpurgesuccess.default"),
    //Couldn't find any messages written by that user.
    MENTION_PURGE_FAIL(0, "action.mentionpurgefail.default"),
    //Oh, you want me to ban nobody? Fair enough.
    BAN_EMPTY(0, "action.banempty.default"),
    //{} got banned.
    BAN_SUCCESS(1, "action.bansuccess.default"),
    //Oh, no one's getting kicked? Alright...
    KICK_EMPTY(0, "action.kickempty.default"),
    //{} got kicked.
    KICK_SUCCESS(1, "action.kicksuccess.default"),
    //You don't have the {} permission!
    NOPERM_USER(1, "action.nopermuser.default"),
    //I don't seem to have the {} permission!
    NOPERM_BOT(1, "action.nopermbot.default"),
    //I can't find the Living Meme role!
    MEME_NOT_FOUND(1, "action.memenotfound.default"),
    //I don't have the Living Meme role!
    MEME_NOT_APPLIED(1, "action.memenotapplied.default"),
    //I'm not even playing any music!
    MUSIC_NOT_PLAYING(0, "action.musicnotplaying.default"),
    //You have to be in {} to run music commands.
    MUSIC_NOT_IN_CHANNEL(1, "action.musicnotinchannel.default"),
    //Fine, I didn't wanna play music anyway...
    MUSIC_EMPTY_CHANNEL(0, "action.musicemptychannel.default"),
    //You have to be in a voice channel to run this command!
    GET_IN_VOICE_CHANNEL(0, "action.getinvoicechannel.default"),
    //{} has been given the role.
    ROLE_ADDED(1, "action.roleadded.default"),
    //{} no longer has that role.
    ROLE_TAKEN(1, "action.roletaken.default"),
    //Hm, I'll go with {}.
    CHOICE_MADE(1, "action.choicemade.default"),
    //Generic Error Message wew
    EXCEPTION_THROWN(0, "action.exceptionthrown.default");

    Action(int blanks, String unlocalizedMessage) {
        this.blanks = blanks;
        this.unlocalizedMessage = unlocalizedMessage;
    }

    private final int blanks;
    private final String unlocalizedMessage;

    public int getBlanksNeeded() {
        return blanks;
    }

    public String getUnlocalizedMessage() {
        return unlocalizedMessage;
    }
}
