package ci.server;

import ci.server.exception.CommandException;
import ci.server.service.BisectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    @Autowired
    private BisectionService bisectionService;

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    public void run(ApplicationArguments args) throws CommandException {
        if (args.getSourceArgs().length != 3) {
            logger.error("Wrong number of parameters. " +
                    "Please use enter these parameters: git repository path, branch name and build script path");
            return;
        }
        String repoPath = args.getSourceArgs()[0];
        String branchName = args.getSourceArgs()[1];
        String buildPath = args.getSourceArgs()[2];
        bisectionService.bisectionProcess(repoPath, branchName, buildPath);
    }
}
