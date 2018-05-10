package com.godson.kekbot;

/**
 * Represents an error message that's a simple String
 */
@SuppressWarnings("serial")
public class ThrowableString extends Throwable {

    public ThrowableString(String message) { super(message); }
    public ThrowableString(String message, Throwable cause) { super(message, cause); }
}
