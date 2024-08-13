package io.jenkins.plugins.traceable.ast;

import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.Secret;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import io.jenkins.plugins.traceable.ast.scan.utils.GenerateReport;
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
    private final FilePath workspace;
    private final TaskListener listener;

    public TraceableASTGenerateReportAction(
            String traceableCliBinaryLocation,
            String scanId,
            Secret clientToken,
            String traceableRootCaFileName,
            String traceableCliCertFileName,
            String traceableCliKeyFileName,
            FilePath workspace,
            TaskListener listener) {
        this.traceableCliBinaryLocation = traceableCliBinaryLocation;
        this.scanId = scanId;
        this.clientToken = clientToken;
        this.traceableRootCaFileName = traceableRootCaFileName;
        this.traceableCliCertFileName = traceableCliCertFileName;
        this.traceableCliKeyFileName = traceableCliKeyFileName;
        this.workspace = workspace;
        this.listener = listener;
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
            String[] status = this.workspace.act(new GenerateReport(scriptPath, args, run.getId()));

            if (status.length > 0 && status[0] != null && status[0].equals("FAILURE")) {
                run.setResult(Result.FAILURE);
            }

            if (status.length > 1 && status[1] != null) {
                this.htmlReport = status[1];
            } else {
                listener.getLogger().println("No Output for the scan");
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
}
