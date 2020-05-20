package ci.server.exception;

public class CommandFailureException extends CommandException {
    public CommandFailureException(Exception e) {
        super(e);
    }

    public CommandFailureException(String msg) {
        super(msg);
    }
}
