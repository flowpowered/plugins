package com.flowpowered.plugins.simple;

import com.flowpowered.plugins.PluginHandle;
import com.flowpowered.plugins.PluginManager;

public abstract class AbstractPluginHandle extends PluginHandle {
    private final PluginManager manager;
    private final String name;

    public AbstractPluginHandle(PluginManager manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PluginManager getManager() {
        return manager;
    }

}