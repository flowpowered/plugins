package com.flowpowered.plugins.annotated;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.flowpowered.plugins.PluginException;
import com.flowpowered.plugins.PluginManager;
import com.flowpowered.plugins.WrappedPluginException;
import com.flowpowered.plugins.simple.AbstractPluginHandle;

public class AnnotatedJavaPluginHandle extends AbstractPluginHandle {
    private final Object plugin;
    private final Method enable;
    private final Method disable;
    private final Context context;

    public AnnotatedJavaPluginHandle(PluginManager manager, String name, Object plugin, Method enable, Method disable) {
        super(manager, name);
        this.plugin = plugin;
        this.enable = enable;
        this.disable = disable;
        this.context = new Context(manager, this);
    }

    @Override
    protected void onEnable() throws PluginException {
        try {
            enable.invoke(plugin, context);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IllegalStateException("Got exception from reflection even though the method passed our checks", e);
        } catch (InvocationTargetException e) {
            throw new WrappedPluginException("Exception in enable method", e.getCause());
        }

    }

    @Override
    protected void onDisable() throws PluginException {
        try {
            enable.invoke(plugin, context);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IllegalStateException("Got exception from reflection even though the method passed our checks", e);
        } catch (InvocationTargetException e) {
            throw new WrappedPluginException("Exception in enable method", e.getCause());
        }

    }

}
