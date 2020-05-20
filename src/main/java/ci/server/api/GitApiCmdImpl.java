package ci.server.api;

import ci.server.exception.CommandException;
import ci.server.exception.GitException;
import ci.server.service.CommandService;

import java.io.File;

public class GitApiCmdImpl implements GitApi {
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
    public void fetch(File directory, String branchName) throws GitException {
        runCommandHelper("Could not fetch branch",
                directory, "git", "fetch", "origin", branchName);
    }

    @Override
    public void checkout(File directory, String branchName) throws GitException {
        runCommandHelper("Could not checkout branch",
                directory, "git", "checkout", branchName);
    }

    @Override
    public void startBisect(File directory, String version) throws GitException {
        runCommandHelper("Could not start bisection",
                directory, "git", "bisect", "start", "HEAD", version);
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
    public String getLastBranchTag(File directory, String branchName) throws GitException {
//        new String(commandService.runCommand(directory, "git", "tag", "--merged", branchName, "|", "grep",
//                VERSION_TAG_PREFIX, "|", "sort", "-r", "|", "head", "-n", "1"));
        return new String(runCommandHelper("Could not get last branch tag", directory,
                "git", "tag", "--merged", branchName, "|", "grep",
                VERSION_TAG_PREFIX, "|", "sort", "/R", "|", "head", "-n", "1"));

    }

    public byte[] runCommandHelper(String errorMsg, File directory, String... command) throws GitException {
        try {
            return commandService.runCommand(directory, command);
        } catch (CommandException e) {
            throw new GitException(errorMsg, e);
        }
    }

}
