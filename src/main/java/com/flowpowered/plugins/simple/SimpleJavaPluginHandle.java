package com.flowpowered.plugins.simple;

import com.flowpowered.plugins.PluginHandle;
import com.flowpowered.plugins.PluginManager;

public class SimpleJavaPluginHandle extends PluginHandle {
    private final PluginManager manager;
    private final JavaPlugin plugin;
    private final String name;

    public SimpleJavaPluginHandle(PluginManager manager, JavaPlugin plugin, String name) {
        this.manager = manager;
        this.plugin = plugin;
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

    @Override
    protected void onEnable() {
        plugin.onEnable();
    }

    @Override
    protected void onDisable() {
        plugin.onDisable();
    }

}
