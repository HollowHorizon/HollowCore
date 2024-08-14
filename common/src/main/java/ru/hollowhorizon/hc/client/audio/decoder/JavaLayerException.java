package ru.hollowhorizon.hc.client.audio.decoder;

import java.io.PrintStream;

public class JavaLayerException extends Exception {

    private Throwable exception;

    public JavaLayerException () {
    }

    public JavaLayerException (String msg) {
        super(msg);
    }

    public JavaLayerException (String msg, Throwable t) {
        super(msg);
        exception = t;
    }

    public Throwable getException () {
        return exception;
    }

    public void printStackTrace () {
        printStackTrace(System.err);
    }

    public void printStackTrace (PrintStream ps) {
        if (exception == null)
            super.printStackTrace(ps);
        else
            exception.printStackTrace();
    }

}
