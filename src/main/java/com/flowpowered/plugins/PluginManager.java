/*
 * This file is part of Flow Plugins, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <https://spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginManager<C extends Context> {
    private final List<PluginLoader<C>> loaders = new LinkedList<>(); // TODO: Make this a set and make PluginLoaders hashset/map compatible?
    private final Map<String, Plugin<C>> plugins = new HashMap<>();
    private final Map<Plugin<C>, PluginState> states = new HashMap<>();
    private final Logger logger;
    private final PluginLoggerFactory logFactory;

    public PluginManager(Logger logger, PluginLoggerFactory factory) {
        this.logger = logger;
        this.logFactory = factory;
    }

    public PluginManager(Logger logger) {
        this(logger, defaultPluginLoggerFactory(logger));
    }

    public void enable(Plugin<C> plugin) throws Exception {
        if (plugin.getManager() != this) {
            throw new IllegalArgumentException("Not our plugin");
        }
        // TODO: thread safety
        if (states.get(plugin) != PluginState.DISABLED) {
            // TODO: don't fail silently
            return;
        }
        states.put(plugin, PluginState.ENABLING);
        try {
            plugin.onEnable();
            states.put(plugin, PluginState.ENABLED);
        } catch (Exception e) {
            states.put(plugin, PluginState.DISABLED); // TODO: sure? Maybe add state FAILED ?
            throw e;
        }
    }

    public void disable(Plugin<C> plugin) throws Exception {
        if (plugin.getManager() != this) {
            throw new IllegalArgumentException("Not our plugin");
        }
        // TODO: thread safety
        if (states.get(plugin) != PluginState.ENABLED) {
            // TODO: don't fail silently
            return;
        }
        states.put(plugin, PluginState.DISABLING);
        try {
            plugin.onDisable();
            states.put(plugin, PluginState.DISABLED);
        } catch (Exception e) {
            states.put(plugin, PluginState.DISABLED); // TODO: Now it's state is what? DISABLED ? It failed to disable... add FAILED state maybe?
            throw e;
        }
    }

    protected void addLoader(PluginLoader<C> loader) {
        loaders.add(loader);
        /* TODO: Don't load all of them like this:
         * - this overrides existing ones with the same name
         * - this loads more than needed, should be more lazy
         * - this scans for them only once and never checks again
         */
        Map<String, Plugin<C>> all = loader.loadAll(this);
        for (Entry<String, Plugin<C>> e : all.entrySet()) {
            Plugin<C> plugin = e.getValue();
            plugins.put(e.getKey(), plugin);
            states.put(plugin, PluginState.DISABLED);
        }
    }

    public Logger getLogger(Plugin<C> plugin) {
        return logFactory.getLogger(plugin.getName());
    }

    public Plugin<C> getPlugin(String name) {
        return plugins.get(name);
    }

    public PluginState getState(Plugin<C> plugin) {
        return states.get(plugin);
    }

    public Collection<Plugin<C>> getPlugins() {
        return Collections.unmodifiableCollection(plugins.values());
    }

    protected static PluginLoggerFactory defaultPluginLoggerFactory(Logger logger) {
        final String prefix = logger.getName() + ".";
        return new PluginLoggerFactory() {
            @Override
            public Logger getLogger(String pluginName) {
                return LoggerFactory.getLogger(prefix + pluginName);
            }
        };
    }
}
