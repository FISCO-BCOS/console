package console.exception;

import java.io.IOException;

public class ConsoleMessageException extends IOException {

    private static final long serialVersionUID = 1L;

    public ConsoleMessageException() {
        super();
    }

    public ConsoleMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsoleMessageException(String message) {
        super(message);
    }

    public ConsoleMessageException(Throwable cause) {
        super(cause);
    }
}
