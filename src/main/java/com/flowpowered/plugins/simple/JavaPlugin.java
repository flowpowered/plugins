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
