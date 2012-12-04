package net.masterthought.cucumber;

import net.masterthought.cucumber.json.Element;
import net.masterthought.cucumber.json.Feature;
import net.masterthought.cucumber.json.Step;
import net.masterthought.cucumber.json.Tag;
import net.masterthought.cucumber.util.Util;
import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Date: 7/7/12
 * Time: 4:18 PM
 */
public class Project {

    private String name;
    private int numberOfFeatures = 0;
    private int numberOfScenarios = 0;
    private int numberOfStepsFailed = 0;
    private int numberOfStepsPassed = 0;
    private int numberOfStepsSkipped = 0;
    private int numberOfScenariosFailed = 0;
    private int numberOfScenariosPassed = 0;
    private Feature[] features;
    private List<TagObject> tags;
    private int numberOfSteps;
    private long durationOfSteps = 0L;
    private String fileName;
    private int numberOfStepsMissed = 0;
    private int numberOfStepsPending = 0;
    private int totalNumberOfTagScenarios = 0;
    private int totalNumberOfTagSteps = 0;
    private int totalNumberOfTagStepsPasses = 0;
    private int totalNumberOfTagStepsFails = 0;
    private int totalNumberOfTagScenariosPasses = 0;
    private int totalNumberOfTagScenariosFails = 0;
    private int totalNumberOfTagStepsSkipped = 0;
    private int totalNumberOfTagStepsPending = 0;
    private Long totalTagDuration = 0L;
    private boolean failedBuild = false;

    public Project(String jsonFile, Feature[] features) {
        if (features == null) {
            failedBuild = true;
            features = new Feature[0];
        }
        fileName = jsonFile;
        this.features = features;
            numberOfFeatures = features.length;
        processFeatures();
        name = getProjectName();
    }

    public String getProjectFeatureUri() {
        return name + "-feature-overview.html";
    }

    public String getProjectTagUri() {
        return name + "-tag-overview.html";
    }

    public String getProjectName() {
        //remove directory path
        return FilenameUtils.getBaseName(this.fileName);
    }

    public String getFormattedDuration() {
        return Util.formatDuration(getDurationOfSteps());
    }

    private List<Util.Status> processFeatures() {
        List<Util.Status> steps = new ArrayList<Util.Status>();
        Map<Tag, List<ScenarioTag>> tagMap = new HashMap<Tag, List<ScenarioTag>>();
        boolean markNextScenarioAsFailed = false;

        for (Feature feature : features) {
            feature.setParentProject(this);
            if (Util.hasScenarios(feature)) {
                for (Element scenario : feature.getElements()) {
                    if (Util.hasSteps(scenario) && !scenario.isOutline()) {
                        if (!scenario.isBackground()) {
                            feature.getAllScenarios().add(scenario);
                            if (markNextScenarioAsFailed) {
                                markNextScenarioAsFailed = false;
                                feature.getFailedScenarios().add(scenario);
                                numberOfScenariosFailed++;
                            } else {
                                switch (scenario.getStatus()) {
                                    case PASSED:
                                        feature.getPassedScenarios().add(scenario);
                                        numberOfScenariosPassed++;
                                        break;
                                    case FAILED:
                                        feature.getFailedScenarios().add(scenario);
                                        numberOfScenariosFailed++;
                                        break;
                                    default:
                                        break;
                                }
                            }
                            numberOfScenarios++;
                        } else {
                            if (scenario.getStatus() == Util.Status.FAILED) {
                                markNextScenarioAsFailed = true;
                            }
                        }
                        for (Step step : scenario.getSteps()) {
                            feature.getAllSteps().add(step);
                            steps.add(step.getStatus());
                            if (step.getStatus() != null) {
                                numberOfSteps++;
                                switch (step.getStatus()) {
                                    case PASSED:
                                        feature.getPassedSteps().add(step);
                                        numberOfStepsPassed++;
                                        break;
                                    case FAILED:
                                        feature.getFailedSteps().add(step);
                                        numberOfStepsFailed++;
                                        break;
                                    case SKIPPED:
                                        feature.getSkippedSteps().add(step);
                                        numberOfStepsSkipped++;
                                        break;
                                    case UNDEFINED:
                                        feature.getPendingSteps().add(step);
                                        numberOfStepsPending++;
                                        break;
                                    case MISSING:
                                        feature.getMissingSteps().add(step);
                                        numberOfStepsMissed++;
                                        break;
                                }
                            }
                            feature.setDurationOfSteps(feature.getDurationOfSteps() + step.getDuration());
                            durationOfSteps += step.getDuration();
                        }
                        List<Tag> tempTags = combineTagArrays(feature.getRawTags(), scenario.getRawTags());
                        if (tempTags.size() > 0) {
                            ScenarioTag scenarioTag = new ScenarioTag(scenario, feature.getFileName());
                            for (Tag tag : tempTags) {
                                if (tagMap.containsKey(tag)) {
                                    tagMap.get(tag).add(scenarioTag);
                                } else {
                                    List<ScenarioTag> values = new ArrayList<ScenarioTag>();
                                    values.add(scenarioTag);
                                    tagMap.put(tag, values);
                                }
                            }
                        }
                    }
                }
            }
        }
        tags = convertTagMapToTagObjectList(tagMap);
        processTags(tags);
        return steps;
    }

