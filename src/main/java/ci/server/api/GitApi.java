package ci.server.api;

import ci.server.exception.GitException;

import java.io.File;

public interface GitApi {
    void clone(File directory, String repoPath) throws GitException;

    void checkout(File directory, String branchName) throws GitException;

    String startBisect(File directory, String version) throws GitException;

    String runBisect(File directory, String buildPath) throws GitException;

    void resetBisect(File directory) throws GitException;

    String getFirstCommit(File directory) throws GitException;

    void revertCommit(File directory, String commit) throws GitException;

    void abortRevert(File directory) throws GitException;

    void push(File directory) throws GitException;
}
