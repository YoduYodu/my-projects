package hotel;

/**
 * Exception when rating is out of range
 */
public class InvalidRatingException extends Exception{
    /**
     * Constructor
     * @param message
     */
    public InvalidRatingException(String message) {
        super(message);
    }
}
