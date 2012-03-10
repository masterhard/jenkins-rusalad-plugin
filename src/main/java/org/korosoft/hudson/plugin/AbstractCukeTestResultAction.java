package org.korosoft.hudson.plugin;

import com.thoughtworks.xstream.XStream;
import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.model.ProminentProjectAction;
import hudson.util.XStream2;
import org.korosoft.hudson.plugin.model.CukeTestResult;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Russian Salad Cucumber test result action generic implementation
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public abstract class AbstractCukeTestResultAction implements ProminentProjectAction {
    private static final Logger logger = Logger.getLogger(AbstractCukeTestResultAction.class.getName());

    private CukeTestResult result;

    public final Object owner;

    protected AbstractCukeTestResultAction(Object owner) {
        this.owner = owner;
    }

    public CukeTestResult getResult() {
        if (result == null) {
            result = load();
        }
        return result;
    }

    public void setResult(CukeTestResult result) {
        try {
            this.result = result;
            getDataFile().write(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getIconFileName() {
        return "graph.gif";
    }

    private CukeTestResult load() {
        CukeTestResult result;
        try {
            XmlFile dataFile = getDataFile();
            if (dataFile != null) {
                result = (CukeTestResult) dataFile.read();
            } else {
                result = null;
            }
        } catch (IOException e) {
            result = null;
            logger.log(Level.WARNING, "Failed to load test result", e);
        }
        return result;
    }

    private XmlFile getDataFile() {
        AbstractBuild<?, ?> build = getBuild();
        return build == null ? null : new XmlFile(XSTREAM, new File(build.getRootDir(), "cukeResult.xml"));
    }

    private static final XStream XSTREAM = new XStream2();

    public abstract AbstractBuild<?, ?> getBuild();
}
