package no.nav.arbeidsgiver.tiltakrefusjon.exceptions;

public class RefusjonException extends RuntimeException {

    public RefusjonException(String message) {
        super(message);
    }

    public RefusjonException(String message, Throwable cause) {
        super(message, cause);
    }
}
