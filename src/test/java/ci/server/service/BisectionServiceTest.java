package ci.server.service;

import ci.server.api.GitApi;
import ci.server.entity.BisectionStatus;
import ci.server.exception.CommandException;
import ci.server.exception.ExceptionMessage;
import ci.server.exception.GitException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@RunWith(SpringRunner.class)
@Import(AppTestConfiguration.class)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class BisectionServiceTest {
    private static final String runDir = System.getProperty("user.dir");
    private static final File runDirFile = new File(runDir);
    private static final String branchName = "master";
    private static final String testRepoPath = "testRepo";
    private static final String commitMessage = "test commit";
    private static final File testRepo = new File(runDir + File.separator + testRepoPath);
    private static final String testRepoSshPath = String.format("git@github.com:MasGus/%s.git", testRepoPath);
    private static final String buildScript = SystemUtils.IS_OS_WINDOWS ? "build.cmd" : "build.sh";
    private static final String fileNamePattern = "commitFile_%d.txt";
    private static final int commitCount = 7;
    private static final int buildScriptBadCommitId = 3;
    private final File buildScriptFile = new File(this.getClass().getResource(buildScript).getFile());
    private final List<String> commits = new ArrayList<>();

    @Autowired
    private CommandService commandService;

    @Autowired
    private BisectionService bisectionService;

    @SpyBean
    private GitApi gitApi;

    @Before
    public void setUp() throws CommandException, GitException, IOException {
        testRepo.mkdir();
        commandService.runCommand(testRepo, "git", "init");
        doNothing().when(gitApi).clone(runDirFile, testRepoSshPath);
        doNothing().when(gitApi).push(testRepo);
        Pattern commitPattern = Pattern.compile(String.format("(\\w+)] %s", commitMessage));
        File testRepoBuildScript = new File(testRepo + File.separator + buildScript);
        Files.copy(buildScriptFile.toPath(), testRepoBuildScript.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
        for (int i = 0; i <= commitCount; i++) {
            String fileName = String.format(fileNamePattern, i);
            File commitFile = new File(testRepoPath + File.separator + fileName);
            commitFile.createNewFile();
            commandService.runCommand(testRepo, "git", "add", fileName);
            String commitResponse = new String(commandService.runCommand(testRepo, "git", "commit", "-m", commitMessage));
            Matcher commitMatcher = commitPattern.matcher(commitResponse);
            commitMatcher.find();
            commits.add(commitMatcher.group(1));
        }
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.forceDelete(testRepo);
    }

    @Test
    public void bisectionProcess_shouldFindBadCommit() {
        bisectionService.bisectionProcess(testRepoSshPath, branchName, buildScript);
        assertFalse("File should be deleted after commit reversion", new File(String.format(fileNamePattern, buildScriptBadCommitId)).exists());
        assertTrue("Wrong bad commit", bisectionService.getResult().startsWith(commits.get(buildScriptBadCommitId)));
        assertEquals("Bisection statuses should be equal", BisectionStatus.finished, bisectionService.getStatus());
        assertTrue("Bad commit should be reverted", bisectionService.isBadCommitReverted());
        assertEquals("Branch names should be equal", branchName, bisectionService.getBranchName());
        assertEquals("Repo paths should be equal", testRepoSshPath, bisectionService.getRepoPath());
        assertNull("Exception should be null", bisectionService.getException());
    }

    @Test
    public void bisectionProcess_wrongGitPathFormat() {
        String wrongRepoSshPath = "wrongtestRepo.ssh";
        bisectionService.bisectionProcess(wrongRepoSshPath, branchName, buildScript);
        assertEquals("Exception messages should be equal", ExceptionMessage.WRONG_REPO_PATH, bisectionService.getException());
        assertEquals("Bisection statuses should be equal", BisectionStatus.failed, bisectionService.getStatus());
    }

    @Test
    public void bisectionProcess_gitCommandFailed() {
        String fakeBranch = "fakeBranch";
        bisectionService.bisectionProcess(testRepoSshPath, fakeBranch, buildScript);
        assertEquals("Exception messages should be equal", ExceptionMessage.CHECKOUT_FAILED, bisectionService.getException());
        assertEquals("Bisection statuses should be equal", BisectionStatus.failed, bisectionService.getStatus());
    }
}