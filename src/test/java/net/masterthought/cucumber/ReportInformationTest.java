package net.masterthought.cucumber;

import net.masterthought.cucumber.json.Artifact;
import net.masterthought.cucumber.json.Feature;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.*;
import static org.junit.internal.matchers.StringContains.containsString;

public class ReportInformationTest {

    ReportInformation reportInformation;
    ReportParser reportParser;

    @Before
    public void setUpReportInformation() throws IOException, URISyntaxException {
        ConfigurationOptions.setSkippedFailsBuild(false);
        ConfigurationOptions.setUndefinedFailsBuild(false);
        List<String> jsonReports = new ArrayList<String>();
        //will work iff the resources are not jarred up, otherwise use IOUtils to copy to a temp file.
        jsonReports.add(new File(ReportInformationTest.class.getClassLoader().getResource("net/masterthought/cucumber/project1.json").toURI()).getAbsolutePath());
        jsonReports.add(new File(ReportInformationTest.class.getClassLoader().getResource("net/masterthought/cucumber/project2.json").toURI()).getAbsolutePath());
        reportParser = new ReportParser(jsonReports);
        reportInformation = new ReportInformation(reportParser.getProjects());
    }

//    @Test
//    public void shouldDisplayArtifacts() throws Exception {
//        ConfigurationOptions.setArtifactsEnabled(true);
//        String configuration = "Account has sufficient funds again~the account balance is 300~balance~account_balance.txt~xml";
//        ArtifactProcessor artifactProcessor = new ArtifactProcessor(configuration);
//        Map<String, Artifact> map = artifactProcessor.process();
//        ConfigurationOptions.setArtifactConfiguration(map);
//        reportInformation = new ReportInformation(reportParser.getProjects());
//        assertThat(reportInformation.getProjects().get(0).getFeatures()[2].getElements()[7].getSteps()[0].getName(), is("<div class=\"passed\"><span class=\"step-keyword\">Given  </span><span class=\"step-name\">the account <div style=\"display:none;\"><textarea id=\"Account_has_sufficient_funds_againthe_account_balance_is_300\" class=\"brush: xml;\"></textarea></div><a onclick=\"applyArtifact('Account_has_sufficient_funds_againthe_account_balance_is_300','account_balance.txt')\" href=\"#\">balance</a> is 300</span></div>"));
//    }

    @Test
    public void shouldListAllFeatures() throws IOException {
        assertThat(reportInformation.getProjects().get(0).getFeatures()[0], is(Feature.class));
    }

    @Test
    public void shouldListAllTags() {
        assertThat(reportInformation.getProjects().get(0).getTags().get(0), is(TagObject.class));
    }

//    @Test
//    public void shouldListFeaturesInAMap() {
//	//not really needed now -- have type safety with generics in object usage and would have failed had we not found the resource.
//        assertThat(reportInformation.getProjects().get(0)., hasItem(containsString("project1.json")));
//        assertThat(reportInformation.getProjectFeatureMap().entrySet().iterator().next().getValue().get(0), is(Feature.class));
//    }

    @Test
    public void shouldReturnTotalNumberOfScenarios() {
        assertThat(reportInformation.getTotalNumberOfScenarios(), is(10));
    }

    @Test
    public void shouldReturnTotalNumberOfFeatures() {
        assertThat(reportInformation.getTotalNumberOfFeatures(), is(4));
    }

    @Test
    public void shouldReturnTotalNumberOfSteps() {
        assertThat(reportInformation.getTotalNumberOfSteps(), is(98));
    }

    @Test
    public void shouldReturnTotalNumberPassingSteps() {
        assertThat(reportInformation.getTotalNumberOfPassingSteps(), is(90));
    }

    @Test
    public void shouldReturnTotalNumberFailingSteps() {
        assertThat(reportInformation.getTotalNumberOfFailingSteps(), is(2));
    }

