package ci.server.api;

import ci.server.exception.GitException;

import java.io.File;

public interface GitApi {
    public static final String VERSION_TAG_PREFIX = "civ";

    void clone(File directory, String repoPath) throws GitException;

    void fetch(File directory, String branchName) throws GitException;

    void checkout(File directory, String branchName) throws GitException;

    void pull(File directory, String branchName) throws GitException;

    void startBisect(File directory, String version) throws GitException;

    String runBisect(File directory, String buildPath) throws GitException;

    void resetBisect(File directory) throws GitException;

    String getLastBranchTag(File directory, String branchName) throws GitException;

    String getFirstCommit(File directory) throws GitException;
}
