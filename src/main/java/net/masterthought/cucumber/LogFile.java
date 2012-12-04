package net.masterthought.cucumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 8/1/12
 * Time: 9:15 AM
 */
public class LogFile {
    private String link;
    private String name;
    private static Pattern pattern = Pattern.compile("(.+)(\\s+)(.*)");
    private static int logNumber = 1;
    private String localLogPath;

    public LogFile() {
    }

    public LogFile(String logFile) {
        logFile = logFile.trim();
        Matcher matcher = pattern.matcher(logFile);
        if (matcher.find()) {
            this.name = (matcher.group(1)).trim();
            this.link = (matcher.group(3)).trim();
        } else {
            this.link = logFile;
            this.name = logFile;
        }
        localLogPath = "remote_log_" + logNumber + ".log";
        logNumber++;
        addHttp();
    }

    private void addHttp() {
        if (link.length() < 4 || !link.substring(0, 4).equals("http")) {
            link = "http://" + link;
        }
    }

    public LogFile(String link, String name) {
        this.link = link.trim();
        this.name = name.trim();
        addHttp();
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalLogPath() {
        return localLogPath;
    }

    public void setLocalLogPath(String localLogPath) {
        this.localLogPath = localLogPath;
    }
}
