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
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;

public class RunScript implements FileCallable<String> {

    private final TaskListener listener;
    private final String scriptPath;
    private String[] args;
    private final String caller;

    private static String scanId = null;
    private File tempFile;

    public RunScript(TaskListener listener, String scriptPath, String[] args, String caller) {
        this.listener = listener;
        this.scriptPath = scriptPath;
        this.args = args;
        this.caller = caller;
    }

    @Override
    public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {

        copyScript();
        runScript();
        deleteScript();

        return RunScript.scanId;
    }

    @Override
    public void checkRoles(RoleChecker checker) throws SecurityException {
        // We want this to be able to run on any type of machine (controller or agent)
        return;
    }

    private void copyScript() throws IOException {

        listener.getLogger().println(this.scriptPath);
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

        for (int i = 0; i < args.length; i++) {
            if (!StringUtils.isEmpty(args[i])) {
                args[i] = args[i].replace(" ", "");
            }

            if (args[i] != null && !args[i].isEmpty()) command.add(args[i]);
            else command.add("''");
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        logOutput(p.getInputStream(), "");
        logOutput(p.getErrorStream(), "Error: ");
        p.waitFor();
    }

    private void deleteScript() throws FileNotFoundException {
        boolean deleted_temp = tempFile.delete();
        if (!deleted_temp) {
            throw new FileNotFoundException("Temporary script file not found");
        }
    }

    private void logOutput(InputStream inputStream, String prefix) {
        new Thread(() -> {
                    Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
                    while (scanner.hasNextLine()) {
                        synchronized (this) {
                            String line = scanner.nextLine();
                            // Extract the scan ID from the cli output of scan init command.
                            if (prefix.isEmpty() && line.contains("Running scan with ID")) {
                                String[] tokens = line.split(" ");
                                RunScript.scanId = tokens[tokens.length - 1].substring(0, 36);
                            }

                            if (!caller.equals("abortScan")) {
                                listener.getLogger().println(prefix + line);
                            }
                        }
                    }
                    scanner.close();
                })
                .start();
    }
}
