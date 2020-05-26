package ci.server.api;

import ci.server.exception.CommandException;
import ci.server.exception.GitException;
import ci.server.service.CommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

import static ci.server.exception.ExceptionMessage.*;

@Component
public class GitApiCmdImpl implements GitApi {
    private static final Logger logger = LoggerFactory.getLogger(GitApiCmdImpl.class);

    @Autowired
    private CommandService commandService;

    @Override
    public void clone(File directory, String repoPath) throws GitException {
        runCommandHelper(CLONE_FAILED,
                directory, "git", "clone", repoPath);
    }

    @Override
    public void checkout(File directory, String branchName) throws GitException {
        runCommandHelper(CHECKOUT_FAILED,
                directory, "git", "checkout", branchName);
    }

    @Override
    public String startBisect(File directory, String version) throws GitException {
        return new String(runCommandHelper(START_BISECT_FAILED,
                directory, "git", "bisect", "start", "HEAD", version));
    }

    @Override
    public String runBisect(File directory, String buildPath) throws GitException {
        return new String(runCommandHelper(RUN_BISECT_FAILED,
                directory, "git", "bisect", "run", buildPath));
    }

    @Override
    public void resetBisect(File directory) throws GitException {
        runCommandHelper(RESET_BISECT_FAILED, directory, "git", "bisect", "reset");
    }

    @Override
    public String getFirstCommit(File directory) throws GitException {
        return new String(runCommandHelper(GET_FIRST_COMMIT_FAILED,
                directory,"git", "rev-list", "--max-parents=0", "HEAD")).trim();
    }

    @Override
    public void revertCommit(File directory, String commit) throws GitException {
        runCommandHelper(REVERT_FAILED, directory, "git", "revert", commit, "--no-edit");
    }

    @Override
    public void abortRevert(File directory) throws GitException {
        runCommandHelper(ABORT_REVERT_FAILED, directory, "git", "revert", "--abort");
    }

    @Override
    public void push(File directory) throws GitException {
        runCommandHelper(PUSH_FAILED, directory, "git", "push");
    }

    public byte[] runCommandHelper(String errorMsg, File directory, String... command) throws GitException {
        try {
            return commandService.runCommand(directory, command);
        } catch (CommandException e) {
            logger.error("{}. Command was failed with exception", errorMsg, e);
            throw new GitException(errorMsg, e);
        }
    }
}
