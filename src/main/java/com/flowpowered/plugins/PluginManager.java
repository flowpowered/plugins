package com.flowpowered.plugins;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginManager {
    private final List<PluginLoader> loaders = new LinkedList<>(); // TODO: Make this a set and make PluginLoaders hashset/map compatible?
    private final Map<String, PluginHandle> plugins = new HashMap<>();
    private final Logger logger;
    private final String pluginLoggerPrefix;

    public PluginManager(Logger logger) {
        this.logger = logger;
        this.pluginLoggerPrefix = logger.getName() + ".";
    }

    public void enable(PluginHandle plugin) {
        if (plugin.getManager() != this) {
            throw new IllegalArgumentException("Not our plugin");
        }
        // TODO: thread safety
        if (plugin.getState() != PluginState.DISABLED) {
            // TODO: don't fail silently
            return;
        }
        plugin.setState(PluginState.ENABLING);
        plugin.onEnable(); // TODO: Add a way for it to fail
        plugin.setState(PluginState.ENABLED);
    }

    public void disable(PluginHandle plugin) {
        if (plugin.getManager() != this) {
            throw new IllegalArgumentException("Not our plugin");
        }
        // TODO: thread safety
        if (plugin.getState() != PluginState.ENABLED) {
            // TODO: don't fail silently
            return;
        }
        plugin.setState(PluginState.DISABLING);
        plugin.onDisable(); // TODO: Add a way for it to fail
        plugin.setState(PluginState.DISABLED);
    }

    public void addLoader(PluginLoader loader) {
        loaders.add(loader);
        /* TODO: Don't load all of them like this:
         * - this overrides existing ones with the same name
         * - this loads more than needed, should be more lazy
         * - this scans for them only once and never checks again
         */
        plugins.putAll(loader.loadAll(this));
    }

    public Logger getLogger(PluginHandle plugin) {
        return LoggerFactory.getLogger(pluginLoggerPrefix + plugin.getName());
    }

    public PluginHandle getPluginHandle(String name) {
        return plugins.get(name);
    }
}
