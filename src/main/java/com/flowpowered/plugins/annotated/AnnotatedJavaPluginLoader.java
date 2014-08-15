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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import com.flowpowered.plugins.PluginHandle;
import com.flowpowered.plugins.PluginManager;
import com.flowpowered.plugins.simple.AbstractSingleClassLoaderJavaPluginLoader;

public class AnnotatedJavaPluginLoader extends AbstractSingleClassLoaderJavaPluginLoader {
    private static final String DESCRIPTOR_NAME = "annotatedPlugin.yml";

    public AnnotatedJavaPluginLoader(ClassLoader cl) {
        super(cl, DESCRIPTOR_NAME);
    }

    @Override
    protected PluginHandle load(PluginManager manager, String pluginName, Map<String, String> mains) {
        String main = mains.get(pluginName);
        if (main != null) {
            try {
                Class<?> clazz = Class.forName(main, false, getClassLoader());
                Method enable = null;
                Method disable = null;

                for (Class<?> cls = clazz; cls != null; cls = cls.getSuperclass()) {
                    for (Method m : cls.getDeclaredMethods()) {
                        m.setAccessible(true);
                        boolean canBeEnable = (enable == null && m.isAnnotationPresent(Enable.class));
                        boolean canBeDisable = (disable == null && m.isAnnotationPresent(Disable.class));
                        if (!(canBeEnable || canBeDisable)) {
                            continue;
                        }
                        if (!validateMethod(m)) {
                            continue;
                        }
                        if (canBeEnable) {
                            enable = m;
                        }
                        if (canBeDisable) {
                            disable = m;
                        }
                        if (enable != null && disable != null) {
                            break;
                        }
                    }
                    if (enable != null && disable != null) {
                        break;
                    }
                }

                Object plugin = clazz.newInstance();
                PluginHandle handle = new AnnotatedJavaPluginHandle(manager, pluginName, plugin, enable, disable);
                return handle;
            } catch (ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException | ExceptionInInitializerError e) {
                // TODO: log
                e.printStackTrace();
            }
        }
        return null;
    }

    protected boolean validateMethod(Method method) {
        if (Modifier.isAbstract(method.getModifiers())) {
            // TODO: log
            return false;
        }
        if (!method.isAccessible()) {
            // TODO: log
            return false;
        }
        Class<?>[] params = method.getParameterTypes();
        if (params.length != 1) {
            // TODO: log
            return false;
        }
        if (Context.class.isAssignableFrom(params[0])) {
            return true;
        }
        // TODO: log
        return false;
    }

}
