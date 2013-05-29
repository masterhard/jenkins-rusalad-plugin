package org.korosoft.hudson.plugin.dynamic;

/*

The New BSD License

Copyright (c) 2011-2013, Dmitry Korotkov
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

- Redistributions in binary form must reproduce the above copyright notice, this
  list of conditions and the following disclaimer in the documentation and/or
  other materials provided with the distribution.

- Neither the name of the Jenkins RuSalad Plugin nor the names of its
  contributors may be used to endorse or promote products derived from this
  software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
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
import org.korosoft.hudson.plugin.AbstractCukeTestResultAction;
import org.korosoft.hudson.plugin.RuSaladPublisher;
import org.korosoft.hudson.plugin.model.CukeFeature;
import org.korosoft.hudson.plugin.model.RuSaladDynamicAction;
import org.korosoft.hudson.plugin.model.RuSaladDynamicActionContext;

import javax.servlet.ServletException;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CukeTestResultChartAction implements RuSaladDynamicAction {
    Log log = LogFactory.getLog(CukeTestResultChartAction.class);

    public String getUrlName() {
        return "Chart";
    }

    public void doApply(RuSaladDynamicActionContext context, FilePath reportPath) {
    }

    public void doDynamic(RuSaladDynamicActionContext context, StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        rsp.setContentType("image/png");
        rsp.setHeader("Cache-Control", "no-cache");
        final OutputStream stream = rsp.getOutputStream();
        final JFreeChart chart = createChart(retrieveHistory(context.getProject()));
        ChartUtilities.writeChartAsPNG(stream, chart, 800, 450);
        stream.close();
    }

    private List<ChartBuildEntry> retrieveHistory(AbstractProject<?, ?> project) {
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
