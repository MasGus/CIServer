package ci.server.exception;

public class ExceptionMessage {
    private ExceptionMessage() {}

    public static final String ABORT_REVERT_FAILED = "Could not abort revert";
    public static final String CHECKOUT_FAILED = "Could not checkout branch";
    public static final String CLONE_FAILED = "Could not clone repository";
    public static final String GET_FIRST_COMMIT_FAILED = "Could not get first commit";
    public static final String NO_BAD_COMMIT = "Could not find bad commit. Please look for more information in application log.";
    public static final String PUSH_FAILED = "Could not push";
    public static final String REVERT_FAILED = "Could not revert commit";
    public static final String RESET_BISECT_FAILED = "Could not reset bisection";
    public static final String RUN_BISECT_FAILED = "Could not run bisection";
    public static final String START_BISECT_FAILED = "Could not start bisection";
    public static final String WRONG_REPO_PATH = "Wrong repository path";
}
