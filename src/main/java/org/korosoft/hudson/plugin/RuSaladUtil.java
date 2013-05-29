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
