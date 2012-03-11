package org.korosoft.hudson.plugin;

import org.korosoft.hudson.plugin.dynamic.CukeTestHistoryAction;
import org.korosoft.hudson.plugin.dynamic.CukeTestResultChartAction;
import org.korosoft.hudson.plugin.dynamic.CukeTestResultFileAction;
import org.korosoft.hudson.plugin.dynamic.VersionDynamicAction;
import org.korosoft.hudson.plugin.model.RuSaladDynamicAction;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Russian Salad utilities
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class RuSaladUtil {
    public static final String PROP_CURRENT_VERSION = "rusalad.plugin.current.version";
    public static final String PROP_LATEST_VERSION_URL = "rusalad.plugin.latest.stable.version.url";

    private final AtomicLong nextVersionCheckTime = new AtomicLong();

    private final String latestVersionUrl;
    private final String currentVersion;
    private final AtomicReference<String> latestVersion = new AtomicReference<String>();

    private final ConcurrentMap<String, RuSaladDynamicAction> actions;

    private static RuSaladUtil instance;

    public static RuSaladUtil getInstance() {
        if (instance == null) {
            synchronized (RuSaladUtil.class) {
                if (instance == null) {
                    instance = new RuSaladUtil();
                }
            }
        }
        return instance;
    }

    private RuSaladUtil() {

        actions = new ConcurrentHashMap<String, RuSaladDynamicAction>();

        registerDynamicAction(new VersionDynamicAction());
        registerDynamicAction(new CukeTestHistoryAction());
        registerDynamicAction(new CukeTestResultFileAction());
        registerDynamicAction(new CukeTestResultChartAction());

        final Properties properties = new Properties();
        final InputStream stream = RuSaladUtil.class.getResourceAsStream("/versions.properties");
        boolean loaded = true;
        try {
            properties.load(stream);
            stream.close();
        } catch (Exception e) {
            loaded = false;
        }
        currentVersion = loaded && properties.containsKey(PROP_CURRENT_VERSION) ? properties.get(PROP_CURRENT_VERSION).toString() : null;
        latestVersionUrl = loaded && properties.containsKey(PROP_LATEST_VERSION_URL) ? properties.get(PROP_LATEST_VERSION_URL).toString() : null;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getLatestVersion() {
        long t = System.currentTimeMillis();
        long l = nextVersionCheckTime.get();
        if (l < t) {
            if (nextVersionCheckTime.compareAndSet(l, t + 1000 * 60 * 60 * 24)) { // Check for updates daily
                try {
                    final Properties properties = new Properties();
                    final InputStream inStream = new URL(latestVersionUrl).openStream();
                    properties.load(inStream);
                    inStream.close();
                    final String latestStable = properties.get("rusalad.latest.stable.version").toString();
                    latestVersion.set(latestStable);
                } catch (IOException ignored) {
                }
            }
        }
        return latestVersion.get();
    }

    public RuSaladDynamicAction getRegisteredAction(String name) {
        return actions.get(name);
    }
    
    public Collection<RuSaladDynamicAction> getAllDynamicActions() {
        return Collections.unmodifiableCollection(actions.values());
    }

    private void registerDynamicAction(RuSaladDynamicAction action) {
        actions.put(action.getUrlName(), action);
    }
}
