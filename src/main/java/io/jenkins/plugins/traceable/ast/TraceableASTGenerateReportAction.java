package io.jenkins.plugins.traceable.ast;

import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import hudson.util.Secret;
import io.jenkins.plugins.traceable.ast.scan.utils.GenerateReport;
import java.io.*;
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

    public TraceableASTGenerateReportAction(
            String traceableCliBinaryLocation,
            String scanId,
            Secret clientToken,
            String traceableRootCaFileName,
            String traceableCliCertFileName,
            String traceableCliKeyFileName,
            FilePath workspace) {
        this.traceableCliBinaryLocation = traceableCliBinaryLocation;
        this.scanId = scanId;
        this.clientToken = clientToken;
        this.traceableRootCaFileName = traceableRootCaFileName;
        this.traceableCliCertFileName = traceableCliCertFileName;
        this.traceableCliKeyFileName = traceableCliKeyFileName;
        this.workspace = workspace;
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

            if (status[0].equals("FAILURE")) {
                run.setResult(Result.FAILURE);
            }

            this.htmlReport = status[1];
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
