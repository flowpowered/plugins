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
