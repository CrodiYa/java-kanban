package util.exceptions;

public class ManagerLoadException extends ManagerSaveException {

    public ManagerLoadException() {
        super();
    }

    public ManagerLoadException(String message) {
        super(message);
    }

    public ManagerLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
