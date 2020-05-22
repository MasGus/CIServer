package ci.server.service;

import ci.server.entity.BisectionStatus;
import ci.server.api.GitApi;
import ci.server.api.GitApiCmdImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BisectionService {
    public final File RUN_DIR = new File(".");
    public BisectionStatus status = BisectionStatus.processing;
    public String repoPath;
    public String branchName;
    public String result;
    public String lastBranchTag;
    public String exception;
    private static final Logger logger = LoggerFactory.getLogger(BisectionService.class);

    public void bisectionProcess(String repoPath, String branchName, String buildPath) {
        this.repoPath = repoPath;
        this.branchName = branchName;
        Pattern repoPattern = Pattern.compile("/(\\w+).git");
        Matcher repoMatcher = repoPattern.matcher(repoPath);
        if(!repoMatcher.find()) {
            logger.error("Wrong repository path");
            return;
        }
        String repoName = repoMatcher.group(1);

        CommandService commandService = new CommandService();
        GitApi gitApi = new GitApiCmdImpl(commandService);
        File repoDir = new File("./" + repoName);
        try {
//            gitApi.clone(RUN_DIR, repoPath);
            gitApi.fetch(repoDir, branchName);
            gitApi.checkout(repoDir, branchName);
            String lastBranchTag = gitApi.getLastBranchTag(repoDir, branchName);
            gitApi.startBisect(repoDir, !lastBranchTag.isEmpty() ? lastBranchTag : gitApi.getFirstCommit(repoDir));
            String runBisectResponse = gitApi.runBisect(repoDir, buildPath);
            status = BisectionStatus.finished;
            Pattern badCommitPattern = Pattern.compile("(\\w+) is the first bad commit");
            Matcher badCommitMatcher = badCommitPattern.matcher(runBisectResponse);
            if(badCommitMatcher.find()) {
               result = badCommitMatcher.group(0);
            } else {
                result = "There are no bad commits. Please look for more information in application log.";
            }
            gitApi.resetBisect(repoDir);
        } catch (Exception e) {
            exception = e.getMessage();
        }
    }
}
