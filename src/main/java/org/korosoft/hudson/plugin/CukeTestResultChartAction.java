package org.korosoft.hudson.plugin;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.korosoft.hudson.plugin.model.CukeFeature;

import javax.servlet.ServletException;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CukeTestResultChartAction implements Action {
    Log log = LogFactory.getLog(CukeTestResultChartAction.class);
    private final AbstractProject<?, ?> project;

    public CukeTestResultChartAction(AbstractProject<?, ?> project) {
        this.project = project;
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "RSChart";
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        rsp.setContentType("image/png");
        rsp.setHeader("Cache-Control", "no-cache");
        final OutputStream stream = rsp.getOutputStream();
        final JFreeChart chart = createChart(retrieveHistory());
        ChartUtilities.writeChartAsPNG(stream, chart, 800, 450);
        stream.close();
    }

    private List<ChartBuildEntry> retrieveHistory() {
        List<ChartBuildEntry> result = new ArrayList<ChartBuildEntry>();
        List<AbstractCukeTestResultAction> actions = new ArrayList<AbstractCukeTestResultAction>();
        AbstractBuild<?, ?> b = project.getLastBuild();
        int maxBuilds = project.getPublishersList().get(RuSaladPublisher.class).myGetDescriptor().getNumberOfHistoryBuildsInChart();
        for (int i = 0; i < maxBuilds; i++) {
            if (b == null) {
                break;
            }
            AbstractCukeTestResultAction action = b.getAction(AbstractCukeTestResultAction.class);
            if (action != null) {
                actions.add(action);
            }
            b = b.getPreviousBuild();
        }
        AbstractCukeTestResultAction previous = null;
        for (int idx = actions.size() - 1; idx >= 0; idx--) {
            AbstractCukeTestResultAction action = actions.get(idx);
            // i - improvements
            // p - passes
            // f - failures
            // r - regressions
            int i = 0, p = 0, f = 0, r = 0;
            try {
                for (Map.Entry<String, CukeFeature> entry : action.getResult().getFeatures().entrySet()) {
                    final CukeFeature feature = entry.getValue();
                    final String featureName = entry.getKey();
                    for (Object sc : feature.getReport().getJSONArray("scenarios")) {
                        if (!(sc instanceof JSONObject)) {
                            continue;
                        }
                        JSONObject scenario = (JSONObject) sc;
                        boolean passed = scenario.getBoolean("passed");
                        if (passed) {
                            p++;
                        } else {
                            f++;
                        }
                        if (previous != null) {
                            CukeFeature prevFeature = previous.getResult().getFeatures().get(featureName);
                            if (prevFeature != null) {
                                for (Object psc : prevFeature.getReport().getJSONArray("scenarios")) {
                                    if (!(psc instanceof JSONObject)) {
                                        continue;
                                    }
                                    JSONObject prevScenario = (JSONObject) psc;
                                    if (!prevScenario.getString("scenarioName").equals(scenario.getString("scenarioName"))) {
                                        continue;
                                    }
                                    boolean prevPassed = prevScenario.getBoolean("passed");
                                    if (prevPassed && !passed) {
                                        r++;
                                        f--;
                                    }
                                    if (!prevPassed && passed) {
                                        i++;
                                        p--;
                                    }
                                }
                            }
                        }
                    }
                }
                result.add(new ChartBuildEntry(action.getBuild().getNumber(), p, f, r, i));
            } catch (RuntimeException e) {
                log.warn(e);
            }
            previous = action;
        }
        return result;
    }

    private JFreeChart createChart(List<ChartBuildEntry> history) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String seriesFailed = "Failed";
        String seriesPassed = "Passed";
        String seriesRegression = "Failed [Regression]";
        String seriesImprovement = "Passed [Improvement]";

        dataset.setValue(0, seriesFailed, "0");
        dataset.setValue(0, seriesRegression, "0");
        dataset.setValue(0, seriesImprovement, "0");
        dataset.setValue(0, seriesPassed, "0");
        dataset.removeColumn(0);

        for (ChartBuildEntry entry : history) {
            final String buildLabel = Integer.toString(entry.buildNo);
            dataset.setValue(entry.failed, seriesFailed, buildLabel);
            dataset.setValue(entry.regressed, seriesRegression, buildLabel);
            dataset.setValue(entry.improved, seriesImprovement, buildLabel);
            dataset.setValue(entry.passed, seriesPassed, buildLabel);
        }

        JFreeChart chart = ChartFactory.createStackedAreaChart(null, "Build #", "Number of scenarios", dataset, PlotOrientation.VERTICAL, true, true, true);
        chart.setBackgroundPaint(Color.white);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.getDomainAxis().setAxisLineVisible(false);
        plot.getDomainAxis().setCategoryMargin(0);
        plot.getDomainAxis().setUpperMargin(0);
        plot.getDomainAxis().setLowerMargin(0);
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        plot.getRenderer().setSeriesPaint(0, new Color(0xFF8080));
        plot.getRenderer().setSeriesPaint(1, new Color(0xFF0000));
        plot.getRenderer().setSeriesPaint(2, new Color(0x0000C0));
        plot.getRenderer().setSeriesPaint(3, new Color(0x8080FF));
        return chart;
    }

    private static class ChartBuildEntry {
        private final int buildNo;
        private final int passed;
        private final int failed;
        private final int regressed;
        private final int improved;

        private ChartBuildEntry(int buildNo, int passed, int failed, int regressed, int improved) {
            this.buildNo = buildNo;
            this.passed = passed;
            this.failed = failed;
            this.regressed = regressed;
            this.improved = improved;
        }
    }
}
