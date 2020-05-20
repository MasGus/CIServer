package ci.server.service;

import ci.server.exception.CommandException;
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
    public final File RUN_DIR = new File(".");
    public boolean isFinished = false;
    public String result = "";
    public String exception = "";
    private static final Logger logger = LoggerFactory.getLogger(BisectionService.class);

    public void bisectionProcess(String repoPath, String branchName, String buildPath) {
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
//            String lastBranchTag = gitApi.getLastBranchTag(repoDir, branchName);
//            gitApi.startBisect(repoDir, lastBranchTag);
            gitApi.startBisect(repoDir, "9844179f941fc5dd2a5880f75ba66358e878ab55");
            String runBisectResponse = gitApi.runBisect(repoDir, buildPath);
            result = runBisectResponse;
            isFinished = true;
            Pattern badCommitPattern = Pattern.compile("(\\w+) is the first bad commit");
            Matcher badCommitMatcher = badCommitPattern.matcher(runBisectResponse);
            if(badCommitMatcher.find()) {
               result = badCommitMatcher.group(0);
            } else {
                result = "There are no bad commits";
            }
            gitApi.resetBisect(repoDir);
        } catch (Exception e) {
            exception = e.getMessage();
        }
    }
}
