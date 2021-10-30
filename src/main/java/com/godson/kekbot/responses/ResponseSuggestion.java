package com.godson.kekbot.responses;

import net.dv8tion.jda.api.entities.User;

public class ResponseSuggestion {
    private final String suggesterID;
    private final String actionName;
    private final String suggestedResponse;

    public ResponseSuggestion(User suggester, Action action, String response) {
        suggesterID = suggester.getId();
        actionName = action.name();
        suggestedResponse = response;
    }

    public String getSuggesterID() {
        return suggesterID;
    }

    public String getActionName() {
        return actionName;
    }

    public String getSuggestedResponse() {
        return suggestedResponse;
    }
}
