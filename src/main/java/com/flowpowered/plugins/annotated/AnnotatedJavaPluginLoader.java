package com.flowpowered.plugins.annotated;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

public class AnnotatedJavaPluginLoader implements PluginLoader {
    private static final String DESCRIPTOR_NAME = "annotatedPlugin.yml";
    private static final String NAME_KEY = "name";
    private static final String MAIN_KEY = "main";
    private final ClassLoader cl;

    public AnnotatedJavaPluginLoader(ClassLoader cl) {
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

    protected boolean validateMethod(Method method) {
        if (Modifier.isAbstract(method.getModifiers())) {
            return false;
        }
        Class<?>[] params = method.getParameterTypes();
        if (params.length != 1) {
            return false;
        }
        if (Context.class.isAssignableFrom(params[0])) {
            return true;
        }
        return false;
    }

}
