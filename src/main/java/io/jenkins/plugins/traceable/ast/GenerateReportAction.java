package io.jenkins.plugins.traceable.ast;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.RunAction2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateReportAction implements RunAction2 {

    private String scanId;
    private String traceableCliBinaryLocation;
    private String report;
    private transient Run run;

    public GenerateReportAction (String scanId, String traceableCliBinaryLocation) {
        this.scanId = scanId;
        this.traceableCliBinaryLocation = traceableCliBinaryLocation;
    }

    @Override
    public void onAttached(Run<?, ?> r) {
        this.run = r;
        String scriptPath = "shell_scripts/show_ast_scan.sh";
        String[] args = new String[]{
                traceableCliBinaryLocation,
                scanId
        };
        runScript(scriptPath, args);
    }

    private void logOutput(InputStream inputStream) {
        new Thread(() -> {
            Scanner scanner = new Scanner(inputStream, "UTF-8");
            while (scanner.hasNextLine()) {
                synchronized (this) {
                    String line = scanner.nextLine();
                    report += line + "\n";
                }
            }
            scanner.close();
            Pattern REPORT_PATTERN = Pattern.compile("Name.*",Pattern.DOTALL);
            Matcher m = REPORT_PATTERN.matcher(report);
            if(m.find()) {
                report = m.group();
            }

        }).start();

    }
    private void runScript(String scriptPath, String[] args) {
        try{
            // Read the bundled script as string
            String bundledScript = CharStreams.toString(
                    new InputStreamReader(getClass().getResourceAsStream(scriptPath), Charsets.UTF_8));
            // Create a temp file with uuid appended to the name just to be safe
            File tempFile = File.createTempFile("script_" + UUID.randomUUID().toString(), ".sh");
            // Write the string to temp file
            BufferedWriter x = Files.newWriter(tempFile,  Charsets.UTF_8);
            x.write(bundledScript);
            x.close();
            String execScript = "/bin/sh " + tempFile.getAbsolutePath();
            for(int i=0;i<args.length;i++) {
                execScript += " " + args[i];
            }
            Process pb = Runtime.getRuntime().exec(execScript);
            logOutput(pb.getInputStream());
            logOutput(pb.getErrorStream());
            pb.waitFor();
            tempFile.delete();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLoad(Run<?, ?> r) {
        this.run = r;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/traceable-ast/img/Traceable_white_bg_logo.png";
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

    public String getReport() { return report; }
}