    private void processTags(List<TagObject> tags) {
        for (TagObject tag : tags) {
            //todo: merge this step with what's in convertTagMapToTagObjectList to cut out some loops
            totalNumberOfTagScenarios += tag.getNumberOfScenarios();
            totalNumberOfTagSteps += tag.getNumberOfSteps();
            totalNumberOfTagStepsPasses += tag.getNumberOfStepsPassed();
            totalNumberOfTagStepsFails += tag.getNumberOfStepsFailed();
            totalNumberOfTagStepsSkipped += tag.getNumberOfStepsSkipped();
            totalNumberOfTagStepsPending += tag.getNumberOfStepsPending();
            totalNumberOfTagScenariosPasses += tag.getNumberOfScenariosPassed();
            totalNumberOfTagScenariosFails += tag.getNumberOfScenariosFailed();
            totalTagDuration += tag.getDurationOfSteps();
        }
    }

    private List<Tag> combineTagArrays(Tag[] rawTags, Tag[] rawTags1) {
        if (rawTags == null) {
            rawTags = new Tag[0];
        }
        if (rawTags1 == null) {
            rawTags1 = new Tag[0];
        }
        HashSet<Tag> tagSet = new HashSet<Tag>();
        Collections.addAll(tagSet, rawTags);
        Collections.addAll(tagSet, rawTags1);
        List<Tag> retval = new ArrayList<Tag>();
        retval.addAll(tagSet);
        return retval;
    }

    private List<TagObject> convertTagMapToTagObjectList(Map<Tag, List<ScenarioTag>> tagMap) {
        List<TagObject> tagObjects = new ArrayList<TagObject>(tagMap.keySet().size());
        TagObject tagObject;
        for (Tag tag : tagMap.keySet()) {
            tagObject = new TagObject(tag.getName(), tagMap.get(tag));
            tagObjects.add(tagObject);
        }
        return tagObjects;
    }

    public Util.Status getStatus() {
        if (failedBuild) {
            return Util.Status.FAILED_BUILD;
        }
        if (numberOfScenariosFailed == 0) {
            return Util.Status.PASSED;
        }
        return Util.Status.FAILED;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfFeatures() {
        return numberOfFeatures;
    }

    public int getNumberOfScenarios() {
        return numberOfScenarios;
    }

    public int getNumberOfStepsFailed() {
        return numberOfStepsFailed;
    }

    public int getNumberOfStepsPassed() {
        return numberOfStepsPassed;
    }

    public int getNumberOfStepsSkipped() {
        return numberOfStepsSkipped;
    }

    public int getNumberOfScenariosFailed() {
        return numberOfScenariosFailed;
    }

    public int getNumberOfScenariosPassed() {
        return numberOfScenariosPassed;
    }

    public Feature[] getFeatures() {
        return features;
    }

    public List<TagObject> getTags() {
        return tags;
    }

    public int getNumberOfSteps() {
        return numberOfSteps;
    }

    public long getDurationOfSteps() {
        return durationOfSteps;
    }

    public String getFileName() {
        return fileName;
    }

    public int getNumberOfStepsMissed() {
        return numberOfStepsMissed;
    }

    public int getNumberOfStepsPending() {
        return numberOfStepsPending;
    }

    public int getTotalNumberOfTagScenarios() {
        return totalNumberOfTagScenarios;
    }

    public int getTotalNumberOfTagSteps() {
        return totalNumberOfTagSteps;
    }

    public Long getTotalTagDuration() {
        return totalTagDuration;
    }

    public int getTotalNumberOfTagStepsPasses() {
        return totalNumberOfTagStepsPasses;
    }

    public int getTotalNumberOfTagStepsFails() {
        return totalNumberOfTagStepsFails;
    }

    public int getTotalNumberOfTagScenariosPasses() {
        return totalNumberOfTagScenariosPasses;
    }

    public int getTotalNumberOfTagScenariosFails() {
        return totalNumberOfTagScenariosFails;
    }

    public int getTotalNumberOfTagStepsSkipped() {
        return totalNumberOfTagStepsSkipped;
    }

    public int getTotalNumberOfTagStepsPending() {
        return totalNumberOfTagStepsPending;
    }

    public String getTotalTagDurationAsString() {
        return Util.formatDuration(durationOfSteps);
    }
}
