package com.flowpowered.plugins;

import org.slf4j.Logger;

public abstract class PluginHandle {

    public abstract String getName();

    public abstract PluginManager getManager();

    protected abstract void onEnable();

    protected abstract void onDisable();

    public void enable() {
        getManager().enable(this);
    }

    public void disable() {
        getManager().disable(this);
    }

    public Logger getLogger() {
        return getManager().getLogger(this);
    }
}
