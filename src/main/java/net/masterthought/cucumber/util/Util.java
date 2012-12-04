package net.masterthought.cucumber.util;

import net.masterthought.cucumber.ScenarioTag;
import net.masterthought.cucumber.json.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    private PrintStream log = System.out;
    private static int BUFFER_SIZE = 1<<14; //16384 bytes
    public static byte[] buffer = new byte[BUFFER_SIZE];

    public Util() {
    }

    public Util(PrintStream log) {
        this.log = log;
    }

    public static enum Status {
        PASSED, FAILED, SKIPPED, UNDEFINED, MISSING, FAILED_BUILD
    }

    public static Map<String, Status> resultMap = new HashMap<String, Status>() {{
        put("passed", Util.Status.PASSED);
        put("failed", Util.Status.FAILED);
        put("skipped", Util.Status.SKIPPED);
        put("undefined", Util.Status.UNDEFINED);
        put("missing", Util.Status.MISSING);
        put("failed_build", Status.FAILED_BUILD);
    }};

    public static String result(Status status) {
        String result = "<div>";
        if (status == Status.PASSED) {
            result = "<div class=\"passed\">";
        } else if (status == Status.FAILED) {
            result = "<div class=\"failed\">";
        } else if (status == Status.SKIPPED) {
            result = "<div class=\"skipped\">";
        } else if (status == Status.UNDEFINED) {
            result = "<div class=\"undefined\">";
        } else if (status == Status.MISSING) {
            result = "<div class=\"missing\">";
        } else if (status == Status.FAILED_BUILD) {
            result = "<div class=\"failed\">";
        }
        return result;
    }

    public static String readFileAsString(String filePath) throws java.io.IOException {
        byte[] buffer = new byte[(int) new File(filePath).length()];
        BufferedInputStream f = null;
        try {
            f = new BufferedInputStream(new FileInputStream(filePath));
            f.read(buffer);
        } finally {
            if (f != null) try {
                f.close();
            } catch (IOException ignored) {
            }
        }
        return new String(buffer);
    }

    public static <T> boolean itemExists(T[] tags) {
        boolean result = false;
        if (tags != null) {
            result = tags.length != 0;
        }
        return result;
    }

    public static boolean itemExists(String item) {
        return !(item == null || item.isEmpty());
    }

    public static boolean itemExists(List<String> listItem) {
        return listItem.size() != 0;
    }

    public static boolean itemExists(Tag[] tags) {
        boolean result = false;
        if (tags != null) {
            result = tags.length != 0;
        }
        return result;
    }

    public static String passed(boolean value) {
        return value ? "<div class=\"passed\">" : "</div>";
    }

    public static String closeDiv() {
        return "</div>";
    }

    public static <T, R> List<R> collectScenarios(Element[] list, Closure<String, Element> clo) {
        List<R> res = new ArrayList<R>();
        for (final Element t : list) {
            res.add((R) clo.call(t));
        }
        return res;
    }

    public static <T, R> List<R> collectSteps(Step[] list, Closure<String, Step> clo) {
        List<R> res = new ArrayList<R>();
        if (list == null) {
            return res;
        }
        for (final Step t : list) {
            res.add((R) clo.call(t));
        }
        return res;
    }

    public static <T, R> List<R> collectTags(Tag[] list, StringClosure<String, Tag> clo) {
        List<R> res = new ArrayList<R>();
        for (final Tag t : list) {
            res.add((R) clo.call(t));
        }
        return res;
    }

    public static String U2U(String s) {
        final Pattern p = Pattern.compile("\\\\u\\s*([0-9(A-F|a-f)]{4})", Pattern.MULTILINE);
        String res = s;
        Matcher m = p.matcher(res);
        while (m.find()) {
            res = res.replaceAll("\\" + m.group(0),
                    Character.toString((char) Integer.parseInt(m.group(1), 16)));
        }
        return res;
    }

    public static boolean isValidCucumberJsonReport(String fileContent) {
        return fileContent.contains("Feature");
    }

    public static String formatDuration(Long duration) {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendDays()
                .appendSuffix(" day", " days")
                .appendSeparator(" and ")
                .appendHours()
                .appendSuffix(" hour", " hours")
                .appendSeparator(" and ")
                .appendMinutes()
                .appendSuffix(" min", " mins")
                .appendSeparator(" and ")
                .appendSeconds()
                .appendSuffix(" sec", " secs")
                .appendSeparator(" and ")
                .appendMillis()
                .appendSuffix(" ms", " ms")
                .toFormatter();
        return formatter.print(new Period(0, duration / 1000000));
    }

    public static List<Step> setStepStatus(List<Step> steps, Step step, Util.Status stepStatus, Util.Status status) {
        if (stepStatus == status) {
            steps.add(step);
        }
        return steps;
    }

    public static List<Element> setScenarioStatus(List<Element> scenarios, Element scenario, Util.Status scenarioStatus, Util.Status status) {
        if (scenarioStatus == status) {
            scenarios.add(scenario);
        }
        return scenarios;
    }

    public static int findStatusCount(List<Util.Status> statuses, Status statusToFind) {
        int occurrence = 0;
        for (Util.Status status : statuses) {
            if (status == statusToFind) {
                occurrence++;
            }
        }
        return occurrence;
    }

    public static boolean hasSteps(Element element) {
        boolean result = element.getSteps() == null || element.getSteps().length == 0;
        if (result) {
            System.out.println("[WARNING] scenario has no steps:  " + element.getName());
        }
        return !result;
    }

    public static boolean hasSteps(ScenarioTag scenario) {
        boolean result = scenario.getScenario().getSteps() == null || scenario.getScenario().getSteps().length == 0;
        if (result) {
            System.out.println("[WARNING] scenario tag has no steps:  " + scenario.getScenario().getName());
        }
        return !result;
    }

    public static boolean hasScenarios(Feature feature) {
        boolean result = feature.getElements() == null || feature.getElements().length == 0;
        if (result) {
            System.out.println("[WARNING] feature has no scenarios:  " + feature.getName());
        }
        return !result;
    }


    public void copyResourcesRecursively(URL originUrl, File destination) throws Exception {
        URLConnection urlConnection = originUrl.openConnection();
        if (urlConnection instanceof JarURLConnection) {
            copyJarResourcesRecursively(destination, (JarURLConnection) urlConnection);
        } else if (urlConnection instanceof FileURLConnection) {
            FileUtils.copyDirectory(new File(originUrl.getPath()), destination);
        } else {
            throw new Exception("URLConnection[" + urlConnection.getClass().getSimpleName() +
                    "] is not a recognized/implemented connection type.");
        }
    }

    public void copyJarResourcesRecursively(File destination, JarURLConnection jarConnection ) throws IOException {
        JarFile jarFile = jarConnection.getJarFile();
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        JarEntry entry;
        while(jarEntries.hasMoreElements()) {
            entry = jarEntries.nextElement();
            if (entry.getName().startsWith(jarConnection.getEntryName())) {
                String fileName = StringUtils.removeStart(entry.getName(), jarConnection.getEntryName());
                if (!entry.isDirectory()) {
                    InputStream entryInputStream = null;
                    try {
                        entryInputStream = jarFile.getInputStream(entry);
                        copyInputStreamToFile(entryInputStream, new File(destination, fileName));
                    } finally {
                        if (entryInputStream != null) {
                            entryInputStream.close();
                        }
                    }
                } else {
//                    FileUtils.ensureDirectoryExists(new File(destination, fileName));
                }
            }
        }
    }

    public void copyInputStreamToFile(InputStream in, File destFile) {
        FileOutputStream out;
        try {
            if (!destFile.getParentFile().exists())
                destFile.getParentFile().mkdirs();
            if (!destFile.exists())
                destFile.createNewFile();
            out = new FileOutputStream(destFile);
        } catch (FileNotFoundException e) {
            log.println("Exception in copyInputStreamToFile");
            e.printStackTrace(log);
            return;
        } catch (IOException e) {
            log.println("Exception in copyInputStreamToFile");
            e.printStackTrace(log);
            return;
        }
        int len;
        try {
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            log.println("Exception in copyInputStreamToFile");
            e.printStackTrace(log);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                log.println("Exception in copyInputStreamToFile");
                e.printStackTrace(log);
            }
        }
    }
}
