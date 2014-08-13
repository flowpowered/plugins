package com.flowpowered.plugins;

import java.util.Map;
import java.util.Set;

public interface PluginLoader {

    Set<String> scan();

    PluginHandle load(PluginManager manager, String pluginName);

    Map<String, PluginHandle> loadAll(PluginManager manager);
}
