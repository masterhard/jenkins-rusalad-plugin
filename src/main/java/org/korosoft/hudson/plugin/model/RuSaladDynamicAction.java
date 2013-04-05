package org.korosoft.hudson.plugin.model;

import hudson.FilePath;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * A plugin action for {@link org.korosoft.hudson.plugin.CukeTestResultDynamicAction} hub.
 *
 * @author Dmitry Korotkov
 * @since 1.0.1
 */
public interface RuSaladDynamicAction {
    /**
     * This method is invoked from within {@link org.korosoft.hudson.plugin.CukeTestResultDynamicAction}.
     * <p/>
     * <b>Note:</b> {@link org.kohsuke.stapler.StaplerRequest#getRestOfPath()} will return path preceded by
     * action name (the same as {@link #getUrlName()} returns).
     *
     * @param context Dynamic action context
     * @param req     Stapler request
     * @param rsp     Stapler response
     * @throws IOException      when IO exception occurs
     * @throws ServletException when ServletException occurs
     */
    void doDynamic(RuSaladDynamicActionContext context, StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException;

    /**
     * This method is invoked when the action is bound to the build / project.
     * @param context Dynamic action context
     */
    void doApply(RuSaladDynamicActionContext context, FilePath reportPath);

    /**
     * Method getUrlName should return the name for action to be used in URL part.
     *
     * @return the name for action to be used in URL part
     */
    String getUrlName();

}
