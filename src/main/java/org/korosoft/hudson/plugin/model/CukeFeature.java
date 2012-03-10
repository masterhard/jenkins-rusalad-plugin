package org.korosoft.hudson.plugin.model;

import hudson.FilePath;
import net.sf.json.JSONObject;

import java.io.IOException;

/**
 * Cucumber test result feature model
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class CukeFeature {
    public static final String REPORT_JSON = "report.json";
    private final String name;
    private final FilePath path;
    private final JSONObject report;
    private final JSONObject videoLogs;

    public CukeFeature(String name, FilePath path) throws IOException, InterruptedException {
        this.name = name;
        this.path = path;
        FilePath[] files = path.list("*.mkv");
        videoLogs = new JSONObject();
        for (FilePath dir : files) {
            videoLogs.put(dir.getName(), dir.getBaseName());
        }
        FilePath reportFile = path.child(REPORT_JSON);
        if (!reportFile.exists()) {
            throw new RuntimeException(String.format("Report file %s not found for feature %s", REPORT_JSON, name));
        }
        String jsonReportString = reportFile.readToString();
        report = JSONObject.fromObject(jsonReportString);
        report.put("dirName", name);
    }

    public String getName() {
        return name;
    }

    public FilePath getPath() {
        return path;
    }

    public JSONObject getReport() {
        return report;
    }

    public JSONObject getVideoLogs() {
        return videoLogs;
    }
}
