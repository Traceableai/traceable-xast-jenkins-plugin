package io.jenkins.plugins.traceable.ast;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import hudson.model.Result;
import hudson.model.Run;
import hudson.util.Secret;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jenkins.model.RunAction2;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TraceableASTGenerateReportAction implements RunAction2 {

    private String htmlReport;
    private transient Run run;
    private String traceableCliBinaryLocation;
    private String scanId;
    private Secret clientToken;
    private String traceableRootCaFileName;
    private String traceableCliCertFileName;
    private String traceableCliKeyFileName;

    public TraceableASTGenerateReportAction(
            String traceableCliBinaryLocation,
            String scanId,
            Secret clientToken,
            String traceableRootCaFileName,
            String traceableCliCertFileName,
            String traceableCliKeyFileName) {
        this.traceableCliBinaryLocation = traceableCliBinaryLocation;
        this.scanId = scanId;
        this.clientToken = clientToken;
        this.traceableRootCaFileName = traceableRootCaFileName;
        this.traceableCliCertFileName = traceableCliCertFileName;
        this.traceableCliKeyFileName = traceableCliKeyFileName;
    }

    @Override
    public void onAttached(Run<?, ?> r) {
        this.run = r;
        String scriptPath = "shell_scripts/show_ast_scan.sh";
        String[] args = null;
        args = new String[] {
            traceableCliBinaryLocation,
            scanId,
            clientToken.getPlainText(),
            traceableRootCaFileName,
            traceableCliCertFileName,
            traceableCliKeyFileName
        };
        runScript(scriptPath, args);
    }

    private void runScript(String scriptPath, String[] args) {
        try {
            // Read the bundled script as string
            String bundledScript = CharStreams.toString(
                    new InputStreamReader(getClass().getResourceAsStream(scriptPath), Charsets.UTF_8));
            // Create a temp file with uuid appended to the name just to be safe
            File tempFile = File.createTempFile("script_" + UUID.randomUUID().toString(), ".sh");
            // Write the string to temp file
            BufferedWriter x = Files.newWriter(tempFile, Charsets.UTF_8);
            x.write(bundledScript);
            x.close();
            String execScript = new StringBuffer()
                    .append("/bin/bash ")
                    .append(tempFile.getAbsolutePath())
                    .toString();
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null && !args[i].equals("")) execScript += " " + args[i];
                else execScript += " ''";
            }
            ProcessBuilder processBuilder = new ProcessBuilder(execScript);
            processBuilder.redirectErrorStream(true);
            Process pb = processBuilder.start();
            logOutput(pb.getInputStream());
            pb.waitFor();
            int reportCmdExitValue = pb.exitValue();
            boolean deleted_temp = tempFile.delete();
            if (reportCmdExitValue == 4) {
                run.setResult(Result.FAILURE);
            }
            if (!deleted_temp) {
                throw new FileNotFoundException("Temp file not found");
            }
        } catch (Exception e) {
            log.error("Exception in running {} script : {}", scriptPath, e);
            e.printStackTrace();
        }
    }

    @Override
    public void onLoad(Run<?, ?> r) {
        this.run = r;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/traceable/img/Traceable_white_bg_logo.png";
    }

    @Override
    public String getDisplayName() {
        return "Traceable AST Report";
    }

    @Override
    public String getUrlName() {
        return "traceable_ast_report";
    }

    public Run getRun() {
        return run;
    }

    public String getHtmlReport() {
        return htmlReport;
    }

    private void logOutput(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        StringBuilder report = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            report.append(line).append("\n");
            System.out.println(line);
        }
        scanner.close();
        Pattern REPORT_PATTERN = Pattern.compile("<.*", Pattern.DOTALL);
        Matcher m = REPORT_PATTERN.matcher(report.toString());
        if (m.find()) {
            String markdownReport = m.group();
            htmlReport = createHtmlReport(markdownReport);
        }
    }

    private String createHtmlReport(String markdown) {
        try {
            File mdTempFile = new File("md_temp.md");
            writeToFile(mdTempFile, markdown);

            // Prepare a file path for output
            String home = System.getProperty("user.home");
            java.nio.file.Files.createDirectories(Paths.get(home, "/.traceable_jenkins"));

            // Creating an instance of file
            Path htmlFilePath = Paths.get(home, "/.traceable_jenkins/", run.getId(), "_report.html");

            // Convert Markdown to HTML

            com.aspose.html.converters.Converter.convertMarkdown("md_temp.md", htmlFilePath.toString());
            String report = readFile(htmlFilePath);
            boolean deleted_temp = mdTempFile.delete();
            if (!deleted_temp) {
                throw new FileNotFoundException("Temp file not found");
            }
            return report;
        } catch (Exception e) {
            log.error("Not able to generate report ", e);
            e.printStackTrace();
        }
        return "";
    }

    private String readFile(Path filePath) {
        try {
            InputStream is = java.nio.file.Files.newInputStream(filePath);
            InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void writeToFile(File file, String data) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file, true);
            fw.append(data);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
