package com.godson.kekbot.Questionaire;

class Question {
    private QuestionType type;
    private String message;

    public Question(String message) {
        this.message = message;
    }

    public Question setType(QuestionType type) {
        this.type = type;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public QuestionType getType() {
        return type;
    }
}
