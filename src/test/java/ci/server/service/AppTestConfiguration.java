package ci.server.service;

import ci.server.api.GitApi;
import ci.server.api.GitApiCmdImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AppTestConfiguration {
    @Bean
    CommandService createCommandService(){
        return new CommandService();
    }

    @Bean
    GitApi createGitApi(){
        return new GitApiCmdImpl();
    }

    @Bean
    BisectionService createBisectionService(){
        return new BisectionService();
    }
}
