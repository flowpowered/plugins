package com.flowpowered.plugins.annotated;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.flowpowered.plugins.PluginHandle;
import com.flowpowered.plugins.PluginManager;

public class AnnotatedJavaPluginHandle extends PluginHandle {
    private final PluginManager manager;
    private final String name;
    private final Object plugin;
    private final Method enable;
    private final Method disable;
    private final Context context;

    public AnnotatedJavaPluginHandle(PluginManager manager, String name, Object plugin, Method enable, Method disable) {
        this.manager = manager;
        this.name = name;
        this.plugin = plugin;
        this.enable = enable;
        this.disable = disable;
        this.context = new Context(manager, this);
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
        try {
            enable.invoke(plugin, context); // TODO: pass context
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    protected void onDisable() {
        try {
            disable.invoke(plugin, context); // TODO: pass context
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
