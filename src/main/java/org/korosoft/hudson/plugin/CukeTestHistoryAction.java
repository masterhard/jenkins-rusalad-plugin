package org.korosoft.hudson.plugin;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.korosoft.hudson.plugin.model.CukeFeature;
import org.korosoft.hudson.plugin.model.CukeTestResult;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Russian Salad Cucumber test result history action
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class CukeTestHistoryAction implements Action {

    private final AbstractBuild<?, ?> build;
    private transient JSONObject history;

    public CukeTestHistoryAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "RSHistory";
    }

    public JSONObject getHistory() throws IOException {
        history = loadHistory(build);
        return history;
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        rsp.setContentType("application/json");
        rsp.setCharacterEncoding("UTF-8");
        String res = getHistory().toString(4);
        PrintWriter writer = rsp.getWriter();
        writer.write(res);
        writer.close();
    }

    private JSONObject loadHistory(AbstractBuild<?, ?> build) throws IOException {
        JSONObject result = new JSONObject();
        int numberOfBuildsToProcess = build.getProject().getPublishersList().get(RuSaladPublisher.class).myGetDescriptor().getNumberOfHistoryBuildsInPopup();
        while (build != null && numberOfBuildsToProcess > 0) {
            try {
                processBuild(result, build);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            numberOfBuildsToProcess--;
            build = build.getPreviousBuild();
        }
        return result;
    }

    private void processBuild(JSONObject result, AbstractBuild<?, ?> build) throws IOException, InterruptedException {

        if (!result.has("featureNames")) {
            result.put("featureNames", new JSONArray());
        }
        if (!result.has("features")) {
            result.put("features", new JSONObject());
        }
        if (!result.has("builds")) {
            result.put("builds", new JSONArray());
        }
        result.getJSONArray("builds").add(0, build.getNumber());

        CukeTestResult cukeTestResult;
        try {
            cukeTestResult = new CukeTestResult(new FilePath(build.getRootDir()).child(CukeTestResultFileAction.CUKE_RESULT));
        } catch (IOException e) {
            return;
        }

        JSONObject featuresObject = result.getJSONObject("features");
        JSONArray featureNames = result.getJSONArray("featureNames");
        int lastIdx = 0;
        for (String featureName : cukeTestResult.getFeatureNames()) {
            int idx = featureNames.indexOf(featureName);
            if (idx < 0) {
                featureNames.add(lastIdx, featureName);
                JSONObject feature = new JSONObject();
                feature.put("scenarioNames", new JSONArray());
                feature.put("scenarios", new JSONObject());
                featuresObject.put(featureName, feature);
                lastIdx++;
            } else {
                lastIdx = idx;
            }

            processFeatureResult(result, build, featureName, cukeTestResult.getFeatures().get(featureName));
        }
    }

    private void processFeatureResult(JSONObject result, AbstractBuild<?, ?> build, String featureName, CukeFeature feature) {
        JSONArray scenarioNames = result.getJSONObject("features").getJSONObject(featureName).getJSONArray("scenarioNames");
        JSONObject scenarioResults = result.getJSONObject("features").getJSONObject(featureName).getJSONObject("scenarios");

        int lastIdx = 0;
        for (Object s : feature.getReport().getJSONArray("scenarios")) {
            if (!(s instanceof JSONObject)) {
                continue;
            }
            JSONObject scenario = (JSONObject) s;
            String scenarioName = scenario.getString("scenarioName");
            int idx = scenarioNames.indexOf(scenarioName);
            if (idx == -1) {
                scenarioNames.add(lastIdx, scenarioName);
                scenarioResults.put(scenarioName, new JSONObject());
                lastIdx++;
            } else {
                lastIdx = idx;
            }
            JSONObject scenarioResultsObject = scenarioResults.getJSONObject(scenarioName);
            String status;
            if (scenario.has("status")) {
                status = scenario.getString("status");
            } else {
                status = scenario.getBoolean("passed") ? "passed" : "failed";
            }
            scenarioResultsObject.put(Integer.toString(build.getNumber()), status);
        }
    }
}
