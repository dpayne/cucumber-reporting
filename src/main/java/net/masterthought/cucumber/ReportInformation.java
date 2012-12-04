package net.masterthought.cucumber;

import net.masterthought.cucumber.json.Artifact;
import net.masterthought.cucumber.json.Element;
import net.masterthought.cucumber.json.Feature;
import net.masterthought.cucumber.json.Step;
import net.masterthought.cucumber.util.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportInformation {
    private List<Project> projects;
    private int numberOfScenarios = 0;
    private int numberOfSteps = 0;
    private int numberOfFeatures = 0;
    private int totalNumberOfPassingScenarios = 0;
    private int totalNumberOfFailingScenarios = 0;
    private int totalNumberOfPassingSteps = 0;
    private int totalNumberOfFailingSteps = 0;
    private int totalNumberOfSkippedSteps = 0;
    private int totalNumberOfPendingSteps = 0;
    private int totalNumberOfMissingSteps = 0;
    private Long totalDuration = 0l;

    public ReportInformation(List<Project> projects) {
        this.projects = projects;
        processProjects();
    }

    public List<Project> getProjects() {
        return projects;
    }

    public int getTotalNumberOfScenarios() {
        return numberOfScenarios;
    }

    public int getTotalNumberOfFeatures() {
        return numberOfFeatures;
    }

    public int getTotalNumberOfSteps() {
        return numberOfSteps;
    }

    public int getTotalNumberOfPassingScenarios() {
        return totalNumberOfPassingScenarios;
    }

    public int getTotalNumberOfFailingScenarios() {
        return totalNumberOfFailingScenarios;
    }

    public int getTotalNumberOfPassingSteps() {
        return totalNumberOfPassingSteps;
    }

    public int getTotalNumberOfFailingSteps() {
        return totalNumberOfFailingSteps;
    }

    public int getTotalNumberOfSkippedSteps() {
        return totalNumberOfSkippedSteps;
    }

    public int getTotalNumberOfPendingSteps() {
        return totalNumberOfPendingSteps;
    }

    public int getTotalNumberOfMissingSteps() {
        return totalNumberOfMissingSteps;
    }

    public String getTotalDurationAsString() {
        return Util.formatDuration(totalDuration);
    }

    public Long getTotalDuration() {
        return totalDuration;
    }

    public String timeStamp() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    public String getReportStatusColour(Feature feature) {
        return feature.getStatus() == Util.Status.PASSED ? "#C5D88A" : "#D88A8A";
    }

    public String getTagReportStatusColour(TagObject tag) {
        return tag.getStatus() == Util.Status.PASSED ? "#C5D88A" : "#D88A8A";
    }

    private void processProjects() {
        for (Project project : projects) {
            numberOfFeatures += project.getNumberOfFeatures();
            numberOfScenarios += project.getNumberOfScenarios();
            numberOfSteps += project.getNumberOfSteps();
            totalDuration += project.getDurationOfSteps();
            totalNumberOfFailingScenarios += project.getNumberOfScenariosFailed();
            totalNumberOfPassingScenarios += project.getNumberOfScenariosPassed();
            totalNumberOfPassingSteps += project.getNumberOfStepsPassed();
            totalNumberOfFailingSteps += project.getNumberOfStepsFailed();
            totalNumberOfSkippedSteps += project.getNumberOfStepsSkipped();
            totalNumberOfMissingSteps += project.getNumberOfStepsMissed();
            totalNumberOfPendingSteps += project.getNumberOfStepsPending();
        }
    }

}
