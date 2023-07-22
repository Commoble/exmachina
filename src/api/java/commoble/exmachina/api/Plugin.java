package commoble.exmachina.api;

import java.util.function.Consumer;

public interface Plugin extends Consumer<PluginRegistrator>
{
	/**
	 * Called when plugins are loaded.
	 * Plugins may use the given object to register json deserializers, get references to API data, etc
	 * @param registry The interface to the Ex Machina plugin registry
	 */
	@Override
	public void accept(PluginRegistrator registry);
}
