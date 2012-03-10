package org.korosoft.hudson.plugin;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import org.korosoft.hudson.plugin.model.CukeTestResult;

import java.io.File;
import java.io.IOException;

/**
 * Russian Salad Cucumber test result action for specific build
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class CukeTestResultAction extends AbstractCukeTestResultAction {

    private final AbstractBuild<?, ?> build;

    private String displayName;

    public CukeTestResultAction(AbstractBuild<?, ?> build, CukeTestResult result) {
        super(build);
        this.build = build;
        setResult(result);
    }

    @Override
    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public String getUrlName() {
        return "RSTestResult";
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
