package ci.server.exception;

public class GitException extends Exception {
    public GitException(Exception e) {
        super(e);
    }

    public GitException(String msg) {
        super(msg);
    }

    public GitException(String message, Throwable cause) {
        super(message, cause);
    }
}
