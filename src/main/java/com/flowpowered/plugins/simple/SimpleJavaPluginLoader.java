package com.flowpowered.plugins.simple;

import java.util.Map;

import com.flowpowered.plugins.PluginHandle;
import com.flowpowered.plugins.PluginManager;

public class SimpleJavaPluginLoader extends AbstractSingleClassLoaderJavaPluginLoader {
    public SimpleJavaPluginLoader(ClassLoader cl) {
        super(cl);
    }

    @Override
    protected PluginHandle load(PluginManager manager, String pluginName, Map<String, String> mains) {
        String main = mains.get(pluginName);
        if (main != null) {
            try {
                Class<?> clazz = Class.forName(main, false, getClassLoader());
                Class<? extends JavaPlugin> pluginClass = clazz.asSubclass(JavaPlugin.class);
                JavaPlugin plugin = pluginClass.newInstance();
                PluginHandle handle = new SimpleJavaPluginHandle(manager, plugin, pluginName);
                plugin.init(manager, handle);
                return handle;
            } catch (ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException | ExceptionInInitializerError e) {
                // TODO: log
                e.printStackTrace();
            }
        }
        return null;
    }
}
