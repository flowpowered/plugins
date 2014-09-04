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
package com.flowpowered.plugins;

import java.lang.reflect.Field;
import java.util.Map;

public abstract class PluginLoader<C extends Context> {
    private static final Field nameField = PluginLoader.getFieldSilent(Plugin.class, "name");
    private static final Field managerField = PluginLoader.getFieldSilent(Plugin.class, "manager");
    private static final Field contextField = PluginLoader.getFieldSilent(Plugin.class, "context");
    private final ContextCreator<C> contextCreator;

    public PluginLoader(ContextCreator<C> contextCreator) {
        this.contextCreator = contextCreator;
    }

    public abstract Plugin<C> load(PluginManager<C> manager, String pluginName) throws InvalidPluginException;

    public abstract Map<String, Plugin<C>> loadAll(PluginManager<C> manager);

    protected Plugin<C> init(Plugin<C> plugin, String name, PluginManager<C> manager) {
        setField(nameField, plugin, name);
        setField(managerField, plugin, manager);
        setField(contextField, plugin, createContext(plugin));
        return plugin;
    }

    public C createContext(Plugin<C> plugin) {
        return contextCreator.createContext(plugin);
    }

    public static Field getFieldSilent(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static void setField(Field f, Object instance, Object value) {
        try {
            f.setAccessible(true);
            f.set(instance, value);
            f.setAccessible(false);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
