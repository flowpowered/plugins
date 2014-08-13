package com.flowpowered.plugins.basic;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.flowpowered.cerealization.config.Configuration;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.ConfigurationNodeSource;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.flowpowered.plugins.PluginHandle;
import com.flowpowered.plugins.PluginLoader;
import com.flowpowered.plugins.PluginManager;

public class SimpleJavaPluginLoader implements PluginLoader {
    private static final String DESCRIPTOR_NAME = "plugin.yml";
    private static final String NAME_KEY = "name";
    private static final String MAIN_KEY = "main";
    private final ClassLoader cl;

    public SimpleJavaPluginLoader(ClassLoader cl) {
        this.cl = cl;
    }

    @Override
    public Set<String> scan() {
        return findMains().keySet();
    }

    @Override
    public PluginHandle load(PluginManager manager, String pluginName) {
        return load(manager, pluginName, findMains());
    }

    protected PluginHandle load(PluginManager manager, String pluginName, Map<String, String> mains) {
        String main = mains.get(pluginName);
        if (main != null) {
            try {
                Class<?> clazz = Class.forName(main, false, cl);
                Class<? extends JavaPlugin> pluginClass = clazz.asSubclass(JavaPlugin.class);
                JavaPlugin plugin = pluginClass.newInstance();
                return new SimpleJavaPluginHandle(manager, plugin, pluginName);
            } catch (ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException | ExceptionInInitializerError e) {
                // TODO: log
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public Map<String, PluginHandle> loadAll(PluginManager manager) {
        Map<String, PluginHandle> loaded = new HashMap<>();
        Map<String, String> mains = findMains();
        for (String name : mains.keySet()) {
            PluginHandle plugin = load(manager, name, mains);
            if (plugin != null) {
                loaded.put(name, plugin);
            }
        }
        return loaded;
    }

    protected Map<String, String> findMains() {
        Map<String, String> mains = new HashMap<>();
        Enumeration<URL> urls;
        try {
            urls = cl.getResources(DESCRIPTOR_NAME);
        } catch (IOException e) {
            // TODO log
            e.printStackTrace();
            return mains;
        }
        for (URL url : Collections.list(urls)) {
            try {
                Configuration conf = new YamlConfiguration(url.openStream());
                ConfigurationNode name = conf.getChild(NAME_KEY);
                if (name == null || name.getString("").isEmpty()) {
                    // TODO: log
                    continue;
                }
                ConfigurationNode main = conf.getChild(MAIN_KEY);
                if (name == null || name.getString("").isEmpty()) {
                    // TODO: log
                    continue;
                }
                mains.put(name.getString(), main.getString());
            } catch (IOException e) {
                // TODO: log
                e.printStackTrace();
            }

        }
        return mains;
    }

    protected ConfigurationNodeSource parseDescriptor(Reader reader) {
        Configuration conf = new YamlConfiguration(reader);
        return conf;
    }
}
