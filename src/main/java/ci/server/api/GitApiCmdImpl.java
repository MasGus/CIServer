package ci.server.api;

import ci.server.exception.CommandException;
import ci.server.exception.GitException;
import ci.server.service.CommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class GitApiCmdImpl implements GitApi {
    private static final Logger logger = LoggerFactory.getLogger(GitApiCmdImpl.class);
    private CommandService commandService;
    
    public GitApiCmdImpl(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public void clone(File directory, String repoPath) throws GitException {
        runCommandHelper("Could not clone repository",
                directory, "git", "clone", repoPath);
    }

    @Override
    public void checkout(File directory, String branchName) throws GitException {
        runCommandHelper("Could not checkout branch",
                directory, "git", "checkout", branchName);
    }

    @Override
    public String startBisect(File directory, String version) throws GitException {
        return new String(runCommandHelper("Could not start bisection",
                directory, "git", "bisect", "start", "HEAD", version));
    }

    @Override
    public String runBisect(File directory, String buildPath) throws GitException {
        return new String(runCommandHelper("Could not run bisection",
                directory, "git", "bisect", "run", buildPath));
    }

    @Override
    public void resetBisect(File directory) throws GitException {
        runCommandHelper("Could not reset bisection", directory, "git", "bisect", "reset");
    }

    @Override
    public String getFirstCommit(File directory) throws GitException {
        return new String(runCommandHelper("Could not get first commit",
                directory,"git", "rev-list", "--max-parents=0", "HEAD")).trim();
    }

    @Override
    public void revertCommit(File directory, String commit) throws GitException {
        runCommandHelper("Could not revert commit", directory, "git", "revert", commit, "--no-edit");
    }

    @Override
    public void abortRevert(File directory) throws GitException {
        runCommandHelper("Could not abort revert", directory, "git", "revert", "--abort");
    }

    @Override
    public void push(File directory) throws GitException {
        runCommandHelper("Could not push", directory, "git", "push");
    }

    public byte[] runCommandHelper(String errorMsg, File directory, String... command) throws GitException {
        try {
            return commandService.runCommand(directory, command);
        } catch (CommandException e) {
            logger.error("Command was failed with exception", e);
            throw new GitException(errorMsg, e);
        }
    }

}
