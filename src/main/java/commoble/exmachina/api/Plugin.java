package com.github.commoble.exmachina.api;

public interface Plugin
{
	/**
	 * Called when plugins are loaded.
	 * Plugins may use the given object to register json deserializers, get references to API data, etc
	 * @param registry
	 */
	public void register(PluginRegistrator registry);
}
