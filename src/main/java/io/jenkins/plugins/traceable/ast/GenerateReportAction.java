package io.jenkins.plugins.traceable.ast;

import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.RunAction2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
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
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "src/main/resources/io/jenkins/plugins/traceable/ast/TraceableASTPluginBuilder/shell_scripts/show_ast_scan.sh",
                    traceableCliBinaryLocation,
                    scanId
            );
            Process showAstScan = pb.start();
            logOutput(showAstScan.getInputStream(), "");
            logOutput(showAstScan.getErrorStream(), "Error: ");
            showAstScan.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void logOutput(InputStream inputStream, String prefix) {
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
