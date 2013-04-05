package org.korosoft.hudson.plugin;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.korosoft.hudson.plugin.model.CukeFeature;
import org.korosoft.hudson.plugin.model.CukeTestResult;
import org.korosoft.hudson.plugin.model.RuSaladDynamicActionContext;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Russian Salad test result publisher
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class RuSaladPublisher extends Recorder {

    private String reportFolder;
    private int buildFailThreshold = Integer.MAX_VALUE;
    private boolean useUnstableMarker = true;
    private boolean performForUnstableBuild = true;

    @DataBoundConstructor
    public RuSaladPublisher(String reportFolder, int buildFailThreshold, boolean useUnstableMarker, boolean performForUnstableBuild) {
        this.reportFolder = reportFolder;
        this.buildFailThreshold = buildFailThreshold;
        this.useUnstableMarker = useUnstableMarker;
        this.performForUnstableBuild = performForUnstableBuild;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        // Do not publish report for unsuccessful builds.
        if (!performForUnstableBuild && build.getResult().isWorseOrEqualTo(Result.UNSTABLE)) {
            return true;
        }

        final CukeTestResultAction existingAction = build.getAction(CukeTestResultAction.class);

        final CukeTestResult result;
        final FilePath reportFolder = build.getWorkspace().child(this.reportFolder);
        try {
            result = new CukeTestResult(reportFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        final CukeTestResultAction action;
        if (existingAction == null) {
            action = new CukeTestResultAction(build, result);
            action.setDisplayName("Russian Salad Test Report");
        } else {
            action = existingAction;
            action.setResult(result);
        }
        if (existingAction == null) {
            build.getActions().add(action);
            try {
                build.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        RuSaladDynamicActionContext context = new RuSaladDynamicActionContext(build.getProject(), build);
        int failCount = 0;
        try {
            for (CukeFeature cukeFeature : result.getFeatures().values()) {
                for (Object scenario : cukeFeature.getReport().getJSONArray("scenarios")) {
                    if (!((JSONObject) scenario).optBoolean("passed")) {
                        failCount++;
                    }
                }

            }
        } catch (Exception ignored) {
        }
        if (failCount > buildFailThreshold) {
            build.setResult(Result.FAILURE);
        } else if (failCount > 0 && useUnstableMarker) {
            build.setResult(Result.UNSTABLE);
        }

        build.getActions().add(new CukeTestResultDynamicAction(context, reportFolder));
        try {
            build.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    public String getReportFolder() {
        return reportFolder;
    }

    public boolean isUseUnstableMarker() {
        return useUnstableMarker;
    }

    public boolean isPerformForUnstableBuild() {
        return performForUnstableBuild;
    }

    public int getBuildFailThreshold() {
        return buildFailThreshold;
    }

    public DescriptorImpl myGetDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        RuSaladDynamicActionContext context = new RuSaladDynamicActionContext(project, null);
        return Arrays.asList(new LatestCukeTestResultAction(project), new CukeTestResultDynamicAction(context, null));
    }


    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private int numberOfHistoryBuildsInPopup = 25;
        private int numberOfHistoryBuildsInChart = 50;

        public FormValidation doCheckReportFolder(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException, ServletException {
            try {
                if (value.length() == 0)
                    return FormValidation.error("Please specify report folder");
                FilePath someWorkspace = project.getSomeWorkspace();
                if (someWorkspace == null) {
                    return FormValidation.warning("Cannot validate the path since the project was never built.");
                }
                FilePath path = someWorkspace.child(value);
                if (!path.exists()) {
                    if (project.getLastStableBuild() == null) {
                        return FormValidation.warning("Cannot validate the path since there is no stable build yet. Is the path is correct it should work.");
                    }
                    return FormValidation.error("Specified path does not exist.");
                }
                if (!path.isDirectory()) {
                    return FormValidation.error("Specified file must point to a folder but it points to a file");
                }
                // Try to find at least one report...
                boolean reportFileFound = false;
                boolean tooDeep = false;
                for (FilePath child : path.list()) {
                    if (child.getName().equals(CukeFeature.REPORT_JSON)) {
                        tooDeep = true;
                        break;
                    }
                    if (!child.isDirectory()) {
                        continue;
                    }
                    for (FilePath grandChild : child.list(CukeFeature.REPORT_JSON)) {
                        reportFileFound = true;
                    }
                    if (reportFileFound) {
                        break;
                    }
                }
                if (tooDeep) {
                    return FormValidation.error(String.format("Specified path contains '%s' file. Normally that means that you should specify a folder one lever higher.", CukeFeature.REPORT_JSON));
                }
                if (!reportFileFound) {
                    return FormValidation.error("Specified path exists, but no reports were found inside. Note that reports are usually placed into folders with the names of features.");
                }
                return FormValidation.ok();
            } catch (InterruptedException e) {
                return FormValidation.error(e, "Interrupted");
            }
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Publish Russian Salad Cucumber test report";
        }

        public int getNumberOfHistoryBuildsInPopup() {
            return numberOfHistoryBuildsInPopup;
        }

        public int getNumberOfHistoryBuildsInChart() {
            return numberOfHistoryBuildsInChart;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            numberOfHistoryBuildsInPopup = formData.getInt("numberOfHistoryBuildsInPopup");
            numberOfHistoryBuildsInChart = formData.getInt("numberOfHistoryBuildsInChart");
            save();
            return super.configure(req, formData);
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(RuSaladPublisher.class, formData);
        }
    }
}

