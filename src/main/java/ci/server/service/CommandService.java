package ci.server.service;

import ci.server.exception.CommandException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
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
                if (!exited || exitValue != 0) {
                    throw new CommandException("Return code is " + exitValue + ", value = " + new String(data));
                }
                return data;
            }
        } catch (Exception e) {
            throw new CommandException(e);
        }
    }
}
