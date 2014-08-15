/*
 * This file is part of Flow Plugins, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
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

import java.util.Map;

import com.flowpowered.plugins.PluginException;
import com.flowpowered.plugins.PluginHandle;
import com.flowpowered.plugins.PluginManager;
import com.flowpowered.plugins.WrappedPluginException;

public class SimpleJavaPluginLoader extends AbstractSingleClassLoaderJavaPluginLoader {
    public SimpleJavaPluginLoader(ClassLoader cl) {
        super(cl);
    }

    @Override
    protected PluginHandle load(PluginManager manager, String pluginName, Map<String, String> mains) throws PluginException {
        String main = mains.get(pluginName);
        if (main == null) {
            throw new PluginException("No main class specified");
        }
        try {
            Class<?> clazz = Class.forName(main, false, getClassLoader());
            Class<? extends JavaPlugin> pluginClass = clazz.asSubclass(JavaPlugin.class);
            JavaPlugin plugin = pluginClass.newInstance();
            PluginHandle handle = new SimpleJavaPluginHandle(manager, plugin, pluginName);
            plugin.init(manager, handle);
            return handle;
        } catch (ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException e) {
            throw new PluginException("Invalid plugin main class", e);
        } catch (ExceptionInInitializerError e) {
            throw new WrappedPluginException("Could not instantiate plugin main class", e.getCause());
        }
    }
}
