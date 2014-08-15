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
