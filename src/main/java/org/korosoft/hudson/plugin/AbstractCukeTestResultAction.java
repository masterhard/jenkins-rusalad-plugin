package org.korosoft.hudson.plugin;

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
