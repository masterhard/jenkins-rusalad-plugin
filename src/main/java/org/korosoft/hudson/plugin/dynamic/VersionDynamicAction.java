package org.korosoft.hudson.plugin.dynamic;

import hudson.FilePath;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.korosoft.hudson.plugin.RuSaladUtil;
import org.korosoft.hudson.plugin.model.RuSaladDynamicAction;
import org.korosoft.hudson.plugin.model.RuSaladDynamicActionContext;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Version information dynamic action
 *
 * @author Dmitry Korotkov
 * @since 1.0.1
 */
public class VersionDynamicAction implements RuSaladDynamicAction {
    public static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    public void doDynamic(RuSaladDynamicActionContext context, StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        JSONObject json = new JSONObject();
        json.put("currentVersion", RuSaladUtil.getInstance().getCurrentVersion());
        json.put("latestVersion", RuSaladUtil.getInstance().getLatestVersion());
        json.put("message", getNewVersionAvailableMessage());
        rsp.setContentType("application/json");
        PrintWriter writer = rsp.getWriter();
        writer.write(json.toString(4));
        writer.close();
    }

    public String getUrlName() {
        return "Version";
    }

    public void doApply(RuSaladDynamicActionContext context, FilePath reportPath) {
    }

    private String getNewVersionAvailableMessage() {
        String currentVersion = RuSaladUtil.getInstance().getCurrentVersion();
        boolean snapshot = false;
        if (currentVersion.endsWith(SNAPSHOT_SUFFIX)) {
            currentVersion = currentVersion.replace(SNAPSHOT_SUFFIX, "");
            snapshot = true;
        }
        final String latestStable = RuSaladUtil.getInstance().getLatestVersion();
        if (currentVersion.compareToIgnoreCase(latestStable) < 0 || (currentVersion.compareToIgnoreCase(latestStable) == 0 && snapshot)) {
            return "UPDATE IS AVAILABLE. Visit <a href=\"http://code.google.com/p/russian-salad/downloads/list\">http://code.google.com/p/russian-salad/downloads/list</a> page to get updated plugin version.";
        } else {
            return null;
        }
    }

}
