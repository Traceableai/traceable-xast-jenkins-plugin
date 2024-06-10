package io.jenkins.plugins.traceable.ast;

import hudson.model.Run;
import jenkins.model.RunAction2;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TraceableApiInspectorReportAction implements RunAction2 {

    private transient Run run;

    String reportSummary;

    public String getReportSummary() {
        return reportSummary;
    }

    public TraceableApiInspectorReportAction(String report) {
        this.reportSummary = report;
    }

    @Override
    public void onAttached(Run<?, ?> r) {
        this.run = r;
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
        return "Traceable API Inspector Report";
    }

    @Override
    public String getUrlName() {
        return "traceable_api_inspector_report";
    }

    public Run getRun() {
        return run;
    }
}
