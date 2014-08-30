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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.flowpowered.plugins.Context;
import com.flowpowered.plugins.ContextCreator;
import com.flowpowered.plugins.InvalidPluginException;
import com.flowpowered.plugins.PluginClassLoader;
import com.flowpowered.plugins.PluginLoader;
import com.flowpowered.plugins.PluginManager;
import com.flowpowered.plugins.simple.SimplePluginLoader;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;


public class AnnotatedPluginLoader<C extends Context> extends PluginLoader<C> {
    private final ClassLoader cl;
    private final Path path;
    private static final Field objectField = PluginLoader.getFieldSilent(AnnotatedPlugin.class, "annotated");
    private static final Field enableField = PluginLoader.getFieldSilent(AnnotatedPlugin.class, "enable");
    private static final Field disableField = PluginLoader.getFieldSilent(AnnotatedPlugin.class, "disable");

    public AnnotatedPluginLoader(ContextCreator<C> contextCreator, Path folder, ClassLoader cl) {
        super(contextCreator);
        this.cl = cl;
        this.path = folder;
    }

    protected Path getPath() {
        return path;
    }

    @Override
    public Map<String, com.flowpowered.plugins.Plugin<C>> loadAll(PluginManager<C> manager) {
        return loadPath(manager, path);
    }

    private Map<String, com.flowpowered.plugins.Plugin<C>> loadPath(PluginManager<C> manager, Path path) {
        Map<String, com.flowpowered.plugins.Plugin<C>> loaded = new HashMap<>();
        try {
            if (Files.isDirectory(path)) {
                for (Path localPath : Files.newDirectoryStream(path, DirectoryStreamFilter.INSTANCE)) {
                    loaded.putAll(loadPath(manager, localPath));
                }
            } else if (path.toString().endsWith(".jar") || path.toString().endsWith("zip")) {
                URL url = path.toUri().toURL();
                URLClassLoader ucl = new URLClassLoader(new URL[] { url });
                for (Class<?> plugin : find(url, ucl)) {
                    checkAndLoad(loaded, plugin, manager, new PluginClassLoader(ucl));
                }
            } else if (path.toString().endsWith(".class")) {
                InputStream in = Files.newInputStream(path);
                byte[] bytes = new byte[in.available()];
                in.read(bytes);
                PluginClassLoader pcl = new PluginClassLoader(cl);
                Class<?> check = pcl.defineClass(bytes);
                checkAndLoad(loaded, check, manager, pcl);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return loaded;
    }

    private void checkAndLoad(Map<String, com.flowpowered.plugins.Plugin<C>> loaded, Class<?> check, PluginManager<C> manager, PluginClassLoader pcl) {
        Plugin ann = check.getAnnotation(Plugin.class);
        String name = ann.name();
        try {
            loaded.put(name, load(manager, ann, check, pcl));
        } catch (InvalidPluginException ex) {
            ex.printStackTrace();
        }
    }

    public AnnotatedPlugin<C> load(PluginManager<C> manager, Plugin annotation, Class<?> clazz, ClassLoader cl) throws InvalidPluginException {
        try {
            Object annotated = clazz.newInstance();
            AnnotatedPlugin<C> plugin = new AnnotatedPlugin<>();
            return init(plugin, annotation.name(), manager, annotated);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // Shouldn't happen
            throw new IllegalStateException(ex);
        } catch (InstantiationException ex) {
            throw new InvalidPluginException("Cannot instantiate object", ex);
        }
    }

    protected boolean validateMethod(Method method, Class<? extends Context> contextClass) {
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
        // We can use the generic Context or a specifc one
        // However, the parameter can never be a subclass
        if (!params[0].isAssignableFrom(contextClass)) {
            // TODO: log
            return false;
        }
        return true;
    }

    protected AnnotatedPlugin<C> init(AnnotatedPlugin<C> plugin, String name, PluginManager<C> manager, Object annotated) throws IllegalArgumentException, IllegalAccessException {
        super.init(plugin, name, manager);
        Method enable = null;
        Method disable = null;
        outer:
        for (Class<?> cls = annotated.getClass(); cls != null; cls = cls.getSuperclass()) {
            for (Method m : cls.getDeclaredMethods()) {
                m.setAccessible(true);
                boolean canBeEnable = (enable == null && m.isAnnotationPresent(Enable.class));
                boolean canBeDisable = (disable == null && m.isAnnotationPresent(Disable.class));
                if (!(canBeEnable || canBeDisable)) {
                    continue;
                }
                if (!validateMethod(m, plugin.getContext().getClass())) {
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
        SimplePluginLoader.setField(objectField, plugin, annotated);
        SimplePluginLoader.setField(enableField, plugin, enable);
        SimplePluginLoader.setField(disableField, plugin, disable);
        return plugin;
    }

    @Override
    public com.flowpowered.plugins.Plugin<C> load(PluginManager<C> manager, String pluginName) throws InvalidPluginException {
        // TODO: remove this
        return loadAll(manager).get(pluginName);
    }

    public static Set<Class<?>> find(URL url, ClassLoader cl) {
        Reflections ref = new Reflections(new ConfigurationBuilder().addUrls(url).addClassLoader(cl).setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));
        return ref.getTypesAnnotatedWith(Plugin.class);
    }

    private static class DirectoryStreamFilter implements DirectoryStream.Filter<Path> {
        private static DirectoryStreamFilter INSTANCE = new DirectoryStreamFilter();
        @Override
        public boolean accept(Path entry) throws IOException {
            return Files.isDirectory(entry) || entry.getFileName().toString().endsWith(".jar") || entry.getFileName().toString().endsWith(".zip") || entry.getFileName().toString().endsWith(".class");
        }
    }
}
