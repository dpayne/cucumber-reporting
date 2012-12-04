package net.masterthought.cucumber.json;

import net.masterthought.cucumber.Project;
import net.masterthought.cucumber.util.Util;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Feature {

    Project parentProject;
    private String name;
    private String uri;
    private String description;
    private String keyword;
    private Element[] elements;
    private Tag[] tags;
    private List<Step> allSteps = new ArrayList<Step>();
    private List<Element> allScenarios = new ArrayList<Element>();
    private List<Step> passedSteps = new ArrayList<Step>();
    private List<Step> failedSteps = new ArrayList<Step>();
    private List<Step> pendingSteps = new ArrayList<Step>();
    private List<Step> skippedSteps = new ArrayList<Step>();
    private List<Step> missingSteps = new ArrayList<Step>();
    private List<Element> passedScenarios = new ArrayList<Element>();
    private List<Element> failedScenarios = new ArrayList<Element>();
    private long durationOfSteps = 0L;

    public Feature() {

    }

    public Element[] getElements() {
        return elements;
    }

    public String getFileName() {
        List<String> matches = new ArrayList<String>();
        String [] lines = uri.split("/|\\\\");
        for (String line : lines) {
            String modified = line.replaceAll("\\)|\\(", "");
            modified = StringUtils.deleteWhitespace(modified).trim();
            matches.add(modified);
        }

        List<String> sublist = matches.subList(1, matches.size());

        matches = (sublist.size() == 0) ? matches : sublist;
        String fileName = StringUtils.join(matches.iterator(), "-");
        return parentProject.getName() + "-" + fileName + ".html";
    }

    public boolean hasTags() {
        return Util.itemExists(tags);
    }

    public List<String> getTagList() {
        List<String> tagList = new ArrayList<String>();
        for (Tag tag : tags) {
            tagList.add(tag.getName());
        }
        return tagList;
    }

    public String getTags() {
        String result = "<div class=\"feature-tags\"></div>";
        if (Util.itemExists(tags)) {
            String tagList = StringUtils.join(getTagList().toArray(), ",");
            result = "<div class=\"feature-tags\">" + tagList + "</div>";
        }
        return result;
    }

    public Util.Status getStatus() {
        Closure<String, Element> scenarioStatus = new Closure<String, Element>() {
            public Util.Status call(Element step) {
                return step.getStatus();
            }
        };
        List<Util.Status> results = new ArrayList<Util.Status>();
        if (Util.itemExists(elements)) {
            results = Util.collectScenarios(elements, scenarioStatus);
        }
        return results.contains(Util.Status.FAILED) ? Util.Status.FAILED : Util.Status.PASSED;
    }

    public String getName() {
        return Util.itemExists(name) ? Util.result(getStatus()) + "<div class=\"feature-line\"><span class=\"feature-keyword\">Feature:</span> " + name + "</div>" + Util.closeDiv() : "";
    }

    public String getRawName() {
        return Util.itemExists(name) ? name : "";
    }

    public String getRawStatus() {
        return getStatus().toString().toUpperCase();
    }

    public Tag[] getRawTags() {
        return tags;
    }

    public String getDescription() {
        String result = "";
        if (Util.itemExists(description)) {
            String content = description.replaceFirst("As an", "<span class=\"feature-role\">As an</span>");
            content = content.replaceFirst("I want to", "<span class=\"feature-action\">I want to</span>");
            content = content.replaceFirst("So that", "<span class=\"feature-value\">So that</span>");
            content = content.replaceAll("\n", "<br/>");
            result = "<div class=\"feature-description\">" + content + "</div>";
        }
        return result;
    }

    public int getNumberOfScenarios() {
        int result = 0;
        if (Util.itemExists(elements)) {
            result = elements.length;
        }
        return result;
    }

    public List<Step> getAllSteps() {
        return allSteps;
    }

    public void setAllSteps(List<Step> allSteps) {
        this.allSteps = allSteps;
    }

    public List<Step> getPassedSteps() {
        return passedSteps;
    }

    public void setPassedSteps(List<Step> passedSteps) {
        this.passedSteps = passedSteps;
    }

    public List<Step> getFailedSteps() {
        return failedSteps;
    }

    public void setFailedSteps(List<Step> failedSteps) {
        this.failedSteps = failedSteps;
    }

    public List<Step> getPendingSteps() {
        return pendingSteps;
    }

    public void setPendingSteps(List<Step> pendingSteps) {
        this.pendingSteps = pendingSteps;
    }

    public List<Step> getSkippedSteps() {
        return skippedSteps;
    }

    public void setSkippedSteps(List<Step> skippedSteps) {
        this.skippedSteps = skippedSteps;
    }

    public List<Element> getPassedScenarios() {
        return passedScenarios;
    }

    public void setPassedScenarios(List<Element> passedScenarios) {
        this.passedScenarios = passedScenarios;
    }

    public List<Element> getFailedScenarios() {
        return failedScenarios;
    }

    public void setFailedScenarios(List<Element> failedScenarios) {
        this.failedScenarios = failedScenarios;
    }

    public List<Element> getAllScenarios() {
        return allScenarios;
    }

    public long getDurationOfSteps() {
        return durationOfSteps;
    }

    public void setDurationOfSteps(long durationOfSteps) {
        this.durationOfSteps = durationOfSteps;
    }

    public String getFormattedDurationOfSteps() {
        return Util.formatDuration(durationOfSteps);
    }

    public List<Step> getMissingSteps() {
        return missingSteps;
    }

    public Project getParentProject() {
        return parentProject;
    }

    public void setParentProject(Project parentProject) {
        this.parentProject = parentProject;
    }
}
