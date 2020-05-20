package ci.server.exception;

public class CommandException extends Exception {
    public CommandException(Exception e) {
        super(e);
    }

    public CommandException(String msg) {
        super(msg);
    }
}
