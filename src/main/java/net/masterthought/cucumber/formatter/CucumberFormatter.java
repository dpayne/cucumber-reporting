package net.masterthought.cucumber.formatter;

import gherkin.formatter.JSONFormatter;
import net.masterthought.cucumber.ReportBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 8/2/12
 * Time: 9:55 AM
 */
public class CucumberFormatter extends JSONFormatter {
    private final File outputJson;
    private final File outputDirectory;
    private final String projectName;

    public CucumberFormatter(File outputDirectory) {
        super(getOutputJson(outputDirectory));
        this.outputDirectory = outputDirectory;
        File temp = outputDirectory.getAbsoluteFile();
        if (temp.getParentFile() != null && temp.getParentFile().getParentFile() != null) {
            projectName = temp.getParentFile().getParentFile().getName();
        } else {
            projectName = "cucumber";
        }

        this.outputJson = new File(outputDirectory, projectName + ".json");
    }

    public static Appendable getOutputJson(File outputDirectory) {
        String projectName;
        File outputJson;
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        File temp = outputDirectory.getAbsoluteFile();
        if (temp.getParentFile() != null && temp.getParentFile().getParentFile() != null) {
            projectName = temp.getParentFile().getParentFile().getName();
        } else {
            projectName = "cucumber";
        }

        outputJson = new File(outputDirectory, projectName + ".json");

        try {
            if (!outputJson.exists()) {
                outputJson.createNewFile();
            }
            return (new BufferedWriter(new FileWriter(outputJson)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        super.close();
        List<String> list = new ArrayList<String>();
        list.add(outputJson.getAbsolutePath());
        ReportBuilder reportBuilder;

        try {
            //todo: enable artifacts
            reportBuilder = new ReportBuilder(list, outputDirectory, "", "1", projectName, false, false, false, false, System.out, null, false, "");
            reportBuilder.generateReports();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

