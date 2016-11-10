package com.godson.kekbot.Objects;

public class SPoll {
    private long id;
    private String title;
    private String[] options;
    private boolean multi;
    private String dupcheck;
    private boolean captcha;
    private int[] votes;

    public SPoll(String title) {
        this.title = title;
    }

    public long getID() {
        return this.id;
    }

    public SPoll withOptions(String... options) {
        if (options.length <= 30) this.options = options;
        return this;
    }

    public SPoll isMulti(boolean multi) {
        this.multi = multi;
        return this;
    }

}
