package ru.hollowhorizon.hc.client.audio.decoder;

import ru.hollowhorizon.hc.client.audio.formats.Mp3Format;

public class DecoderException extends JavaLayerException  {
    private int errorcode = Mp3Format.UNKNOWN_ERROR;

    public DecoderException (String msg, Throwable t) {
        super(msg, t);
    }

    public DecoderException (int errorcode, Throwable t) {
        this(getErrorString(errorcode), t);
        this.errorcode = errorcode;
    }

    public int getErrorCode () {
        return errorcode;
    }

    static public String getErrorString (int errorcode) {
        // REVIEW: use resource file to map error codes
        // to locale-sensitive strings.

        return "Decoder errorcode " + Integer.toHexString(errorcode);
    }

}
