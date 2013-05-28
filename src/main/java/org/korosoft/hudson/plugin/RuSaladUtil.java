package org.korosoft.hudson.plugin;

import org.korosoft.hudson.plugin.dynamic.CukeTestHistoryAction;
import org.korosoft.hudson.plugin.dynamic.CukeTestResultChartAction;
import org.korosoft.hudson.plugin.dynamic.CukeTestResultFileAction;
import org.korosoft.hudson.plugin.model.RuSaladDynamicAction;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Russian Salad utilities
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class RuSaladUtil {
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

        registerDynamicAction(new CukeTestHistoryAction());
        registerDynamicAction(new CukeTestResultFileAction());
        registerDynamicAction(new CukeTestResultChartAction());
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
