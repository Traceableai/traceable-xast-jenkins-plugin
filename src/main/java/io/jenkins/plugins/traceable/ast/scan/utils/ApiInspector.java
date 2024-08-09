package io.jenkins.plugins.traceable.ast.scan.utils;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import org.jenkinsci.remoting.RoleChecker;

public class ApiInspector implements FileCallable<String> {

    private final TaskListener listener;
    private final String scriptPath;
    private String[] args;
    private final boolean printLogsToConsole;

    private StringBuilder returnOutput = new StringBuilder();
    private File tempFile;

    public ApiInspector(TaskListener listener, String scriptPath, String[] args, boolean printLogsToConsole) {
        this.listener = listener;
        this.scriptPath = scriptPath;
        this.args = args;
        this.printLogsToConsole = printLogsToConsole;
    }

    @Override
    public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {

        copyScript();
        runScript();
        deleteScript();

        return returnOutput.toString();
    }

    @Override
    public void checkRoles(RoleChecker checker) throws SecurityException {
        return;
    }

    private void copyScript() throws IOException {

        String bundledScript = CharStreams.toString(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream(this.scriptPath)), Charsets.UTF_8));

        File tempFile = File.createTempFile(
                "script_" + this.scriptPath.replaceAll(".sh", "") + "_"
                        + UUID.randomUUID().toString(),
                ".sh");

        BufferedWriter x = com.google.common.io.Files.newWriter(tempFile, Charsets.UTF_8);
        x.write(bundledScript);
        x.close();

        this.tempFile = tempFile;
    }

    private void runScript() throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("/bin/bash");
        command.add(this.tempFile.getAbsolutePath());

        for (String arg : args) {
            if (arg != null && !arg.isEmpty()) command.add(arg);
            else command.add("''");
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        Process p = pb.start();

        if (printLogsToConsole) {
            logOutput(p.getInputStream(), "");
            logOutput(p.getErrorStream(), "Error: ");
        } else {
            Scanner scanner = new Scanner(p.getInputStream(), StandardCharsets.UTF_8);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                returnOutput.append(line).append("\n");
            }
            scanner.close();
        }
        p.waitFor();
    }

    private void deleteScript() throws FileNotFoundException {
        boolean deleted_temp = tempFile.delete();
        if (!deleted_temp) {
            throw new FileNotFoundException("Temporary script file not found");
        }
    }

    private void logOutput(InputStream is, String prefix) {
        new Thread(() -> {
                    Scanner scanner = new Scanner(is, StandardCharsets.UTF_8);
                    while (scanner.hasNextLine()) {
                        synchronized (this) {
                            String line = scanner.nextLine();
                            returnOutput.append(line).append("\n");
                            this.listener.getLogger().println(prefix + line);
                        }
                    }
                    scanner.close();
                })
                .start();
    }
}
