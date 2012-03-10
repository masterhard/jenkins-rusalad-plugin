package org.korosoft.hudson.plugin.model;

import hudson.FilePath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Cucumber test result model
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class CukeTestResult {
    private Map<String, CukeFeature> features;

    public CukeTestResult(FilePath reportPath) throws IOException, InterruptedException {
        if (!reportPath.exists()) {
            throw new FileNotFoundException("Report file not found: " + reportPath);
        }
        List<FilePath> dirs = reportPath.listDirectories();
        features = new HashMap<String, CukeFeature>(dirs.size(), 1.0f);
        for (FilePath dir : dirs) {
            features.put(dir.getName(), new CukeFeature(dir.getName(), dir));
        }
    }

    public List<String> getFeatureNames() {
        List<String> res = new ArrayList<String>(features.keySet());
        Collections.sort(res);
        return res;
    }

    public Map<String, CukeFeature> getFeatures() {
        return features;
    }
}

