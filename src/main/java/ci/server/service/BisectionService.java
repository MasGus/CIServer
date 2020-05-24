package ci.server.service;

import ci.server.entity.BisectionStatus;
import ci.server.api.GitApi;
import ci.server.api.GitApiCmdImpl;
import ci.server.exception.GitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BisectionService {
    public final String RUN_DIR = System.getProperty("user.dir");
    public final File RUN_DIR_FILE = new File(RUN_DIR);
    public BisectionStatus status = BisectionStatus.processing;
    public String repoPath;
    public String branchName;
    public String result;
    public String bisectStartedCommit;
    public Long commitCount;
    public String exception;
    public Long startedTime;
    public boolean isBadCommitReverted;
    private static final Logger logger = LoggerFactory.getLogger(BisectionService.class);

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

        CommandService commandService = new CommandService();
        GitApi gitApi = new GitApiCmdImpl(commandService);
        File repoDir = new File(RUN_DIR + File.separator + repoName);
        try {
            gitApi.clone(RUN_DIR_FILE, repoPath);
            gitApi.checkout(repoDir, branchName);
            String startBisectResponse = gitApi.startBisect(repoDir, gitApi.getFirstCommit(repoDir));
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
                try {
                    gitApi.revertCommit(repoDir, badCommitMatcher.group(1));
                    gitApi.push(repoDir);
                    this.isBadCommitReverted = true;
                } catch (GitException e) {
                    this.exception = e.getMessage();
                    gitApi.abortRevert(repoDir);
                }

            } else {
                this.result = "There are no bad commits. Please look for more information in application log.";
            }
        } catch (Exception e) {
            this.exception = e.getMessage();
            this.status = BisectionStatus.failed;
        }
    }
}
