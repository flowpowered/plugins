package com.flowpowered.plugins;

/**
 * Used to wrap unexpected throwables from the plugin
 */
public class WrappedPluginException extends PluginException {
    private static final long serialVersionUID = 4691527735033425617L;

    public WrappedPluginException(Throwable cause) {
        super(cause);
    }

    public WrappedPluginException(String message, Throwable cause) {
        super(message, cause);
    }

}
