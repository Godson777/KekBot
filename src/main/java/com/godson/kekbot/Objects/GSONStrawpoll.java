package com.godson.kekbot.Objects;

public class GSONStrawpoll {
    private long id;
    private String title;
    private String[] options;
    private boolean multi;
    private String dupcheck;
    private boolean captcha;
    private int[] votes;

    public GSONStrawpoll(String title) {
        this.title = title;
    }

    public long getID() {
        return this.id;
    }

    public GSONStrawpoll withOptions(String... options) {
        if (options.length <= 30) this.options = options;
        return this;
    }

    public GSONStrawpoll isMulti(boolean multi) {
        this.multi = multi;
        return this;
    }

}
