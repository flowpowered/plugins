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

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.flowpowered.plugins.PluginHandle;
import com.flowpowered.plugins.PluginLoader;
import com.flowpowered.plugins.PluginManager;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

public class AnnotatedJavaPluginLoader implements PluginLoader {
    private final ClassLoader cl;
    private final File folder;

    public AnnotatedJavaPluginLoader(File folder, ClassLoader cl) {
        this.cl = cl;
        this.folder = folder;
    }

    protected File getFolder() {
        return folder;
    }

    protected Collection<File> getJarFiles() {
        return Arrays.asList(folder.listFiles(JarFilenameFilter.INSTANCE));
    }

    private static class JarFilenameFilter implements FilenameFilter {
        private static JarFilenameFilter INSTANCE = new JarFilenameFilter();
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    }

    @Override
    public PluginHandle load(PluginManager manager, String pluginName) {
        String main = null;
        if (main != null) {
            try {
                Class<?> clazz = Class.forName(main, false, cl);
                Method enable = null;
                Method disable = null;

                outer:
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
                            break outer;
                        }
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

    @Override
    public Map<String, PluginHandle> loadAll(PluginManager manager) {
        Map<String, PluginHandle> loaded = new HashMap<>();
        for (File file : getJarFiles()) {
            try {
                for (Class<?> plugin : find(file.toURI().toURL(), cl)) {
                    Plugin ann = plugin.getAnnotation(Plugin.class);
                    String name = ann.name();
                    loaded.put(name, load(manager, name));
                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return loaded;
    }

    public static Set<Class<?>> find(URL url, ClassLoader cl) {
        Reflections ref = new Reflections(new ConfigurationBuilder().addUrls(url).addClassLoader(cl).setScanners(new TypeAnnotationsScanner()));
        return ref.getTypesAnnotatedWith(Plugin.class);
    }
}
