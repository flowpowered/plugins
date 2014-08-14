package com.flowpowered.plugins.annotated;

import org.slf4j.Logger;

import com.flowpowered.plugins.PluginHandle;
import com.flowpowered.plugins.PluginManager;

public class Context {
    private final PluginManager manager;
    private final PluginHandle handle;

    public Context(PluginManager manager, PluginHandle handle) {
        this.manager = manager;
        this.handle = handle;
    }

    public String getName() {
        return handle.getName();
    }

    public PluginManager getManager() {
        return manager;
    }

    public Logger getLogger() {
        return handle.getLogger();
    }

}
