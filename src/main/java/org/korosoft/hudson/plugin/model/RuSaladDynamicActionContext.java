package org.korosoft.hudson.plugin.model;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

/**
 * Context data for {@link RuSaladDynamicAction}.
 *
 * @author Dmitry Korotkov
 * @since 1.0.1
 */
public class RuSaladDynamicActionContext {
    private final AbstractProject<?, ?> project;
    private final AbstractBuild<?, ?> build;
    private final FilePath reportPath;

    public RuSaladDynamicActionContext(AbstractProject<?, ?> project, AbstractBuild<?, ?> build, FilePath reportPath) {
        this.project = project;
        this.build = build;
        this.reportPath = reportPath;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public FilePath getReportPath() {
        return reportPath;
    }

}