    @Test
    public void shouldReturnTotalNumberSkippedSteps() {
        assertThat(reportInformation.getTotalNumberOfSkippedSteps(), is(6));
    }

    @Test
    public void shouldReturnTotalNumberPendingSteps() {
        assertThat(reportInformation.getTotalNumberOfPendingSteps(), is(0));
    }

    @Test
    public void shouldReturnTotalNumberMissingSteps() {
        assertThat(reportInformation.getTotalNumberOfMissingSteps(), is(0));
    }

    @Test
    public void shouldReturnTotalDuration() {
        assertThat(reportInformation.getTotalDuration(), is(236050000L));
    }

    @Test
    public void shouldReturnTotalDurationAsString() {
        assertThat(reportInformation.getTotalDurationAsString(), is("236 ms"));
    }

    @Test
    public void shouldReturnTimeStamp() {
        assertThat(reportInformation.timeStamp(), is(String.class));
    }

    @Test
    public void shouldReturnReportStatusColour() {
        assertThat(reportInformation.getReportStatusColour(reportInformation.getProjects().get(0).getFeatures()[0]), is("#C5D88A"));
    }

    @Test
    public void shouldReturnTagReportStatusColour() {
        assertThat(reportInformation.getTagReportStatusColour(reportInformation.getProjects().get(0).getTags().get(0)), is("#C5D88A"));
    }

    @Test
    public void shouldReturnTotalTags() {
        assertThat(reportInformation.getProjects().get(0).getTags().size(), is(3));
    }

    @Test
    public void shouldReturnTotalTagScenarios() {
        assertThat(reportInformation.getProjects().get(0).getTotalNumberOfTagScenarios(), is(10));
        assertThat(reportInformation.getProjects().get(1).getTotalNumberOfTagScenarios(), is(10));
    }

    @Test
    public void shouldReturnTotalTagSteps() {
        assertThat(reportInformation.getProjects().get(0).getTotalNumberOfTagSteps(), is(82));
        assertThat(reportInformation.getProjects().get(1).getTotalNumberOfTagSteps(), is(82));
    }

    @Test
    public void shouldReturnTotalTagPasses() {
        assertThat(reportInformation.getProjects().get(0).getTotalNumberOfTagScenariosPasses(), is(10));
        assertThat(reportInformation.getProjects().get(1).getTotalNumberOfTagScenariosPasses(), is(10));
    }

    @Test
    public void shouldReturnTotalTagFails() {
        assertThat(reportInformation.getProjects().get(0).getTotalNumberOfTagScenariosFails(), is(0));
        assertThat(reportInformation.getProjects().get(1).getTotalNumberOfTagScenariosFails(), is(0));
    }

//    @Test
//    public void shouldReturnTotalTagSkipped() {
//        assertThat(reportInformation.getProjects().get(0).getTotalNumberOfTagSkipped(), is(0));
//    }
//
//    @Test
//    public void shouldReturnTotalTagPending() {
//        assertThat(reportInformation.getProjects().get(0).getTotalNumberOfTagPending(), is(0));
//    }

    @Test
    public void shouldReturnTotalTagDuration() {
        assertThat(reportInformation.getProjects().get(0).getTotalTagDuration(), is(122431000L));
        assertThat(reportInformation.getProjects().get(1).getTotalTagDuration(), is(123306000L));
    }

    @Test
    public void shouldReturnTotalScenariosPassed() {
        assertThat(reportInformation.getProjects().get(0).getNumberOfScenariosFailed(), is(1));
        assertThat(reportInformation.getProjects().get(1).getNumberOfScenariosFailed(), is(1));
    }

    @Test
    public void shouldReturnTotalScenariosFailed() {
        assertThat(reportInformation.getProjects().get(0).getNumberOfScenariosFailed(), is(1));
        assertThat(reportInformation.getProjects().get(1).getNumberOfScenariosFailed(), is(1));
    }


}
