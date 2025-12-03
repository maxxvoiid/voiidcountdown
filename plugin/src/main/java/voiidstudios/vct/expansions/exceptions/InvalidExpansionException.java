package voiidstudios.vct.expansions.exceptions;

public class InvalidExpansionException extends Exception {
    public InvalidExpansionException(String message) {
        super(message);
    }

    public InvalidExpansionException(String message, Throwable cause) {
        super(message, cause);
    }
}