/*
 * This file is part of Flow Plugins, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <https://spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.plugins.simple;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.flowpowered.cerealization.config.Configuration;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.flowpowered.plugins.Context;
import com.flowpowered.plugins.ContextCreator;
import com.flowpowered.plugins.InvalidPluginException;
import com.flowpowered.plugins.Plugin;
import com.flowpowered.plugins.PluginLoader;
import com.flowpowered.plugins.PluginManager;

public class SimplePluginLoader<C extends Context> extends PluginLoader<C> {
    private static final String DESCRIPTOR_NAME = "plugin.yml";
    private static final String NAME_KEY = "name";
    private static final String MAIN_KEY = "main";
    private final ClassLoader cl;
    private final String descriptorName, nameKey, mainKey;

    public SimplePluginLoader(ContextCreator<C> contextCreator, ClassLoader cl) {
        this(contextCreator, cl, DESCRIPTOR_NAME);
    }

    public SimplePluginLoader(ContextCreator<C> contextCreator, ClassLoader cl, String descriptorName) {
        this(contextCreator, cl, descriptorName, NAME_KEY, MAIN_KEY);
    }

    public SimplePluginLoader(ContextCreator<C> contextCreator, ClassLoader cl, String descriptorName, String nameKey, String mainKey) {
        super(contextCreator);
        this.cl = cl;
        this.descriptorName = descriptorName;
        this.nameKey = nameKey;
        this.mainKey = mainKey;
    }

    protected ClassLoader getClassLoader() {
        return cl;
    }

    @Override
    public Plugin<C> load(PluginManager<C> manager, String pluginName) throws InvalidPluginException {
        return load(manager, pluginName, findMains());
    }

    protected Plugin<C> load(PluginManager<C> manager, String pluginName, Map<String, String> mains) throws InvalidPluginException {
        String main = mains.get(pluginName);
        if (main == null) {
            throw new InvalidPluginException("No main class specified");
        }
        try {
            Class<?> clazz = Class.forName(main, true, cl);
            @SuppressWarnings("unchecked")
            Class<Plugin<C>> pluginClass = (Class<Plugin<C>>) clazz.asSubclass(Plugin.class);
            return init(pluginClass.newInstance(), pluginName, manager);
        } catch (ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException e) {
            // TODO: log
            e.printStackTrace();
        } catch (ExceptionInInitializerError e) {
            throw new InvalidPluginException("Exception in Plugin initialization", e);
        }
        return null;
    }

    @Override
    public Map<String, Plugin<C>> loadAll(PluginManager<C> manager) {
        Map<String, Plugin<C>> loaded = new HashMap<>();
        Map<String, String> mains = findMains();
        for (String name : mains.keySet()) {
            Plugin<C> plugin;
            try {
                plugin = load(manager, name, mains);
                if (plugin != null) {
                    loaded.put(name, plugin);
                }
            } catch (Exception ex) {
                // TODO: log
                ex.printStackTrace();
            }
        }
        return loaded;
    }

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
        ArrayList<URL> list = Collections.list(urls);
        for (URL url : list) {
            try {
                Configuration conf = new YamlConfiguration(url.openStream());
                conf.load();
                ConfigurationNode name = conf.getChild(nameKey);
                if (name == null || name.getString("").isEmpty()) {
                    // TODO: log
                    continue;
                }
                ConfigurationNode main = conf.getChild(mainKey);
                if (main == null || main.getString("").isEmpty()) {
                    // TODO: log
                    continue;
                }
                mains.put(name.getString(), main.getString());
            } catch (IOException e) {
                // TODO: log
                e.printStackTrace();
            } catch (ConfigurationException ex) {
               ex.printStackTrace();
            }

        }
        return mains;
    }
}
