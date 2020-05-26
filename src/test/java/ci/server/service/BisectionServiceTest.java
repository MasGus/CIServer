package ci.server.service;

import ci.server.api.GitApi;
import ci.server.exception.CommandException;
import ci.server.exception.GitException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ci.server.service.BisectionService.RUN_DIR;
import static ci.server.service.BisectionService.RUN_DIR_FILE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doNothing;

@RunWith(SpringRunner.class)
@Import(AppTestConfiguration.class)
public class BisectionServiceTest {
    private final static String branchName = "master";
    private final static String testRepoPath = "testRepo";
    private final static String commitMessage = "test commit";
    private final static File testRepo = new File(RUN_DIR + File.separator + testRepoPath);
    private final static String testRepoSshPath = String.format("git@github.com:MasGus/%s.git", testRepoPath);
    private final String buildPath = new File(this.getClass().getResource("build.cmd").getFile()).getAbsolutePath();
    private static String badCommit;
    private static File badFile;

    @Autowired
    private CommandService commandService;

    @Autowired
    private BisectionService bisectionService;

    @SpyBean
    private GitApi gitApi;

    @Before
    public void init() throws CommandException, GitException, IOException {
        testRepo.mkdir();
        commandService.runCommand(testRepo, "git", "init");
        doNothing().when(gitApi).clone(RUN_DIR_FILE, testRepoSshPath);
        doNothing().when(gitApi).push(testRepo);
        Pattern commitPattern = Pattern.compile(String.format("(\\w+)] %s", commitMessage));
        for (int i = 1; i <= 7; i++) {
            String fileName = String.format("commitFile_%d.txt", i);
            File commitFile = new File(testRepoPath + File.separator + fileName);
            commitFile.createNewFile();
            commandService.runCommand(testRepo, "git", "add", fileName);
            String commitResponse = new String(commandService.runCommand(testRepo, "git", "commit", "-m", commitMessage));
            if (i == 3) {
                Matcher commitMatcher = commitPattern.matcher(commitResponse);
                commitMatcher.find();
                badCommit = commitMatcher.group(1);
                badFile = commitFile;
            }
        }
    }

    @After
    public void finalize() throws IOException {
        FileUtils.forceDelete(testRepo);
    }

    @Test
    public void bisectionProcess_shouldFindBadCommit() {
        bisectionService.bisectionProcess(testRepoSshPath, branchName, buildPath);
        assertFalse(badFile.exists());
        assertTrue(bisectionService.getResult().startsWith(badCommit));
        assertTrue(bisectionService.isBadCommitReverted());
        assertEquals(branchName, bisectionService.getBranchName());
        assertEquals(testRepoSshPath, bisectionService.getRepoPath());
        assertNull(bisectionService.getException());
    }
}