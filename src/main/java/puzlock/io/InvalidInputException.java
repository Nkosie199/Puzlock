package puzlock.io;

/** The input file cannot be turned into a voxel grid. The message is shown to the user as-is. */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }
}
