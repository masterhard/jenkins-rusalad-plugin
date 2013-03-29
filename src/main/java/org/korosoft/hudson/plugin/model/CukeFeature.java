package org.korosoft.hudson.plugin.model;

import hudson.FilePath;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Cucumber test result feature model
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class CukeFeature {
    public static final String REPORT_JSON = "report.json";
    private final String name;
    private final JSONObject report;
    private final JSONObject videoLogs;

    public CukeFeature(String name, FilePath path) throws IOException, InterruptedException {
        this.name = name;
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

    public JSONObject getReport() {
        return report;
    }

    public JSONObject getVideoLogs() {
        return videoLogs;
    }

    @SuppressWarnings("unchecked")
    private void refineJsonObject(JSONObject jsonObject) {
        for (Map.Entry o : (Set<Map.Entry>) jsonObject.entrySet()) {
            if (o.getValue() instanceof JSONNull) {
                o.setValue(JSONNull.getInstance());
            }
            if (o.getValue() instanceof JSONObject) {
                refineJsonObject((JSONObject) o.getValue());
            }
            if (o.getValue() instanceof JSONArray) {
                JSONArray a = (JSONArray) o.getValue();
                for (int i = a.size() - 1; i >= 0; i--) {
                    if (a.get(i) instanceof JSONObject) {
                        refineJsonObject(a.getJSONObject(i));
                    }
                }
            }
        }
    }

    private Object readResolve() {
        refineJsonObject(report);
        return this;
    }

}
