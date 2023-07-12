package io.jenkins.plugins.traceable.ast;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.RunAction2;
import lombok.extern.slf4j.Slf4j;

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

    public Run getRun() { return run; }


    }
