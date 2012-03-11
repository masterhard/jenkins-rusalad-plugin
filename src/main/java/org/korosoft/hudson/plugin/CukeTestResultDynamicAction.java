package org.korosoft.hudson.plugin;

import hudson.model.Action;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.korosoft.hudson.plugin.model.RuSaladDynamicAction;
import org.korosoft.hudson.plugin.model.RuSaladDynamicActionContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Russian Salad Cucumber test result dynamic action
 *
 * @author Dmitry Korotkov
 * @since 1.0.1
 */
public class CukeTestResultDynamicAction implements Action {

    private final RuSaladDynamicActionContext context;

    public CukeTestResultDynamicAction(RuSaladDynamicActionContext context) {
        this.context = context;
        for (RuSaladDynamicAction action : RuSaladUtil.getInstance().getAllDynamicActions()) {
            action.doApply(context);
        }
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "RSDynamic";
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String path = req.getRestOfPath();
        int p = path.indexOf('/', 1);
        String name = p >= 0 ? path.substring(1, p) : path.substring(1);
        RuSaladDynamicAction action = RuSaladUtil.getInstance().getRegisteredAction(name);
        if (action == null) {
            rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            action.doDynamic(context, req, rsp);
        }
    }
}
