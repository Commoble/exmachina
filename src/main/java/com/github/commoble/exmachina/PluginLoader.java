package com.github.commoble.exmachina;

import java.util.Objects;
import java.util.stream.Stream;

import org.objectweb.asm.Type;

import com.github.commoble.exmachina.api.AutoPlugin;
import com.github.commoble.exmachina.api.Plugin;
import com.github.commoble.exmachina.api.PluginRegistrator;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;

public class PluginLoader
{	
	/**
	 * Instantiate one instance of all classes that are
	 * A) annotated with {@link AutoPlugin}, and
	 * B) implementing {@link Plugin}
	 * and then allow the instances to register circuit behaviors
	 */
	public static CircuitBehaviourRegistry loadPlugins()
	{
		ExMachina.LOGGER.info("Loading Ex Machina plugins");
		
		Type pluginType = Type.getType(AutoPlugin.class);
		PluginRegistrator registrator = CircuitBlocks::getCircuitEntry;
		
		// get the names of all classes annotated with the plugin annotation
		ModList.get().getAllScanData().stream()
			.flatMap(modData -> modData.getAnnotations().stream())
			.filter(annotationData -> Objects.equals(annotationData.getAnnotationType(), pluginType)) 
			.map(AnnotationData::getMemberName)
			
			// try to create instances of these classes
			.flatMap(PluginLoader::createPluginInstance)
			// and allow them to register circuit behaviors if they were instantiated successfully
			.forEach(plugin -> plugin.register(registrator));
	}
	
	/**
	 * Attempts to create a plugin instance, given the name of the class to instantiate.
	 * We use a Stream instead of Optional so the mod scan stream can flatmap it.
	 * @param className The fully-qualified class name of the plugin implementation class
	 * @return A Stream containing the instance if successful, or an empty stream otherwise.
	 */
	private static Stream<Plugin> createPluginInstance(String className)
	{
		try
		{
			return Stream.of(
				Class.forName(className) // get the exact class by name
				.asSubclass(Plugin.class) // as a subclass of Plugin
				.newInstance()); // and try to instantiate it via its argless constructor
		}
		catch (Exception e)
		{
			ExMachina.LOGGER.error("Failed to load Ex Machina Plugin: {}", className, e);
			return Stream.empty();
		}
	}
}
