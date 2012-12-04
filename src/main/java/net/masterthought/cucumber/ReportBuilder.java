package net.masterthought.cucumber;

import net.masterthought.cucumber.charts.FlashChartBuilder;
import net.masterthought.cucumber.charts.JsChartUtil;
import net.masterthought.cucumber.charts.TagOverviewChart;
import net.masterthought.cucumber.json.Feature;
import net.masterthought.cucumber.util.UnzipUtils;
import net.masterthought.cucumber.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class ReportBuilder {

    ReportInformation ri;
    private File reportDirectory;
    private String buildNumber;
    private String buildProject;
    private String pluginUrlPath;
    private boolean flashCharts;
    private boolean runWithJenkins;
    private boolean artifactsEnabled;
    private static PrintStream logger;
    private String logFiles;

    public ReportBuilder(List<String> jsonReports, File reportDirectory, String pluginUrlPath, String buildNumber,
                String buildProject, boolean skippedFails, boolean undefinedFails, boolean runWithJenkins, boolean flashCharts,
        PrintStream logger, String logFiles, boolean artifactsEnabled, String artifactConfig) throws Exception {
        ConfigurationOptions.setSkippedFailsBuild(skippedFails);
        ConfigurationOptions.setUndefinedFailsBuild(undefinedFails);
        ConfigurationOptions.setArtifactsEnabled(artifactsEnabled);
        if(artifactsEnabled){
            ArtifactProcessor artifactProcessor = new ArtifactProcessor(artifactConfig);
            ConfigurationOptions.setArtifactConfiguration(artifactProcessor.process());
        }
        this.logFiles = logFiles;

        if (logger == null) {
            ReportBuilder.logger = System.out;
        } else {
            ReportBuilder.logger = logger;
        }
        ReportParser reportParser = new ReportParser(jsonReports);
        this.ri = new ReportInformation(reportParser.getProjects());
        this.reportDirectory = reportDirectory;
        this.buildNumber = buildNumber;
        this.buildProject = buildProject;
        this.pluginUrlPath = getPluginUrlPath(pluginUrlPath);
        this.flashCharts = flashCharts;
        this.runWithJenkins = runWithJenkins;
        this.artifactsEnabled = artifactsEnabled;
    }

    public boolean getBuildStatus() {
        return !(ri.getTotalNumberOfFailingSteps() > 0);
    }

    public void generateReports() throws Exception {
        if (flashCharts) {
            copyResource("charts", "flash_charts.zip");
        } else {
            copyResource("charts", "js.zip");
        }
        if(artifactsEnabled){
            copyResource("charts", "codemirror.zip");
        }
        copyResources(reportDirectory);
        generateProjectOverview();
        for (Project project : ri.getProjects()) {
            generateFeatureOverview(project);
            generateTagOverview(project);
            generateFeatureReports(project);
            generateTagReports(project);
        }
        generateLogFilePage();
    }

    private void generateProjectOverview() throws Exception {
        VelocityEngine ve = new VelocityEngine();
        ve.init(getProperties());
        Template projectOverview = ve.getTemplate("templates/projectOverview.vm");
        VelocityContext context = new VelocityContext();
        context.put("build_project", buildProject);
        context.put("build_number", buildNumber);
        context.put("projects", ri.getProjects());
        context.put("total_projects", ri.getProjects().size());
        context.put("total_features", ri.getTotalNumberOfFeatures());
        context.put("total_scenarios", ri.getTotalNumberOfScenarios());
        context.put("total_steps", ri.getTotalNumberOfSteps());
        context.put("total_scenario_passes", ri.getTotalNumberOfPassingScenarios());
        context.put("total_scenario_fails", ri.getTotalNumberOfFailingScenarios());
        context.put("total_step_passes", ri.getTotalNumberOfPassingSteps());
        context.put("total_step_fails", ri.getTotalNumberOfFailingSteps());
        context.put("total_step_skipped", ri.getTotalNumberOfSkippedSteps());
        context.put("total_step_pending", ri.getTotalNumberOfPendingSteps());
        context.put("time_stamp", ri.timeStamp());
        context.put("total_duration", ri.getTotalDurationAsString());
        context.put("jenkins_base", pluginUrlPath);
        context.put("fromJenkins", runWithJenkins);
        generateReport("project-overview.html", projectOverview, context);

        if (!runWithJenkins) {
            //create index file
            File index = new File(reportDirectory, "index.html");
            if (!index.exists())
                index.createNewFile();

            FileOutputStream out = new FileOutputStream(index);
            String output = "<meta http-equiv=\"Refresh\" content=\"0; url=project-overview.html\">\n";
            out.write(output.getBytes());
        }
    }

    private void generateLogFilePage() throws Exception {

        ArrayList<LogFile> logs = new ArrayList<LogFile>();
        if (logFiles != null && !logFiles.isEmpty()) {
            String[] links = logFiles.split(",");
            for (String link : links) {
                logs.add(new LogFile(link));
            }
        }

        copyLogs(reportDirectory, logs);

        if (runWithJenkins) {
            LogFile jenkinsLogs = new LogFile();
            jenkinsLogs.setName(buildProject + " Build " + buildNumber + " log");
            jenkinsLogs.setLink("../consoleText");
            logs.add(jenkinsLogs);
        }

        VelocityEngine ve = new VelocityEngine();
        ve.init(getProperties());
        Template logFilesTemplate = ve.getTemplate("templates/logFiles.vm");
        VelocityContext context = new VelocityContext();
        context.put("build_project", buildProject);
        context.put("build_number", buildNumber);
        context.put("projects", ri.getProjects());
        context.put("time_stamp", ri.timeStamp());
        context.put("jenkins_base", pluginUrlPath);
        context.put("fromJenkins", runWithJenkins);
        context.put("logFiles", logs);
        generateReport("logFiles.html", logFilesTemplate, context);
    }

    public void generateFeatureReports(Project project) throws Exception {
        for (Feature feature : project.getFeatures()) {
            VelocityEngine ve = new VelocityEngine();
            ve.init(getProperties());
            Template featureResult = ve.getTemplate("templates/featureReport.vm");
            VelocityContext context = new VelocityContext();
            context.put("feature", feature);
            context.put("project", project);
            context.put("report_status_colour", ri.getReportStatusColour(feature));
            context.put("build_project", buildProject);
            context.put("build_number", buildNumber);
            context.put("scenarios", feature.getElements());
            context.put("time_stamp", ri.timeStamp());
            context.put("jenkins_base", pluginUrlPath);
            context.put("fromJenkins", runWithJenkins);
            context.put("artifactsEnabled", ConfigurationOptions.artifactsEnabled());
            generateReport(project.getName() + "-" + feature.getFileName(), featureResult, context);
        }
    }

    private void generateFeatureOverview(Project project) throws Exception {
        VelocityEngine ve = new VelocityEngine();
        ve.init(getProperties());
        Template featureOverview = ve.getTemplate("templates/featureOverview.vm");
        VelocityContext context = new VelocityContext();
        context.put("build_project", buildProject);
        context.put("build_number", buildNumber);
        context.put("project", project);
        context.put("features", project.getFeatures());
        context.put("total_features", project.getNumberOfFeatures());
        context.put("total_scenarios", project.getNumberOfScenarios());
        context.put("total_steps", project.getNumberOfSteps());
        context.put("total_passes", project.getNumberOfStepsPassed());
        context.put("total_fails", project.getNumberOfStepsFailed());
        context.put("total_skipped", project.getNumberOfStepsSkipped());
        context.put("total_pending", project.getNumberOfStepsPending());
        context.put("total_scenario_passes", project.getNumberOfScenariosPassed());
        context.put("total_scenario_fails", project.getNumberOfScenariosFailed());
        context.put("time_stamp", ri.timeStamp());
        context.put("total_duration", project.getFormattedDuration());
        context.put("jenkins_base", pluginUrlPath);
        context.put("fromJenkins", runWithJenkins);
        context.put("flashCharts", flashCharts);

        if (flashCharts) {
            context.put("step_data", FlashChartBuilder.donutChart(project.getNumberOfStepsPassed(), project.getNumberOfStepsFailed(), project.getNumberOfStepsSkipped(), project.getNumberOfStepsSkipped()));
            context.put("scenario_data", FlashChartBuilder.pieChart(project.getNumberOfScenariosPassed(), project.getNumberOfScenariosFailed()));
        } else {
            JsChartUtil pie = new JsChartUtil();
            List<String> stepColours = pie.orderStepsByValue(project.getNumberOfStepsPassed(), project.getNumberOfStepsFailed(), project.getNumberOfStepsSkipped(), project.getNumberOfStepsSkipped());
            context.put("step_data", stepColours);
            context.put("scenarios_passed", project.getNumberOfScenariosPassed());
            context.put("scenarios_failed", project.getNumberOfScenariosFailed());
            List<String> scenarioColours = pie.orderScenariosByValue(project.getNumberOfScenariosPassed(), project.getNumberOfScenariosFailed());
            context.put("scenario_data", scenarioColours);
        }

        generateReport(project.getProjectFeatureUri(), featureOverview, context);
    }


    public void generateTagReports(Project project) throws Exception {
        for (TagObject tagObject : project.getTags()) {
            VelocityEngine ve = new VelocityEngine();
            ve.init(getProperties());
            Template featureResult = ve.getTemplate("templates/tagReport.vm");
            VelocityContext context = new VelocityContext();
            context.put("tag", tagObject);
            context.put("project", project);
            context.put("time_stamp", ri.timeStamp());
            context.put("jenkins_base", pluginUrlPath);
            context.put("build_project", buildProject);
            context.put("build_number", buildNumber);
            context.put("report_status_colour", ri.getTagReportStatusColour(tagObject));
            context.put("fromJenkins", runWithJenkins);
            generateReport(project.getName() + "-" + tagObject.getTagName().replace("@", "").trim() + ".html", featureResult, context);

        }
    }

    public void generateTagOverview(Project project) throws Exception {

        VelocityEngine ve = new VelocityEngine();
        ve.init(getProperties());
        Template featureOverview = ve.getTemplate("templates/tagOverview.vm");
        VelocityContext context = new VelocityContext();
        context.put("build_project", buildProject);
        context.put("build_number", buildNumber);
        context.put("project", project);
        context.put("tags", project.getTags());
        context.put("numberOfTags", project.getTags().size());
        context.put("total_scenarios", project.getTotalNumberOfTagScenarios());
        context.put("total_scenario_passes", project.getNumberOfScenariosPassed());
        context.put("total_scenario_fails", project.getNumberOfScenariosFailed());
        context.put("total_steps", project.getTotalNumberOfTagSteps());
        context.put("total_passes", project.getTotalNumberOfTagStepsPasses());
        context.put("total_fails", project.getTotalNumberOfTagScenariosFails());
        context.put("total_skipped", project.getTotalNumberOfTagStepsSkipped());
        context.put("total_pending", project.getTotalNumberOfTagStepsPending());
        context.put("chart_scenario_data", TagOverviewChart.generateTagOverviewChartScenariosData(project));
        context.put("chart_step_data", TagOverviewChart.generateTagOverviewChartStepsData(project));
        context.put("total_duration", project.getTotalTagDurationAsString());
        context.put("time_stamp", ri.timeStamp());
        context.put("jenkins_base", pluginUrlPath);
        context.put("fromJenkins", runWithJenkins);

        if (flashCharts) {
            context.put("chart_data", FlashChartBuilder.StackedColumnChart(project.getTags()));
        } else {
            context.put("chart_rows", JsChartUtil.generateTagChartData(project.getTags()));
        }
        context.put("flashCharts", flashCharts);

        generateReport(project.getProjectTagUri(), featureOverview, context);
    }

    private static void copyLogs(File reportDirectory, ArrayList<LogFile> logFiles) {
        int logNumber = 1;
        try {
            for (LogFile log : logFiles) {
                logger.println("Copying log files from \"" + log.getLink() + "\"to " + reportDirectory.getAbsolutePath()
                        + "/" + "remote_log_" + logNumber + ".log");
                FileUtils.copyURLToFile(new URL(log.getLink()), new File(reportDirectory, "remote_log_"
                        + logNumber + ".log"));
                logNumber++;
            }
        } catch (IOException e) {
            logger.println("Exception in copyLogs: ");
            e.printStackTrace(logger);
        } catch (Exception e) {
            logger.println("Exception in copyLogs: ");
            e.printStackTrace(logger);
        }
    }

    private static void copyResources(File reportDirectory) {
        try {
            Util util = new Util(logger);
            logger.println("Copying resources from \"" + ReportBuilder.class.getResource("/themes/blue").toURI() + "\"to " + reportDirectory.getAbsolutePath());
            util.copyResourcesRecursively(ReportBuilder.class.getResource("/themes/blue").toURI().toURL(), reportDirectory);
        } catch (IOException e) {
            logger.println("Exception in copyResource: ");
            e.printStackTrace(logger);
        } catch (URISyntaxException e) {
            logger.println("Exception in copyResource: ");
            e.printStackTrace(logger);
        } catch (Exception e) {
            logger.println("Exception in copyResource: ");
            e.printStackTrace(logger);
        }
    }

    private static void copyResource(String relativePath, File reportDirectory) {
        try {
            Util util = new Util(logger);
            logger.println("Copying resources from \"" + ReportBuilder.class.getResource(relativePath).toURI() + "\"to " + reportDirectory.getAbsolutePath());
            util.copyResourcesRecursively(ReportBuilder.class.getResource(relativePath).toURI().toURL(), reportDirectory);
        } catch (IOException e) {
            logger.println("Exception in copyResource: ");
            e.printStackTrace(logger);
        } catch (URISyntaxException e) {
            logger.println("Exception in copyResource: ");
            e.printStackTrace(logger);
        } catch (Exception e) {
            logger.println("Exception in copyResource: ");
            e.printStackTrace(logger);
        }
    }

    private void copyResource(String resourceLocation, String resourceName) throws IOException, URISyntaxException {
        final File tmpResourcesArchive = File.createTempFile("temp", resourceName + ".zip");

        InputStream resourceArchiveInputStream = ReportBuilder.class.getResourceAsStream(resourceLocation + "/" + resourceName);
        if (resourceArchiveInputStream == null) {
            resourceArchiveInputStream = ReportBuilder.class.getResourceAsStream("/" + resourceLocation + "/" + resourceName);
        }
        OutputStream resourceArchiveOutputStream = new FileOutputStream(tmpResourcesArchive);
        try {
            IOUtils.copy(resourceArchiveInputStream, resourceArchiveOutputStream);
        } finally {
            IOUtils.closeQuietly(resourceArchiveInputStream);
            IOUtils.closeQuietly(resourceArchiveOutputStream);
        }
        UnzipUtils.unzipToFile(tmpResourcesArchive, reportDirectory);
        FileUtils.deleteQuietly(tmpResourcesArchive);
    }

    private String getPluginUrlPath(String path) {
        return path.isEmpty() ? "/" : path;
    }

    private void generateReport(String fileName, Template featureResult, VelocityContext context) throws Exception {
        Writer writer = new FileWriter(new File(reportDirectory, fileName));
        featureResult.merge(context, writer);
        writer.flush();
        writer.close();
    }

    private Properties getProperties() {
        Properties props = new Properties();
        props.setProperty("resource.loader", "class");
        props.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
        return props;
    }
}
