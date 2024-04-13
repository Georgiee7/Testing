package me.george.testing.utils;

import lombok.Getter;
import me.george.testing.Testing;

/**
 * A general exception that will throw an alert when thrown.
 * Created by George K. on 4/13/2024.
 */
@Getter
public class GeneralException extends RuntimeException {

    private final String alert;
    private final Throwable exception;

    public GeneralException(String alert) {
        this(null, alert);
    }

    public GeneralException(String alert, Object... args) {
        this(Utils.formatStringSafely(alert));
    }

    public GeneralException(Throwable e, String alert, Object... args) {
        this(e, Utils.formatStringSafely(alert));
    }

    public GeneralException(Throwable cause, String alert) {
        super(getMessage(alert, cause), cause);
        this.alert = alert;
        this.exception = cause;

        try {
            Testing.logInfo(alert + (cause != null ? " (" + getClass().getSimpleName() + ")" : ""));
        } catch (Throwable th) {
            Testing.logInfo("WARNING: There was an error broadcasting the fact that there was an error.");
            //noinspection CallToPrintStackTrace
            th.printStackTrace();
        }
    }

    /**
     * Get the displayed error message.
     * @param alert Alert for this exception.
     * @param e     Exception
     * @return message
     */
    private static String getMessage(String alert, Throwable e) {
        return e != null && e.getLocalizedMessage() != null ?
                (e instanceof GeneralException ? e.getLocalizedMessage() : alert + " (" + e.getLocalizedMessage() + ")")
                : alert;
    }
}
