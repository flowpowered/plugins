package com.flowpowered.plugins.simple;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.flowpowered.cerealization.config.Configuration;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.flowpowered.plugins.PluginHandle;
import com.flowpowered.plugins.PluginLoader;
import com.flowpowered.plugins.PluginManager;

public abstract class AbstractSingleClassLoaderJavaPluginLoader implements PluginLoader {
    static final String DESCRIPTOR_NAME = "plugin.yml";
    static final String NAME_KEY = "name";
    static final String MAIN_KEY = "main";

    private final ClassLoader cl;
    private final String descriptorName, nameKey, mainKey;


    public AbstractSingleClassLoaderJavaPluginLoader(ClassLoader cl) {
        this(cl, DESCRIPTOR_NAME);
    }

    public AbstractSingleClassLoaderJavaPluginLoader(ClassLoader cl, String descriptorName) {
        this(cl, descriptorName, NAME_KEY, MAIN_KEY);
    }

    public AbstractSingleClassLoaderJavaPluginLoader(ClassLoader cl, String descriptorName, String nameKey, String mainKey) {
        this.cl = cl;
        this.descriptorName = descriptorName;
        this.nameKey = nameKey;
        this.mainKey = mainKey;
    }

    protected ClassLoader getClassLoader() {
        Object o;
        return cl;
    }

    @Override
    public Set<String> scan() {
        return findMains().keySet();
    }

    @Override
    public PluginHandle load(PluginManager manager, String pluginName) {
        return load(manager, pluginName, findMains());
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

    protected abstract PluginHandle load(PluginManager manager, String pluginName, Map<String, String> mains);

    protected Map<String, String> findMains() {
        Map<String, String> mains = new HashMap<>();
        Enumeration<URL> urls;
        try {
            urls = cl.getResources(descriptorName);
        } catch (IOException e) {
            // TODO log
            e.printStackTrace();
            return mains;
        }
        for (URL url : Collections.list(urls)) {
            try {
                Configuration conf = new YamlConfiguration(url.openStream());
                ConfigurationNode name = conf.getChild(nameKey);
                if (name == null || name.getString("").isEmpty()) {
                    // TODO: log
                    continue;
                }
                ConfigurationNode main = conf.getChild(mainKey);
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

}