package util.exceptions;

public class TaskTimeOverlapException extends RuntimeException{

    public TaskTimeOverlapException() {
        super();
    }

    public TaskTimeOverlapException(String message) {
        super(message);
    }

    public TaskTimeOverlapException(String message, Throwable cause) {
        super(message, cause);
    }
}
