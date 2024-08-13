package io.jenkins.plugins.traceable.ast.scan.utils;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jenkinsci.remoting.RoleChecker;

public class GenerateReport implements FileCallable<String[]> {

    private final String scriptPath;
    private String[] args;
    private final String runId;

    private File tempFile;
    private String htmlReport;
    private String processExitValue = "SUCCESS";
    private StringBuilder report = new StringBuilder();

    public GenerateReport(String scriptPath, String[] args, String runId) {
        this.scriptPath = scriptPath;
        this.args = args;
        this.runId = runId;
    }

    @Override
    public String[] invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {

        copyScript();
        runScript();
        deleteScript();

        String[] ret = {this.processExitValue, this.htmlReport};
        return ret;
    }

    @Override
    public void checkRoles(RoleChecker checker) throws SecurityException {
        return;
    }

    private void copyScript() throws IOException {

        String bundledScript = CharStreams.toString(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream(this.scriptPath)), Charsets.UTF_8));

        File tempFile = File.createTempFile("script_" + UUID.randomUUID().toString(), ".sh");

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
        pb.redirectErrorStream(true);
        Process p = pb.start();
        logOutput(p.getInputStream());
        p.waitFor();

        if (p.exitValue() == 4) {
            this.processExitValue = "FAILURE";
        }

        // Creating HTML
        Pattern REPORT_PATTERN = Pattern.compile("Name.*", Pattern.DOTALL);
        Matcher m = REPORT_PATTERN.matcher(report.toString());
        if (m.find()) {
            String textReport = m.group();
            this.htmlReport = createHtmlReport(textReport);
        }
    }

    private void deleteScript() throws FileNotFoundException {
        boolean deleted_temp = tempFile.delete();
        if (!deleted_temp) {
            throw new FileNotFoundException("Temporary script file not found");
        }
    }

    private void logOutput(InputStream inputStream) throws IOException {

        Scanner scanner = new Scanner(inputStream, Charsets.UTF_8);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            report.append(line).append("\n");
            System.out.println(line);
        }
        scanner.close();
    }

    private String createHtmlReport(String reportText) {
        StringBuilder htmlString = new StringBuilder();

        htmlString.append("<html>");
        htmlString.append("<head>");
        htmlString.append("</head>");
        htmlString.append("<body>");
        htmlString.append("<pre>");

        // Add text to html
        htmlString.append(reportText);

        // Adding closing tags
        htmlString.append("</pre>");
        htmlString.append("</body>");
        htmlString.append("</html>");

        return htmlString.toString();
    }
}
