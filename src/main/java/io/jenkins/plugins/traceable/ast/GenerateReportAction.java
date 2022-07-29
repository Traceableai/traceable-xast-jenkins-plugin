package io.jenkins.plugins.traceable.ast;

import hudson.model.Run;
import jenkins.model.RunAction2;

public class GenerateReportAction implements RunAction2 {

    private transient Run run;

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
        return "/plugin/Traceable-AST/img/Traceable_white_bg_logo.png";
    }

    @Override
    public String getDisplayName() {
        return "Traceable AST Report";
    }

    @Override
    public String getUrlName() {
        return "Traceable_AST_Report";
    }

    public Run getRun() {
        return run;
    }
}
