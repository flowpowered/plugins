package com.flowpowered.plugins.simple;

import com.flowpowered.plugins.PluginManager;

public class SimpleJavaPluginHandle extends AbstractPluginHandle {
    private final JavaPlugin plugin;
    public SimpleJavaPluginHandle(PluginManager manager, JavaPlugin plugin, String name) {
        super(manager, name);
        this.plugin = plugin;
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
