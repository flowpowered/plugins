package com.flowpowered.plugins.simple;

import org.slf4j.Logger;

import com.flowpowered.plugins.PluginHandle;
import com.flowpowered.plugins.PluginManager;

/**
 * This is specific to SimpleJavaPluginLoader. Don't worry, if you use the {@link com.flowpowered.plugins.annotated.AnnotatedJavaPluginLoader} this class won't be used.
 */
public abstract class JavaPlugin {
    private boolean initialized = false;
    private PluginManager manager;
    private PluginHandle handle; // TODO: Sure?

    protected final void init(PluginManager manager, PluginHandle handle) {
        if (initialized) {
            throw new IllegalStateException("Tried to initialize twice");
        }
        this.initialized = true;
        this.manager = manager;
        this.handle = handle;
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    protected PluginManager getManager() {
        return manager;
    }

    protected PluginHandle getSelfHandle() {
        return handle;
    }

    protected Logger getLogger() {
        return handle.getLogger();
    }
}
