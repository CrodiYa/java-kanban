package util.exceptions;

public class TaskNotFound extends RuntimeException {

    public TaskNotFound() {
        super();
    }

    public TaskNotFound(String message) {
        super(message);
    }

    public TaskNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
