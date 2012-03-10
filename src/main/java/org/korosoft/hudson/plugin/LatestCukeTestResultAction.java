package org.korosoft.hudson.plugin;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import org.korosoft.hudson.plugin.model.CukeTestResult;

/**
 * Russian Salad Cucumber latest test result action for the project
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class LatestCukeTestResultAction extends AbstractCukeTestResultAction {
    public final AbstractProject<?, ?> project;

    public LatestCukeTestResultAction(AbstractProject<?, ?> project) {
        super(project);
        this.project = project;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }

    @Override
    public AbstractBuild<?, ?> getBuild() {
        return project.getLastBuild();
    }

    public String getDisplayName() {
        return "Latest Russian Salad Report";
    }

    public String getUrlName() {
        return "LatestRSTestResult";
    }

    @Override
    public CukeTestResult getResult() {
        CukeTestResultAction action = getBuild().getAction(CukeTestResultAction.class);
        return action == null ? null : action.getResult();
    }
}
