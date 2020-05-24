package ci.server.service;

import ci.server.entity.BisectionStatus;
import ci.server.api.GitApi;
import ci.server.exception.GitException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Component
public class BisectionService {
    private static final Logger logger = LoggerFactory.getLogger(BisectionService.class);
    public static final String RUN_DIR = System.getProperty("user.dir");
    public static final File RUN_DIR_FILE = new File(RUN_DIR);
    private BisectionStatus status = BisectionStatus.processing;
    private String repoPath;
    private String branchName;
    private String result;
    private String bisectStartedCommit;
    private Long commitCount;
    private String exception;
    private Long startedTime;
    private boolean isBadCommitReverted;

    @Autowired
    private GitApi gitApi;

    public void bisectionProcess(String repoPath, String branchName, String buildPath) {
        this.repoPath = repoPath;
        this.branchName = branchName;
        this.startedTime = System.currentTimeMillis();
        Pattern repoPattern = Pattern.compile("/(\\w+).git");
        Matcher repoMatcher = repoPattern.matcher(repoPath);
        if(!repoMatcher.find()) {
            logger.error("Wrong repository path");
            return;
        }
        String repoName = repoMatcher.group(1);
        File repoDir = new File(RUN_DIR + File.separator + repoName);
        try {
            gitApi.clone(RUN_DIR_FILE, repoPath);
            gitApi.checkout(repoDir, branchName);
            this.bisectStartedCommit = gitApi.getFirstCommit(repoDir);
            String startBisectResponse = gitApi.startBisect(repoDir, bisectStartedCommit);
            Pattern countCommitPattern = Pattern.compile("roughly (\\d+) step");
            Matcher countCommitMather = countCommitPattern.matcher(startBisectResponse);
            if(countCommitMather.find()) {
                this.commitCount = Long.valueOf(countCommitMather.group(1));
            }
            String runBisectResponse = gitApi.runBisect(repoDir, buildPath);
            this.status = BisectionStatus.finished;
            Pattern badCommitPattern = Pattern.compile("(\\w+) is the first bad commit");
            Matcher badCommitMatcher = badCommitPattern.matcher(runBisectResponse);
            gitApi.resetBisect(repoDir);
            if(badCommitMatcher.find()) {
                this.result = badCommitMatcher.group(0);
                revertCommit(repoDir, badCommitMatcher.group(1));

            } else {
                this.result = "There are no bad commits. Please look for more information in application log.";
            }
        } catch (Exception e) {
            this.exception = e.getMessage();
            this.status = BisectionStatus.failed;
        }
    }

    private void revertCommit(File directory, String commit) throws GitException {
        try {
            gitApi.revertCommit(directory, commit);
            gitApi.push(directory);
            this.isBadCommitReverted = true;
        } catch (GitException e) {
            this.exception = e.getMessage();
            gitApi.abortRevert(directory);
        }
    }
}
