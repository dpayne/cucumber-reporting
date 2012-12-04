package net.masterthought.cucumber;

import com.google.gson.Gson;
import net.masterthought.cucumber.json.Feature;
import net.masterthought.cucumber.util.Util;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportParser {
    private PrintStream log = System.out;

    private List<Project> projects = new ArrayList<Project>();
    private static final String FAILED_PROJECT = "FAILED";

    public ReportParser(PrintStream log) {
        this.log = log;
    }

    public ReportParser() {
    }

    public ReportParser(List<String> jsonReportFiles) throws IOException {
        parseJsonResults(jsonReportFiles);
    }

    private Map<String, List<Feature>> parseJsonResults(List<String> jsonReportFiles) throws IOException {
        Map<String, List<Feature>> featureResults = new HashMap<String, List<Feature>>();
        Project project;
        Feature[] features;
        for (String jsonFile : jsonReportFiles) {
            String fileContent = Util.readFileAsString(jsonFile);
            if (Util.isValidCucumberJsonReport(fileContent)) {
                try {
                    features = new Gson().fromJson(Util.U2U(fileContent), Feature[].class);
                    project = new Project(jsonFile, features);
                    projects.add(project);
                } catch (Exception e) {
                    log.println("Exception in parsing JSON file " + jsonFile);
                    e.printStackTrace(log);
                }
            } else if (fileContent.trim().equalsIgnoreCase(FAILED_PROJECT)) {
                //detected a failed project
                project = new Project(jsonFile, null);
                projects.add(project);
            }
        }
        return featureResults;
    }

    public PrintStream getLog() {
        return log;
    }

    public void setLog(PrintStream log) {
        this.log = log;
    }

    public List<Project> getProjects() {
        return projects;
    }
}
