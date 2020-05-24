package ci.server.service;

import ci.server.exception.CommandException;
import ci.server.exception.CommandFailureException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class CommandService {
    public byte[] runCommand(File directory, String... command) throws CommandException {
        ProcessBuilder pb = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .directory(directory);
        try {
            Process process = pb.start();
            try (InputStream stream = process.getInputStream()) {
                byte[] data = IOUtils.toByteArray(stream);
                boolean exited = process.waitFor(10, TimeUnit.SECONDS);
                int exitValue = process.exitValue();
                if (!exited) {
                    throw new CommandException("Return code is " + exitValue + ", value = " + new String(data));
                } else if (exitValue != 0) {
                    throw new CommandFailureException("Return code is " + exitValue + ", value = " + new String(data));
                }
                return data;
            }
        } catch (Exception e) {
            throw new CommandException(e);
        }
    }
}
