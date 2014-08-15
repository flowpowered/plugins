package com.flowpowered.plugins.simple;

import com.flowpowered.plugins.PluginException;
import com.flowpowered.plugins.PluginManager;
import com.flowpowered.plugins.WrappedPluginException;

public class SimpleJavaPluginHandle extends AbstractPluginHandle {
    private final JavaPlugin plugin;
    public SimpleJavaPluginHandle(PluginManager manager, JavaPlugin plugin, String name) {
        super(manager, name);
        this.plugin = plugin;
    }

    @Override
    protected void onEnable() throws PluginException {
        try {
            plugin.onEnable();
        } catch (Throwable t) {
            throw new WrappedPluginException("Exception in onEnable()", t);
        }
    }

    @Override
    protected void onDisable() throws PluginException {
        try {
            plugin.onDisable();
        } catch (Throwable t) {
            throw new WrappedPluginException("Exception in onDisable()", t);
        }
    }

}
