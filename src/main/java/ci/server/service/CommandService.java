package ci.server.service;

import java.io.File;
import java.io.InputStream;

import ci.server.exception.CommandException;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

@Component
public class CommandService {
    public byte[] runCommand(File directory, String... command) throws CommandException {
        ProcessBuilder pb = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .directory(directory);
        try {
            Process process = pb.start();
            try (InputStream stream = process.getInputStream()) {
                byte[] data = IOUtils.toByteArray(stream);
                process.waitFor();
                int exitValue = process.exitValue();
                if (exitValue != 0) {
                    throw new CommandException("Return code is " + exitValue + ", value = " + new String(data));
                }
                return data;
            }
        } catch (Exception e) {
            throw new CommandException(e);
        }
    }
}
