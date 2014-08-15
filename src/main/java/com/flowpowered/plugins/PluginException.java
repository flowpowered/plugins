package com.flowpowered.plugins;

public class PluginException extends Exception {
    private static final long serialVersionUID = 1760171730297062811L;

    // TODO: Store plugin name of the plugin that caused exception?

    public PluginException() {
    }

    public PluginException(String message) {
        super(message);
    }

    public PluginException(Throwable cause) {
        super(cause);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
